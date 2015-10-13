package net.smartpager.android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;

import net.smartpager.android.activity.ChatActivity;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.MessagesQueryHelper;
import net.smartpager.android.notification.NotificationHelper;

public class AlarmService extends Service {
	/**
	 * Factor to {@linkplain #REPEAT_INTERVAL}<br>
	 * Determinates the interval for invoking the checking procedure for non-urgent unread messages
	 */
	private static final int CHECK_UNREAD_NONURGENT_THRESHOLD = 5;

	private Context mContext;
	
	public AlarmService() {}

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mContext = getApplicationContext();

		int checkCounter = SmartPagerApplication.getInstance().getPreferences().getCheckAlarmCounter()+1;
		boolean areCritical = checkCriticalUnread();
		boolean oneMinCheckAndCritical = areCritical && SmartPagerApplication.isApplicationInBackground(); 
		boolean fiveMinCheckAndCritical = areCritical && isFiveMinIntervalEnds(checkCounter) && !SmartPagerApplication.isActivityOnFront(ChatActivity.class); 
		boolean fiveMinCheckAndNonCritical = isFiveMinIntervalEnds(checkCounter) && !SmartPagerApplication.isActivityOnFront(ChatActivity.class); 			
		
		if ( oneMinCheckAndCritical  || fiveMinCheckAndCritical){				
			NotificationHelper.playAlert(true);				
		}

		if (fiveMinCheckAndNonCritical ){
			if (checkCasualUnread()){
				NotificationHelper.playAlertCasual();
			} else if (checkNonCriticalUnread()) {
				NotificationHelper.playAlert(false);
			}
		}
		checkCounter %= CHECK_UNREAD_NONURGENT_THRESHOLD;
		SmartPagerApplication.getInstance().getPreferences().setCheckAlarmCounter(checkCounter);
		return super.onStartCommand(intent, flags, startId);
	}
	private boolean checkCriticalUnread(){
		Uri uri = SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI;
		String whereCritical = MessagesQueryHelper.selectUnreadCritical();				
		Cursor cursorCritical = getContentResolver().query(uri, null, whereCritical, null, null);
		boolean newMessagesRequiringAlert = (cursorCritical != null && cursorCritical.getCount() > 0);

		if (isUnreadMessageMuted(cursorCritical)) {
			newMessagesRequiringAlert = false;
		}

		if (cursorCritical != null) {
			cursorCritical.close();
		}
		return newMessagesRequiringAlert;
	}

	private boolean checkNonCriticalUnread(){
		Uri uri = SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI;
		String whereNonCritical = MessagesQueryHelper.selectUnread();
		Cursor cursorNonCritical = getContentResolver().query(uri, null, whereNonCritical, null, null);
		boolean newMessagesRequiringAlert = (cursorNonCritical != null && cursorNonCritical.getCount() > 0 );

		if (isUnreadMessageMuted(cursorNonCritical)) {
			newMessagesRequiringAlert = false;
		}

		if (cursorNonCritical != null){
			cursorNonCritical.close();
		}
		return newMessagesRequiringAlert;
	}
	
	private boolean checkCasualUnread() {
		Uri uri = SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI;
		String whereCasual= MessagesQueryHelper.selectUnreadCasual();
		Cursor cursorCasualUnread = getContentResolver().query(uri, null, whereCasual, null, null);
		boolean newMessagesRequiringAlert = (cursorCasualUnread != null && cursorCasualUnread.getCount() > 0);

		if (isUnreadMessageMuted(cursorCasualUnread)) {
			newMessagesRequiringAlert = false;
		}

		if (cursorCasualUnread != null) {
			cursorCasualUnread.close();
		}
		return newMessagesRequiringAlert;
	}

	private boolean isUnreadMessageMuted(Cursor cursor) {
		boolean isMessageMuted = false;

		if (cursor.moveToFirst()) {
			// If this thread is muted, we want checkCasualUnread to return false;
			isMessageMuted = SmartPagerApplication.getInstance().isThreadMuted(mContext,
					Integer.parseInt(cursor.getString(DatabaseHelper.MessageTable.threadID.ordinal())));
		}
		
		return isMessageMuted;
	}
	
	private boolean isFiveMinIntervalEnds(int checkCounter){
		return checkCounter >= CHECK_UNREAD_NONURGENT_THRESHOLD;
	}
}
