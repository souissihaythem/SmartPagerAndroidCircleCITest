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
 * After arming a phone number to an authenticated user, calling that users pager number will result in the call being
 * routed through our system to the armed phone number. The Caller Id presented to the phone receiving the call is based
 * on the users outgoing Caller-Id mode.<br>
 * <b>Request JSON</b><br>
 * <tt>{<br>
 *  &nbsp;  smartPagerID: &lt;string&gt;>,<br>
 *  &nbsp;   uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
 *   &nbsp; phoneNumber: &lt;string(11 digits)><br>
 * }</tt><br>
 * <b>Response JSON</b><br>
 * <tt>{<br>
 *  &nbsp;  success: &lt;boolean&gt;,<br>
 *  &nbsp;   errorCode: &lt;long: 0=OK, 401=access denied, 404=service not found, 400=invalid or absent phone number, 500=internal server error&gt;,<br>
 *  &nbsp;   errorMessage: &lt;string&gt;<br>
 * }</tt><br>
 * 
 * @author Roman
 */
public class ArmCallbackCommand extends AbstractUserCommand {

	
	private static final long serialVersionUID = 441802780404207794L;
	private String mPhoneNumber;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {		
		root.put(WebSendParam.phoneNumber.name(), mPhoneNumber);
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.armCallback;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null && bundle.containsKey(WebSendParam.phoneNumber.name())){
			mPhoneNumber = bundle.getString(WebSendParam.phoneNumber.name());
		}
	}

	public String getPhoneNumber() {
		return mPhoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		mPhoneNumber = phoneNumber;
	}

}
