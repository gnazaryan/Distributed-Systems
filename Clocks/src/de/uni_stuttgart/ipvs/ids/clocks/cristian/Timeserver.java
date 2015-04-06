package de.uni_stuttgart.ipvs.ids.clocks.cristian;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import de.uni_stuttgart.ipvs.ids.clocks.Clock;
import de.uni_stuttgart.ipvs.ids.communicationLib.VSDatagramSocket;

public class Timeserver implements Runnable {

	private Clock clock;
	private VSDatagramSocket serverSocket;

	public Timeserver(Clock clock, InetSocketAddress address) {

		this.clock = clock;
		serverSocket = new VSDatagramSocket(address);

	}

	public void run() {
		//receive data from client sockets and send the clock time
		byte[] buf = new byte[1000];
	    DatagramPacket receiveData = new DatagramPacket(buf, buf.length);
		serverSocket.receive(receiveData);
		String received = new String(receiveData.getData(), 0, receiveData.getLength());
		if (received.equals("getTime")){
			Long time = clock.getTime();
			byte[] sendData = Utility.longToBytes(time);
			DatagramPacket out = new DatagramPacket(sendData, sendData.length, receiveData.getAddress(), receiveData.getPort());
			try {
				serverSocket.send(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//recursively run the loop to handle other requests
		run();
	}
}