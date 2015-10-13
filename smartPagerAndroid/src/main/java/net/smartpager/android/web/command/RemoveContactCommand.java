package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
/**
 * The remove contact method initiates the contact removing procudure.
 * There isn't API guide for this command 
 * @author Roman
 */
public class RemoveContactCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -1915652630753382500L;
	private String mContactId;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.contactId.name(), mContactId);
		return new StringEntity(root.toString());
	}
	
	@Override
	public AbstractResponse getResponse(String string) {
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.removeContact;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			setContactId(bundle.getString(WebSendParam.contactId.name()));
		}
	}

	public String getContactId() {
		return mContactId;
	}

	public void setContactId(String contactId) {
		mContactId = contactId;
	}

}
