package net.smartpager.android.web.command;

import android.content.ContentValues;
import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.util.GregorianCalendar;

/**
 * Mark a specified message(s) as replied to. Sent to the MarkMessageAsReplied service. <br>
 * <b>Request JSON </b><br>
 * <tt>[<br>
 * &nbsp;	smartPagerID:	&lt;string&gt;<br>
 * &nbsp;	uberPassword:	&lt;string&gt;<br>
 * &nbsp;	messageId:	&lt;long(optional)&gt;,<br>
 * &nbsp;    messageIdList: [ &lt;long(messageId)&gt; ] &lt;(optional)&gt;<br>
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
public class MarkMessageRepliedCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -794953725046839041L;
	private String mMessageId;
	
	public String getMessageId() {
		return mMessageId;
	}

	public void setMessageId(String messageId) {
		this.mMessageId = messageId;
	}
	
	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.messageId.name(), mMessageId);
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		BaseResponse responce = new BaseResponse(string);
		ContentValues values = new ContentValues();
		if(responce.isSuccess()){
			Long readTime = GregorianCalendar.getInstance().getTime().getTime();
			values.put(MessageTable.repliedTime.name(), readTime);
			values.put(MessageTable.lastUpdate.name(), readTime);
		}
		SmartPagerApplication.getInstance().getContentResolver().update(SmartPagerContentProvider.CONTENT_MESSAGE_URI, values, MessageTable.id.name()+" = '"+mMessageId+"'", null);
		SmartPagerApplication.getInstance().getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
		SmartPagerApplication.getInstance().getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_URI, null);
		return responce;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.markMessageReplied;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			mMessageId = bundle.getString(WebSendParam.messageId.name());			
		}

	}

}
