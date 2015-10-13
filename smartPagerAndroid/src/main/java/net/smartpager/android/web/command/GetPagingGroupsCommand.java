package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetPagingGroupsResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Get a list of paging fromGroupId that the authenticated user has access to.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;    smartPagerID: &lt;string&gt;, <br>
 * &nbsp;    uberPassword: &lt;hashed-uberPassword-string&gt;<br>
 * ]</tt><br>
 * 
 * @author Roman
 * @see GetPagingGroupsResponse
 */
public class GetPagingGroupsCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -3206735718612724471L;

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
		GetPagingGroupsResponse response = new GetPagingGroupsResponse(string, this.getStatus());
		return response;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.getPagingGroups;
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

	public List<Integer> getIds() {
		return mIds;
	}
}
