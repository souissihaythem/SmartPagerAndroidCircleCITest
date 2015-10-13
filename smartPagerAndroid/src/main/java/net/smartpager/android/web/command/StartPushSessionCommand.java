package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Checks the credentials given against the database.<br>
 * <b>Google Clound Messaging JSON</b><br>
 * <tt>[<br>
 * &nbsp;     smartPagerID: &lt;string&gt;,<br> 
 * &nbsp;     uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
 * &nbsp;     alias: &lt;string&gt;,<br>
 * &nbsp;     androidAPID: &lt;string(the gcm identifier)&gt;,<br>
 * &nbsp;     version: 1<br>
 * ]</tt><br>
 * <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;     success: &lt;boolean&gt;,<br> 
 * &nbsp;     errorCode: &lt;long: 0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br> 
 * &nbsp;     errorMessage: &lt;string&gt;,<br>
 * &nbsp;     pushSessionHashKey: &lt;string&gt;<br>
 * ]</tt><br>
 * 
 * @author Roman
 */
public class StartPushSessionCommand extends AbstractUserCommand {
	
	private static final long serialVersionUID = 691139894622513810L;
	private Bundle mArguments;
	
	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.alias.name(), mArguments.getString(WebSendParam.alias.name()));
		root.put(WebSendParam.androidAPID.name(), mArguments.getString(WebSendParam.androidAPID.name()));
		root.put(WebSendParam.version.name(), mArguments.getString(WebSendParam.version.name()));
		StringEntity stringEntity = new StringEntity(root.toString());
		return stringEntity;
	}

	
	@Override
	public AbstractResponse getResponse(String string) {
		BaseResponse response = new BaseResponse(string);
		try {
			String pushSessionHashKey = response.getJsonObject().optString("hashKey");
            SmartPagerApplication.getInstance().getPreferences().setPushSessionHashKey(pushSessionHashKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.startPushSession;
	}

	@Override
	public void setArguments(Bundle bundle) {
		mArguments = bundle;
	}

}
