package net.smartpager.android.web.response;

import net.smartpager.android.consts.WebResponceParam;
import net.smartpager.android.web.command.VerifySMSCommand;

import org.json.JSONObject;

/**
 * Response on {@linkplain VerifySMSCommand}
 * <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;     success: &lt;true/false&gt;,<br>
 * &nbsp;     errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
 * &nbsp;     errorMessage: &lt;string-value&gt;,<br>
 * &nbsp;     uberPassword: &lt;string&gt;<br>
 * ]</tt><br>
 * @author Roman
 */
public class VerifySMSResponse extends AbstractResponse {

	private static final long serialVersionUID = 7257487055770704860L;
	private String mUberPassword;
	
	public VerifySMSResponse(String responceContent) {
		super(responceContent);
	}

	public String getUberPassword() {
		return mUberPassword;
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
		mUberPassword = jsonObject.optString(WebResponceParam.uberPassword.name());
	}

}
