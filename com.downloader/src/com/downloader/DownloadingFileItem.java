package com.downloader;

public class DownloadingFileItem {
	//id: link id
	//status: links status - 0=not found, 1=normal working link, 2=hotlink link
	//name: file name
	//size: file size in bytes
	
	public DownloadingFileItem(int id, Boolean status, String name,
			String downloadLink, int size) {
		super();
		this.id = id;
		this.status = status;
		this.name = name;
		this.downloadLink = downloadLink;
		this.size = size;
	}

	private int id;
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
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
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
