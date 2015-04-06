package de.uni_stuttgart.ipvs.ids.communicationLib.crypto;

/**
 * Thrown to indicate that a given key was longer than the maximum blocksize
 * that can be handled by a block cypher.
 * 
 */
public class KeyTooLongException extends Exception {

	private static final long serialVersionUID = 3809506331649348026L;

	public KeyTooLongException(int actual, int maximum) {
		super("Key too long. Got " + actual
				+ " Bytes, cannot deal with more than " + maximum + " Bytes.");
	}

}
