package net.smartpager.android.web.response;

import net.smartpager.android.consts.GroupDetails;
import net.smartpager.android.web.command.GetPagingGroupDetailsCommand;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Response on {@linkplain GetPagingGroupDetailsCommand}<br>
 * <b>Response JSON</b><br>
 * <tt>{<br>
 * &nbsp;    success: &lt;boolean&gt;, <br>
 * &nbsp;     errorCode: &lt;long: 0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;, <br>
 * &nbsp;     errorMessage: &lt;string&gt;,<br>
 * &nbsp;     id: &lt;long&gt;, <br>
 * &nbsp;     type: &lt;string[�ABSOLUTESCHEDULE�, �SCHEDULE�, �ESCALATION� , �BROADCAST� , �RSS SCHEDULE�]&gt;, <br>
 * &nbsp;     current: [ { id: &lt;long&gt;, smartPagerID: &lt;string(username)&gt;, firstName: &lt;string&gt;, lastName: &lt;string&gt; }, ... ],<br> 
 * &nbsp;     users: [ { id: &lt;long&gt;, smartPagerID: &lt;string(username)&gt;, firstName: &lt;string&gt;, lastName: &lt;string&gt; }, ... ]<br>
 * }</tt><br>
 * 
 * @author Roman
 */
public class GetPagingGroupDetailsResponse extends AbstractResponse {

	private static final long serialVersionUID = 3450552102825467512L;
	private String mGroupId;
	private String mGroupName;
	private String mGroupType;
	private ArrayList< String[] > mUsers;
	
	private static String statusOnCall = "On Call -";
	private static String statusOffline = "";
	
	public GetPagingGroupDetailsResponse(String responceContent) {
		super(responceContent);		
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
		
		mUsers = new ArrayList< String[] >();
		JSONObject details = jsonObject.optJSONObject(GroupDetails.details.name());
		if (details != null){
			
			mGroupType =  details.optString(GroupDetails.type.name());
			mGroupId = details.optString(GroupDetails.id.name());
			
			HashMap<String, String> usersOncallMap = new HashMap<String, String>();
			
			JSONArray current = details.optJSONArray(GroupDetails.current.name());
			if (current != null){
				for (int i = 0; i < current.length(); i++){
					JSONObject oncallUser = current.optJSONObject(i);
					String name = oncallUser.optString(GroupDetails.firstName.name()) 
								+ " " 
								+ oncallUser.optString(GroupDetails.lastName.name()); 
					String id = oncallUser.optString(GroupDetails.id.name());
					
					mUsers.add( new String[]{ statusOnCall, name} );
					usersOncallMap.put(id, name);
				}
			}
			
			JSONArray users = details.optJSONArray(GroupDetails.users.name());
			if (users != null){
				for (int i = 0; i < users.length(); i++){
					JSONObject offlineUser = users.optJSONObject(i);
					String name = offlineUser.optString(GroupDetails.firstName.name()) 
								+ " " 
								+ offlineUser.optString(GroupDetails.lastName.name()); 
					String id = offlineUser.optString(GroupDetails.id.name());
					
					if (!usersOncallMap.containsKey(id)){
						mUsers.add( new String[]{ statusOffline, name });
					}
				}
			}
		}
	}

	public String getGroupType() {
		return mGroupType;
	}
	
	public void setGroupType(String groupType) {
		this.mGroupType = groupType;
	}

	public String getGroupName() {
		return mGroupName;
	}

	public void setGroupName(String groupName) {
		this.mGroupName = groupName;
	}
	
	public String getGroupId() {
		return mGroupId;
	}
	
	public ArrayList<String[]> getUsers(){ 
		return mUsers;
	}	
}
