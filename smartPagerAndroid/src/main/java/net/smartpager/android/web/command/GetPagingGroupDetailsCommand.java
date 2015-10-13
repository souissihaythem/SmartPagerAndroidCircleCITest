package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.GroupDetails;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetPagingGroupDetailsResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
/**
 * Get information regarding one specific paging group, including a list of users and 
 * in the case of schedule types, a subset of those users who are �current� on call
 *  (would receive the message if sent at the time the details were requested).<br>
*<b>Request JSON</b><br>
*<tt>[<br>
* &nbsp;    smartPagerID: &lt;string&gt;, <br>
* &nbsp;    uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
* &nbsp;    groupId: &lt;long&gt;<br>
*]</tt><br>
 * @author Roman
 * @see GetPagingGroupDetailsResponse
 */
public class GetPagingGroupDetailsCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 2772741577338762855L;
	
	private String mGroupId;
	private String mGroupName;
	
	
	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.groupId.name(), mGroupId);
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		GetPagingGroupDetailsResponse responce = 
				new GetPagingGroupDetailsResponse(string);
		responce.setGroupName(mGroupName);
		return responce;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.getPagingGroupDetails;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			mGroupId = bundle.getString(WebSendParam.groupId.name());
			mGroupName = bundle.getString(GroupDetails.name.name());
		}	
	}

}
