package com.downloader.Services;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class UsernamePasswordMD5Storage {
	private static String username;
	private static String passwordmd5;
	private static String filePath;

	public static String getUsername() {
		return username;
	}

	public static String getPasswordMD5() {
		return passwordmd5;
	}

	public static String getDirectory() {
		return filePath;
	}

	public static void setUsernameAndPasswordMD5(final String mUsernameA,
			final String mPasswordmd5) {
		try {
			if (checkUsernamePasswordValid(mUsernameA, mPasswordmd5)) {
				username = mUsernameA;
				passwordmd5 = mPasswordmd5;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setDirectoryPath(final String directory) {
		filePath = directory;
	}

	/**
	 * Check validity of username and password Example response ->
	 * is_premium=1&premium_until
	 * =2011-01-01T05:25:58-06:00&hotlink_traffic_kb=209715200 Only first we
	 * checking already
	 */
	private static Boolean checkUsernamePasswordValid(
			final String mUsername, final String mPasswordmd5)
			throws ClientProtocolException, IOException {
		String request = "http://api.hotfile.com/?action=getuserinfo&username="
				+ mUsername + "&passwordmd5=" + mPasswordmd5;
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost getDirectLink = new HttpPost(request);
		HttpResponse response = httpclient.execute(getDirectLink);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);
		int indexOfEquationSign = responseText.indexOf("=");
		if (indexOfEquationSign == -1)
			return false;
		if (responseText.substring(indexOfEquationSign + 1,
				indexOfEquationSign + 2).equals("1"))
			return true;
		return false;
	}
}