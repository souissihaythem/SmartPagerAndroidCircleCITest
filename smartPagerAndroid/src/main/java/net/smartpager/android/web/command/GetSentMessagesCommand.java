package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetSentMessagesResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Retrieve all sent messages for the given user.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;    smartPagerID: &lt;username&gt;,<br>
 * &nbsp;    uberPassword: &lt;MD5 Hashed uberPassword - will be SHA256 soon&gt;<br>
 * ]</tt><br>
 * @author Roman
 * @see GetSentMessagesResponse
 */
public class GetSentMessagesCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -5853966667932877648L;
	private List<Integer> mIds;
	
	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		if (mIds != null) {
			JSONArray array = new JSONArray();
			for (Integer id : mIds) {
				array.put(id);
			}
			root.put("ids", array);
		}
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		GetSentMessagesResponse response = new GetSentMessagesResponse(string, this.getStatus());
		return response;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.getSentMessages;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			mIds = bundle.getIntegerArrayList(WebSendParam.ids.name());
			if (bundle.containsKey(UpdatesStatus.status.name())){
				UpdatesStatus status = 
						UpdatesStatus.valueOf(bundle.getString(UpdatesStatus.status.name())); 
				setStatus( status );
			}
		}
	}
	
	public void setIds(List<Integer> ids){
		this.mIds = ids;
	}

}
