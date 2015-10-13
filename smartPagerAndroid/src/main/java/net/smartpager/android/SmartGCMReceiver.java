package net.smartpager.android;

import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteQuery;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import net.smartpager.android.activity.LockActivity;
import net.smartpager.android.consts.PushAction;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ChatsTable;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.database.MessageCursorTable;
import net.smartpager.android.service.UnreadAlarmService;

import java.util.ArrayList;
import java.util.GregorianCalendar;

import biz.mobidev.framework.utils.Log;

public class SmartGCMReceiver extends BroadcastReceiver {
    private static final String MSG_FIELD_ACTION = "action";
    private static final String MSG_FIELD_ID = "id";
    private static final String MSG_FIELD_RECIPIENT_ID = "msgRecipientID";

	public SmartGCMReceiver() {
		
	}

	String oldAction = "";
	long oldID = -1;

	@Override
	public void onReceive(Context context, Intent intent) {
		String payloadAction = intent.getStringExtra(MSG_FIELD_ACTION);
		try {
			switch (PushAction.valueOf(payloadAction)) {
				case UNLOCK:
					SmartPagerApplication.getInstance().tryUnlockDevice();
				break;
				case LOCK:
					SmartPagerApplication.getInstance().lockDevice();
				break;
				case SEND:
					sendAction(context, intent);
					break;
				case request:
					startGetUpdates(context);
					break;
				
                case SENT:
                    break;
				case FAILED:
				case READ:
				case DELIVERED:
				case DELAYED:
				case REPLIED:
				case ACCEPTED:
                    updateMessage(context, intent);
                    break;
				default:
					defaultAction(context, intent);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

    private void updateMessage(Context context, Intent intent){
        String msgID = intent.getStringExtra(MSG_FIELD_ID);
        String recipientID = intent.getStringExtra(MSG_FIELD_RECIPIENT_ID);
        String action = intent.getStringExtra(MSG_FIELD_ACTION);
        Long currTime = GregorianCalendar.getInstance().getTime().getTime();
        ArrayList<ContentProviderOperation.Builder> builders = new ArrayList<ContentProviderOperation.Builder>();
        ContentProviderOperation.Builder messageBuilder = ContentProviderOperation.newUpdate(SmartPagerContentProvider.CONTENT_MESSAGE_URI);
        messageBuilder.withSelection(DatabaseHelper.MessageTable.id.name() + " = ? ", new String[]{msgID});
        messageBuilder.withValue(DatabaseHelper.MessageTable.lastUpdate.name(), currTime);

        ContentProviderOperation.Builder recipientBuilder = ContentProviderOperation.newUpdate(SmartPagerContentProvider.CONTENT_RECIPIENTS_URI);
        recipientBuilder.withSelection(DatabaseHelper.RecipientTable.messageId.name() + " = ? AND " + DatabaseHelper.RecipientTable.contactId + " = ?", new String[]{msgID, recipientID});
        recipientBuilder.withValue(DatabaseHelper.RecipientTable.lastUpdate.name(), currTime);

        MessageStatus msgStatus = null;
        MessageStatus oldMsgStatus = null;
        
        String pushActionToMessageStatus = action.toLowerCase();
        pushActionToMessageStatus = pushActionToMessageStatus.substring(0, 1).toUpperCase() + pushActionToMessageStatus.substring(1);
        
        SQLiteDatabase spdb = context.openOrCreateDatabase("SmartPagerDB.db", 0, null);
        Cursor cursor = spdb.rawQuery("select * from Message where id = ?", new String[] {msgID});
        if (cursor != null) {
        	cursor.moveToFirst();
        	oldMsgStatus = MessageStatus.valueOf(cursor.getString(MessageTable.status.ordinal()));
        	
        	System.out.println("");
        }
        
        System.out.println("");
        
        if (PushAction.valueOf(action) == PushAction.DELAYED || PushAction.valueOf(action) == PushAction.FAILED) {
			msgStatus = ChatsTable.MessageStatus.Sent;
		} else if (PushAction.valueOf(action) == PushAction.DELIVERED && MessageStatus.valueOf(pushActionToMessageStatus).ordinal() >= oldMsgStatus.ordinal()) {
			msgStatus = ChatsTable.MessageStatus.Delivered;
			messageBuilder.withValue(DatabaseHelper.MessageTable.deliveredTime.name(), currTime);
			recipientBuilder.withValue(DatabaseHelper.RecipientTable.deliveredTime.name(), currTime);
		} else if (PushAction.valueOf(action) == PushAction.READ && MessageStatus.valueOf(pushActionToMessageStatus).ordinal() >= oldMsgStatus.ordinal()) {
			msgStatus = ChatsTable.MessageStatus.Read;
			messageBuilder.withValue(DatabaseHelper.MessageTable.readTime.name(), currTime);
			recipientBuilder.withValue(DatabaseHelper.RecipientTable.readTime.name(), currTime);
		} else if (PushAction.valueOf(action) == PushAction.REPLIED && MessageStatus.valueOf(pushActionToMessageStatus).ordinal() >= oldMsgStatus.ordinal()) {
			msgStatus = ChatsTable.MessageStatus.Replied;
			recipientBuilder.withValue(DatabaseHelper.RecipientTable.repliedTime.name(), currTime);
			messageBuilder.withValue(DatabaseHelper.MessageTable.repliedTime.name(), currTime);
		} else if (PushAction.valueOf(action) == PushAction.ACCEPTED && MessageStatus.valueOf(pushActionToMessageStatus).ordinal() >= oldMsgStatus.ordinal()) {
			msgStatus = ChatsTable.MessageStatus.Accepted;
		}
        
        
//        switch (PushAction.valueOf(action))
//        {
//        	  case DELAYED:
//            case FAILED:
//                msgStatus = ChatsTable.MessageStatus.Sent;
//                break;
//            case DELIVERED:
//                msgStatus = ChatsTable.MessageStatus.Delivered;
//                messageBuilder.withValue(DatabaseHelper.MessageTable.deliveredTime.name(), currTime);
//                recipientBuilder.withValue(DatabaseHelper.RecipientTable.deliveredTime.name(), currTime);
//                break;
//            case READ:
//                msgStatus = ChatsTable.MessageStatus.Read;
//                messageBuilder.withValue(DatabaseHelper.MessageTable.readTime.name(), currTime);
//                recipientBuilder.withValue(DatabaseHelper.RecipientTable.readTime.name(), currTime);
//                break;
//            case REPLIED:
//                msgStatus = ChatsTable.MessageStatus.Replied;
//                recipientBuilder.withValue(DatabaseHelper.RecipientTable.repliedTime.name(), currTime);
//                messageBuilder.withValue(DatabaseHelper.MessageTable.repliedTime.name(), currTime);
//                break;
//            case ACCEPTED:
//            	msgStatus = ChatsTable.MessageStatus.Accepted;
//            	break;
//            default:
//                break;
//        }
        
        
        
        if(msgStatus != null)
        {
            recipientBuilder.withValue(DatabaseHelper.RecipientTable.status.name(), msgStatus.name());
            messageBuilder.withValue(DatabaseHelper.MessageTable.status.name(), msgStatus.name());
        }
        builders.add(messageBuilder);
        builders.add(recipientBuilder);
        try {
            SPContentProviderHelper.applyBatches(context, builders, SmartPagerContentProvider.CONTENT_CHATS_URI, SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, SmartPagerContentProvider.CONTENT_RECIPIENTS_FULLINFO_URI);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean validateID (long id, String action, boolean rewriteID)
    {
        boolean res = (oldID != id || !oldAction.equalsIgnoreCase(action));
        if(res && rewriteID)
        {
            oldID = id;
            oldAction = action;
        }
        return res;
    }

	private void sendAction(Context context, Intent intent) {
		long id = Long.valueOf(intent.getStringExtra(MSG_FIELD_ID));
		String newAction = intent.getStringExtra(MSG_FIELD_ACTION);
		if (validateID(id, newAction, true)) {
			ArrayList<Integer> ids = new ArrayList<Integer>();
			ids.add(Long.valueOf(id).intValue());
			Log.e(newAction);
            Bundle extras = new Bundle();
            extras.putIntegerArrayList(WebSendParam.ids.name(), ids);
            SmartPagerApplication.getInstance().startWebAction(WebAction.getInbox, extras);
			context.startService(new Intent(context, UnreadAlarmService.class));
		}
	}

	private void startGetUpdates(Context context) {
        SmartPagerApplication.getInstance().startWebAction(WebAction.getUpdates, null);
	}

	private void defaultAction(Context context, Intent intent) {
		long id = Long.valueOf(intent.getStringExtra(MSG_FIELD_ID));
		String newAction = intent.getStringExtra(MSG_FIELD_ACTION);
		if (validateID(id, newAction, true)) {
			Log.e(newAction);
			ArrayList<Integer> ids = new ArrayList<Integer>();
			ids.add(Long.valueOf(id).intValue());
            Bundle extras = new Bundle();
            extras.putIntegerArrayList(WebSendParam.ids.name(), ids);
            SmartPagerApplication.getInstance().startWebAction(WebAction.getSentMessages, extras);
		}
	}
}
