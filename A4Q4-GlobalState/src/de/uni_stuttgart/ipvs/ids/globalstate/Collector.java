package de.uni_stuttgart.ipvs.ids.globalstate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Collector implements Runnable{
	private int listenPort;
	private int processCount;		// for checking if all processes send their states
	
	// for the output
	double sumStates;
	double sumChannels;

	//Useful constructor no reason to my love
	public Collector(int listenPort,int processCount) {
		this.listenPort = listenPort;
		this.processCount = processCount;
		
	}
	
	@Override
	public void run() {
		try{
			ServerSocket server = new ServerSocket(listenPort);
			int processReceived = 0;
			while(processReceived < processCount) {
				Socket client = server.accept();
				ObjectInputStream inputStreem =  new ObjectInputStream(client.getInputStream());
				try {
					RecordedState message = (RecordedState) inputStreem.readObject();
					//calculate the states and channel states for the pending process
					sumStates += message.getBalance();
					sumChannels += message.chunelSum();
					processReceived++;
					if (processReceived == processCount) {
						// call this method after all states were collected
						//print the info to console as soon as all processes reported the channel and current states
						printToConsole();
						processReceived= 0;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public void printToConsole(){
		System.out.println("------------------------------");
		System.out.println("Total amount of money is: " + (sumStates+sumChannels));
		System.out.println("Total amount of money in states is: " + sumStates);
		System.out.println("Total amount of money in channels is: " + sumChannels);
		System.out.println("------------------------------");
		sumStates=0;
		sumChannels=0;
	}
	
	
	public String getAddress() {
		try {
			return java.net.InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return "localhost";
		}
	} 
	
	public int getPort(){
		return listenPort;
	}
	
}
