package de.uni_stuttgart.ipvs.ids.clocks.lamport;

import java.net.DatagramPacket;

import de.uni_stuttgart.ipvs.ids.clocks.Clock;
import de.uni_stuttgart.ipvs.ids.clocks.cristian.Utility;
import de.uni_stuttgart.ipvs.ids.communicationLib.VSDatagramSocket;

public class TimeReceiver implements Runnable {

	private Clock clock;
	private VSDatagramSocket receiverSocket;
	private final static long minimum_delay = 100;

	public TimeReceiver(Clock clock, VSDatagramSocket socket) {
		this.clock = clock;
		receiverSocket = socket;
	}

	public void run() {
		//periodically receive updates from neighboring nodes and update the local clock 
		byte[] buf = new byte[1000];
	    DatagramPacket receiveData = new DatagramPacket(buf, buf.length);
		receiverSocket.receive(receiveData);
		long time = Utility.bytesToLong(receiveData.getData()) + minimum_delay;
		System.out.println("---------Time Receive---------");
		System.out.println("Sender Time: " + time);
		if (clock.getTime() > time) {
			time = clock.getTime();
		}
		System.out.println("Receiver Clock Time: " + clock.getTime());
		System.out.println("Set Time: " + time);
		System.out.println("------------------------------");
		clock.setTime(time);
		run();
	}

}
