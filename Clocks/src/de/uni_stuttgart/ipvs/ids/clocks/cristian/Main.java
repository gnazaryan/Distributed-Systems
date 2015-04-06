package de.uni_stuttgart.ipvs.ids.clocks.cristian;

import java.net.InetSocketAddress;

import de.uni_stuttgart.ipvs.ids.clocks.Clock;
import de.uni_stuttgart.ipvs.ids.communicationLib.EventQueue;


public class Main {

	static InetSocketAddress p2_addr;

	public static void main(String[] args) {
		InetSocketAddress p1_addr = new InetSocketAddress("localhost", 1236);
		p2_addr = new InetSocketAddress("localhost", 4000);
		InetSocketAddress p3_addr = new InetSocketAddress("localhost", 1237);
		InetSocketAddress p4_addr = new InetSocketAddress("localhost", 1238);

		long incrementTime = 50; // ms

		Timeclient p_1 = new Timeclient(new Clock("1", incrementTime, 0.07), p1_addr, "P2");
		Timeserver p_2 = new Timeserver(new Clock("2", incrementTime, 0.00), p2_addr);
		Timeclient p_3 = new Timeclient(new Clock("3", incrementTime, 0.15), p3_addr, "P3");
		Timeclient p_4 = new Timeclient(new Clock("4", incrementTime, -0.10), p4_addr, "P4");

		EventQueue eventQueue = EventQueue.getInstance();
		eventQueue.addChannel(p1_addr, p2_addr, 100);
		eventQueue.addChannel(p2_addr, p1_addr, 130);
		eventQueue.addChannel(p2_addr, p3_addr, 140);
		eventQueue.addChannel(p3_addr, p2_addr, 180);
		eventQueue.addChannel(p2_addr, p4_addr, 245);
		eventQueue.addChannel(p4_addr, p2_addr, 150);

		new Thread(p_1, "TimeClient P1").start();
		new Thread(p_2, "TimeServer P2").start();
		new Thread(p_3, "TimeClient P3").start();
		new Thread(p_4, "TimeClient P4").start();
	}
}