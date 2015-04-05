package de.uni_stuttgart.ipvs.ids.globalstate;

public class Main implements Runnable {
	private Collector c;
	private Process  p1;
	private Process  p2;
	private Process  p3;
	private Process  p4;
	
	public Main() {
		p1 = new Process(1,  300, 4000);
		p2 = new Process(2,  750, 4001);
		p3 = new Process(3,  150, 4002);
		p4 = new Process(4, 1300, 4003);
		
		c = new Collector(4004, 4);
		p1.registerCollector(c);
		p2.registerCollector(c);
		p3.registerCollector(c);
		p4.registerCollector(c);
		
		p1.addNeighbour(p2);
		p1.addNeighbour(p3);
		p2.addNeighbour(p1);
		p2.addNeighbour(p4);
		p3.addNeighbour(p1);
		p3.addNeighbour(p4);
		p4.addNeighbour(p2);
		p4.addNeighbour(p3);
		
		new Thread(p1).start();
		new Thread(p2).start();
		new Thread(p3).start();
		new Thread(p4).start();
		new Thread(c).start();				
		
		new Thread(this).start();
	}
	
	public static void main(String[] args) {
		new Main();					
	}
		
	@Override
	public void run() {
		//Test scenario
		try {
			//As processes are not synchronized, we should have an ID for marker message to terminate the recording
			//I did not use the senderId because later the same sender may initiate the recording request again
			int stateSaveId = 1;
			p1.saveCurrentState(stateSaveId);
			p2.transferMoney(170, p1);
			Thread.sleep(10);
			p4.transferMoney(250, p2);
			p4.transferMoney(50,  p3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
