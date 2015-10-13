package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetInboxResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Request the contents of the inbox for the given user.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;     smartPagerID: &lt;username&gt;,<br>
 * &nbsp;    uberPassword: &lt;MD5 Hashed uberPassword - will be SHA256 soon&gt;,<br>
 * &nbsp;    ids: [ &lt;long&gt; ] &lt;array(optional,a subset of messages)&gt;<br>
 * ]</tt><br>
 * @author Roman
 * @see GetInboxResponse
 */
public class GetInboxCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 2114799256371818330L;
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
		GetInboxResponse response = new GetInboxResponse(string, this.getStatus());
		return response;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.getInbox;
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
