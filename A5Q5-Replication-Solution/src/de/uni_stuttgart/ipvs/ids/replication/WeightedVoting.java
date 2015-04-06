package de.uni_stuttgart.ipvs.ids.replication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.Vector;

import de.uni_stuttgart.ipvs.ids.communication.MessageWithSource;
import de.uni_stuttgart.ipvs.ids.communication.NonBlockingReceiver;
import de.uni_stuttgart.ipvs.ids.communication.ReadRequestMessage;
import de.uni_stuttgart.ipvs.ids.communication.ReleaseReadLock;
import de.uni_stuttgart.ipvs.ids.communication.ReleaseWriteLock;
import de.uni_stuttgart.ipvs.ids.communication.RequestReadVote;
import de.uni_stuttgart.ipvs.ids.communication.RequestWriteVote;
import de.uni_stuttgart.ipvs.ids.communication.ValueResponseMessage;
import de.uni_stuttgart.ipvs.ids.communication.Vote;
import de.uni_stuttgart.ipvs.ids.communication.WriteRequestMessage;

public class WeightedVoting<T> {

	protected Collection<SocketAddress> replicas;

	protected DatagramSocket socket;
	protected NonBlockingReceiver nbio;
	final protected int readQuorum;
	final protected int writeQuorum;

	final static int TIMEOUT = 1000;

	public WeightedVoting(Collection<SocketAddress> replicas, int totalVotes)
			throws SocketException {
		this.replicas = replicas;
		SocketAddress address = new InetSocketAddress("127.0.0.1", 4999);
		this.socket = new DatagramSocket(address);
		this.nbio = new NonBlockingReceiver(socket);
		readQuorum = (replicas.size() / 2) + 1;
		writeQuorum = (replicas.size() / 2) + 1;
	}

	/**
	 * Part c) Implement this method.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected Collection<SocketAddress> requestReadVote() throws QuorumNotReachedException {
		Collection<MessageWithSource<Vote>> result = null;
		try {
			//send vote request to the following replicas
			for(SocketAddress replica: replicas) {
				RequestReadVote rRVote = new RequestReadVote();
				byte[] sendData = Replica.objectToBytes(rRVote);
				DatagramPacket dp = new DatagramPacket(sendData, sendData.length, replica);
				socket.send(dp);
			}
			//receive votes from requested replicas
			Vector<DatagramPacket> datagramPackets = nbio.receiveMessages(1000, replicas.size());
			result = unpuckVotes(datagramPackets);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//check the quorum threshold and return the set of nodes
		return this.checkReadQuorum(result);
	}

	/**
	 * Part c) Implement this method.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected void releaseReadLock(Collection<SocketAddress> lockedReplicas) {
		try {
			//send release read lock request to locked replicas
			for (SocketAddress socketAddress: lockedReplicas) {
				ReleaseReadLock rRLock = new ReleaseReadLock();
				byte[] sendData = Replica.objectToBytes(rRLock);
				DatagramPacket dp = new DatagramPacket(sendData, sendData.length, socketAddress);
				socket.send(dp);
			}
			//receive responses as acknowledgement
			Vector<DatagramPacket> datagramPackets = nbio.receiveMessages(1000, lockedReplicas.size());
			@SuppressWarnings("unused")
			Collection<MessageWithSource<Vote>> result = unpuckVotes(datagramPackets);
			//result, do something
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Part d) Implement this method.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected Collection<SocketAddress> requestWriteVote() throws QuorumNotReachedException {
		Collection<MessageWithSource<Vote>> result = null;
		try {
			//send write lock request to all replicas
			for(SocketAddress replica: replicas) {
				RequestWriteVote rWVote = new RequestWriteVote();
				byte[] sendData = Replica.objectToBytes(rWVote);
				DatagramPacket dp = new DatagramPacket(sendData, sendData.length, replica);
				socket.send(dp);
			}
			//receive votes from requested replicas
			Vector<DatagramPacket> datagramPackets = nbio.receiveMessages(1000, replicas.size());
			result = unpuckVotes(datagramPackets);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//check the quorum threshold and return the set of nodes
		return this.checkWriteQuorum(result);
	}

	/**
	 * Part d) Implement this method.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected void releaseWriteLock(Collection<SocketAddress> lockedReplicas) {
		try {
			for (SocketAddress socketAddress: lockedReplicas) {
				ReleaseWriteLock rWLock = new ReleaseWriteLock();
				byte[] sendData = Replica.objectToBytes(rWLock);
				DatagramPacket dp = new DatagramPacket(sendData, sendData.length, socketAddress);
				socket.send(dp);
			}
			Vector<DatagramPacket> datagramPackets = nbio.receiveMessages(1000, lockedReplicas.size());
			@SuppressWarnings("unused")
			Collection<MessageWithSource<Vote>> result = unpuckVotes(datagramPackets);
			//result, do something
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<MessageWithSource<Vote>> unpuckVotes(Vector<DatagramPacket> datagramPackets) throws IOException, ClassNotFoundException {
		Collection<MessageWithSource<Vote>> result = new Vector<MessageWithSource<Vote>>();
		Collection<MessageWithSource<ValueResponseMessage>> receivedMessages = NonBlockingReceiver.<ValueResponseMessage>unpack(datagramPackets);
		for (MessageWithSource<ValueResponseMessage> mWSource: receivedMessages) {
			ValueResponseMessage<T> vRM = mWSource.getMessage();
			Object obj = vRM.getValue();
			if (obj instanceof Vote) {
				Vote v = (Vote)obj;
				MessageWithSource mWS = new MessageWithSource(mWSource.getSource(), v);
				result.add(mWS);
			}
		}
		return result;
	}

	/**
	 * Part c) Implement this method.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected VersionedValue<T> readReplica(SocketAddress replica) {
		VersionedValue<T> result = null;
		ReadRequestMessage rRMessage = new ReadRequestMessage();
		Collection<MessageWithSource<ValueResponseMessage>> receivedMessages = null;
		try {
			byte[] sendData = Replica.objectToBytes(rRMessage);
			DatagramPacket dp = new DatagramPacket(sendData, sendData.length, replica);
			socket.send(dp);
			Vector<DatagramPacket> datagramPackets = nbio.receiveMessages(1000, 1);
			receivedMessages = NonBlockingReceiver.<ValueResponseMessage>unpack(datagramPackets);
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (MessageWithSource<ValueResponseMessage> mWS: receivedMessages ) {
			ValueResponseMessage vRMessage = mWS.getMessage();
			Object obj = vRMessage.getValue();
			if (obj instanceof VersionedValue) {
				VersionedValue<T> vValue = (VersionedValue<T>)obj;
				result = vValue;
			}
		}
		return result;
	}

	/**
	 * Part d) Implement this method.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	protected void writeReplicas(Collection<SocketAddress> lockedReplicas, VersionedValue<T> newValue) {
		try {
			WriteRequestMessage<T> wRMessage = new WriteRequestMessage<T>(newValue);
			byte[] sendData = Replica.objectToBytes(wRMessage);
			for (SocketAddress socketAddress: lockedReplicas) {
				DatagramPacket dp = new DatagramPacket(sendData, sendData.length, socketAddress);
				socket.send(dp);
			}
			Vector<DatagramPacket> datagramPackets = nbio.receiveMessages(1000, lockedReplicas.size());
			@SuppressWarnings("unused")
			Collection<MessageWithSource<Vote>> result = unpuckVotes(datagramPackets);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//result, do something here
	}
	
	/**
	 * Part c) Implement this method (and checkReadQuorum()/checkWriteQuorum, see below) to read the
	 * replicated value using the weighted voting protocol.
	 */
	public VersionedValue<T> get() throws QuorumNotReachedException {
		//get the quorum of replicas with a read lock
		Collection<SocketAddress> quorum = this.requestReadVote();
		//read one
		VersionedValue<T> result = this.readReplica(quorum.iterator().next());
        //release locked replicas
        this.releaseReadLock(quorum);
		return result;
	}

