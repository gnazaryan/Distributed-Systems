package de.uni_stuttgart.ipvs.ids.communicationLib.crypto;

/**
 * Thrown to indicate that
 * - Adding padding data to an input yielded an error
 * - A given input was not correctly padded
 *
 */
public class PaddingException extends Exception {

	private static final long serialVersionUID = -6574905135608277222L;

}
