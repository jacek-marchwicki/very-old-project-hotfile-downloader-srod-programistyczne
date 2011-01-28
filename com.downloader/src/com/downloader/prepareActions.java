package com.downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.downloader.Widgets.TextProgressBar;

public class prepareActions {

	/*
	 * Check if file exists on server
	 * http://api.hotfile.com/?c=checklinks
	 */
	public List<DownloadingFileItem> prepareFilesToDownload(List<String> downloadList) throws ClientProtocolException, IOException
	{
		List<String> keysIds = cutKeysIdsFromLinks(downloadList);
		String request = "http://api.hotfile.com/?action=checklinks&ids=";
		Boolean idStringExist = false;												//check if word 'keys' is in request
		for(String arg: keysIds)
		{
			if(arg.charAt(0) == 'i')request += arg.substring(1) + ",";
			else
					if(idStringExist) request += arg.substring(1) + ",";
					else {
						request = request.substring(0, request.length()-1);			//remove last comma
						request += "&keys=" + arg.substring(1) + ",";
						idStringExist = true;
					}
		}
		request = request.substring(0, request.length()-1) + "&fields=id,status,name,size";							//remove last comma
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost getDirectLink = new HttpPost(request);
		HttpResponse response = httpclient.execute(getDirectLink);
		HttpEntity entity = response.getEntity();
		String responseText = EntityUtils.toString(entity);
		
		List<DownloadingFileItem> list = new LinkedList<DownloadingFileItem>();
		BufferedReader reader = new BufferedReader(new StringReader(responseText));
		String str;
		int iterator = 0;
		while((str = reader.readLine()) != null){
			if(str.length()>0){
				//DownloadingFileItem item = new DownloadingFileItem(
				int firstcomma = str.indexOf(",");					//positions of comma's
				int secondcomma = str.indexOf(",", firstcomma+1);
				int thirdcomma = str.indexOf(",", secondcomma+1);
				list.add(new DownloadingFileItem(0,
						Integer.parseInt(str.substring(0, firstcomma)),
						Boolean.parseBoolean(str.substring(firstcomma+1, secondcomma)),
						downloadList.get(iterator),
						str.substring(secondcomma+1, thirdcomma),
						Integer.parseInt(str.substring(thirdcomma+1))
						));
				++iterator;
			}
		}
		return list;
	}

	
	
	private List<String> cutKeysIdsFromLinks(List<String> downloadList)
	{
		List<String> ids = new LinkedList<String>();
		List<String> keys = new LinkedList<String>();
		for(String link:downloadList)
		{
			if(link.lastIndexOf("/") == link.length()-1) link = link.substring(0, link.length()-1);
			ids.add("i"+link.substring(link.indexOf("dl/")+3, link.indexOf("/", link.indexOf("dl/")+4)));
			keys.add("k"+link.substring(link.indexOf("/", link.indexOf("dl/")+4)+1, link.lastIndexOf("/")));
		}
		ids.addAll(keys);
		return ids;
	}
	

}
