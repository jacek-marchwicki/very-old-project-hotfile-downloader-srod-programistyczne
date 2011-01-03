package com.downloader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Create {

	public static String generateMD5Hash(String inSt){
		try{
		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		byte[] tab = inSt.getBytes();

		digest.update(tab);
		byte[] hash = digest.digest();
		return String.format("%x", new BigInteger(hash));
		} catch (NoSuchAlgorithmException nsae) {
			// ignore
		}
		return null;
	}

}
