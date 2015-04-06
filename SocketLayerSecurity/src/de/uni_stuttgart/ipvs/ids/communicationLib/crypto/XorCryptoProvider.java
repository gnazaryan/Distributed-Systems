package de.uni_stuttgart.ipvs.ids.communicationLib.crypto;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class XorCryptoProvider {

	/**
	 * The shared secret key used for encryption/decryption.
	 */
	protected byte[] key = null;

	/**
	 * Converts the given String (in UTF-8 interpretation) to a byte array which
	 * is in turn used as the key.
	 * 
	 * @param key
	 *            The key to be used for encryption/decryption/signing
	 * @throws KeyTooLongException
	 *             If the key is longer than this implementation can handle
	 */
	public void setKey(String key) throws KeyTooLongException {
		this.setKey(key.getBytes(Charset.forName("UTF8")));
	}

	/**
	 * Sets the given byte array as the key for this XorCryptoProvider.
	 * 
	 * @param key
	 *            The key to be used for encryption/decryption/signing
	 * @throws KeyTooLongException
	 *             If the key is longer than this implementation can handle
	 */
	public void setKey(byte[] key) throws KeyTooLongException {
		if (key.length > Byte.MAX_VALUE) {
			throw new KeyTooLongException(key.length, Byte.MAX_VALUE);
		}
		this.key = key;
	}

	/**
	 * Create a new array with the data from the original plain array plus
	 * padding at the end.
	 * 
	 * @param plain
	 *            The data to be padded.
	 * @return A new array containing the padded data.
	 * @throws PaddingException
	 *             If an error occurs during creating the padding data.
	 */
	protected byte[] addPadding(byte[] plain) throws PaddingException {
		if (plain == null || plain.length == 0) {
			throw new PaddingException();
		}
		//add padding to the input to be equal to the block size, if equal add another block
		int BlockSize = key.length;
		int paddingLength = BlockSize - plain.length % BlockSize;
		if (paddingLength == 0) {
			paddingLength = BlockSize;
		}
		byte[] result = new byte[plain.length + paddingLength];
		for (int i = 0; i < result.length; i++) {
			if (i < plain.length) {
				result[i] = plain[i];
			} else {
				result[i] = (byte) paddingLength;
			}
		}
		return result;
	}

	/**
	 * Creates a new array with only payload data, without the padding data.
	 * Verifies that the padding data is intact.
	 * 
	 * @param data
	 *            The padded data
	 * @return A new array containing only payload data.
	 * @throws PaddingException
	 *             Thrown if either the input is of incorrect length or if the
	 *             padding data is not intact.
	 */
	protected byte[] removePadding(byte[] data) throws PaddingException {
		//remove the padding that was previously added
		//by verifying the last value to be equal to the last number occurrences
		if (data == null || data.length == 0 || data.length % key.length != 0) {
			throw new PaddingException();
		}
		byte verifyingValue = data[data.length - 1];
		for (int i = data.length - 1; i >= data.length - verifyingValue; i--) {
			if (data[i] != verifyingValue) {
				throw new PaddingException();
			}
		}
		return Arrays.copyOfRange(data, 0, data.length - data[data.length - 1]);
	}

	/**
	 * Encrypts the given byte array. The encrypted data is stored in a new
	 * array that is returned. The new array may be longer than the input, since
	 * padding may have been added.
	 * 
	 * @param plain
	 *            The plaintext data
	 * @param offset
	 *            Offset for the start of the data in the input
	 * @param length
	 *            Length of data in the input
	 * @return A new array containing the encrypted data, padded to match the
	 *         block size.
	 * @throws PaddingException
	 *             Thrown if an error occurs during padding the data.
	 */
	public byte[] encrypt(byte[] plain) throws PaddingException {
		//Encript the given byte stream by ixoring with the key
		//I have added the padding before encryption to make sure the block size is not
		//attacked by middle man
		plain = addPadding(plain);
		final byte[] result = new byte[plain.length];
	    int k = 0;
	    for (int i = 0; i < plain.length; i++) {
	    	result[i] = (byte) (plain[i] ^ key[k]);
	        k++;
	        if (k >= key.length) {
	            k = 0;
	        }
	    }
	    return result;
	}

	/**
	 * Decrypts the given byte array and returns the decrypted and unpadded data
	 * in a new array.
	 * 
	 * @param cypher
	 *            The encrypted data. After Decryption, this array contains the
	 *            plain data.
	 * @return The length of the plain data in the array. This may be smaller
	 *         than the length of the array, since padding might be removed.
	 * @throws PaddingException
	 *             Thrown if either the length of the input array is not a
	 *             multiple of the block size or if the padding data contains an
	 *             error.
	 */
	public byte[] decrypt(byte[] cipher) throws PaddingException {
		//Decrypt the cipher text by ixoring with key
	    final byte[] result = new byte[cipher.length];
	    int k = 0;
	    for (int i = 0; i < cipher.length; i++) {
	    	result[i] = (byte) (cipher[i] ^ key[k]);
	        k++;
	        if (k >= key.length) {
	            k = 0;
	        }
	    }
	    return removePadding(result);
	}

	/**
	 * Generates a signature for the given content.
	 * 
	 * @param content
	 *            The content to be signed.
	 * @return A new byte array containing the generated signature.
	 * @throws NoSuchAlgorithmException
	 *             Thrown if the chosen hash algorithm is not available on this
	 *             platform.
	 * @throws PaddingException
	 *             Thrown if an error occurs during padding the signature to
	 *             block size.
	 */
	public byte[] generateSignature(byte[] content)
			throws NoSuchAlgorithmException, PaddingException {
		//Generate a SHA1 signature for the given content using MessageDiges
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
        byte[] result = mDigest.digest(content);
        return result;
	}

	/**
	 * Tests whether the given signature is valid for the given content.
	 * 
	 * @param content
	 *            Content whose signature is to be verified (passed without the
	 *            signature)
	 * @param signature
	 *            Signature to be verified
	 * @return True in case the signature is valid for the given content, false
	 *         otherwise.
	 * @throws NoSuchAlgorithmException
	 *             Thrown if the chosen hash algorithm is not available on this
	 *             platform.
	 * @throws PaddingException
	 *             Thrown if an error occurs during padding the signature to
	 *             block size.
	 */
	public boolean checkSignature(byte[] content, byte[] signature)
			throws NoSuchAlgorithmException, PaddingException {
		//Generate a signature over the content and compare with the given one
		byte[] newSignature = generateSignature(content);
        return Arrays.equals(newSignature, signature);
	}

	/**
	 * Returns the length of the signature produced by this algorithm. Note
	 * that, depending on the signature scheme, the length of a signature may
	 * not always be constant.
	 * 
	 * @param data
	 *            Data to determine the signature length for/from
	 * @param offset
	 *            Offset of data in the array
	 * @param length
	 *            Length of data in the array
	 * @return The size (in bytes) of the signature data
	 */
	public int getSignatureLength(byte[] data, int offset, int length) {
		//iteratively run through multiple of blocksizes until the  signature 
		//of message will not be equal to the bytestreem after that multiple of blocksize index.
		boolean invalid = true;
		int nextMultiple = 0;
		while(invalid) {
			if (nextMultiple + key.length > length) {
				break;
			}
			nextMultiple+=key.length;
			byte[] nextData = Arrays.copyOfRange(data, 0, length - nextMultiple);
			try {
				byte[] nextSignature = decrypt(Arrays.copyOfRange(data, length - nextMultiple, length));
				invalid = !checkSignature(nextData, nextSignature);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (PaddingException e) {
				e.printStackTrace();
			}
		}
		return nextMultiple;
	}
}