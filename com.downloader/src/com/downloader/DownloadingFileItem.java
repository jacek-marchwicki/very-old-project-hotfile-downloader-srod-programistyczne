package com.downloader;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;

public class DownloadingFileItem extends Activity{
	//id: link id from database!
	//linkID: link id from hotfile! 
	//status: links status - 0=not found, 1=normal working link, 2=hotlink link
	//name: file name
	//size: file size in bytes
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
    }
	
	public DownloadingFileItem(){
		
	}
	
	public DownloadingFileItem(long id, int linkID, Boolean status, String name,
			String downloadLink, int size) {
		super();
		this.id = id;		//id of database
		this.linkID = linkID;		//id of hotfile
		this.status = status;
		this.name = name;
		this.downloadLink = downloadLink;
		this.size = size;
	}
	
	private long id;
	private int linkID;
	private Boolean status;
	private String name, downloadLink;
	/**
	 * @return the downloadLink
	 */
	public String getDownloadLink() {
		return downloadLink;
	}

	/**
	 * @param downloadLink the downloadLink to set
	 */
	public void setDownloadLink(String downloadLink) {
		this.downloadLink = downloadLink;
	}

	private int size;
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the status
	 */
	public Boolean getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Boolean status) {
		this.status = status;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
}
