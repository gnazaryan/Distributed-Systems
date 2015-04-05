package de.uni_stuttgart.ipvs.ids.globalstate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Process implements Runnable{
	
	private int id;
	private int stateSaveId = 0;
	private double balance;
	private int listenPort;
	private ArrayList<Process> neighbors;
	private ArrayList<Collector> collectors;
	
	private RecordedState rs;
	
	private Object markerLockObject = new Object();
	
	public Process(int id, double balance,int listenPort){
		this.id = id;
		this.balance = balance;
		this.listenPort = listenPort;
		neighbors = new ArrayList<Process>();
		collectors = new ArrayList<Collector>();
	}
	
	public void addNeighbour(Process p){
		if(!neighbors.contains(p))
			neighbors.add(p);
	}
	
	public void registerCollector(Collector c){
		if(!collectors.contains(c))
			collectors.add(c);
	}
	
	public void saveCurrentState(int stateSaveId){
		//I have synchronized this block of code with the transfer money method code block with the markerLockObject
		//because as soon as the process receives a saveCurrentState command, it should not transfere any money messages
		synchronized (markerLockObject) {
			rs = new RecordedState(this.balance, this.id);
			MarkerMessage markerMessage = new MarkerMessage(this.id, stateSaveId);
			sendMarkerMessgaeToNeighbors(markerMessage);
		}
	}

	//This method sends the given marker message to the all neighboring processes
	public void sendMarkerMessgaeToNeighbors(MarkerMessage markerMessage) {
		for (Process process: neighbors) {
			try {
				Socket client = new Socket(process.getAddress(), process.getPort());
				OutputStream outToServer = client.getOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(outToServer);
				out.writeObject(markerMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void transferMoney(double amount, Process receiver){
		if(!neighbors.contains(receiver))
			return;
		//synchronize the transfer money with saveCurrentStatemethod's block
		synchronized (markerLockObject) {
			try {
				//send the amounted money to the given process
				MoneyMessage moneyMessage = new MoneyMessage(amount, this.getID());
				Socket client = new Socket(receiver.getAddress(), receiver.getPort());
				OutputStream outToServer = client.getOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(outToServer);
		        out.writeObject(moneyMessage);
		        this.balance -= amount;
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Process " + id + " send " + amount +"$ to process "+receiver.getID());
			System.out.println("New balance of process " + id + " is " + balance + "$");
		}
	}

	public void run() {
		try {
		ServerSocket server = new ServerSocket(listenPort);
			while(true){
				Socket client = server.accept();
				Thread.sleep(100); // simulate delay - do not change

				ObjectInputStream inputStreem =  new ObjectInputStream(client.getInputStream());
				try {
					Object message = inputStreem.readObject();
					synchronized (markerLockObject) {
						if (message instanceof MoneyMessage) {
							//receive any money message and update the balance
							MoneyMessage moneyMessage = (MoneyMessage)message;
							balance += moneyMessage.getAmount();
							if (rs != null) {
								rs.addMessageToChanel(moneyMessage);
							}
						} else if (message instanceof MarkerMessage) {					
							MarkerMessage markerMessage = (MarkerMessage)message;
							if (rs != null && this.stateSaveId != markerMessage.stateSaveId) {
								//terminate the save state marker requests and send data to collector
								for (Collector collector: collectors) {
									try {
										Socket collectorClient = new Socket(collector.getAddress(), collector.getPort());
										OutputStream outToServer = collectorClient.getOutputStream();
										ObjectOutputStream out = new ObjectOutputStream(outToServer);
										out.writeObject(rs);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								rs = null;
								this.stateSaveId = markerMessage.stateSaveId;
							} else if (rs == null) {
								//save the state and send the requests to neighboring processes
								rs = new RecordedState(this.balance, this.id);
								sendMarkerMessgaeToNeighbors(markerMessage);								
							}
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
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
		
	public int getID() {
		return id;
	}

	public double getBalance(){
		return balance;
	}
}