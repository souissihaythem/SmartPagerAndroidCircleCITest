package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetProfileResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Returns the information about the authenticated user's profile<br>
*<b>Request JSON</b><br>
*<tt>{<br>
* &nbsp;    smartPagerID: &lt;string&gt;, <br>
* &nbsp;    uberPassword: &lt;string&gt;<br>
*}</tt><br>
 * @author Roman
 * @see GetProfileResponse
 */
public class GetProfileCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 441802780404207794L;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new GetProfileResponse(string);
	}

	@Override
	public WebAction getWebAction() {

		return WebAction.getProfile;
	}

	@Override
	public void setArguments(Bundle bundle) {
		
	}

}
