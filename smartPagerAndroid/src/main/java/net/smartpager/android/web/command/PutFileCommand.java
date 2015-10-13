package net.smartpager.android.web.command;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FileNames;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.utils.CipherUtils;
import net.smartpager.android.utils.FfmpegUtils;
import net.smartpager.android.utils.PhotoFileUtils;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import biz.mobidev.framework.utils.Log;
import biz.mobidev.library.lazybitmap.FileCache;
import biz.mobidev.library.lazybitmap.ImageLoader;

/**
 * Put file onto the server. If a savePublic value of true is specified, the file will be publically available via the
 * url returned from this call. This should not be used for any personal information.<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;     smartPagerID: &lt;string&gt;,<br> 
 * &nbsp;     uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
 * &nbsp;     fileName: &lt;string&gt;,<br>
 * &nbsp;     mimeType: &lt;string&gt;,<br>
 * &nbsp;     savePublic: &lt;Boolean(optional), default false&gt;<br>
 * &nbsp;     file: &lt;string base64 encoded binary file&gt;<br>
 * ]</tt><br>
 * <b>Request HTTP PUT</b><br>
 * <tt>[<br>
 * &nbsp;     smartPagerID: &lt;string&gt;,<br> 
 * &nbsp;     uberPassword: &lt;hashed-uberPassword-string&gt;,<br>
 * &nbsp;     fileName: &lt;string&gt;,<br>
 * &nbsp;     mimeType: &lt;string&gt;,<br>
 * &nbsp;     binaryFile: &lt;posted file&gt;<br>
 * ]</tt><br>
 * <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;     success: &lt;true/false&gt;,<br>
 * &nbsp;     errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
 * &nbsp;     errorMessage: &lt;string-value&gt;,<br>
 * &nbsp;     url: &lt;string&gt;<br>
 * ]</tt><br>
 * 
 * @author Roman
 */
public class PutFileCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -6942688116970480475L;
	public static final String AUDIO_MPEG = "audio/mpeg";
	public static final String IMAGE_JPEG = "image/jpeg";

	private String mMimeType = IMAGE_JPEG;
	private String mFileName;
	private boolean mSavePublic = false;
	private File mDeletedFile;

	public boolean isSavePublic() {
		return mSavePublic;
	}

	public void setSavePublic(boolean savePublic) {
		mSavePublic = savePublic;
	}

	public String getFileName() {
		return mFileName;
	}

	public void setFileName(String fileName) {
		mFileName = FileCache.getLocalFile(fileName).getAbsolutePath();
	}

	public String getMimeType() {
		return mMimeType;
	}

	public void setMimeType(String mimeType) {
		mMimeType = mimeType;
	}

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		FileBody fentity = null;
		MultipartEntity entity = new MultipartEntity();
		if (mMimeType.equals(IMAGE_JPEG)) {
			fentity = new FileBody(getImageFile(), IMAGE_JPEG);
		}
		if (mMimeType.equals(AUDIO_MPEG)) {
			fentity = new FileBody(getAudioFile(), AUDIO_MPEG);
		}

		entity.addPart("binaryFile", fentity);
		entity.addPart(WebSendParam.mimeType.name(), new StringBody(mMimeType));
		return entity;
	}

	@Override
	public String getUrl() {
		String fileName = Uri.parse(this.mFileName).getLastPathSegment();
		// String url =
		// String.format("%s/putFile?smartPagerID=%s&uberPassword=%s&fileName=%s&mimeType=%s&savePublic=%s",
		// Constans.BASE_REST_URL, Prefferens.getUserID(), Prefferens.getPassword(), fileName, mMimeType, mSavePublic);
		// return url;
		StringBuilder builder = new StringBuilder();
		builder.append(Constants.BASE_REST_URL).append("/");
		builder.append(WebAction.putFile.name()).append("?");
		builder.append(WebSendParam.smartPagerID.name()).append("=").append(SmartPagerApplication.getInstance().getPreferences().getUserID()).append("&");
		builder.append(WebSendParam.uberPassword.name()).append("=").append(SmartPagerApplication.getInstance().getPreferences().getPassword()).append("&");
		builder.append(WebSendParam.fileName.name()).append("=").append(fileName).append("&");
		builder.append(WebSendParam.mimeType.name()).append("=").append(mMimeType).append("&");
		builder.append(WebSendParam.savePublic.name()).append("=").append(mSavePublic);
		return builder.toString();
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.putFile;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null) {
			mFileName = FileCache.getLocalFile(bundle.getString(WebSendParam.fileName.name())).getAbsolutePath();
		}
	}

	private File getImageFile() throws Exception {

		File imageFile;
		Uri originalUri = Uri.parse(this.mFileName);
		String originalPath = originalUri.getPath();
		boolean isEncrypted = originalPath.contains(FileNames.CACHE_DIR.getPath());
		// check if file encrypted or not
		if (isEncrypted && ImageLoader.IMAGE_CODING_ENABLED) {
			File originalImageFile = new File(originalPath);
			String decodedPath = CipherUtils.decryptFile(SmartPagerApplication.getInstance(), originalImageFile);
			imageFile = new File(decodedPath);
		} else {
			imageFile = new File(originalPath);
		}

		return imageFile;
	}

	private File getAudioFile() throws Exception {
		//Log.e("ENCODE AUDIO FILE", mFileName);
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		File file = new File(this.mFileName);
		// check if file encrypted or not
		if (file.exists() && mFileName.indexOf(FileNames.AUDIO_EXT) == -1) {
			this.mFileName = CipherUtils.decryptFile(context, file);
			mDeletedFile = new File(this.mFileName);
			//	Log.e("ENCODE AUDIO FILE - is cyphered");
		}
		this.mFileName = FfmpegUtils.convertToWav(this.mFileName);
		mDeletedFile = new File(this.mFileName);
		Uri originalPath = Uri.parse(this.mFileName);
		File soundFile = new File(originalPath.getPath());

		return soundFile;
	}

}
