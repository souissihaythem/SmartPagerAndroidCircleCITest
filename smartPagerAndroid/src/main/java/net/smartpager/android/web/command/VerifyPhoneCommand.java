package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;
import net.smartpager.android.web.response.VerifyPhoneResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * A device requests the device registration handshake. Only one registration request can ever be made for a mobile
 * number. The result of this api is a SMS to the requested phone number containing a confirmation code which expires
 * within an hour.<br>
 * Below is the JSON schema used to transmit this data:<br>
 * <b>Request JSON</b><br>
 * <tt>{<br>
 * &nbsp;  phoneNumber: &lt;11 digits&gt;<br>
 * }</tt><br>
 * <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;     success: &lt;true/false&gt;,<br>
 * &nbsp;     errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
 * &nbsp;     errorMessage: &lt;string-value&gt;,<br>
 * ]</tt><br>
 * @author Roman
 */
public class VerifyPhoneCommand extends AbstractCommand {

	private static final long serialVersionUID = -7857284929339869350L;
	private String mNumber;
    private String mDevicePlatform;
    private String mDeviceModel;
    private String mDeviceOSVersion;
    private String mDeviceProduct;
    private String mCarrierName;
	
	@Override
	public HttpEntity getEntity() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(WebSendParam.phoneNumber.name(), mNumber);
        obj.put(WebSendParam.devicePlatform.name(), mDevicePlatform);
        obj.put(WebSendParam.deviceModel.name(), mDeviceModel);
        obj.put(WebSendParam.deviceOSVersion.name(), mDeviceOSVersion);
        obj.put(WebSendParam.deviceProduct.name(), mDeviceProduct);
        obj.put(WebSendParam.carrierName.name(), mCarrierName);
		return new StringEntity(obj.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
        VerifyPhoneResponse response = new VerifyPhoneResponse(string);
		return response;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.deviceRegistrationStep1;
	}

	@Override
	public void setArguments(Bundle bundle) {
		mNumber =bundle.getString(WebSendParam.phoneNumber.name());
        mDevicePlatform =bundle.getString(WebSendParam.devicePlatform.name());
        mDeviceModel =bundle.getString(WebSendParam.deviceModel.name());
        mDeviceOSVersion =bundle.getString(WebSendParam.deviceOSVersion.name());
        mDeviceProduct =bundle.getString(WebSendParam.deviceProduct.name());
        mCarrierName =bundle.getString(WebSendParam.carrierName.name());
	}

}
