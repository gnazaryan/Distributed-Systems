package de.uni_stuttgart.ipvs.ids.communication;

import java.io.Serializable;

public class Vote implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7977513854513072668L;

	public enum State {YES, NO};
	
	final protected State responseState;
	final protected int version;
	final protected int noOfVotes;
	
	public State getState() {
		return responseState;
	}
	
	public int getVersion() {
		return version;
	}
	
	public int getNoOfVotes() {
		return noOfVotes;
	}
	
	public Vote(State responseState, int version, int noOfVotes) {
		this.responseState = responseState;
		this.version = version;
		this.noOfVotes = noOfVotes;
	}
	
}
