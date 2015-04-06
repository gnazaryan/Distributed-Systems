package de.uni_stuttgart.ipvs.ids.communication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.Vector;

import de.uni_stuttgart.ipvs.ids.replication.Replica;

/**
 * Part b) Extend the method receiveMessages to return all DatagramPackets that
 * were received during the given timeout.
 * 
 * Also implement unpack() to conveniently convert a Collection of
 * DatagramPackets containing ValueResponseMessages to a collection of
 * MessageWithSource objects.
 * 
 */
public class NonBlockingReceiver {

	protected DatagramSocket socket;

	public NonBlockingReceiver(DatagramSocket socket) {
		this.socket = socket;
	}

	public Vector<DatagramPacket> receiveMessages(int timeoutMillis, int expectedMessages) throws IOException {
		//either receive all messages or return already collected after given timeout
	    socket.setSoTimeout(timeoutMillis);
	    boolean continueSending = true;
	    int counter = 0;
	    Vector<DatagramPacket> result = new Vector<DatagramPacket>();
	    while (continueSending && counter < expectedMessages) {
			counter++;
		    try {
		    	DatagramPacket dgram = new DatagramPacket(new byte[Replica.BUFFER_LENGTH], Replica.BUFFER_LENGTH);
		        socket.receive(dgram);
		        result.add(dgram);
		    } catch (SocketTimeoutException e) {
		    	continueSending = false;
		    }
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Collection<MessageWithSource<T>> unpack(
			Collection<DatagramPacket> packetCollection) throws IOException,
			ClassNotFoundException {
		//unpuck collection of DatagramPacket's to corresponding java object, could be written better considering generic types :)
		Vector<MessageWithSource<T>> result = new Vector<MessageWithSource<T>>();
		for (DatagramPacket datagramPacket: packetCollection) {
			ByteArrayInputStream baos = new ByteArrayInputStream(datagramPacket.getData());
		    ObjectInputStream oos = new ObjectInputStream(baos);
		    Object obj = oos.readObject();
		    if (obj instanceof ValueResponseMessage) {
		    	ValueResponseMessage vRMessage = (ValueResponseMessage)obj;
		    	MessageWithSource<ValueResponseMessage> MessageWithSource = new MessageWithSource<ValueResponseMessage>(datagramPacket.getSocketAddress(), vRMessage);
		    	result.add((MessageWithSource<T>)MessageWithSource);
		    }
		}
		return result;
	}
}