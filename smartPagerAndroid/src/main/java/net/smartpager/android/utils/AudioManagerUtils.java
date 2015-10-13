package net.smartpager.android.utils;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import net.smartpager.android.SmartPagerApplication;

public class AudioManagerUtils {

	public static final int STREAM = AudioManager.STREAM_NOTIFICATION;
	private static AudioManager mAudioManager;
	private static int mOldAudioModePlay;
	private static int mOldRingerModePlay;
	private static boolean mIsOldSpeakerOnPlay;
	private static int mOldAudioModeNotif;
	private static int mOldRingerModeNotif;
	private static boolean mIsOldSpeakerOnNotif;
	static TelephonyManager mTelephonyManager;

	private static AudioManager getAudioManager() {
		if (mAudioManager == null) {
			Context context = SmartPagerApplication.getInstance();
			mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		}
		return mAudioManager;
	}

	public static void setLoudSpeaker(boolean loud) {
		// if (SmartPagerApplication.isGalaxyS()) {
		getAudioManager().setMode((loud) ? AudioManager.MODE_NORMAL : AudioManager.MODE_IN_CALL);
		// }
		getAudioManager().setSpeakerphoneOn(loud);
        SmartPagerApplication.getInstance().getPreferences().setLoudSpeaker(loud);
	}
	
	public static void setMicrophoneMute(boolean mute) {
		getAudioManager().setMicrophoneMute(mute);
	}

	public static int getStreamMaxVolume() {
		return getAudioManager().getStreamMaxVolume(STREAM);
	}

	public static int getStreamVolume() {
		return getAudioManager().getStreamVolume(STREAM);
	}

	public static void setStreamVolume(int volume) {
		getAudioManager().setStreamVolume(STREAM, volume, 0);
	}

	public static int getStream() {
		return STREAM;
	}

	public static void setNotificationMode() {
		// requestAudioFocus();
		saveNotificationMode();
		getAudioManager().setStreamVolume(STREAM, SmartPagerApplication.getInstance().getSettingsPreferences().getVolume(), 0);
		getAudioManager().setMode(AudioManager.MODE_NORMAL);
		getAudioManager().setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
			getAudioManager().setSpeakerphoneOn(true);
		}
	}

	public static void resetNotificationMode(int originalVolume) {
		restoreNotificationMode();
		Log.d("AudioManagerUtils", "originalVolume: " + originalVolume);
		getAudioManager().setStreamVolume(STREAM, originalVolume, 0);
	}

	private static void restoreNotificationMode() {
		Log.d("AudioManagerUtils", "mOldAudioModeNotif: " + mOldAudioModeNotif);
		Log.d("AudioManagerUtils", "mOldRingerModeNotif: " + mOldRingerModeNotif);
		
		
		
		getAudioManager().setMode(mOldAudioModeNotif);
		getAudioManager().setRingerMode(mOldRingerModeNotif);
		if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
			getAudioManager().setSpeakerphoneOn(mIsOldSpeakerOnNotif);
		}
		
		// Attempt to fix [SPANDROID-259] - Alert tones intermittently causing device audio to be muted.
		// https://smartpager.atlassian.net/browse/SPANDROID-259
		//
		// Reported by a very small number of users, app will play a notification and then mute all audio on the device.
		
		getAudioManager().setStreamMute(AudioManager.STREAM_ALARM, false);
		getAudioManager().setStreamMute(AudioManager.STREAM_DTMF, false);
		getAudioManager().setStreamMute(AudioManager.STREAM_MUSIC, false);
		getAudioManager().setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
		getAudioManager().setStreamMute(AudioManager.STREAM_RING, false);
		getAudioManager().setStreamMute(AudioManager.STREAM_SYSTEM, false);
		getAudioManager().setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
	}

	private static void saveNotificationMode() {
		mOldAudioModeNotif = getAudioManager().getMode();
		mOldRingerModeNotif = getAudioManager().getRingerMode();
		if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
			mIsOldSpeakerOnNotif = getAudioManager().isSpeakerphoneOn();
		}
	}

	public static void restorePlayMode() {
		getAudioManager().setMode(mOldAudioModePlay);
		getAudioManager().setRingerMode(mOldRingerModePlay);
		if (mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
			getAudioManager().setSpeakerphoneOn(mIsOldSpeakerOnPlay);
		}
	}

	public static void savePlayMode() {
		mOldAudioModePlay = getAudioManager().getMode();
		mOldRingerModePlay = getAudioManager().getRingerMode();
		mIsOldSpeakerOnPlay = getAudioManager().isSpeakerphoneOn();
	}

	public static void stopExternalMusic(Context context) {
		if (getAudioManager().isMusicActive()) {
			Intent i = new Intent("com.android.music.musicservicecommand");
			i.putExtra("command", "pause");
			context.sendBroadcast(i);
		}
	}

	// -------------------------------------------------------------------
	// EXPERIMENTAL METHODS
	public static void requestAudioFocus() {
		getAudioManager().requestAudioFocus(audiofocusChangeListener, STREAM, AudioManager.AUDIOFOCUS_GAIN);
	}

	public static void abandonAudioFocus() {
		getAudioManager().abandonAudioFocus(audiofocusChangeListener);
	}

	public static void setStreamSolo() {
		getAudioManager().setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
	}

	public static void muteAllExceptVoice(boolean mute) {
		getAudioManager().setStreamMute(AudioManager.STREAM_ALARM, mute);
		getAudioManager().setStreamMute(AudioManager.STREAM_DTMF, mute);
		getAudioManager().setStreamMute(AudioManager.STREAM_MUSIC, mute);
		getAudioManager().setStreamMute(AudioManager.STREAM_NOTIFICATION, mute);
		getAudioManager().setStreamMute(AudioManager.STREAM_RING, mute);
		getAudioManager().setStreamMute(AudioManager.STREAM_SYSTEM, mute);
	}

	// -------------------------------------------------
	private static OnAudioFocusChangeListener audiofocusChangeListener = new OnAudioFocusChangeListener() {
		public void onAudioFocusChange(int focusChange) {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				getAudioManager().abandonAudioFocus(this);
			}

		}
	};

}
