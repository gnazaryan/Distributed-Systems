package de.uni_stuttgart.ipvs.ids.ex7;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;

import de.uni_stuttgart.ipvs.ids.communicationLib.SLSDatagramSocket;
import de.uni_stuttgart.ipvs.ids.communicationLib.crypto.KeyTooLongException;
import de.uni_stuttgart.ipvs.ids.communicationLib.crypto.XorCryptoProvider;

public class Process {

	String name;
	DatagramSocket socket;
	SLSDatagramSocket slsSocket;

	public Process(String name, int localPort, SocketAddress remote, String key) throws SocketException,
			KeyTooLongException {
		this.name = name;
		this.socket = new DatagramSocket(localPort);
		this.socket.connect(remote);
		XorCryptoProvider cryptoProvider = new XorCryptoProvider();
		cryptoProvider.setKey(key);
		this.slsSocket = new SLSDatagramSocket(this.socket, cryptoProvider);
	}

	public void sendPlain(String msg) throws IOException {
		System.out.println(name + " sending plain message: " + msg);
		byte[] buf = msg.getBytes(Charset.forName("UTF8"));
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		socket.send(p);
	}

	public void sendEncrypted(String msg) throws IOException {
		System.out.println(name + " sending encrypted message: " + msg);
		byte[] buf = msg.getBytes(Charset.forName("UTF8"));
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		slsSocket.send(p);
	}
	
	public void sendEncryptedBrokenMsg(String msg) throws IOException {
		System.out.println(name + " sending encrypted broken message: " + msg);
		byte[] buf = msg.getBytes(Charset.forName("UTF8"));
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		slsSocket.sendWithBrokenMessage(p);
	}
	
	public void sendEncryptedBrokenSignature(String msg) throws IOException {
		System.out.println(name + " sending encrypted message with broken signature: " + msg);
		byte[] buf = msg.getBytes(Charset.forName("UTF8"));
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		slsSocket.sendWithBrokenSignature(p);
	}

	public String receivePlain() throws IOException {
		byte[] buf = new byte[4096];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		socket.receive(p);
		String msg = new String(p.getData(), p.getOffset(), p.getLength(),
				Charset.forName("UTF8"));
		System.out.println(name + " received plain message: " + msg);
		return msg;
	}

	public String receiveEncrypted() throws IOException {
		byte[] buf = new byte[4096];
		DatagramPacket p = new DatagramPacket(buf, buf.length);
		slsSocket.receive(p);
		String msg = new String(p.getData(), p.getOffset(), p.getLength(),
				Charset.forName("UTF8"));
		System.out.println(name + " received encrypted message: " + msg);
		return msg;
	}

}
