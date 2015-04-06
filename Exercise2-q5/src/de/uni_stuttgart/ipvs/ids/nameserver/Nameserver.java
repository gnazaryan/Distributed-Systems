/* 
 * Nameserver.java
 * - implement a name server process
 *
 * Distributed Systems Exercise
 * Assignment 2 Part II
 */

package de.uni_stuttgart.ipvs.ids.nameserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

public class Nameserver {

	private final static String IP = "localhost";

	
	private HashMap<String, Nameserver> children;
	private HashMap<String, String> savedNames;
	
	private int tcpPort;
	private int udpPort;
	private String domain;

	private TCPListener tcpListener;
	private UDPListener udpListener;

	public Nameserver(String domain, Nameserver parent, int udpPort, int tcpPort) { // DO NOT MODIFY!
		this.domain = domain;
		this.tcpPort = tcpPort;
		this.udpPort = udpPort;
		children = new HashMap<String, Nameserver>();
		savedNames = new HashMap<String, String>();

		try {
			tcpListener = new TCPListener(this, tcpPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		udpListener = new UDPListener(this, udpPort);
		tcpListener.start();
		udpListener.start();
	}

	public void addChild(String domain, Nameserver nameserver) { // DO NOT MODIFY!
		children.put(domain, nameserver);
	}

	public void addNameAddressPair(String name, String address) { // DO NOT MODIFY!
		savedNames.put(name, address);
	}

	public int getUdpPort() { // DO NOT MODIFY!
		return udpPort;
	}

	public int getTcpPort() { // DO NOT MODIFY!
		return tcpPort;
	}

	public String getAddress() { // DO NOT MODIFY!
		return IP;
	}

	public String getDomain() { // DO NOT MODIFY!
		return domain;
	}

	// part a) recursive lookup
	public String lookupRecursive(String name) {
		// diagnostic output - DO NOT MODIFY!!
		System.err.println("[INFO] lookupRecursive(\"" + name + "\")" +
		                   " on Nameserver \"" + this.domain + "\"");
        //Lookup if server contains names in it's cache
		if(savedNames.containsKey(name)) {
			return savedNames.get(name);
		} 

		//exploit address key from domain
		String key = name;
		String newName = name;
		if(name.contains(".")){
			key = name.substring(name.lastIndexOf(".") + 1);
			newName = name.substring(0, name.lastIndexOf("."));
		}
		//Propagate the request to appropriate children depending on provided key
		if(children.containsKey(key)) {
			Nameserver nameServer = children.get(key);
			try {
				//Send Recursive call to children
				Socket udpConnection = new Socket(
						nameServer.getAddress(), 
						nameServer.getUdpPort());
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(udpConnection.getInputStream()));
				OutputStream out = udpConnection.getOutputStream();
				String message = newName + "\n";
				out.write(message.getBytes());
				// reed message from bottom children and return to the root node
				String response = reader.readLine().trim();
				udpConnection.close();
				return response;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return "[UNKNOWN]";
	}

	// part b) iterative lookup
	public String lookupIterative(String name) {
		// diagnostic output - DO NOT MODIFY!!
		System.err.println("[INFO] lookupIterative(\"" + name + "\")" +
		                   " on Nameserver \"" + this.domain + "\"");
		StringBuilder sb = new StringBuilder();
		String[] names = name.split("\\.");
		String namekey = names[0];
		//Check if current server contains the requested information
		if (this.savedNames.containsKey(namekey)) {
			sb.append("[FOUND]_:_" + savedNames.get(namekey));
			return sb.toString();
		}
		
		//If address is not found in local cache then return to client the address of next level server
		namekey = names[names.length - 1];
		if (this.children.containsKey(namekey)) {
			Nameserver nameserver = this.children.get(namekey);
			name = name.replace("." + namekey, "");
			sb.append("[NOTFOUND]_:_" + name + "_:_" + nameserver.getTcpPort() + "_:_" + nameserver.getAddress());
			return sb.toString();
		}
		return "[UNKNOWN]";
	}
	// TODO: add additional methods if necessary...
}