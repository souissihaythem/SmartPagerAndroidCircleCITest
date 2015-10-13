package net.smartpager.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.rey.material.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.FileNames;
import net.smartpager.android.model.PlayInfo;
import net.smartpager.android.model.PlayInfo.PlayStatus;
import net.smartpager.android.service.PlayerService;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.FileUtils;

import java.io.File;

import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.library.lazybitmap.FileCache;

public abstract class BasePlayerActivity extends BaseActivity {

	
	@ViewInjectAnatation(viewID = R.id.player)
	protected View mPlayerView;
	@ViewInjectAnatation(viewID = R.id.player_action_button)
	protected ImageButton mPlayButton;
	@ViewInjectAnatation(viewID = R.id.player_seekbar)
	protected SeekBar mSeekBar;
	@ViewInjectAnatation(viewID = R.id.player_progress_text_view)
	protected TextView mProgressTextView;
	@ViewInjectAnatation(viewID = R.id.player_button_close)
	protected Button mCloseButton;
	protected String mLocalRecordPath = "";
	protected String mRealRecordPath;
	// private SoundPlayer mPlayer;

	@Override
	protected void onCreate(Bundle arg0) {

		super.onCreate(arg0);
		setExternalRecordUrl(getActivityRecordFilePath());
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		int menuId = (SmartPagerApplication.getInstance().getPreferences().getLoudSpeaker()) ?  R.menu.speaker_off : R.menu.speaker_on;
		getMenuInflater().inflate(menuId, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.speaker_off:
				lowSpeaker();
			break;
			case R.id.speaker_on:
				loudSpeaker();
			break;
			default:
				return false;
		}
		return true;
	}

	
	protected void setExternalRecordUrl(String recordPath){
		mLocalRecordPath = FileCache.getLocalFile(recordPath).getAbsolutePath();
		mRealRecordPath = recordPath;
	}
	
	protected String getActivityRecordFilePath() {
		return new File(FileUtils.getCacheDir(), String.valueOf(this.hashCode()) + FileNames.AUDIO_EXT).getAbsolutePath();
	}

	private PlayInfo mPlayInfo = PlayInfo.createPlayerInfor(PlayStatus.stop);
	
	private BroadcastReceiver mBasePlayerReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			PlayInfo playInfo = (PlayInfo) intent.getSerializableExtra(BundleKey.playInfo.name());

