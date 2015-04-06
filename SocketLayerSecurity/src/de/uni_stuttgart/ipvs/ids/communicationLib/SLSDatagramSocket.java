package de.uni_stuttgart.ipvs.ids.communicationLib;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import de.uni_stuttgart.ipvs.ids.communicationLib.crypto.InvalidSignatureException;
import de.uni_stuttgart.ipvs.ids.communicationLib.crypto.PaddingException;
import de.uni_stuttgart.ipvs.ids.communicationLib.crypto.XorCryptoProvider;

public class SLSDatagramSocket {

	DatagramSocket wrappedSocket;
	private XorCryptoProvider cryptoProvider;

	public SLSDatagramSocket(DatagramSocket wrappedSocket,
			XorCryptoProvider cryptoProvider) {
		this.wrappedSocket = wrappedSocket;
		this.cryptoProvider = cryptoProvider;
	}

	/**
	 * Passes the contents of the given DatagramPacket to the cryptoPRovider for decryption.
	 * The contents of the Packet are replaced with the decrypted data.
	 * 
	 * Node: This method MUST throw an InvalidSignatureException, if the signature does not match the payload data!
	 * 
	 * @param p The DatagramPacket whose data is to be decrypted.
	 * @throws IOException If an error during decryption occurs.
	 */
	protected void decryptPacket(DatagramPacket p) throws IOException {
		//decrypt the received packet
		byte[] result = p.getData();
	    //get the signature length to split the received data
		int signatureLength = cryptoProvider.getSignatureLength(p.getData(), p.getOffset(), p.getLength());
		byte[] cypherText = Arrays.copyOfRange(result, 0, p.getLength() - signatureLength);
		byte[] signature = Arrays.copyOfRange(result, p.getLength() - signatureLength, p.getLength());
		boolean isValis = false;
		try {
			//decrypt the signature to check for validity
			signature = cryptoProvider.decrypt(signature);
			isValis = cryptoProvider.checkSignature(cypherText, signature);
		} catch (PaddingException e1) {
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		//if not valid throw InvalidSignatureException
		if (!isValis) {
			throw new InvalidSignatureException();
		}
        byte[] plainText = null;
		try {
			plainText = cryptoProvider.decrypt(cypherText);
		} catch (PaddingException e) {
			e.printStackTrace();
		}
		p.setData(plainText);
	}

	/**
	 * Passes the contents of the given DatagramPacket to the cryptoPRovider for encryption.
	 * The contents of the Packet are replaced with the encryption data.
	 * @param p The DatagramPacket whose data is to be encryption.
	 * @throws IOException If an error during encryption occurs.
	 */
	protected void encryptPacket(DatagramPacket p) throws IOException {
		byte[] cypherText = null;
		byte[] signature = null;
		try {
			//encrypt the data using crypto provider
			cypherText = cryptoProvider.encrypt(p.getData());
			//encrypt the signature
			signature = cryptoProvider.encrypt(cryptoProvider.generateSignature(cypherText));
		} catch (PaddingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		byte[] result = concatArrays(cypherText, signature);
        p.setData(result);
	}

	//some utility method for concatenating 2 arrays
	public static byte[] concatArrays(byte[] array1, byte[] array2) {
	    int arr1Length = (array1 != null && array1.length > 0) ? array1.length : 0;
	    int arr2Length = (array2 != null && array2.length > 0) ? array2.length : 0;
	    byte[] resutlentArray = new byte[arr1Length+arr2Length]; 
	    for (int i=0,j=0; i < resutlentArray.length; i++){
	        if (i+1 <= arr1Length) {
	            resutlentArray[i]=array1[i];
	        } else {
	            resutlentArray[i]=array2[j];
	            j++;
	        }
	    }
	    return resutlentArray;
	}

	public synchronized void receive(DatagramPacket p) throws IOException {
		wrappedSocket.receive(p);
		decryptPacket(p);
	}

	public void send(DatagramPacket p) throws IOException {
		encryptPacket(p);
		wrappedSocket.send(p);
	}

	public void sendWithBrokenMessage(DatagramPacket p) throws IOException {
		encryptPacket(p);
		// manipulate the message
		int signatureLength = cryptoProvider.getSignatureLength(p.getData(), p.getOffset(), p.getLength());
		int byteToFlip = (int) Math.rint(Math.random() * (p.getLength() - signatureLength));
		p.getData()[byteToFlip] ^= 1;
		wrappedSocket.send(p);
	}

	public void sendWithBrokenSignature(DatagramPacket p) throws IOException {
		encryptPacket(p);
		// manipulate the signature
		int signatureLength = cryptoProvider.getSignatureLength(p.getData(), p.getOffset(), p.getLength());
		int byteToFlip = (int) Math.rint(Math.random() * (signatureLength));
		byteToFlip += p.getLength() - signatureLength;
		p.getData()[byteToFlip] ^= 1;
		wrappedSocket.send(p);
	}
}