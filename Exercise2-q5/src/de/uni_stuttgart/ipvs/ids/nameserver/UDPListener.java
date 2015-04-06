/* 
 * UDPListener.java
 * - implement a UPD listener thread for Nameserver instances
 *
 * Distributed Systems Exercise
 * Assignment 2 Part II
 */

package de.uni_stuttgart.ipvs.ids.nameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

// part b) listen for UDP datagrams for recursive lookup
public class UDPListener extends Thread {

	private int port;
	private Nameserver nameserver; // for issuing callbacks to owner
	private ServerSocket udpSocket;

	public UDPListener(Nameserver nameserver, int port) {
		this.nameserver = nameserver;
		this.port = port;
		try {
			udpSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		/* TODO: implement UDP listener here
		 *     - pass requests to owner using nameserver.lookupRecursive(...)
		 */
		try {
			//Accept a UDP connection and listen for request
			Socket socket = udpSocket.accept();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			OutputStream out = socket.getOutputStream();
			String message = reader.readLine().trim();
			//command the Root node to look the request recursively
			out.write(nameserver.lookupRecursive(message).getBytes());
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		run();
	}
}
