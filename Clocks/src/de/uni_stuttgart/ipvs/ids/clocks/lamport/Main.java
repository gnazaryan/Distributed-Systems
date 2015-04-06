package de.uni_stuttgart.ipvs.ids.clocks.lamport;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

import de.uni_stuttgart.ipvs.ids.clocks.Clock;
import de.uni_stuttgart.ipvs.ids.communicationLib.EventQueue;

public class Main {
	public static void main(String[] args) {
		InetSocketAddress p1_addr = new InetSocketAddress("localhost", 1236);
		InetSocketAddress p2_addr = new InetSocketAddress("localhost", 4000);
		InetSocketAddress p3_addr = new InetSocketAddress("localhost", 1237);
		InetSocketAddress p4_addr = new InetSocketAddress("localhost", 1238);

		long incrementTime = 50; // ms

		Set<InetSocketAddress> neighbourSet1 = new HashSet<InetSocketAddress>();
		neighbourSet1.add(p2_addr);
		try {
			LamportsSynchronizationProcess process1 = new LamportsSynchronizationProcess(
					new Clock("1", incrementTime, 0.07), p1_addr, neighbourSet1);
			LamportsSynchronizationProcess process3 = new LamportsSynchronizationProcess(
					new Clock("3", incrementTime, 0.15), p3_addr, neighbourSet1);
			LamportsSynchronizationProcess process4 = new LamportsSynchronizationProcess(
					new Clock("4", incrementTime, -0.10), p4_addr,
					neighbourSet1);
			Set<InetSocketAddress> neighbourSet2 = new HashSet<InetSocketAddress>();
			neighbourSet2.add(p1_addr);
			neighbourSet2.add(p3_addr);
			neighbourSet2.add(p4_addr);
			LamportsSynchronizationProcess process2 = new LamportsSynchronizationProcess(
					new Clock("2", incrementTime, 0.00), p2_addr, neighbourSet2);

			EventQueue eventQueue = EventQueue.getInstance();
			eventQueue.addChannel(p1_addr, p2_addr, 100);
			eventQueue.addChannel(p2_addr, p1_addr, 130);
			eventQueue.addChannel(p2_addr, p3_addr, 140);
			eventQueue.addChannel(p3_addr, p2_addr, 180);
			eventQueue.addChannel(p2_addr, p4_addr, 245);
			eventQueue.addChannel(p4_addr, p2_addr, 150);

			process1.start_Receiver();
			process2.start_Receiver();
			process3.start_Receiver();
			process4.start_Receiver();

			process1.start_Sender();
			process2.start_Sender();
			process3.start_Sender();
			process4.start_Sender();
		} catch (SocketException e) {
			System.err.println("Could not create Socket: " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
