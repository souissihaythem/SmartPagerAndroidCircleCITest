package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.SyncState;
import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

public class SyncronizeCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 4813259499257356721L;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.status.name(), ContactStatus.ONLINE.name());
		return new StringEntity(root.toString());
	}
	
	@Override
	public WebAction getResponseAction() {
		return WebAction.syncronize;
	}

	@Override
	public AbstractResponse getResponse(String string) {
		
		BaseResponse baseResponce = new BaseResponse(string);
		if(baseResponce.isSuccess()) {
            SmartPagerApplication.getInstance().getPreferences().setStatus(ContactStatus.ONLINE.name());
            SmartPagerApplication.getInstance().getPreferences().setSyncState(SyncState.Syncronized);
		}
		return baseResponce;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.setStatus;
	}

	@Override
	public void setArguments(Bundle bundle) {
		
		addPreCommand(new GetContactsCommand());
		addPreCommand(new GetPagingGroupsCommand());
		addPreCommand(new GetSentMessagesCommand());
        AbstractCommand getInboxCommand = new GetInboxCommand();
        getInboxCommand.setStatus(UpdatesStatus.none);
		addPreCommand(getInboxCommand);
	}

}
