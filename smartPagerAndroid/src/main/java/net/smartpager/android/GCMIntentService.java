package net.smartpager.android;


import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	@Override
	protected void onError(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onRegistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

//	public GCMIntentService() {
//		super(BuildSettings.GCM_SENDER_ID);
//	}
//
//	@Override
//	protected void onRegistered(Context arg0, String deviceToken) {
//		Intent intent = new Intent(this, WebService.class);
//		intent.setAction(SmartPagerApplication.getActionName(WebAction.startPushSession.name()));
//		intent.putExtra(WebSendParam.alias.name(), String.valueOf(System.currentTimeMillis()));
//		intent.putExtra(WebSendParam.androidAPID.name(), deviceToken);
//		intent.putExtra(WebSendParam.version.name(), "1" );
//		startService(intent);
//	}
//
//	@Override
//	protected void onUnregistered(Context arg0, String arg1) {
//
//	}
//	
//	@Override
//	protected String[] getSenderIds(Context context) {
//		return super.getSenderIds(context);
//	}
//
//	@Override
//	protected void onError(Context arg0, String arg1) {
//
//	}
//
//	@Override
//	protected void onMessage(Context arg0, Intent intent) {
//		try {
//			String payloadAction = intent.getStringExtra("action");
//			switch (PushAction.valueOf(payloadAction)) {
//				case UNLOCK:
//					SmartPagerApplication.tryUnlockDevice();					
//				break;
//				case LOCK:
//					SmartPagerApplication.lockDevice();
//				break;
//				case SEND:
////					sendAction(intent);
////				break;
//				case request:
//				case accept:
//					startGetUpdates();
//				break;
//				case SENT:
//				case READ:
//				case DELIVERED:
//				case REPLIED:
//				case FAILED:
//				case DELAYED:
//					defaultAction(intent);
//				break;
//				default:
//				break;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			sendPushReceived();
//		}
//	}
//
//
//	private void startGetUpdates() {
//		Intent updatesIntent = new Intent(this, WebService.class);					
//		updatesIntent.setAction(SmartPagerApplication.getActionName(WebAction.getUpdates.name()));					
//		startService(updatesIntent);
//	}
//
//	private void defaultAction(Intent intent) {		
//		long id = Long.valueOf(intent.getStringExtra("id"));
//		ArrayList<Integer> ids = new ArrayList<Integer>();
//		ids.add(Long.valueOf(id).intValue());
//		
//		Intent sentIntent = new Intent(this, WebService.class);		
//		sentIntent.putIntegerArrayListExtra(WebSendParam.ids.name(), ids);
//		sentIntent.setAction(SmartPagerApplication.getActionName(WebAction.getSentMessages.name()));					
//		startService(sentIntent);
//	}
//
//	@SuppressWarnings("unused")
//	private void sendAction(Intent intent) {
//		int id = Integer.valueOf(intent.getStringExtra("id"));
//		ArrayList<Integer> ids = new ArrayList<Integer>();
//		ids.add(id);
//		
//		Intent inboxIntent = new Intent(this, WebService.class);
//		inboxIntent.setAction(SmartPagerApplication.getActionName(WebAction.getInbox.name()));				
//		inboxIntent.putIntegerArrayListExtra(WebSendParam.ids.name(), ids);
//		startService(inboxIntent);
//	}
//
//	@SuppressWarnings("unused")
//	private void notifyChangeState() {
//		
//	}
//
//	@SuppressWarnings("unused")
//	private void sendMessageNotification(long id, String title) {
//		
//	}
//
//	@SuppressWarnings("unused")
//	private void sendRequestAddNotification(Bundle bundle) {
//		
//	}
//
//	private void sendPushReceived() {
//		
//	}
//
//	@SuppressWarnings("unused")
//	private String getExtra(Intent intent, String extraName) {
//		return intent.hasExtra(extraName) ? intent.getStringExtra(extraName) : "";
//	}

}
