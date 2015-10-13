package net.smartpager.android.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.ChatActivity;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.utils.AudioManagerUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import biz.mobidev.framework.utils.Log;

/**
 * Helper class-wrapper for NotificationManager
 * 
 * @author Roman
 */
public class NotificationHelper {

	private static int TIME_OFFSET = 500; // to avoid early sound settings restoring

	private static Context mContext;
	private static NotificationManager mNotificationManager;
	// private static Vibrator mVibrator;
	private static String mPackageName;
	private static boolean mEnabled;
	private static int mOriginalVolume;
	private static Timer mEndTimer;

	static {
		mEnabled = true;
		mContext = SmartPagerApplication.getInstance();
		mPackageName = mContext.getPackageName();
		mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		// mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
	}

	public static void playSound(int id, int resSound, boolean vibrate) {
		Log.e("NotificationHelper", "playSound1");
        if (SmartPagerApplication.getInstance().getSettingsPreferences().getVolume() == 0) return;
		if (isEnabled()) {
			Uri uri = Uri.parse("android.resource://" + mPackageName + "/" + resSound);
			setVolumeAndVibration(resSound, vibrate);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext).setOnlyAlertOnce(true)
					.setSound(uri, AudioManagerUtils.getStream());
			if (vibrate) {
				builder.setDefaults(Notification.DEFAULT_VIBRATE);
			}
			mNotificationManager.notify(id, builder.build());

		}
	}

	public static void playSound(int resSound) {
		Log.e("NotificationHelper", "playSound2");
		int id = (int) System.currentTimeMillis();
		boolean vibrate = SmartPagerApplication.getInstance().getSettingsPreferences().getVibration();
		playSound(id, resSound, vibrate);
	}

	public static void playAlert(boolean critical) {
		Log.e("NotificationHelper", "playAlert");
		int res = critical ? AlertFilesUtils.getRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getCriticalAlert()) : AlertFilesUtils
				.getRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getNormalAlert());
		playSound(res);
	}
	
	public static void playAlertCasual() {
		Log.e("NotificationHelper", "playAlertCasual");
		int res = AlertFilesUtils.getCasualRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getCasualAlert());
		playSound(res);
	}

	/**
	 * 
	 */
	public static void playChatNewMessage() {
		Log.e("NotificationHelper", "playChatNewMessage");
		playSound(R.raw.inbound_chat_new_message);
	}

	/**
	 * 
	 */
	public static void playChatSentMessage() {
        int id = (int) System.currentTimeMillis();
		playSound(id, R.raw.sentmessages_chat, false);
	}

	/**
	 * Plays the choosen sound for urgent messages without vibration (for preview at settings screen)
	 */
	public static void soundcheckCriticalAlert() {
		int res = AlertFilesUtils.getRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getCriticalAlert());
		int id = (int) System.currentTimeMillis();
		playSound(id, res, false);
	}

	/**
	 * Plays the choosen sound for non-urgent messages without vibration (for preview at settings screen)
	 */
	public static void soundcheckNormalAlert() {
		int res = AlertFilesUtils.getRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getNormalAlert());
		int id = (int) System.currentTimeMillis();
		playSound(id, res, false);
	}
	
	/**
	 * Plays the choosen sound for casual messages without vibration (for preview at settings screen)
	 */
	public static void soundcheckCasualAlert() {
		int res = AlertFilesUtils.getCasualRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getCasualAlert());
		int id = (int) System.currentTimeMillis();
		playSound(id, res, false);
	}

	/**
	 * Posts notification with sound and message in StatusBar
	 * 
	 * @param message
	 * @param title
	 * @param ticker
	 * @param resSound
	 * @param threadId
	 *            for retrieving
	 * @param messageId
	 * @see #postNotification(String, String, String, boolean, int, String)
	 */
	public static void postNotification(String message, String title, String ticker, int resSound, int threadId,
			String messageId) {

		boolean isThreadMuted = SmartPagerApplication.getInstance().isThreadMuted(mContext, threadId);

		if (isEnabled()) {
			Uri uri = Uri.parse("android.resource://" + mPackageName + "/" + resSound);
			TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			Intent intent = new Intent(mContext, ChatActivity.class);
			intent.putExtra(MessageTable.threadID.name(), threadId);
			intent.putExtra(MessageTable.id.name(), messageId);
			int id = (int) System.currentTimeMillis(); // must be setted in PendingIntent and notify!
			PendingIntent pendingIntent = PendingIntent.getActivity(mContext, id, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext).setContentTitle(title)
					.setContentText(message).setTicker(ticker).setOnlyAlertOnce(true)
					// .setContentIntent(pendingIntent).setSound(uri,
					// AudioManagerUtils.getStream()).setAutoCancel(true)
					.setContentIntent(pendingIntent).setAutoCancel(true).setSmallIcon(R.drawable.ic_launcher);
			if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
				if (!isThreadMuted) {
					boolean vibrate = SmartPagerApplication.getInstance().getSettingsPreferences().getVibration();

					setVolumeAndVibration(resSound, vibrate);
					if (SmartPagerApplication.getInstance().getSettingsPreferences().getVolume() != 0) {
						builder.setSound(uri, AudioManagerUtils.getStream());
					}
					if (vibrate) {
						builder.setDefaults(Notification.DEFAULT_VIBRATE);
					}
				}
			}else{
				//builder.setSound(uri,AudioManager.STREAM_RING);
				//new playNotificationMediaFileAsyncTask().execute(uri.toString());  
				  try {
				       // Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				        Ringtone r = RingtoneManager.getRingtone(mContext, uri);
				        r.play();
				    } catch (Exception e) {
                  }
			}
			mNotificationManager.notify(id, builder.build());
		}
	}

	private static class playNotificationMediaFileAsyncTask extends AsyncTask<String, Void, Void> {

	    protected Void doInBackground(String... params) {
	        MediaPlayer mediaPlayer = null;
	        try{
	            mediaPlayer = new MediaPlayer();
	            mediaPlayer.setLooping(false);
	            mediaPlayer.setDataSource(mContext,  Uri.parse(params[0]));
	            mediaPlayer.prepare();
	            mediaPlayer.start();
	            mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
	                public void onCompletion(MediaPlayer mediaPlayer) {
	                    mediaPlayer.release();
	                    mediaPlayer = null;
	                }
	            }); 
	            return null;
	        }catch(Exception ex){
	            mediaPlayer.release();
	            mediaPlayer = null;
	            return null;
	        }
	    }

	    protected void onPostExecute(Void result) {
	        //Do Nothing
	    }

	}
	/**
	 * 
	 * @param message
	 * @param title
	 * @param ticker
	 * @param critical
	 * @param threadId
	 * @param messageId
	 */
	public static void postNotification(String message, String title, String ticker, boolean critical, int threadId,
			String messageId, String alertType) {

		int res = critical ? AlertFilesUtils.getRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getCriticalAlert()) : AlertFilesUtils
				.getRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getNormalAlert());
		
		if (alertType != null && alertType.equals("casual")) {
			res = AlertFilesUtils.getCasualRawRes().get(SmartPagerApplication.getInstance().getSettingsPreferences().getCasualAlert());
		}
		
		postNotification(message, title, ticker, res, threadId, messageId);
	}

	private static void setVolumeAndVibration(int resId, boolean isVibrate) {

		// if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE)
		{
			int duration = AlertFilesUtils.getDurationById(resId) + TIME_OFFSET;
			mOriginalVolume = AudioManagerUtils.getStreamVolume();
			AudioManagerUtils.setNotificationMode();
			mEndTimer = new Timer();
			mEndTimer.schedule(new TimerTask() {
				@Override
				public void run() {
					resetVolume();
				}
			}, duration);
		}
	}

	private static void resetVolume() {
		TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		// if (telephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE)
		{
			AudioManagerUtils.resetNotificationMode(mOriginalVolume);
		}
	}

	public static void cancelAll() {
		mNotificationManager.cancelAll();
	}

	public static boolean isEnabled() {
		return mEnabled;
	}

	public static void enable() {
		mEnabled = true;
	}

	public static void disable() {
		cancelAll();
		mEnabled = false;
	}

	/**
	 * This method doesn't work with sounds in res folder, but it will be helpful with assests
	 * 
	 * @param uri
	 * @return
	 */
	@SuppressWarnings("unused")
	private static int getMediaFileDuration(Uri uri) {
		MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
		metaRetriever.setDataSource(mContext, uri);
		String duration = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		metaRetriever.release();
		if (duration == null) {
			return 0;
		}
		return Integer.valueOf(duration);
	}
}
