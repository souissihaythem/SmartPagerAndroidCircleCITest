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
 * The accept contact method terminates the contact adding handshake with success, through the act of the contact
 * accepting the requesting user (specified by mobile number). Below is the JSON schema used to transmit this data: <br>
 * <br>
 * <b>Request JSON </b><br>
 * <tt>
 * { <br>
 * &nbsp; contactMobileNumber: <11 digits><br>
 * }</tt><br>
 * <br>
 * 
 * <b>Response JSON</b><br>
 * <tt>{ success: &lt;boolean&gt;, errorCode: &lt;integer&gt;, errorMessage: &lt;string&gt; }</tt><br>
 * <br>
 * <tt><u>Primary Work Flow/Example</u><br>
 * <b>User A (mobileNumber: 12505147990)</b> wants to add <b>User B (mobileNumber: 12504776983)</b> as a contact. 
 * User B must be from a different account than User A.
 * User A sends an api request to add User B as a contact.<br>
 * <b>/rest/requestContact</b><br>
 * JSON Request: <br>
 * {<br>
 * &nbsp;  uberPassword: &lt;SHA-256&gt;,<br>
 * &nbsp;  smartPagerID: &lt;username&gt;,<br>
 * &nbsp;  contactMobileNumber: 12504776983<br>
 * }<br>
 * The server responds with the requested contacts name.<br>
 * <i>[ success: true, errorCode: 0, errorMessage: '', contact: [ firstName: "Michael", lastName: "Ferguson", title: "The Best" ] ]</i><br>
 * The server then sends a contact request push to User B, but is also visible in the getUpdates "contactRequests" list. This information includes the requesting users mobile number.
 * User B, having received the contact request, accepts the request by executing the api.<br>
 * <b>/rest/acceptContact</b><br>
 * JSON Request:<br>
 * {<br>
 * &nbsp;  contactMobileNumber: 12505147990<br>
 * }<br>
 * The server solidifies the relationship, which is now available in the getContacts, as well as appearing in the getUpdates "contacts.add" list.
 * The server also sends a push to the requesting user indicating that the contact has accepted the request. 
 * The server response is:
 * <i>[ success: true, errorCode: 0, errorMessage: '' ]</i></tt>
 * 
 * @author Roman
 * 
 */
public class AcceptContactCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -455399069060533416L;
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
		return WebAction.acceptContact;
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
