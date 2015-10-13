package net.smartpager.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.utils.CipherUtils;
import net.smartpager.android.utils.FfmpegUtils;
import net.smartpager.android.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for downloading and encrypting audiofiles
 * 
 * @author Roman
 * 
 */
public class FileDownloadService extends Service {

	public enum DownloadFileAction {
		ImageLoadAndEncode, WavLoadAndConvertTo3gpEncode
	}

	private static final int THREAD_POOL_SIZE = 2;

	/**
	 * HashSet to avoid simultaneously downloading the same files
	 */
	private HashSet<String> mFilesInProgress;
	private ExecutorService mExecutorService;

	public FileDownloadService() {
		mExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		mFilesInProgress = new HashSet<String>();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			DownloadFileAction action = DownloadFileAction.valueOf((intent.getAction()));
			DownloadRunnable downloadRunnable = null;
			switch (action) {
				case WavLoadAndConvertTo3gpEncode:
					downloadRunnable = new DownloadWavRunnable();
				break;

				default:
				break;
			}
			downloadRunnable.setUrl(intent.getExtras().getString(BundleKey.downloadFile.name()));
			mExecutorService.submit(downloadRunnable);
		}
		return Service.START_STICKY;
	}

	// ------------------------------------------------------------------------------
	/**
	 * Downloads, converts and encrypts audiofiles, received from network
	 * @author Roman
	 */
	public class DownloadWavRunnable extends DownloadRunnable {

		public DownloadWavRunnable() {

		}

		/**
		 * Downloads, converts and encrypts audiofiles, received from network
		 */
		@Override
		public void run() {
			String url = getUrl();

			try {
				File fileForSave = new File(FileUtils.getCacheDir(), getLocalFileWavName());
				final File file3gp = new File(FileUtils.getCacheDir(), getLocalFile3gpName());
				// if (new File(FileUtils.getCacheDir(), getLocalFileName()).exists()
				// || mFilesInProgress.contains(url)) {
				if (mFilesInProgress.contains(url)) {
					return;
				}

				downloadFileFromNetwork(url, fileForSave);
				fileForSave = FfmpegUtils.convertTo3gp(fileForSave, file3gp);
				
				File newPath = new File(FileUtils.getCacheDir(), getLocalFileName());				
				CipherUtils.encryptFile(FileDownloadService.this, fileForSave, newPath);

			} catch (Exception e) {
				e.printStackTrace();
				throw new NullPointerException();
			} finally {
				mFilesInProgress.remove(url);
			}
		}

		/**
		 * Downloads audiofile from the specified network url. If file is already in progress - skips its repeated download 
		 * @param url source audiofile url
		 * @param fileForSave destination audiofile 
		 * @throws IOException
		 * @throws MalformedURLException
		 * @throws FileNotFoundException
		 */
		private void downloadFileFromNetwork(String url, File fileForSave) throws IOException, MalformedURLException,
				FileNotFoundException {
			mFilesInProgress.add(url);

			BufferedInputStream bis = new BufferedInputStream(new URL(getUrl()).openStream());
			FileOutputStream fos = new FileOutputStream(fileForSave, false);
			OutputStream os = new BufferedOutputStream(fos);
			byte[] buffer = new byte[1024];
			int byteRead = 0;
			while ((byteRead = bis.read(buffer)) != -1) {
				os.write(buffer, 0, byteRead);
			}
			os.flush();
			fos.flush();
			fos.close();
			os.close();
			bis.close();
		}
	}

	public abstract class DownloadRunnable implements Runnable {
		private String mUrl;

		public DownloadRunnable() {

		}

		public String getLocalFileName() {
			return String.valueOf(mUrl.hashCode());
		}

		public String getLocalFileWavName() {
			return String.valueOf(mUrl.hashCode()) + ".wav";
		}

		public String getLocalFile3gpName() {
			return String.valueOf(mUrl.hashCode()) + ".3gp";
		}

		public String getUrl() {
			return mUrl;
		}

		public void setUrl(String url) {
			mUrl = url;
		}
	}
}
