package de.uni_stuttgart.ipvs.ids.communicationLib;

import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventQueue extends Thread {

	class QueuedMessage {
		SocketAddress sender;
		DatagramPacket p;

		QueuedMessage(SocketAddress sender, DatagramPacket p) {
			this.sender = sender;
			this.p = p;
		}
	}

	class DestinationUnreachableException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8858528525767516520L;

		public SocketAddress getSource() {
			return source;
		}

		public SocketAddress getDestination() {
			return destination;
		}

		SocketAddress source;
		SocketAddress destination;

		public DestinationUnreachableException(SocketAddress source,
				SocketAddress destination) {
			this.source = source;
			this.destination = destination;
		}

	}

	protected static EventQueue instance;

	public static EventQueue getInstance() {
		if (instance == null) {
			instance = new EventQueue();
		}
		return instance;
	}

	AtomicBoolean continueEventQueue = new AtomicBoolean(false);

	SortedMap<Long, List<QueuedMessage>> messageQueue;
	Map<SocketAddress, Map<SocketAddress, Long>> channelDelayMap;
	Map<SocketAddress, VSDatagramSocket> addressSocketMap;

	/**
	 * Internal use only.
	 * 
	 * @param p
	 */
	protected EventQueue() {
		super("EventQueue DeliveryThread");
		continueEventQueue = new AtomicBoolean();
		continueEventQueue.set(true);
		messageQueue = new TreeMap<Long, List<QueuedMessage>>();
		channelDelayMap = new HashMap<SocketAddress, Map<SocketAddress, Long>>();
		addressSocketMap = new HashMap<SocketAddress, VSDatagramSocket>();
		this.start();
	}

	/**
	 * Cause this thread to exit. Thread may still be alive when this method
	 * exits!
	 */
	public void quitThread() {
		continueEventQueue.set(false);
		synchronized (messageQueue) {
			messageQueue.notifyAll();
		}
	}

	/**
	 * Internal use only.
	 * 
	 * @param p
	 */
	public void run() {
		synchronized (messageQueue) {
			while (continueEventQueue.get()) {
				try {
					if (messageQueue.isEmpty()) {
						messageQueue.wait();
						continue;
					} else {
						long now = System.currentTimeMillis();
						long nextDeliveryTime = messageQueue.firstKey();
						if (now >= nextDeliveryTime) {
							List<QueuedMessage> messagesToDeliver = messageQueue
									.get(nextDeliveryTime);
							for (QueuedMessage qMsg : messagesToDeliver) {
								deliver(qMsg);
							}
							messageQueue.remove(nextDeliveryTime);
						} else {
							// wait until next delivery or woken up
							now = System.currentTimeMillis();
							messageQueue.wait(nextDeliveryTime - now);
							continue;
						}
					}
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
	}

	/**
	 * Internal use only.
	 * 
	 * @param p
	 */
	public void send(SocketAddress sender, DatagramPacket p) {
		SocketAddress receiver = p.getSocketAddress();
		try {
			long deliveryTime = getDelayTime(sender, receiver)
					+ System.currentTimeMillis();
			QueuedMessage msg = new QueuedMessage(sender, p);

			synchronized (messageQueue) {
				if (messageQueue.containsKey(deliveryTime)) {
					messageQueue.get(deliveryTime).add(msg);
				} else {
					List<QueuedMessage> qMsgList = new ArrayList<QueuedMessage>();
					qMsgList.add(msg);
					messageQueue.put(deliveryTime, qMsgList);
				}
				messageQueue.notifyAll();
			}
		} catch (DestinationUnreachableException e) {
			System.err.println("Destination " + e.getDestination()
					+ " not reachable from source " + e.getSource());
		}
	}

	/**
	 * Internal use only.
	 * 
	 * @param p
	 */
	protected long getDelayTime(SocketAddress sender, SocketAddress receiver)
			throws DestinationUnreachableException {
		synchronized (channelDelayMap) {
			Map<SocketAddress, Long> receiverMap = channelDelayMap.get(sender);
			if (receiverMap == null) {
				throw new DestinationUnreachableException(sender, receiver);
			} else {
				Long delay = receiverMap.get(receiver);
				if (delay == null) {
					throw new DestinationUnreachableException(sender, receiver);
				} else {
					return delay;
				}
			}
		}
	}

	/**
	 * Add a delay between two {@link VSDatagramSocket}s specified by their
	 * {@link SocketAddress}es. Unidirectional Channels!
	 * 
	 * @param sender
	 *            The {@link SocketAddress} of the sender.
	 * @param receiver
	 *            The {@link SocketAddress} of the receiver.
	 * @param delay
	 *            The Delay in Milliseconds.
	 */
	public void addChannel(SocketAddress sender, SocketAddress receiver,
			long delay) {
		synchronized (channelDelayMap) {
			if (!channelDelayMap.containsKey(sender)) {
				Map<SocketAddress, Long> receiverMap = new HashMap<SocketAddress, Long>();
				receiverMap.put(receiver, delay);
				channelDelayMap.put(sender, receiverMap);
			} else {
				channelDelayMap.get(sender).put(receiver, delay);
			}
		}
	}

	/**
	 * Registers a new {@link VSDatagramSocket} with the {@link EventQueue}.
	 * 
	 * @param addr
	 *            The {@link SocketAddress} of the socket to be registered.
	 * @param socket
	 *            The {@link VSDatagramSocket} to be registered.
	 */
	public void addSocket(SocketAddress addr, VSDatagramSocket socket) {
		synchronized (addressSocketMap) {
			addressSocketMap.put(addr, socket);
		}
	}

	/**
	 * Internal use only.
	 * 
	 * @param p
	 */
	protected void deliver(QueuedMessage qMsg) {
		VSDatagramSocket socket = addressSocketMap.get(qMsg.p
				.getSocketAddress());
		qMsg.p.setSocketAddress(qMsg.sender);
		socket.storeToBuffer(qMsg.p);
		qMsg.p = null;
	}
}
