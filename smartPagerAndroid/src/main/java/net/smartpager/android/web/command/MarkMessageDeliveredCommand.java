package net.smartpager.android.web.command;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.RemoteException;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Mark a specific message(s) as delivered. Send to the mark message delivered service.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;	smartPagerID:	&lt;string&gt;<br>
 * &nbsp;	uberPassword:	&lt;string&gt;<br>
 * &nbsp;	messageId:	&lt;long(optional)&gt;,<br>
 * &nbsp;	messageIdList: [ &lt;long(messageId)&gt; ] &lt;(optional)&gt;<br>
 * ]</tt><br>
 * <b>Response JSON</b><br>
 * <tt>[<br>	
 * &nbsp;	success:	&lt;boolean&gt;<br>
 * &nbsp;	errorCode:	&lt;int&gt;<br>
 * &nbsp;	errorMessage:	&lt;string&gt;<br>	
 * ]</tt><br>
 * 
 * @author Roman
 */
public class MarkMessageDeliveredCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 1740193824451858593L;
	private String mMessageId;
	ArrayList<Integer> ids = new ArrayList<Integer>();

	public void addMessageId(String messageId) {
		ids.add(Integer.valueOf(messageId));
	}

	public String getMessageId() {
		return mMessageId;
	}

	public void setMessageId(String messageId) {
		this.mMessageId = messageId;
	}

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		if (ids != null && !ids.isEmpty()) {
			JSONArray array = new JSONArray();
			for (Integer id : ids) {
				array.put(id);
			}
			root.put("messageIdList", array);
		}
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		BaseResponse responce = new BaseResponse(string);
		// ContentValues values = new ContentValues();
		// if(responce.isSuccess()){
		// Long time = GregorianCalendar.getInstance().getTime().getTime();
		// values.put(MessageTable.deliveredTime.name(), time);
		// values.put(MessageTable.lastUpdate.name(), time);
		// }
		if (responce.isSuccess()) {
			Context context = SmartPagerApplication.getInstance().getApplicationContext();
			Long readTime = GregorianCalendar.getInstance().getTime().getTime();
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			for (Integer id : ids) {
				Builder builder = ContentProviderOperation.newUpdate(SmartPagerContentProvider.CONTENT_MESSAGE_URI);
				builder.withSelection(MessageTable.id.name() + " = ? ", new String[] { id.toString() });
				builder.withValue(MessageTable.deliveredTime.name(), readTime);
				builder.withValue(MessageTable.lastUpdate.name(), readTime);
				builder.withValue(MessageTable.status.name(), MessageStatus.Delivered.name());
				builder.withValue(MessageTable.messageStatus.name(), MessageStatus.Received.name());
				operations.add(builder.build());
			}

			for (Integer id : ids) {
				SQLiteDatabase spdb = context.openOrCreateDatabase("SmartPagerDB.db", 0, null);
				Cursor cursor = spdb.rawQuery("select * from Message where id = ?", new String[] { id.toString() });
				if (cursor != null) {
					cursor.moveToFirst();
					if (!cursor.isNull(MessageTable.readTime.ordinal()) && cursor.getInt(MessageTable.readTime.ordinal()) > 0) {
						Builder builder = ContentProviderOperation.newUpdate(SmartPagerContentProvider.CONTENT_MESSAGE_URI);
						builder.withSelection(MessageTable.id.name() + " = ? ", new String[] { id.toString() });
						builder.withValue(MessageTable.status.name(), MessageStatus.Read.name());
						builder.withValue(MessageTable.messageStatus.name(), MessageStatus.Read.name());

						operations.add(builder.build());
					}
				}
				spdb.close();
			}

			try {
				context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, operations);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_CHATS_URI, null);
			context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
		}
		// SmartPagerApplication.getInstance().getContentResolver().update(SmartPagerContentProvider.CONTENT_MESSAGE_URI,
		// values, MessageTable.id.name()+" = '"+mMessageId+"'", null);
		// SmartPagerApplication.getInstance().getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI,
		// null);
		// SmartPagerApplication.getInstance().getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_URI,
		// null);
		return responce;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.markMessageDelivered;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			mMessageId = bundle.getString(WebSendParam.messageId.name());
		}

	}

}
