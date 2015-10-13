package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONObject;

import java.io.File;

/**
 * Allows an authenticated user to set its profile picture via an HTTP POST. The image must be a png, gif or jpg between
 * 150-2000 pixels squared. <br>
 * <b>Request POST data </b><br>
 * <tt> [ <br>
 * &nbsp;    smartPagerID: &lt;string&gt;, <br> 
 * &nbsp;    uberPassword: &lt;hashed-uberPassword-string&gt;, <br>
 * &nbsp;    photo: &lt;image/[png,gif,jpg]&gt; <br>
 * ] </tt><br>
 * <b>Response JSON </b><br>
 * <tt>{ <br>
 * &nbsp;     success: &lt;boolean&gt;, <br> 
 * &nbsp;     errorCode: &lt;long: 0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;, <br> 
 * &nbsp;     errorMessage: &lt;string&gt; <br>
 * } </tt><br>
 * 
 * @author Roman
 */
public class SetProfilePictureCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -8841280103606708237L;
	private String mFileName;
	private static final String IMAGE_JPEG = "image/jpeg";
	
	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String fileName) {
		mFileName = fileName;
	}

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		FileBody fentity = new FileBody(new File(mFileName), IMAGE_JPEG);
		MultipartEntity entity = new MultipartEntity();
		entity.addPart(WebSendParam.photo.name(), fentity);
		return entity;
	}
	
	@Override
	public String getUrl() {
		String url = String.format(
                "%s/setProfilePicture?smartPagerID=%s&uberPassword=%s",
                Constants.BASE_REST_URL,
                SmartPagerApplication.getInstance().getPreferences().getUserID(),
                SmartPagerApplication.getInstance().getPreferences().getPassword());
		return url;
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.setProfilePicture;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null){
			mFileName = bundle.getString(WebSendParam.fileName.name());
		}
	}

}
