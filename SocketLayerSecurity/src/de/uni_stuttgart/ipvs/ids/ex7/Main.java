package de.uni_stuttgart.ipvs.ids.ex7;

import java.io.IOException;
import java.net.InetSocketAddress;

import de.uni_stuttgart.ipvs.ids.communicationLib.crypto.InvalidSignatureException;
import de.uni_stuttgart.ipvs.ids.communicationLib.crypto.KeyTooLongException;

public class Main {

	/**
	 * @param args
	 * @throws IOException
	 * @throws KeyTooLongException
	 */
	public static void main(String[] args) throws IOException,
			KeyTooLongException {
		InetSocketAddress aliceAddr = new InetSocketAddress("localhost", 50378);
		InetSocketAddress bobAddr = new InetSocketAddress("localhost", 50388);

		String preSharedKey = "secret";

		Process alice = new Process("Alice", aliceAddr.getPort(), bobAddr,
				preSharedKey);
		Process bob = new Process("Bob", bobAddr.getPort(), aliceAddr,
				preSharedKey);

		String msg = "Hi there, Bob! How are you today? Regards, Alice";

		// Verify sending a plain text message
		alice.sendPlain(msg);
		bob.receivePlain();

		// Send an encrypted message
		alice.sendEncrypted(msg);
		bob.receiveEncrypted();

		// Send an encrypted message where we purposefully mess up the encrypted data
		alice.sendEncryptedBrokenMsg(msg);
		try {
			bob.receiveEncrypted();
		} catch (InvalidSignatureException e) {
			System.out
					.println("Bob received a message with a broken signature.");
		}

		// Send an encrypted message where we purposefully mess up the signature
		alice.sendEncryptedBrokenSignature(msg);
		try {
			bob.receiveEncrypted();
		} catch (InvalidSignatureException e) {
			System.out
					.println("Bob received a message with a broken signature.");
		}
	}

}
