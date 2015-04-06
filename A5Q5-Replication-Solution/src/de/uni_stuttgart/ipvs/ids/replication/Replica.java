package de.uni_stuttgart.ipvs.ids.replication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Random;

import de.uni_stuttgart.ipvs.ids.communication.ReadRequestMessage;
import de.uni_stuttgart.ipvs.ids.communication.ReleaseReadLock;
import de.uni_stuttgart.ipvs.ids.communication.ReleaseWriteLock;
import de.uni_stuttgart.ipvs.ids.communication.RequestReadVote;
import de.uni_stuttgart.ipvs.ids.communication.RequestWriteVote;
import de.uni_stuttgart.ipvs.ids.communication.ValueResponseMessage;
import de.uni_stuttgart.ipvs.ids.communication.Vote;
import de.uni_stuttgart.ipvs.ids.communication.WriteRequestMessage;

public class Replica<T> extends Thread {

	public enum LockType {
		UNLOCKED, READLOCK, WRITELOCK
	};

	private int id;

	private double availability;
	private VersionedValue<T> value;
	int noOfVotes;

	protected DatagramSocket socket = null;

	protected LockType lock;
	/**
	 * This address holds the addres of the client holding the lock. This
	 * variable should be set to NULL every time the lock is set to UNLOCKED.
	 */
	protected SocketAddress lockHolder;

	public static final int BUFFER_LENGTH = 1000;
	private static Random rand = new Random();

	public Replica(int id, int listenPort, double availability, T initialValue, int votes) throws SocketException {
		this.id = id;
		this.noOfVotes = votes;
		SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", listenPort);
		this.socket = new DatagramSocket(socketAddress);
		this.availability = availability;
		this.value = new VersionedValue<T>(0, initialValue);
		this.lock = LockType.UNLOCKED;
	}	

	/**
	 * Part a) Implement this run method to receive and process request
	 * messages. To simulate a replica that is sometimes unavailable, it should
	 * randomly discard requests as long as it is not locked.
	 * The probability for discarding a request is (1 - availability).
	 * 
	 * For each request received, it must also be checked whether the request is valid.
	 * For example:
	 * - Does the requesting client hold the correct lock?
	 * - Is the replica unlocked when a new lock is requested?
	 */
	public void run() {
		while (true) {
			DatagramPacket dgram = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
			try {
				socket.receive(dgram);
				//randomly discard requests
				if (this.lock == LockType.UNLOCKED) {
					if (rand.nextDouble() <= (1 - availability)) {
						continue;
					}
				}
			    Object obj = getObjectFromMessage(dgram);
			    processRequest(obj, dgram.getSocketAddress());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void processRequest(Object obj, SocketAddress socketAddress) throws IOException {
	    if (obj instanceof RequestReadVote || obj instanceof RequestWriteVote) {
	    	//handle read and write vote requests to unlock or lock 
	    	switch(this.lock) {
	    	case UNLOCKED:
	    		if (obj instanceof RequestReadVote) {
	    			this.lock = LockType.READLOCK;
	    			sendVote(socketAddress, Vote.State.YES, value.getVersion());
	    		} else {
	    			this.lock = LockType.WRITELOCK;
	    			sendVote(socketAddress, Vote.State.YES, value.getVersion());
	    		}
	    		break;
	    	case READLOCK:
	    		if (obj instanceof RequestReadVote) {
	    			sendVote(socketAddress, Vote.State.NO, value.getVersion());
	    		} else {
	    			sendVote(socketAddress, Vote.State.NO, value.getVersion());
	    		}
	    		break;
	    	case WRITELOCK:
	    		if (obj instanceof RequestReadVote) {
	    			sendVote(socketAddress, Vote.State.NO, value.getVersion());
	    		} else {
	    			sendVote(socketAddress, Vote.State.NO, value.getVersion());
	    		}
	    	}
	    } else if (obj instanceof ReleaseReadLock || obj instanceof ReleaseWriteLock) {
	    	//handle read and write release lock requests 
	    	this.lock = LockType.UNLOCKED;
	    	sendVote(socketAddress, Vote.State.YES, value.getVersion());
	    } else if (obj instanceof ReadRequestMessage) {
	    	//handle read requests to read and return values
	    	if (this.lock == LockType.READLOCK) {
		    	ValueResponseMessage<VersionedValue<T>> response = new ValueResponseMessage<VersionedValue<T>>(this.value);
				byte[] sendData = objectToBytes(response);
				DatagramPacket dp = new DatagramPacket(sendData, sendData.length, socketAddress);
				socket.send(dp);	    		
	    	}
	    } else if (obj instanceof WriteRequestMessage) {
	    	//handle write requests to write on current replica
	    	if (this.lock == LockType.WRITELOCK) {
		    	@SuppressWarnings("unchecked")
				WriteRequestMessage<T> wRMessage = (WriteRequestMessage<T>)obj;
		    	if (wRMessage.getVersion() >= this.value.getVersion()) {
		    		this.value = (VersionedValue<T>) wRMessage;
		    		sendVote(socketAddress, Vote.State.YES, value.getVersion());
		    	} else {
		    		sendVote(socketAddress, Vote.State.NO, value.getVersion());
	    	    }
	    	}
	    }
	}

	/**
	 * This is a helper method. You can implement it if you want to use it or just ignore it.
	 * Its purpose is to send a Vote (YES/NO depending on the state) to the given address.
	 */
	protected void sendVote(SocketAddress address,
	    Vote.State state, int version) throws IOException {
		//Use UDP connection to respond requests with vote
		Vote vote = new Vote(state, version, this.noOfVotes);
		ValueResponseMessage<Vote> response = new ValueResponseMessage<Vote>(vote);
		byte[] sendData = objectToBytes(response);
		DatagramPacket dp = new DatagramPacket(sendData, sendData.length, address);
		socket.send(dp);
	}

	public static byte[] objectToBytes(Object obj) throws IOException {
		//convert object ot bytes
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(obj);
		byte[] result = bos.toByteArray();
		out.close();
		bos.close();
		return result;
	}

	/**
	 * This is a helper method. You can implement it if you want to use it or just ignore it.
	 * Its purpose is to extract the object stored in a DatagramPacket.
	 */
	protected Object getObjectFromMessage(DatagramPacket packet)
			throws IOException, ClassNotFoundException {
		//convert DatagramPacket to java object
		ByteArrayInputStream baos = new ByteArrayInputStream(packet.getData());
	    ObjectInputStream oos = new ObjectInputStream(baos);
		return oos.readObject();
	}

	public int getID() {
		return id;
	}

	public SocketAddress getSocketAddress() {
		return socket.getLocalSocketAddress();
	}

}
