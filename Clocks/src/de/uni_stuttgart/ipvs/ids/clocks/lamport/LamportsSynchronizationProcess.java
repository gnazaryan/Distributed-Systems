package de.uni_stuttgart.ipvs.ids.clocks.lamport;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Set;

import de.uni_stuttgart.ipvs.ids.clocks.Clock;
import de.uni_stuttgart.ipvs.ids.communicationLib.VSDatagramSocket;

public class LamportsSynchronizationProcess {

	private TimeSender sender;
	private TimeReceiver receiver;

	public LamportsSynchronizationProcess(Clock clock,
			InetSocketAddress listenAddress, Set<InetSocketAddress> neighbourSet)
			throws SocketException {
		VSDatagramSocket socket = new VSDatagramSocket(listenAddress);
		receiver = new TimeReceiver(clock, socket);
		sender = new TimeSender(clock, socket, neighbourSet);

	}

	public void start_Receiver() {
		new Thread(receiver).start();

	}

	public void start_Sender() {

		new Thread(sender).start();
	}
}
