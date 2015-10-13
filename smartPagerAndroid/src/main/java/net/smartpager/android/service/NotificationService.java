package net.smartpager.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.activity.ChatActivity;
import net.smartpager.android.activity.MainActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.notification.NotificationHelper;
import net.smartpager.android.notification.Notificator;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import biz.mobidev.framework.utils.Log;

/**
 * Service for playing notifications on events such as Receiving new message, Sending Message etc
 * 
 * @author Roman
 */
public class NotificationService extends Service {

	public enum NotificationAction {
		disable, enable, cancelAll, soundcheckCriticalAlert, soundcheckNormalAlert, soundcheckCasualAlert, notifyInbox, notifySent, notifyInboxRepeat, notifyInboxChatSelfThread, notifyInboxChatAnotherThread,

	}

	private long timePlayAlert=0;
	private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			boolean critical = false;
			if (intent.getExtras() != null && intent.getExtras().containsKey(BundleKey.optUrgent.name())) {
				critical = intent.getExtras().getBoolean(BundleKey.optUrgent.name());
			}

			boolean isThreadMuted = false;
			if (intent.getExtras() != null && intent.getExtras().containsKey(BundleKey.threadId.name())) {

				for (String key : intent.getExtras().keySet()) {
					Object value = intent.getExtras().get(key);
					Log.d("NotificationService", String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
				}

				int threadID = intent.getExtras().getInt("threadID");
				isThreadMuted = SmartPagerApplication.getInstance().isThreadMuted(getApplicationContext(), threadID);
			}

			NotificationAction action = NotificationAction.valueOf((intent.getAction()));
			switch (action) {
				case disable:
					NotificationHelper.disable();
				break;
				case enable:
					NotificationHelper.enable();
				break;
				case cancelAll:
					NotificationHelper.cancelAll();
				break;
				case soundcheckCriticalAlert:
					NotificationHelper.soundcheckCriticalAlert();
				break;
				case soundcheckNormalAlert:
					NotificationHelper.soundcheckNormalAlert();
				break;
				case soundcheckCasualAlert:
					NotificationHelper.soundcheckCasualAlert();
				break;
				case notifyInbox:
					/**
					 * Notify new inbox message if app is in background - it shows StatusBar notification with playing
					 * appropriate alert sound; if ChatActivity is on top - it sends broadcast to ChatActivity to
					 * determine if new message has current threadID and then receives command from ChatActivity to play
					 * alert sound; otherwise plays alert according to message urgency level
					 */
					PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					boolean isScreenOn = pm.isScreenOn();

					if (isThreadMuted) {
						Intent newIntent = new Intent();
						newIntent.putExtras(intent.getExtras());
						newIntent.removeExtra("alertType");

						intent = newIntent;
					}

					if (SmartPagerApplication.isApplicationInBackground()) {
						mExecutorService.execute(new BackgroundNotificatorRunnable(intent));
					} else if (SmartPagerApplication.isActivityOnFront(ChatActivity.class) && isScreenOn) {
						Intent chatIntent = new Intent();
						chatIntent.putExtras(intent.getExtras());
						chatIntent.setAction(ChatActivity.CHAT_INBOX_ACTION);
						sendBroadcast(chatIntent);
					} else if(SmartPagerApplication.isActivityOnFront(MainActivity.class) && isScreenOn)
                    {
                        Intent chatIntent = new Intent();
                        chatIntent.putExtras(intent.getExtras());
                        chatIntent.setAction(MainActivity.MAIN_INBOX_ACTION);
                        sendBroadcast(chatIntent);
                    } else
                    {
						long diffInMis = Math.abs(timePlayAlert - System.currentTimeMillis());
						if (TimeUnit.MILLISECONDS.toSeconds(diffInMis) > 10) {
							timePlayAlert = System.currentTimeMillis();
							NotificationHelper.playAlert(critical);
						}
					}
				break;
				case notifyInboxChatSelfThread:
					if (!isThreadMuted) {
						NotificationHelper.playChatNewMessage();
					}
				break;
				case notifyInboxChatAnotherThread:
					if (!isThreadMuted) {
						NotificationHelper.playAlert(critical);
					}
				break;
				case notifyInboxRepeat:
					/**
					 * unused
					 */
					if (SmartPagerApplication.isActivityOnFront(ChatActivity.class)
							|| SmartPagerApplication.isActivityOnFront(MainActivity.class)) {
						Notificator.cancelAlarm();
						/*
						 * } else if (SmartPagerApplication.isApplicationInBackground()) {
						 * NotificationHelper.playAlert(critical);
						 */
					} else {
						if (!isThreadMuted) {
							NotificationHelper.playAlert(critical);
						}
					}
				break;
				case notifySent:
					if (SmartPagerApplication.isActivityOnFront(ChatActivity.class)) {
						NotificationHelper.playChatSentMessage();
					}
				break;

				default:
				break;
			}
		}
		return Service.START_STICKY;
	}

	// -----------------------------------------------------------------
	/**
	 * Runnable for retrieving information about message sender from database in the separate thread.<br>
	 * If application is in background, the new inbox notification is appeared in Status Bar with sender's name, so
	 * that's the main purpose of this runnable
	 * 
	 * @author Roman
	 */
	private class BackgroundNotificatorRunnable implements Runnable {
		private Intent intent;
		private boolean critical;
		private Integer threadId;
		private String messageId;
		private String alertType;

		public BackgroundNotificatorRunnable(Intent intent) {
			this.intent = intent;
			if (intent.getExtras() != null) {
				if (intent.getExtras().containsKey(BundleKey.optUrgent.name())) {
					critical = intent.getExtras().getBoolean(BundleKey.optUrgent.name());
				}
				if (intent.getExtras().containsKey(MessageTable.threadID.name())) {
					threadId = intent.getExtras().getInt(MessageTable.threadID.name());
				}
				if (intent.getExtras().containsKey(MessageTable.id.name())) {
					messageId = intent.getExtras().getString(MessageTable.id.name());
				}
				if (intent.getExtras().containsKey(BundleKey.alertType.name())) {
					alertType = intent.getExtras().getString(BundleKey.alertType.name());
				}
				
			}
		}

		@Override
		public void run() {
			String message = "";
			ArrayList<String> fromContactIdList = new ArrayList<String>();
			if (intent.getExtras() != null && intent.getExtras().containsKey(BundleKey.fromContactIdList.name())) {
				fromContactIdList = intent.getExtras().getStringArrayList(BundleKey.fromContactIdList.name());
				// DB query
				StringBuilder builder = new StringBuilder("( ");
				for (int i = 0; i < fromContactIdList.size(); i++) {
					if (i != 0) {
						builder.append(", ");
					}
					builder.append(fromContactIdList.get(i));
				}
				builder.append(") ");
				String selection = ContactTable.id.name() + " IN " + builder.toString();
				Uri uri = SmartPagerContentProvider.CONTENT_CONTACT_URI;
				Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
				// Read data from Cursor
				if (cursor.moveToFirst()) {
					boolean isFirst = true;
					builder = new StringBuilder();
					do {
						if (!isFirst) {
							builder.append(",");
						}
						builder.append(cursor.getString(ContactTable.firstName.ordinal()));
						builder.append(" ");
						builder.append(cursor.getString(ContactTable.lastName.ordinal()));
						isFirst = false;
					} while (cursor.moveToNext());
					message = "from " + builder.toString();
				}

			}
			
			String titlePrefix = critical ? "! " : "";
			String msgNotif = getResources().getString(R.string.notif_new_smartpager_msg);
			NotificationHelper.postNotification(message, msgNotif, titlePrefix + msgNotif, critical, threadId,
					messageId, alertType);
		}
	}

}
