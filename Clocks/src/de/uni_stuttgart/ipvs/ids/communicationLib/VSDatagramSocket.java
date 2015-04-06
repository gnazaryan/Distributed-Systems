package de.uni_stuttgart.ipvs.ids.communicationLib;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class VSDatagramSocket extends Thread {

	protected final InetSocketAddress listenAddress;
	protected EventQueue eventQueue;

	protected List<DatagramPacket> receiveBuffer;

	public VSDatagramSocket(InetSocketAddress listenAddress) {
		this.listenAddress = listenAddress;
		this.eventQueue = EventQueue.getInstance();
		eventQueue.addSocket(listenAddress, this);
		this.receiveBuffer = new ArrayList<DatagramPacket>();
	}

	public InetAddress getLocalAddress() {
		return listenAddress.getAddress();
	}

	public int getPort() {
		return listenAddress.getPort();
	}

	public InetSocketAddress getLocalSocketAddress() {
		return listenAddress;
	}

	/**
	 * Receive a message into the given {@link DatagramPacket}.
	 * 
	 * This method will block when no data is available.
	 * 
	 * @param p
	 */
	public void receive(DatagramPacket p) {
		boolean dataAvailable = false;
		do {
			synchronized (receiveBuffer) {
				dataAvailable = !receiveBuffer.isEmpty();
				if (dataAvailable) {
					DatagramPacket buffered = receiveBuffer.get(0);
					receiveBuffer.remove(0);
					p.setSocketAddress(buffered.getSocketAddress());
					p.setData(buffered.getData());
					return;
				}

				try {
					receiveBuffer.wait();
				} catch (InterruptedException e) {
					// Thread was woken. Continue normally.
				}
			}
		} while (!dataAvailable);
	}

	/**
	 * Send a {@link DatagramPacket} to the destination address specified in the
	 * packet.
	 * 
	 * @param p
	 *            The message to be sent.
	 */
	public void send(DatagramPacket p) throws IOException {
		DatagramPacket copy = null;
		try {
			copy = new DatagramPacket(p.getData().clone(), p.getOffset(),
					p.getLength(), p.getSocketAddress());
		} catch (SocketException e) {
			throw new IOException("Could not copy DatagramPacket", e);
		}
		eventQueue.send(this.listenAddress, copy);
	}

	/**
	 * Internal use only.
	 * 
	 * @param p
	 */
	public void storeToBuffer(DatagramPacket p) {
		synchronized (receiveBuffer) {
			receiveBuffer.add(p);
			receiveBuffer.notifyAll();
		}
	}
}
