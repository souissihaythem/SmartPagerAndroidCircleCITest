package net.smartpager.android.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.activity.ChatActivity;
import net.smartpager.android.database.MessagesQueryHelper;
import net.smartpager.android.notification.NotificationHelper;
import net.smartpager.android.utils.DateTimeUtils;

/**
 * Service for periodic alarm playing if there are unread messages
 * @author Roman
 */
public class UnreadAlarmService extends Service {

	/**
	 * Interval for invoking the checking procedure for urgent unread messages. 
	 */
	private static final long REPEAT_INTERVAL = 1 * DateTimeUtils.MS_IN_MIN;
	/**
	 * Factor to {@linkplain #REPEAT_INTERVAL}<br>
	 * Determinates the interval for invoking the checking procedure for non-urgent unread messages
	 */
	private static final int CHECK_UNREAD_NONURGENT_THRESHOLD = 5;
	/**
	 * Counter to check if the time has come for invoking the checking procedure for non-urgent unread messages 
	 */
	private static int mCheckCounter = 0;
	/**
	 * Handler for check unread messages executing in specified interval 
	 */
	private Handler mHandler; 		

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public UnreadAlarmService() {
		super();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		Log.CRASH();
//		if (mHandler == null){
//			mHandler = new Handler();
//			mHandler.postDelayed(checkUnreadedRunnable, REPEAT_INTERVAL);			
//		}
		return Service.START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		startService(new Intent(this, UnreadAlarmService.class));
		super.onDestroy();
	}
	
	/**
	 *  For urgent messages, replays the user selected urgent ringtone 
	 *  every {@linkplain #REPEAT_INTERVAL} minutes, if the app is minimized, to remind the user that they have messages waiting for them.<br>
	 *  
	 *	For non urgent messages, replays the notify.mp3 tone attached to this document, 
	 *  every ({@linkplain #CHECK_UNREAD_NONURGENT_THRESHOLD} * {@linkplain #REPEAT_INTERVAL}) minutes, 
	 *  to remind the user they have messages waiting for them.
	 */
	private Runnable checkUnreadedRunnable = new Runnable() {
		
		@Override
		public void run() {

			++mCheckCounter;
				
			boolean areCritical = checkCriticalUnread();
			boolean oneMinCheckAndCritical = areCritical && SmartPagerApplication.isApplicationInBackground(); 
			boolean fiveMinCheckAndCritical = areCritical && isFiveMinIntervalEnds() && !SmartPagerApplication.isActivityOnFront(ChatActivity.class); 
			boolean fiveMinCheckAndNonCritical = isFiveMinIntervalEnds() && !SmartPagerApplication.isActivityOnFront(ChatActivity.class); 			
			
			if ( oneMinCheckAndCritical  || fiveMinCheckAndCritical){				
				NotificationHelper.playAlert(true);				
			} else if (fiveMinCheckAndNonCritical ){
				if (checkNonCriticalUnread()){
					NotificationHelper.playAlert(false);		
				}								
			}			
			mCheckCounter %= CHECK_UNREAD_NONURGENT_THRESHOLD;
			mHandler.postDelayed(this, REPEAT_INTERVAL);
		};
		
		private boolean checkCriticalUnread(){
			Uri uri = SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI;
			String whereCritical = MessagesQueryHelper.selectUnreadCritical();				
			Cursor cursorCritical = getContentResolver().query(uri, null, whereCritical, null, null);
			boolean result = (cursorCritical != null && cursorCritical.getCount() > 0);
			if (cursorCritical != null){
				cursorCritical.close();
			}
			return result;
		}

		private boolean checkNonCriticalUnread(){
			Uri uri = SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI;
			String whereNonCritical = MessagesQueryHelper.selectUnread();
			Cursor cursorNonCritical = getContentResolver().query(uri, null, whereNonCritical, null, null);
//			Log.e("----------------CHECK UNREAD -------------------");
//			TestLogUtils.logMessageData(cursorNonCritical);
			boolean result = (cursorNonCritical != null && cursorNonCritical.getCount() > 0 );
			if (cursorNonCritical != null){
				cursorNonCritical.close();
			}
			return result;
		}
		
		private boolean isFiveMinIntervalEnds(){
			return mCheckCounter >= CHECK_UNREAD_NONURGENT_THRESHOLD;
		}
		
	};

}
