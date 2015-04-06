package de.uni_stuttgart.ipvs.ids.clocks.lamport;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.Set;

import de.uni_stuttgart.ipvs.ids.clocks.Clock;
import de.uni_stuttgart.ipvs.ids.clocks.cristian.Utility;
import de.uni_stuttgart.ipvs.ids.communicationLib.VSDatagramSocket;

public class TimeSender implements Runnable {

	private Clock clock;
	private VSDatagramSocket senderSocket;
	private Set<InetSocketAddress> neighbourSet;
	private final static long delta = 1000;
	private final static double maximal_drift_rate = 0.15;
    private static long z = 3;//ms

	public TimeSender(Clock clock, VSDatagramSocket socket,
			Set<InetSocketAddress> neighbourSet) {
		this.clock = clock;
		this.neighbourSet = neighbourSet;
		this.senderSocket = socket;
	}

	public void run() {
		try {
			//Iterate over neighbour set and send time
			for (InetSocketAddress address: neighbourSet) {
				//Send data packet
				byte[] data = Utility.longToBytes(clock.getTime());
				DatagramPacket sendData = new DatagramPacket(data, data.length, address);
				senderSocket.send(sendData);
			}
			//Calculate the next iteration time: delta ~ d(2*c*t + z)
			long taou = (long) ((delta - 2*z) / (4 * maximal_drift_rate));
			Thread.sleep(taou);
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}