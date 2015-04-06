/* 
 * TCPListener.java
 * - implement a TCP listener thread for Nameserver instances
 *
 * Distributed Systems Exercise
 * Assignment 2 Part II
 */

package de.uni_stuttgart.ipvs.ids.nameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

// part a) listen for TCP connections for iterative lookup
public class TCPListener extends Thread {

	private int port;
	private Nameserver nameserver; // for issuing callbacks to owner
	java.net.ServerSocket serverSocket;

	public TCPListener(Nameserver nameserver, int port) throws IOException {
		this.nameserver = nameserver;
		this.port = port;
		serverSocket = new java.net.ServerSocket(port);
	}

	public void run() {
		try {
			//TCP Socket server connection and listener for NameServer
			java.net.Socket server = serverSocket.accept();//wait for a connection and accept it
			String message = readMessage(server);
			//command the node to look the request iteratively
			String sendMessage = nameserver.lookupIterative(message);
			writeMessage(server, sendMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
		run();
    }

	//Read message from socket
	String readMessage(java.net.Socket socket) throws IOException {
        BufferedReader bufferedReader =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
        char[] buffer = new char[200];
        int characterCount = bufferedReader.read(buffer, 0, 200);
        String message = new String(buffer, 0, characterCount);
	 	return message;
    }

	//write message to socket
	public void writeMessage(java.net.Socket socket, String message) throws IOException {
        PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        printWriter.print(message);
        printWriter.flush();
	}
}