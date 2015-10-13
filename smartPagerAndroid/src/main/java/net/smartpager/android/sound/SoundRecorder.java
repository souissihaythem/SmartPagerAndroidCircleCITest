package net.smartpager.android.sound;

import android.media.MediaRecorder;
import android.os.Handler;
import android.widget.TextView;

import net.smartpager.android.utils.DateTimeUtils;

import java.io.IOException;

public class SoundRecorder {

	private static final int PROGRESS_UPDATE_TIME = 1000;
	
	private MediaRecorder mRecorder;
	private Handler mHandler;
	private TextView mProgressTextView;
	private String mProgressString;
	private long mProgressTime;
	private String mFileName;
	private boolean mRecording;
	
	public SoundRecorder(TextView recordTime){
		mProgressTextView = recordTime;
		mRecording = false;
		resetProgress();
	}
	
	public SoundRecorder(){
		this(null);
	}
	
	public void start(String filename) throws IOException {
		mFileName = filename;
		mHandler = new Handler();
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOutputFile(mFileName);
		mRecorder.prepare();
		mRecorder.start();
		resetProgress();
		mRecording = true;
		mHandler.postDelayed(updateProgress, PROGRESS_UPDATE_TIME);
	}
	
	public void stop(){
		if (mHandler != null){
			mHandler.removeCallbacks(updateProgress);
		}
		if (mRecorder != null){
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		mRecording = false;
	}
	
	private void resetProgress(){
		mProgressTime = 0;
		setProgress(mProgressTime);
	}
	
	private void setProgress(long progress){
		mProgressString = DateTimeUtils.progressToString( progress);
		if (mProgressTextView != null){
			mProgressTextView.setText(mProgressString);
		}
	}
	
	public boolean isRecording(){		
		return mRecording;
	}
	
	public String getProgressString(){
		return mProgressString;
	}

	public String getFileName(){
		return mFileName;
	}
	
	private Runnable updateProgress = new Runnable() {
		@Override
		public void run() {
			mProgressTime += PROGRESS_UPDATE_TIME;		
			setProgress(mProgressTime);
			mHandler.postDelayed(this, PROGRESS_UPDATE_TIME);
		}
	};
}
