/* 
 * Client.java
 * - perform name resolution query
 *
 * Distributed Systems Exercise
 * Assignment 2 Part II
 */

package de.uni_stuttgart.ipvs.ids.nameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {

	// part a) start recursive lookup
	public String recursiveNameResolution(String name, String serverAddress, int serverPort) {
		try{
			//Create a socket connection to Root server
			Socket socket = new Socket(serverAddress, serverPort);
			BufferedReader bufferReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));
			OutputStream outputStreem = socket.getOutputStream();
			String requestMessage = name + "\n";
			outputStreem.write(requestMessage.getBytes());
			//Receive the answer message from root server if the address is found
			String responseMessage = bufferReader.readLine().trim();
			socket.close();
			return responseMessage;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	// part b) start iterative lookup
	public String iterativeNameResolution(String name, String serverAddress, int serverPort) {
		try {
			//Create a socket connection to Root server
			Socket socket = new Socket(serverAddress, serverPort);
			writeMessage(socket, name);
			String message = readMessage(socket);
			//If found return the message
			if (message.startsWith("[FOUND]")) {
				String[] messages = message.split("_:_");
				return messages[messages.length - 1];
			} else if (message.startsWith("[NOTFOUND]")) {
				//If not found proceed propagation to the next level server for the request
				String[] messages = message.split("_:_");
				String newName = messages[1];
				int newPort = Integer.parseInt(messages[2]);
				String newAddress = messages[3];
				return iterativeNameResolution(newName, newAddress, newPort);
			} else {
				//Else it is unknown case 
				return message;
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    //Method for writing a message to socket
	private void writeMessage(Socket socket, String message) throws IOException {
	 	 PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
	 	 printWriter.print(message);
	 	 printWriter.flush();
	}

	//method for reading the message from socket
	String readMessage(Socket socket) throws IOException {
	 	BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	 	char[] buffer = new char[200];
	 	int countCharacters = bufferedReader.read(buffer, 0, 200);
	 	String message = new String(buffer, 0, countCharacters);
	 	return message;
	}
}