package net.smartpager.android.web.response;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.web.command.GetProfilePictureCommand;

import org.json.JSONObject;

/**
 * Response on {@linkplain GetProfilePictureCommand}<br>
 * If the contactSmartPagerID is not provided in the JSON request, then the authenticated 
 * user�s profile picture is returned via HTTP code 200, content-type: image/png.  
 * In the event that the user doesn�t have permission to view the profile picture, or 
 * if the requested user doesn�t have a profile picture an HTTP 404 code will be returned.<br>
 * @author Roman
 */
public class GetProfilePictureResponse extends AbstractResponse {

	private static final long serialVersionUID = 205612126148707654L;

	public GetProfilePictureResponse(String responceContent) {
		super(responceContent);
        SmartPagerApplication.getInstance().getPreferences().setProfilePicture(responceContent);
	}

	@Override
	public void processResponse(JSONObject jsonObject) {

	}


}
