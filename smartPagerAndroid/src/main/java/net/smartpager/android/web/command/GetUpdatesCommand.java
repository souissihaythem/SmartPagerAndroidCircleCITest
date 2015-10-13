package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetUpdatesResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Get any updates to contacts, or messages since the specified date.<br>
*<b>Request JSON</b><br>
*<tt>[<br>
* &nbsp;    smartPagerID: &lt;string&gt;<br>
* &nbsp;    uberPassword: &lt;string&gt;<br>
* &nbsp;    since: &lt;datetime, eg: 2012-09-20 18:16:54 +0000&gt;<br>
*]</tt><br>
 * @author Roman
 * @see GetUpdatesResponse
 */
public class GetUpdatesCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -2718629264403672093L;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.since.name(), SmartPagerApplication.getInstance().getPreferences().getLastUpdate());
        return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new GetUpdatesResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.getUpdates;
	}

	@Override
	public void setArguments(Bundle bundle) {

	}

}
