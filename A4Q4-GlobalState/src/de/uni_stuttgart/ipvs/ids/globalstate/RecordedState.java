package de.uni_stuttgart.ipvs.ids.globalstate;

import java.io.Serializable;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class RecordedState implements Serializable{

	private double balance;

	private int id;

	//a channel to keep the recorded messages
	private LinkedList<MoneyMessage> chanel;

	public RecordedState(double balance, int id) {
		this.balance = balance;
		this.id = id;
		chanel = new LinkedList<MoneyMessage>();
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void addMessageToChanel(MoneyMessage moneyMessage) {
		chanel.add(moneyMessage);
	}

	public MoneyMessage remove(){
		return chanel.removeFirst();
	}

	public int chanelSize() {
		return chanel.size();
	}

	//calculate the sum of the chanel
	public double chunelSum() {
		double result = 0.0;
		for(MoneyMessage moneyMessage: chanel) {
			result+=moneyMessage.getAmount();
		}
		return result;
	}
}