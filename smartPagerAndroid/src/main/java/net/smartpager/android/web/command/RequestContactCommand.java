package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.RequestContactResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * By default all users automatically have all of the users in the same account in their contacts. 
 * These account contacts cannot be manipulated (can't be added or removed). 
 * If you want to add a contact from an account you don't belong to, you use the following api.<br>
 * The request contact method initiates the contact adding handshake, by informing the system and the contact that a
 * user wishes to add a the contact to its contacts.<br>
 * <b>Request JSON</b><br>
 * <tt>{<br>
 * &nbsp; uberPassword: &lt;SHA-256&gt;,<br>
 * &nbsp;  smartPagerID: &lt;username&gt;,<br>
 * &nbsp;  contactMobileNumber: &lt;11 digits&gt;<br>
 * }</tt><br>
 * 
 * @author Roman
 * @see RequestContactResponse
 */
public class RequestContactCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -326213722743603099L;
	private String mPhoneNumber;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.contactMobileNumber.name(), mPhoneNumber);
		return new StringEntity(root.toString());
	}
	
	@Override
	public AbstractResponse getResponse(String string) {
		return new RequestContactResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		
		return WebAction.requestContact;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			setPhoneNumber(bundle.getString(WebSendParam.contactMobileNumber.name()));
		}
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

}