	/**
	 * Part d) Implement this method to set the
	 * replicated value using the weighted voting protocol.
	 */
	public void set(T value) throws QuorumNotReachedException {
		//get the quorum of replicas with a write lock
		Collection<SocketAddress> quorum = this.requestWriteVote();
		//write all
		this.writeReplicas(quorum, new VersionedValue<T>(1, value));
		//release locked replicas
		this.releaseWriteLock(quorum);
	}

	/**
	 * Part c) Implement these methods to check whether a sufficient number of
	 * replies were received for RequestReadVote. If a sufficient number was received, 
	 * this method should return the {@link MessageWithSource}s of the locked {@link Replica}s.
	 * Otherwise, a QuorumNotReachedException must be thrown.
	 * @throws QuorumNotReachedException 
	 */
	protected Collection<SocketAddress> checkReadQuorum(
			Collection<MessageWithSource<Vote>> replies) throws QuorumNotReachedException {
		Collection<SocketAddress> result = new Vector<SocketAddress>();
		//Iterate over replies and count the quorum
		for (MessageWithSource<Vote> mwSource: replies) {
			Vote vote = mwSource.getMessage();
			//System.out.println(vote.getState());
			if (vote.getState().equals(Vote.State.YES)) {
				result.add(mwSource.getSource());
			}
		}
		if (result.size() >= this.readQuorum) {
			return result;
		} else {
			//if the quorum does not reach to the threshold then throw QuorumNotReachedException
			throw new QuorumNotReachedException(readQuorum, result);			
		}
	}

	/**
	 * Part d) Implement these methods to check whether a sufficient number of
	 * replies were received for RequestWriteVote. If a sufficient number was received, 
	 * this method should return the {@link MessageWithSource}s of the locked {@link Replica}s.
	 * Otherwise, a QuorumNotReachedException must be thrown.
	 * @throws QuorumNotReachedException 
	 */
	protected Collection<SocketAddress> checkWriteQuorum(
			Collection<MessageWithSource<Vote>> replies) throws QuorumNotReachedException {
		Collection<SocketAddress> result = new Vector<SocketAddress>();
		//Iterate over replies and count the quorum
		for (MessageWithSource<Vote> mwSource: replies) {
			Vote vote = mwSource.getMessage();
			if (vote.getState().equals(Vote.State.YES)) {
				result.add(mwSource.getSource());
			}
		}
		if (result.size() >= this.writeQuorum) {
			return result;
		} else {
			//if the quorum does not reach to the threshold then throw QuorumNotReachedException
			throw new QuorumNotReachedException(writeQuorum, result);			
		}
	}
}