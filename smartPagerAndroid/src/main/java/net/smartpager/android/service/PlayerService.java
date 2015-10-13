package net.smartpager.android.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.model.PlayInfo;
import net.smartpager.android.model.PlayInfo.PlayStatus;
import net.smartpager.android.service.FileDownloadService.DownloadFileAction;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.CipherUtils;
import net.smartpager.android.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.NoSuchPaddingException;

public class PlayerService extends Service {

	private static final int TIMER_UPDATE_DELAY = 1000;
	private MediaPlayer mPlayer;
	private Timer mTimer;
	private boolean isTimerStop = true;
	PlayInfo mCurrentPlayInfo;
	private File deletedFile;

	public PlayerService() {

	}

	public class PlayerServiceBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

	private final IBinder binder = new PlayerServiceBinder();

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			final PlayInfo oldPlayInfo = mCurrentPlayInfo;
			final PlayInfo newPlayInfo = (PlayInfo) intent.getExtras().getSerializable(BundleKey.playInfo.name());
			if (newPlayInfo.getPlayerComandStatus() == PlayStatus.forvard_stop) {
				if (mCurrentPlayInfo == null) {
					mCurrentPlayInfo = newPlayInfo;
				}
				stopPlayer(mCurrentPlayInfo, PlayStatus.stop);
				return Service.START_STICKY;
			}
			if (mCurrentPlayInfo != null && newPlayInfo.getMessageIdForPlay() != oldPlayInfo.getMessageIdForPlay()) {
				stopPlayer(oldPlayInfo);
			}
			mCurrentPlayInfo = newPlayInfo;
			
			switch (mCurrentPlayInfo.getPlayerComandStatus()) {
				case play:
					startPlayer(mCurrentPlayInfo);
				break;
				case stop:
					stopPlayer(mCurrentPlayInfo);
				break;				

				default:
				break;
			}
		}
		return Service.START_STICKY;
	}

	@Override
	public void onDestroy() {
		if (mTimer != null) {
			mTimer.cancel();
		}
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.release();
		}
		super.onDestroy();
	}

	public boolean isPlaying() {
		if (mPlayer != null) {
			return mPlayer.isPlaying();
		}
		return false;
	}

	private void startProgressTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		}
		isTimerStop = false;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!isTimerStop) {
					if (mPlayer != null && mPlayer.isPlaying()) {
						mCurrentPlayInfo.setProgress(mPlayer.getCurrentPosition());
					}
					mCurrentPlayInfo.setPlayerResponceStatus(PlayStatus.update);
					sendBroadcastPlayer(mCurrentPlayInfo);
				}
			}
		}, 0, TIMER_UPDATE_DELAY);

	}

	private void sendBroadcastPlayer(final PlayInfo playInfo) {
		Intent intent = new Intent(SmartPagerApplication.getInstance().getApplicationContext().getPackageName()
				+ ".PlayerAction");
		intent.putExtra(BundleKey.playInfo.name(), playInfo);
		sendBroadcast(intent);
	}

	public static IntentFilter getPlayResiverIntentFilter() {
		IntentFilter intentFilter = new IntentFilter(SmartPagerApplication.getInstance().getApplicationContext()
				.getPackageName()
				+ ".PlayerAction");
		return intentFilter;
	}

	private void startPlayer(final PlayInfo playInfo) {
		String fileName = playInfo.getUrl();
		if (mPlayer == null) {
			mPlayer = new MediaPlayer();
			mPlayer.setOnCompletionListener(mOnCompletionListenter);
			mPlayer.setOnPreparedListener(mOnPreparedListener);
		} else {
			mPlayer.stop();
			mPlayer.reset();
		}
		try {
			playInfo.setPlayerResponceStatus(PlayStatus.pending);
			sendBroadcastPlayer(playInfo);
			File file = new File(FileUtils.getCacheDir(), String.valueOf(fileName.hashCode()));
			if (file.exists()) {
				String encryptedFile = CipherUtils.decryptFile(PlayerService.this, file);
				mPlayer.setDataSource(encryptedFile);
				deletedFile = new File(encryptedFile);
			} else {				
				downloadRecordFile(fileName);
				mPlayer.setDataSource(fileName);
			}
			isTimerStop = false;
			mPlayer.prepareAsync();
			// mCurrentPlayInfo.setDuration(mPlayer.getDuration());
		} catch (IOException e) {
			onError(getString(R.string.couldn_t_play_file));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}	protected int mCurrentSpeakerMode;

	private void stopPlayer(final PlayInfo playInfo) {
		stopPlayer(playInfo, PlayStatus.stop);
	}

	private void stopPlayer(final PlayInfo playInfo, PlayStatus respnce) {
		//AudioManagerUtils.restorePlayMode();
		isTimerStop = true;
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		}
		
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.reset();
			playInfo.setPlayerResponceStatus(respnce);
			sendBroadcastPlayer(playInfo);
		}

	}	

	private void downloadRecordFile(String url){
		if (!TextUtils.isEmpty(url)) {
			Intent intent = new Intent(this, FileDownloadService.class);
			intent.setAction(DownloadFileAction.WavLoadAndConvertTo3gpEncode.name());
			intent.putExtra(BundleKey.downloadFile.name(), url);
			startService(intent);
		}
	}
	
	private void onError(String message) {
		//AudioManagerUtils.restorePlayMode();
		mCurrentPlayInfo.setPlayerResponceStatus(PlayStatus.error);
		mCurrentPlayInfo.setMessage(message);
		sendBroadcastPlayer(mCurrentPlayInfo);
	}

	private OnCompletionListener mOnCompletionListenter = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			stopPlayer(mCurrentPlayInfo);
			if (deletedFile != null) {
				deletedFile.delete();
			}
			// mCurrentPlayInfo.setPlayerResponceStatus(PlayStatus.stop);
			// sendBroadcastPlayer(mCurrentPlayInfo);
		}
	};


	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

		@Override
		public void onPrepared(MediaPlayer mp) {
			AudioManagerUtils.savePlayMode();
			AudioManagerUtils.setLoudSpeaker( SmartPagerApplication.getInstance().getPreferences().getLoudSpeaker() );
									
			mPlayer.start();
			mCurrentPlayInfo.setPlayerResponceStatus(PlayStatus.play);
			mCurrentPlayInfo.setDuration(mPlayer.getDuration());
			mCurrentPlayInfo.setProgress(mPlayer.getCurrentPosition());
			sendBroadcastPlayer(mCurrentPlayInfo);
			startProgressTimer();
		}
	};



}
