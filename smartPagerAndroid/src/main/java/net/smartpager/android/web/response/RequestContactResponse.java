package net.smartpager.android.web.response;

import net.smartpager.android.consts.ResponseParts;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.web.command.RequestContactCommand;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Response on {@linkplain RequestContactCommand}<br>
 * <b>Response JSON</b><br>
 * <tt>{ <br>
 * &nbsp; success: &lt;boolean&gt;, <br>
 * &nbsp; errorCode: &lt;integer&gt;, <br>
 * &nbsp; errorMessage: &lt;string&gt;, <br>
 * &nbsp; contact: <br>
 * &nbsp; &nbsp;   	{ <br>
 * &nbsp; &nbsp; &nbsp;   		firstName: &lt;string&gt;, <br>
 * &nbsp; &nbsp; &nbsp;   		lastName: &lt;string&gt;, <br>
 * &nbsp; &nbsp; &nbsp;   		title: &lt;string&gt; <br>
 * &nbsp; &nbsp;   	} <br>
 * &nbsp;   }</tt><br>
 * 
 * @author Roman
 * 
 */
public class RequestContactResponse extends BaseResponse {

	private static final long serialVersionUID = 8640409764171290562L;
	private String mFirstName;
	private String mLastName;
	private String mTitle;
	
	public RequestContactResponse(String responceContent) {
		super(responceContent);
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
		
		// If contact doesn't exist, return from method
		String errorMessage = null;
		try {
			errorMessage = jsonObject.getString("errorMessage");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (errorMessage.length() > 0) {
			return;
		}
		
		JSONObject jsonContact = jsonObject.optJSONObject(ResponseParts.contact.name());
		setFirstName(jsonContact.optString(ContactTable.firstName.name()));
		setLastName(jsonContact.optString(ContactTable.lastName.name()));
		setTitle(jsonContact.optString(ContactTable.title.name()));
	}

	public String getFirstName() {
		return mFirstName;
	}

	public void setFirstName(String firstName) {
		mFirstName = firstName;
	}

	public String getLastName() {
		return mLastName;
	}

	public void setLastName(String lastName) {
		mLastName = lastName;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

}
