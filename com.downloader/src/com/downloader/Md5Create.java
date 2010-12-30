package com.downloader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Create {

	public static String generateMD5Hash(String word){
		String hashword = null;
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(word.getBytes());
			BigInteger hash = new BigInteger(1, md5.digest());
			hashword = hash.toString(16);
		} catch (NoSuchAlgorithmException nsae) {
			// ignore
		}
		return hashword;
	}

}