			if (playInfo.getMessageIdForPlay() == -1) {
				mPlayInfo = playInfo;
				switch (playInfo.getPlayerResponceStatus()) {
					case pending:
						onInitPlaying(playInfo);
					break;
					case play:
						onStartPlaying(playInfo);
					break;
					case update:
						onUpdateProgress(playInfo);
					break;
					case stop:
						onStopPlaying(playInfo);
					break;
					case error:
						errorPlaying(playInfo);
					break;
					
					default:
					break;
				}
			} else {
				if (playInfo.getPlayerResponceStatus() == PlayStatus.error) {
					playInfo.setPlayerResponceStatus(PlayStatus.stop);
					showErrorDialog(playInfo.getMessage());
				}
				onPlayerStatusChange(playInfo.getPlayerResponceStatus(), playInfo);
			}
		}

	};

	protected boolean isPlaying() {
		return mPlayInfo.getPlayerResponceStatus() != PlayStatus.stop;
	}

	@Override
	protected void onResume() {
			
		registerReceiver(mBasePlayerReceiver, PlayerService.getPlayResiverIntentFilter());
		
		//setExternalRecordUrl(getActivityRecordFilePath());	// refesh record path
		
		if (new File(mLocalRecordPath).exists()) {
			mPlayerView.setVisibility(View.VISIBLE);
		} else {
			mPlayerView.setVisibility(View.GONE);
		}
		
		mCloseButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
            public void onClick(View v) {
            	mPlayerView.setVisibility(View.GONE);
            	if (mLocalRecordPath.equals(getActivityRecordFilePath())) {
            		new File(mLocalRecordPath).delete();
            	}
            	setExternalRecordUrl(getActivityRecordFilePath());
            }
        });

		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mBasePlayerReceiver);
		if (mPlayInfo.getMessageIdForPlay() == -1) {
			onStopPlaying(mPlayInfo);
		} else {
			onPlayerStatusChange(mPlayInfo.getPlayerResponceStatus(), mPlayInfo);
		}
		super.onPause();
	}

	protected abstract void onPlayerStatusChange(PlayStatus status, PlayInfo playInfo);

	protected void onInitPlaying(PlayInfo playInfo) {
		mSeekBar.setMax((int) playInfo.getDuration());
		mSeekBar.setProgress((int) playInfo.getProgress());
	}

	protected void onStartPlaying(PlayInfo playInfo) {
		mSeekBar.setMax((int) playInfo.getDuration());
		mSeekBar.setProgress((int) playInfo.getProgress());
		mPlayButton.setImageResource(R.drawable.voice_dialog_ic_stop);
	}

	protected void onUpdateProgress(PlayInfo playInfo) {
		mSeekBar.setProgress((int) playInfo.getProgress());
		mProgressTextView.setText(playInfo.getProgressString());
	}

	private void onStopPlaying(PlayInfo playInfo) {
		mSeekBar.setProgress(0);
		mProgressTextView.setText(playInfo.getProgressString());
		mPlayButton.setImageResource(R.drawable.voice_dialog_ic_play);
	}

	protected void switchPlayMode() {
		if (isPlaying()) {
			stopPlayFile(mRealRecordPath);

			// mPlayButton.setImageResource(R.drawable.voice_dialog_ic_play);

		} else {
			playFile(mRealRecordPath);
			// mPlayButton.setImageResource(R.drawable.voice_dialog_ic_stop);
		}
	}

	protected void playFile(String url) {
		mProgressTextView.setText("Loading");
		PlayInfo playInfo = null;
		if (!mPlayInfo.getUrl().equalsIgnoreCase(mRealRecordPath)) {
			playInfo = PlayInfo.createPlayerInfor(PlayStatus.play);
			playInfo.setUrl(mRealRecordPath);
		} else {
			playInfo = mPlayInfo;
			playInfo.setPlayerComandStatus(PlayStatus.play);
		}
		sendComandToPlayServer(playInfo);
	}

	protected void stopPlayFile(String url) {
		PlayInfo playInfo = null;
		if (!mPlayInfo.getUrl().equalsIgnoreCase(mRealRecordPath)) {
			playInfo = PlayInfo.createPlayerInfor(PlayStatus.stop);
			playInfo.setUrl(mRealRecordPath);
		} else {
			playInfo = mPlayInfo;
			playInfo.setPlayerComandStatus(PlayStatus.stop);
		}
		sendComandToPlayServer(playInfo);
	}
		

	protected void stopForvardPlayFile() {		
		PlayInfo playInfo = PlayInfo.createPlayerInfor(PlayStatus.forvard_stop);
		playInfo.setPlayerComandStatus(PlayStatus.forvard_stop);
		sendComandToPlayServer(playInfo);
	}

	protected void loudSpeaker(){
		AudioManagerUtils.setLoudSpeaker(true);
		invalidateOptionsMenu();
	}

	protected void lowSpeaker(){
		AudioManagerUtils.setLoudSpeaker(false);
		invalidateOptionsMenu();
	}
	
	
	@Override
	public void onBackPressed() {
		stopForvardPlayFile();
		super.onBackPressed();
	}

	protected void sendComandToPlayServer(PlayInfo playInfo) {
		Intent intent = new Intent(this, PlayerService.class);
		intent.putExtra(BundleKey.playInfo.name(), playInfo);
		startService(intent);
	}

	
	protected void onCompletePlaying(PlayInfo playInfo) {
		
	}

	protected void errorPlaying(PlayInfo playInfo) {
		showErrorDialog(playInfo.getMessage());
		playInfo.setPlayerResponceStatus(PlayStatus.stop);
		onCompletePlaying(playInfo);
	}

	
	@Override
	protected void onStart() {
		AudioManagerUtils.savePlayMode();
		SmartPagerApplication.getInstance().getPreferences().setLoudSpeaker(true);
		invalidateOptionsMenu();
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		AudioManagerUtils.restorePlayMode();
		stopForvardPlayFile();
		super.onStop();
	}
}
