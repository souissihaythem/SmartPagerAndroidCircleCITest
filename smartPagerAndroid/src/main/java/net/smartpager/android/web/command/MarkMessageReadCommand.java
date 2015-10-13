package net.smartpager.android.web.command;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.User;
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
 * Mark a specific message as read. Sent to the MarkMessageRead Service.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;	smartPagerID:	&lt;string&gt;<br>
 * &nbsp;	uberPassword:	&lt;string&gt;<br>
 * &nbsp;	messageId:	&lt;long(optional)&gt;,<br>
 * &nbsp;   messageIdList: [ &lt;long(messageId)&gt; ] &lt;(optional)&gt;<br>
 * ]</tt><br>
 * <b>Response JSON</b><br>
 * <tt>[	<br>
 * &nbsp;	success:	&lt;boolean&gt;<br>
 * &nbsp;	errorCode:	&lt;int&gt;<br>
 * &nbsp;	errorMessage:	&lt;string&gt;<br>	
 * ]</tt><br>
 * 
 * @author Roman
 */
public class MarkMessageReadCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 450397498975531598L;
	private String mMessageId;
	ArrayList<Integer> ids = new ArrayList<Integer>();

//	public String getMessageId() {
//		return mMessageId;
//	}
//
//	public void addMessageId(String messageId) {
//		ids.add(Integer.valueOf(messageId));
//	}
//
//	public void setMessageId(String messageId) {
//		this.mMessageId = messageId;
//	}

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		if (mMessageId != null) {
			root.put(WebSendParam.messageId.name(), mMessageId);
		}
		if (ids != null && !ids.isEmpty()) {
			JSONArray array = new JSONArray();
			for (Integer id : ids) {
				array.put(id);
			}
			root.put("messageIdList", array);
		}
		return new StringEntity(root.toString());
	}

	/**
	 * If server response is successful - update readTime in database for current message
	 */
	@Override
	public AbstractResponse getResponse(String string) {
		BaseResponse responce = new BaseResponse(string);
		if (responce.isSuccess()) {
			Context context= SmartPagerApplication.getInstance().getApplicationContext();
			User user = User.Open(context);
			user.mMarkAsRead.clear();
			User.Save(context, user);
		}
		return responce;
	}

	private void preExecute() {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		Long readTime = GregorianCalendar.getInstance().getTime().getTime();
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for (Integer id : ids) {
			Builder builder = ContentProviderOperation.newUpdate(SmartPagerContentProvider.CONTENT_MESSAGE_URI);
			builder.withSelection(MessageTable.id.name() + " = ? ", new String[] { id.toString() });
			builder.withValue(MessageTable.readTime.name(), readTime);
			builder.withValue(MessageTable.lastUpdate.name(), readTime);
			builder.withValue(MessageTable.status.name(), MessageStatus.Read.name());
			builder.withValue(MessageTable.messageStatus.name(), MessageStatus.Read.name());
			operations.add(builder.build());
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

	@Override
	public WebAction getWebAction() {
		return WebAction.markMessageRead;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			if (bundle.containsKey(WebSendParam.messageId.name())) {
				mMessageId = bundle.getString(WebSendParam.messageId.name());
				if (mMessageId != null) {
					ids.add(Integer.valueOf(mMessageId));
				}
			}
			if (bundle.containsKey(WebSendParam.messageIdList.name())) {
				ids.addAll(bundle.getIntegerArrayList(WebSendParam.messageIdList.name()));
			}
			Context context= SmartPagerApplication.getInstance().getApplicationContext();
			User user = User.Open(context);
			user.mMarkAsRead.addAll(ids);
			User.Save(context, user);
			preExecute();
		}

	}

}
