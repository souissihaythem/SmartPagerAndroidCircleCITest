package net.smartpager.android.web.command;

import android.content.ContentValues;
import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.util.GregorianCalendar;

/**
 * Created by dmitriy on 1/23/14.
 */
public class MarkMessageCalledCommand extends AbstractUserCommand {
    private static final long serialVersionUID = -749537546839041L;
    private String mMessageId;

    @Override
    public HttpEntity getSubEntity(JSONObject root) throws Exception {
        root.put(WebSendParam.messageId.name(), mMessageId);
        return new StringEntity(root.toString());
    }

    @Override
    public WebAction getWebAction() {
        return WebAction.markMessageCalled;
    }

    @Override
    public void setArguments(Bundle bundle) {
        if (bundle != null) {
            mMessageId = bundle.getString(WebSendParam.messageId.name());
        }
    }

    @Override
    public AbstractResponse getResponse(String string) {
        BaseResponse response = new BaseResponse(string);
        ContentValues values = new ContentValues();
        if(response.isSuccess()){
            Long readTime = GregorianCalendar.getInstance().getTime().getTime();
            values.put(DatabaseHelper.MessageTable.repliedTime.name(), readTime);
            values.put(DatabaseHelper.MessageTable.lastUpdate.name(), readTime);
        }
        SmartPagerApplication.getInstance().getContentResolver().update(SmartPagerContentProvider.CONTENT_MESSAGE_URI, values, DatabaseHelper.MessageTable.id.name()+" = '" + mMessageId + "'", null);
        SmartPagerApplication.getInstance().getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
        SmartPagerApplication.getInstance().getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_URI, null);
        return response;
    }
}
