package net.smartpager.android.activity.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.rey.material.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.FileNames;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.model.PlayInfo;
import net.smartpager.android.model.PlayInfo.PlayStatus;
import net.smartpager.android.service.PlayerService;
import net.smartpager.android.sound.SoundRecorder;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.DateTimeUtils;
import net.smartpager.android.utils.FileUtils;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.web.response.AbstractResponse;

import java.io.File;
import java.io.IOException;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.library.lazybitmap.FileCache;

public class GreetingPlayerActivity extends BaseActivity {

	private boolean mPagerOn;
	@ViewInjectAnatation(viewID = R.id.settings_current_greeting_recorded_date_text_view)
	private TextView mRecordedDateTextView;
	@ViewInjectAnatation(viewID = R.id.settings_new_greeting_time_text_view)
	private TextView mProgressTextView;
	@ViewInjectAnatation(viewID = R.id.settings_new_greeting_save_button)
	private Button mNewGreetingSaveButton;
	@ViewInjectAnatation(viewID = R.id.settings_new_greeting_record_button)
	private ImageButton mNewGreetingRecordButton;
	@ViewInjectAnatation(viewID = R.id.settings_new_greeting_play_button)
	private ImageButton mNewGreetingPlayButton;
	@ViewInjectAnatation(viewID = R.id.settings_current_greeting_play_button)
	private ImageButton mCurrentGreetingPlayButton;
	@ViewInjectAnatation(viewID = R.id.settings_greeting_switch)
	private Switch mGreetingSwitch;
	@ViewInjectAnatation(viewID = R.id.settings_new_greeting_seekbar)
	private SeekBar mSeekBar;
	
	protected String mLocalRecordPath = "";
	protected String mRealRecordPath;
	
	private ImageButton mPlayButton;	
	private SoundRecorder mNewRecorder;

