package de.uni_stuttgart.ipvs.ids.clocks;

import java.util.Date;


/**
 * This Class simulates a hardware clock.
 */
public class Clock extends Thread {

	private final long incrementTime;
	private final double drift;
	private long counter;
	private long driftCounter;
	private final String name;

	public Clock(String name, long incrementTime, double drift) {
		super("Clock " + name);
		counter = 0L;
		this.incrementTime = incrementTime;
		this.drift = drift;
		this.name = name;
		this.start();
	}

	//calculate the amount of drifted time and subtract from local clock 
	public synchronized long getTime() {
		long driftPercentage = (long)(drift * 100.0); 
		long driftTime = (driftCounter * incrementTime * driftPercentage) / 100;
		//System.out.println("Clock: " + name + " getTime: " + new Date(counter * incrementTime - driftTime));
		return counter * incrementTime - driftTime;
	}

	public synchronized void setTime(long time) {
		counter = time;
		//reset the drift counter to 0 as the time has been calculated and there is no any drift till now
		driftCounter = 0;
		//System.out.println("Clock: " + name + " setTime: " + new Date(time));
	}

	public void run() {
		//counter is the clock time, later will be multiplied by {incrementTime}
		counter++;
		//driftCounter is used for counting the drifted time, later will be subtracted main counter in method getTime()
		driftCounter++;
		try {
			sleep(incrementTime);
			run();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
