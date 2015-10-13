package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetContactsResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Request all of the contacts for the given user.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 *  &nbsp; smartPagerID: &lt;string&gt;,<br>
 *  &nbsp; uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
 *  &nbsp; ids: &lt;[ &lt;long>, ... ](optional subset of contact ids)&gt;<br>
 * ]</tt>
 * 
 * @author Roman
 * @see GetContactsResponse
 */
public class GetContactsCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -5149767284992893589L;

	private List<Integer> mIds;
    private boolean m_bIsNeedResetOldContacts = false;

	public List<Integer> getIds() {
		return mIds;
	}

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
		GetContactsResponse response = new GetContactsResponse(string, this.getStatus(), false);
        response.setResetOldContacts(m_bIsNeedResetOldContacts);
//        try {
//            response.processResponse(response.getJsonObject());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return response;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.getContacts;
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
            m_bIsNeedResetOldContacts = bundle.getBoolean(WebSendParam.resetContacts.name());
		}
	}
	
	public void setIds(List<Integer> ids){
		this.mIds = ids;
	}

    public void setResetOldContacts (boolean value)
    {
        m_bIsNeedResetOldContacts = value;
    }
}