	@Override
	protected boolean isUpNavigationEnabled() {

		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_greeting);
		Injector.doInjection(this);
		initGui();
		setExternalRecordUrl(getActivityRecordFilePath());
	}
	
	protected void setExternalRecordUrl(String recordPath){
		mLocalRecordPath = FileCache.getLocalFile(recordPath).getAbsolutePath();
		mRealRecordPath = recordPath;
	}
	
	protected String getActivityRecordFilePath() {
		return new File(FileUtils.getCacheDir(), String.valueOf(this.hashCode()) + FileNames.AUDIO_EXT).getAbsolutePath();
	}

	private void initGui() {
		mPagerOn = getIntent().getBooleanExtra(BundleKey.isPagerOn.name(), false);
		// title
		setTitle((mPagerOn) ? "Pager On" : "Pager Off");
		// switch
		boolean switchChecked = (mPagerOn) ? !SmartPagerApplication.getInstance().getPreferences().getSkipOnlinePagingRecording() : !SmartPagerApplication.getInstance().getPreferences().getSkipOfflinePagingRecording();
		mGreetingSwitch.setChecked(switchChecked);
		mGreetingSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!NetworkUtils.isInternetConnectedAsync()){
					showErrorDialog(getString(R.string.connection_is_required));
					return;
				}
				if (mPagerOn) {
					SmartPagerApplication.getInstance().getPreferences().setSkipOnlinePagingRecording(!isChecked);
					Bundle extras = new Bundle();
					extras.putBoolean(WebSendParam.skipOnlinePagingRecording.name(), SmartPagerApplication.getInstance().getPreferences().getSkipOnlinePagingRecording());
					SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, extras);
				} else {
					SmartPagerApplication.getInstance().getPreferences().setSkipOfflinePagingRecording(!isChecked);
					Bundle extras = new Bundle();
					extras.putBoolean(WebSendParam.skipOfflinePagingRecording.name(), SmartPagerApplication.getInstance().getPreferences().getSkipOfflinePagingRecording());
					SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, extras);
				}
				
				showProgressDialog("Saving Custom Greeting settings...");
			}
		});
		// recorded date
		String recordedDate = (mPagerOn) ? SmartPagerApplication.getInstance().getPreferences().getOnlinePagingRecordingTimestamp() :
			SmartPagerApplication.getInstance().getPreferences().getOfflinePagingRecordingTimestamp();
		mRecordedDateTextView.setText(recordedDate);
		// sound core
		mSeekBar.setEnabled(false);
		mNewRecorder = new SoundRecorder(mProgressTextView);
		// play buttons
		mNewGreetingPlayButton.setVisibility(View.INVISIBLE);
		setVisibilityCurrentGreetingPlayButton();

		mNewGreetingSaveButton.setEnabled(false);
		mNewGreetingSaveButton.setAlpha(0.5f);
		
	}
	
	private void enableSaveButton() {
		mNewGreetingSaveButton.setEnabled(true);
		mNewGreetingSaveButton.setAlpha(1.0f);
	}

	private void setVisibilityCurrentGreetingPlayButton() {
		if (mRecordedDateTextView.getText().toString().contains( getString(R.string.settings_no_custom_greetings))){
			mCurrentGreetingPlayButton.setVisibility(View.INVISIBLE);
		} else {
			mCurrentGreetingPlayButton.setVisibility(View.VISIBLE);
		}
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

	
	@ClickListenerInjectAnatation(viewID = R.id.settings_new_greeting_record_button)
	public void startRecordNewGreeting(View v) {
		if (isPlaying()){
			return;
		}
		if (mNewRecorder.isRecording()) {
			mNewRecorder.stop();
			setImageRec( mNewGreetingRecordButton);
			mNewGreetingPlayButton.setVisibility(View.VISIBLE);
			enableSaveButton();
			setVisibilityCurrentGreetingPlayButton();
		} else {
			try {
				mNewRecorder.start(mLocalRecordPath); 
				setImageStop(mNewGreetingRecordButton);
				mNewGreetingPlayButton.setVisibility(View.INVISIBLE);
				setVisibilityCurrentGreetingPlayButton();
			} catch (IOException e) {
				mNewGreetingPlayButton.setVisibility(View.VISIBLE);
				setVisibilityCurrentGreetingPlayButton();
				showErrorDialog(e.getMessage());
			}
		}
	}

	@ClickListenerInjectAnatation(viewID = R.id.settings_new_greeting_play_button)
	public void startPlayNewGreeting(View v) {
		if (mNewRecorder.isRecording() || mPlayButton == mCurrentGreetingPlayButton) {
			return;
		}
		File file = new File( mLocalRecordPath );
		if (!file.exists()){
			showErrorDialog(getString(R.string.no_greeting_recorded));
			return;
		}
		
		mPlayButton = mNewGreetingPlayButton;
		switchPlayMode(mLocalRecordPath );
		
	}

	@ClickListenerInjectAnatation(viewID = R.id.settings_current_greeting_play_button)
	public void startPlayCurrentGreeting(View v) {
		if (mNewRecorder.isRecording() || mPlayButton == mNewGreetingPlayButton) {
			return;
		}
		
		mPlayButton = mCurrentGreetingPlayButton;
		String fileUrl = (mPagerOn) 
				? SmartPagerApplication.getInstance().getPreferences().getOnlinePagingRecording()
				: SmartPagerApplication.getInstance().getPreferences().getOfflinePagingRecording();
		switchPlayMode(fileUrl);
	}
	
	
	
	@ClickListenerInjectAnatation(viewID = R.id.settings_new_greeting_save_button)
	public void saveRecord(View v) {
		startSetProfile();

	}
	
	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.setProfile){
			hideProgressDialog();
			deleteTempFile();
			mNewGreetingPlayButton.setVisibility(View.INVISIBLE);
			mCurrentGreetingPlayButton.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		super.onErrorResponse(action, responce);
	}

	protected void startSetProfile() {
		if (!NetworkUtils.isInternetConnectedAsync()){
			showErrorDialog(getString(R.string.connection_is_required));
			return;
		}
		if (new File(mLocalRecordPath).exists()) {
            Bundle exstras = new Bundle();
			exstras.putString(WebSendParam.firstName.name(), SmartPagerApplication.getInstance().getPreferences().getFirstName());
			exstras.putString(WebSendParam.lastName.name(), SmartPagerApplication.getInstance().getPreferences().getLastName());
			exstras.putString(WebSendParam.title.name(), SmartPagerApplication.getInstance().getPreferences().getTitle());

			String today = getString(R.string.recorded_on_) + DateTimeUtils.today();
			mRecordedDateTextView.setText(today);
			if (mPagerOn) {
				exstras.putString(WebSendParam.onlinePagingRecording.name(), mLocalRecordPath);
				SmartPagerApplication.getInstance().getPreferences().setOnlinePagingRecordingTimestamp(today);
			} else {
				exstras.putString(WebSendParam.offlinePagingRecording.name(), mLocalRecordPath);
				SmartPagerApplication.getInstance().getPreferences().setOfflinePagingRecordingTimestamp(today);
			}
            SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, exstras);
			showProgressDialog("Saving Greeting...");
		} else {
			showErrorDialog(getString(R.string.no_greeting_recorded));
		}		
	}

	@Override
	public void finish() {		
		deleteTempFile();
		super.finish();
	}
	
	private void deleteTempFile(){
		File temp = new File(mLocalRecordPath);
		if (temp.exists()){
			temp.delete();
		}
	}
	
	private void setImageStop(ImageButton button){	
		if (button != null){
			button.setImageResource(R.drawable.settings_ic_stop);
		}
	}
	private void setImagePlay(ImageButton button){
		if (button != null){
			button.setImageResource(R.drawable.settings_ic_play);
		}
	}
	private void setImageRec(ImageButton button){
		if (button != null){
			button.setImageResource(R.drawable.settings_ic_rec);
		}
	}
	
	//------------ Service processing ------------------------
	
	protected String getLocalRecordPath(String recordPath){
		return FileCache.getLocalFile(recordPath).getAbsolutePath();		
	}
	
	protected String getActivityRekordFilePath() {
		return new File(FileUtils.getCacheDir(), String.valueOf(this.hashCode()) + FileNames.AUDIO_EXT).getAbsolutePath();
	}

	private PlayInfo mPlayInfo = PlayInfo.createPlayerInfor(PlayStatus.stop);
	
	private BroadcastReceiver mPlayerUpdate = new BroadcastReceiver() {

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
				//onPlayerStatusChenge(playInfo.getPlayerResponceStatus(), playInfo);
			}
		}

	};

	protected boolean isPlaying() {
		return mPlayInfo.getPlayerResponceStatus() != PlayStatus.stop;
	}

	@Override
	protected void onResume() {
		registerReceiver(mPlayerUpdate, PlayerService.getPlayResiverIntentFilter());
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (mNewRecorder != null) {
			mNewRecorder.stop();
		}
		unregisterReceiver(mPlayerUpdate);
		if (mPlayInfo.getMessageIdForPlay() == -1) {
			onStopPlaying(mPlayInfo);
		} else {
//			onPlayerStatusChenge(mPlayInfo.getPlayerResponceStatus(), mPlayInfo);
		}
		super.onPause();
	}

	protected void onPlayerStatusChenge(PlayStatus status, PlayInfo playInfo){
		
	};

	protected void onInitPlaying(PlayInfo playInfo) {
		mSeekBar.setMax((int) playInfo.getDuration());
		mSeekBar.setProgress((int) playInfo.getProgress());
	}

	protected void onStartPlaying(PlayInfo playInfo) {
		mSeekBar.setMax((int) playInfo.getDuration());
		mSeekBar.setProgress((int) playInfo.getProgress());
		setImageStop(mPlayButton);
	}

	protected void onUpdateProgress(PlayInfo playInfo) {
		mSeekBar.setProgress((int) playInfo.getProgress());
		mProgressTextView.setText(playInfo.getProgressString());
	}

	private void onStopPlaying(PlayInfo playInfo) {
		resetProgress();
		setImagePlay(mPlayButton);
		mPlayButton = null;
	}

	protected void switchPlayMode(String realRecordPath) {
		
		if (isPlaying()) {
			stopPlayFile(realRecordPath);
		} else {
			playFile(realRecordPath);
		}
	}

	protected void playFile(String realRecordPath) {
		mProgressTextView.setText("Loading");
		PlayInfo playInfo = null;
		if (!mPlayInfo.getUrl().equalsIgnoreCase(realRecordPath)) {
			playInfo = PlayInfo.createPlayerInfor(PlayStatus.play);
			playInfo.setUrl(realRecordPath);
		} else {
			playInfo = mPlayInfo;
			playInfo.setPlayerComandStatus(PlayStatus.play);
		}
		sendComandToPlayServer(playInfo);
	}

	protected void stopPlayFile(String realRecordPath) {
		PlayInfo playInfo = null;
		if (!mPlayInfo.getUrl().equalsIgnoreCase(realRecordPath)) {
			playInfo = PlayInfo.createPlayerInfor(PlayStatus.stop);
			playInfo.setUrl(realRecordPath);
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
		resetProgress();
	}

	protected void errorPlaying(PlayInfo playInfo) {
		showErrorDialog(playInfo.getMessage());
		playInfo.setPlayerResponceStatus(PlayStatus.stop);
		onCompletePlaying(playInfo);
		mPlayButton = null;
	}
	
	private void resetProgress(){
		mProgressTextView.setText("00:00");
		mSeekBar.setProgress(0);
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
	protected void onStart() {
		AudioManagerUtils.savePlayMode();
		SmartPagerApplication.getInstance().getPreferences().setLoudSpeaker(true);
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		AudioManagerUtils.restorePlayMode();
		super.onStop();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(BundleKey.localRecordPath.name(), mLocalRecordPath);
		outState.putString(BundleKey.realRecordPath.name(), mRealRecordPath);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mLocalRecordPath = savedInstanceState.getString(BundleKey.localRecordPath.name());
		mRealRecordPath = savedInstanceState.getString(BundleKey.realRecordPath.name());
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	
}