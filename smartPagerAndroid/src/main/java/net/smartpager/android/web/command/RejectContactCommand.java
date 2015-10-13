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
 * The reject contact method terminates a contact request by rejecting 
 * a users (specified by its mobile number) attempt to add the logged in user as a contact.
 * Below is the JSON schema used to transmit this data:<br>
*<b>Request JSON</b><br>
*<tt>{<br>
*&nbsp;  uberPassword: &lt;SHA-256&gt;,<br>
*&nbsp;  smartPagerID: &lt;username&gt;,<br>
*&nbsp;  contactMobileNumber: &lt;11 digits&gt;<br>
*}</tt><br>
*<b>Response JSON</b><br>
*<tt>{ <br>
*&nbsp; 	success: &lt;boolean&gt;,<br> 
*&nbsp; 	errorCode: &lt;integer&gt;, <br>
*&nbsp; 	errorMessage: &lt;string&gt; <br>
* }</tt><br>
 * @author Roman
 */
public class RejectContactCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -8055053753540841369L;
	private String mPhoneNumber;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.contactMobileNumber.name(), mPhoneNumber);
		return new StringEntity(root.toString());
	}
	
	@Override
	public AbstractResponse getResponse(String string) {
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.rejectContact;
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
