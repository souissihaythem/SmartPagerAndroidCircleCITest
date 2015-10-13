package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetProfilePictureResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Allows an authenticated user to fetch his own or one of his contacts profile pictures. <br> 
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;   smartPagerID: &lt;string&gt;, <br>
 * &nbsp;    uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
 * &nbsp;    contactSmartPagerID: &lt;long(optional, the smart pager id)&gt;<br>
 * ]</tt><br>
 * @author Roman
 * @see GetProfilePictureResponse
 */
public class GetProfilePictureCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 4993527436031524489L;
	private String mFileName;
//	private static final String IMAGE_JPEG = "image/jpeg";
	
	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String fileName) {
		mFileName = fileName;
	}

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new GetProfilePictureResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.getProfilePicture;
	}

	@Override
	public void setArguments(Bundle bundle) {
	}

}
