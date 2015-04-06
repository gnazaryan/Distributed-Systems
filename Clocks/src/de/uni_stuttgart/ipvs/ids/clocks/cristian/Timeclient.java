package de.uni_stuttgart.ipvs.ids.clocks.cristian;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;

import de.uni_stuttgart.ipvs.ids.clocks.Clock;
import de.uni_stuttgart.ipvs.ids.communicationLib.VSDatagramSocket;

public class Timeclient implements Runnable {

	private Clock clock;
	private VSDatagramSocket clientSocket;
	private String name;
	private final static long delta = 1000;
	private final static double maximal_drift_rate = 0.15;
	

	public Timeclient(Clock clock, InetSocketAddress address, String name) {
		this.clock = clock;
		clientSocket = new VSDatagramSocket(address);
		this.name = name;
	}

	public void run() {        
	    try {
			//Send data packet
			String command = "getTime";
			byte[] data = command.getBytes();
			DatagramPacket sendData = new DatagramPacket(data, data.length, Main.p2_addr);
			
			//Receive data packet
			byte[] buf = new byte[1000];
		    DatagramPacket receiveData = new DatagramPacket(buf, buf.length);
	    	
		    //Send and Receive data
		    long startTime = System.currentTimeMillis();
			clientSocket.send(sendData);
			clientSocket.receive(receiveData);
			
			//Calculate the actual time and update the clock
			long time = Utility.bytesToLong(receiveData.getData());
			long endTime = System.currentTimeMillis();
			long diff = (endTime - startTime) / 2;
            clock.setTime(time - diff);

            //Sleep for the amount of time for the next cycle 
			long sleepTime = (long)((double)delta / 2.0 * maximal_drift_rate);
			Thread.sleep(sleepTime);
			run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}