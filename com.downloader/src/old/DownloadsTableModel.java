package old;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import com.downloader.DownloaderHotfile;
import com.downloader.HotFile;
import com.downloader.Md5Create;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
	/* ***********************************************
	 * 
	 * ASYNC TASK = MUST BE SUBCLASSED
	 */
	public class Async extends AsyncTask<Void, Integer, Void> {
		String downloadLink, username, password, directory;
		int myProgress;

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
		//	Toast.makeText(HotFile.this, "onPostExecute", Toast.LENGTH_LONG)
		//			.show();
			// butt.setClickable(true);
		}

		protected void setPreconditions(String link, String username,
				String password, String directory) {
			downloadLink = link;
			this.username = username;
			this.password = Md5Create.generateMD5Hash(password);
			this.directory = directory;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
	//		Toast.makeText(HotFile.this, "onPreExecute", Toast.LENGTH_LONG)
	//				.show();
			myProgress = 0;
		}

		
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			// try {
			// downloadFile(downloadLink, username, password, directory);
			// } catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			//myProgressBar.setProgress(values[0]);
		}

		/*
		 * Everything have to be checked before run method
		 */

	}
}
