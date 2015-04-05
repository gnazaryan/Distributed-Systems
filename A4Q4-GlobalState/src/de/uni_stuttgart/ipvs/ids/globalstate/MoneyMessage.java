package de.uni_stuttgart.ipvs.ids.globalstate;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MoneyMessage implements Serializable{
	
	double amount;
	int sender;
	
	public MoneyMessage(double amount, int sender){
		this.amount = amount;
		this.sender = sender;
	}
	
	public double getAmount() {
		return amount;
	}

	public int getSender() {
		return sender;
	}
}
