package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Lock this users out from api access.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;    smartPagerID: &lt;string&gt;,<br> 
 * &nbsp;     uberPassword: &lt;hashed-uberPassword-string&gt;<br>
 * ]</tt><br>
 * <b>Response JSON</b><br>
 * <tt>{<br>
 * &nbsp;    success: &lt;boolean&gt;,<br> 
 * &nbsp;    errorCode: &lt;long: 0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br> 
 * &nbsp;    errorMessage: &lt;string&gt;<br>
 * }</tt><br>
 * 
 * @author Roman
 * @see SmartPagerApplication#lockDevice()
 * @see SmartPagerApplication#tryUnlockDevice()
 */
public class LockCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 441802780404207794L;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		//Log.e("========= LOCKING ==========", root.toString());
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		//Log.e("========= LOCKED ==========", string);
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.lock;
	}

	@Override
	public void setArguments(Bundle bundle) {
	}

}
