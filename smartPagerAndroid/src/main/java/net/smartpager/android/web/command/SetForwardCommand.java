package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.ForwardType;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Request set to whom this user wants to forward messages to.  
 * Set the forwarding to either a user or a group.  
 * Also can enable or disable the act of forwarding by specifying an enable value.<br>
*<b>Request JSON</b><br>
*<tt>[<br>
*&nbsp;     smartPagerID: &lt;string&gt;<br>
*&nbsp;     uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
*&nbsp;     toUser: &lt;long&gt; (optional),<br>
*&nbsp;     toGroup: &lt;long&gt; (optional),<br>
*&nbsp;     enable: &lt;boolean&gt; (optional)<br>
*]</tt><br>
*<b>Response JSON</b><br>
*<tt>[<br>
*&nbsp;     success: &lt;true/false&gt;,<br>
*&nbsp;     errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
*&nbsp;     errorMessage: &lt;string-value&gt;<br>
*]</tt><br>
 * @author Roman
 */
public class SetForwardCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -8607776562001504079L;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		ForwardType type = ForwardType.valueOf( SmartPagerApplication.getInstance().getPreferences().getForwardType());
		if (type == ForwardType.User){
			root.put(WebSendParam.toUser.name(), SmartPagerApplication.getInstance().getPreferences().getForwardContactID());
		} else {
			root.put(WebSendParam.toGroup.name(), SmartPagerApplication.getInstance().getPreferences().getForwardGroupID());
		}
		root.put(WebSendParam.enable.name(), SmartPagerApplication.getInstance().getPreferences().getForwardPagesEnabled());
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.setForward;
	}

	@Override
	public void setArguments(Bundle bundle) {
		
	}

}
