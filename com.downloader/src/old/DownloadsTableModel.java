package old;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.downloader.DownloaderHotfile;

import android.util.Log;

public class DownloadsTableModel implements Observer {

	private ArrayList<DownloaderHotfile> downloadList = new ArrayList<DownloaderHotfile>();
	
	public void addDownload(DownloaderHotfile download)
	{
		//download.addObserver(this);
		downloadList.add(download);
	}

	@Override
	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub
		if(data != null){
		Log.i("eehhh", "test");
		}
		//alarm, something changed
	}
	
	public DownloaderHotfile getDownload(int row)
	{
		return (DownloaderHotfile)downloadList.get(row);
	}
	//remove download from list
	public void clearDownload(int row){
		downloadList.remove(row);
	}
	
	/*public int getColumnCount(){
		
	}*/
	
}
