package de.uni_stuttgart.ipvs.ids.globalstate;

import java.io.Serializable;

@SuppressWarnings("serial")
public class MarkerMessage implements Serializable{
	int sender;
	int stateSaveId;
	public MarkerMessage(int sender){
		this.sender = sender;
	}
	
	public MarkerMessage(int sender, int stateSaveId){
		this.sender = sender;
		this.stateSaveId = stateSaveId;
	}
	
	public int getSender(){
		return sender;
	}
}
