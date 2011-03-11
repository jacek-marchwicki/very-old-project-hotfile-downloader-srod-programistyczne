package com.downloader.Services;

import java.util.Map;

public class DownloadServiceThreadData {
	public Map<Long, DownloadItem> downloads;

	public DownloadServiceThreadData(Map<Long, DownloadItem> downloads) {
		this.downloads = downloads;
	}
}