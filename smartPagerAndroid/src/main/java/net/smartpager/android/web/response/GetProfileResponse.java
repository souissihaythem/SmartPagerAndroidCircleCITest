package net.smartpager.android.web.response;

import android.text.TextUtils;
import android.util.Log;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.CallsSettings;
import net.smartpager.android.consts.SharedPreferencesFields;
import net.smartpager.android.consts.ResponseParts;
import net.smartpager.android.consts.SettingsPreferencesFields;
import net.smartpager.android.utils.DateTimeUtils;
import net.smartpager.android.utils.JsonParserUtil;
import net.smartpager.android.web.command.GetProfileCommand;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Response on {@linkplain GetProfileCommand}<br>
 * <b>Response JSON</b><br>
 * <tt>{ <br>
 * &nbsp; success: &lt;boolean&gt;, <br>
 * &nbsp; errorCode: &lt;integer&gt;, <br>
 * &nbsp; errorMessage: &lt;string&gt;, <br>
 * &nbsp; profile: { <br>
 * &nbsp; &nbsp; smartPagerID: &lt;string&gt;, <br>
 * &nbsp; &nbsp; status: &lt;string["DO_NOT_DISTURB", "ONLINE"]&gt;,<br>
 * &nbsp; &nbsp; accountName: &lt;string(aka organization name)&gt;, <br>
 * &nbsp; &nbsp; incomingCallMode: &lt;string&gt;, <br>
 * &nbsp; &nbsp; incomingCallModeFailoverToPageSeconds: &lt;string&gt;, <br>
 * &nbsp; &nbsp; ringTone: &lt;string&gt;, <br>
 * &nbsp; &nbsp; criticalRingTone: &lt;string&gt;,<br>
 * &nbsp; &nbsp; firstName: &lt;string&gt;, <br>
 * &nbsp; &nbsp; lastName: &lt;string&gt;, <br>
 * &nbsp; &nbsp; title: &lt;string&gt;, <br>
 * &nbsp; &nbsp; canSendCritical: &lt;boolean&gt;, <br>
 * &nbsp; &nbsp; autoResponseEnabled: &lt;boolean&gt;,<br>
 * &nbsp; &nbsp; forwardPagesEnabled: &lt;boolean&gt;, <br>
 * &nbsp; &nbsp; forwardPages: { <br>
 * &nbsp; &nbsp; &nbsp; type: &lt;<br>
 * &nbsp; &nbsp; &nbsp; PagingGroup|User&gt;, <br>
 * &nbsp; &nbsp; &nbsp; groupID: &lt;long&gt;, <br>
 * &nbsp; &nbsp; &nbsp; contactID: &lt;long&gt;, <br>
 * &nbsp; &nbsp; &nbsp; smartPagerID: &lt;String&gt; <br>
 * &nbsp; &nbsp; }<br>
 * &nbsp; &nbsp; autoResponse: &lt;string&gt;, <br>
 * &nbsp; &nbsp; skipOfflinePagingRecording: &lt;boolean&gt;,<br>
 * &nbsp; &nbsp; skipOnlinePagingRecording: &lt;boolean&gt;, <br>
 * &nbsp; &nbsp; offlineSmsPagingMessage: &lt;string&gt;, <br>
 * &nbsp; &nbsp; onlineSmsPagingMessage: &lt;string&gt;, <br>
 * &nbsp; &nbsp; offlinePagingRecording: &lt;string(URL)&gt;,<br>
 * &nbsp; &nbsp; onlinePagingRecording: &lt;string(URL)&gt;, <br>
 * &nbsp; &nbsp; onlinePagingRecordingTimestamp: &lt;string(date yyy-MM-dd)&gt;,<br>
 * &nbsp; &nbsp; offlinePagingRecordingTimestamp: &lt;string(date yyy-MM-dd)&gt;,<br>
 * &nbsp; &nbsp; pagerNumber: &lt;string(11-digits)&gt;, <br>
 * &nbsp; &nbsp; departments: [ &lt;string(deparment name)&gt;, ... ] <br>
 * &nbsp; }<br>
 * }</tt><br>
 * 
 * @author Roman
 */
public class GetProfileResponse extends AbstractResponse {

	private static final long serialVersionUID = -2232335043653706225L;

	public GetProfileResponse(String responceContent) {
		super(responceContent);
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
		jsonObject = jsonObject.optJSONObject(ResponseParts.profile.name());
		if (jsonObject != null) {
			
			//  greetings -------------------------------------------------------------
			SmartPagerApplication.getInstance().getPreferences().setSkipOfflinePagingRecording(jsonObject
                    .optBoolean(SharedPreferencesFields.skipOfflinePagingRecording.name()));
			SmartPagerApplication.getInstance().getPreferences().setSkipOnlinePagingRecording(jsonObject
                    .optBoolean(SharedPreferencesFields.skipOnlinePagingRecording.name()));
			String onRecording = jsonObject.optString(SharedPreferencesFields.onlinePagingRecording.name());			
			SmartPagerApplication.getInstance().getPreferences().setOnlinePagingRecording(onRecording);
			
			String offRecording = jsonObject.optString(SharedPreferencesFields.offlinePagingRecording.name());
			SmartPagerApplication.getInstance().getPreferences().setOfflinePagingRecording(offRecording);
			
			String onDate = jsonObject.optString(SharedPreferencesFields.onlinePagingRecordingTimestamp.name());			
			if (!TextUtils.isEmpty(onRecording)){
				if (TextUtils.isEmpty(onDate)){
					onDate = DateTimeUtils.today();
				} 
				SmartPagerApplication.getInstance().getPreferences().setOnlinePagingRecordingTimestamp("Recorded on " + DateTimeUtils.dateFromTimestamp(onDate));
			} else {
				SmartPagerApplication.getInstance().getPreferences().setOnlinePagingRecordingTimestamp("No custom greeting");
			}
			
			String offDate = jsonObject.optString(SharedPreferencesFields.offlinePagingRecordingTimestamp.name());
			if (!TextUtils.isEmpty(offRecording)){
				if (TextUtils.isEmpty(offDate)){
					offDate = DateTimeUtils.today();
				} 
				SmartPagerApplication.getInstance().getPreferences().setOfflinePagingRecordingTimestamp("Recorded on " + DateTimeUtils.dateFromTimestamp(offDate));
			} else {
				SmartPagerApplication.getInstance().getPreferences().setOfflinePagingRecordingTimestamp("No custom greeting");
			}
			//----------------------------------------------------------------------------
//			Prefferens.setDepartments(JsonParserUtil.getStFromJsonArray(array));
			SmartPagerApplication.getInstance().getPreferences().setDepartments(parseDepartments(jsonObject));
			SmartPagerApplication.getInstance().getPreferences().setAccountName(jsonObject.optString(SharedPreferencesFields.accountName.name()));
			SmartPagerApplication.getInstance().getPreferences().setIncomingCallMode(jsonObject.optString(SharedPreferencesFields.incomingCallMode.name()));
			SmartPagerApplication.getInstance().getPreferences().setIncomingCallModeFailoverToPageSeconds(jsonObject.optString(SharedPreferencesFields.incomingCallModeFailoverToPageSeconds.name()));
			SmartPagerApplication.getInstance().getPreferences().setPinEntryTimeout(jsonObject.optString(SharedPreferencesFields.pinEntryTimeout.name()));
			SmartPagerApplication.getInstance().getPreferences().setDeviceArchivePeriod(jsonObject.optString(SharedPreferencesFields.deviceArchivePeriod.name()));
			SmartPagerApplication.getInstance().getPreferences().setDevicePurgePeriod(jsonObject.optString(SharedPreferencesFields.devicePurgePeriod.name()));
			SmartPagerApplication.getInstance().getPreferences().setLastName(jsonObject.optString(SharedPreferencesFields.lastName.name()));
			SmartPagerApplication.getInstance().getPreferences().setTitle(jsonObject.optString(SharedPreferencesFields.title.name()));
			SmartPagerApplication.getInstance().getPreferences().setRingTone(jsonObject.optString(SharedPreferencesFields.ringTone.name()));
			SmartPagerApplication.getInstance().getPreferences().setCriticalRingTone(jsonObject.optString(SharedPreferencesFields.criticalRingTone.name()));
			SmartPagerApplication.getInstance().getPreferences().setCasualRingTone(jsonObject.optString(SharedPreferencesFields.casualRingTone.name()));
			SmartPagerApplication.getInstance().getPreferences().setStatus(jsonObject.optString(SharedPreferencesFields.status.name()));
			SmartPagerApplication.getInstance().getPreferences().setSmartPagerID(jsonObject.optString(SharedPreferencesFields.smartPagerID.name()));
			SmartPagerApplication.getInstance().getPreferences().setPagerNumber(jsonObject.optString(SharedPreferencesFields.pagerNumber.name()));
			SmartPagerApplication.getInstance().getPreferences().setFirstName(jsonObject.optString(SharedPreferencesFields.firstName.name()));
			SmartPagerApplication.getInstance().getPreferences().setCanSendCritical(jsonObject.optBoolean(SharedPreferencesFields.canSendCritical.name()));
			SmartPagerApplication.getInstance().getPreferences().setForwardPagesEnabled(jsonObject.optBoolean(SharedPreferencesFields.forwardPagesEnabled.name()));
			SmartPagerApplication.getInstance().getPreferences().setAutoResponse(jsonObject.optString(SharedPreferencesFields.autoResponse.name()));
			SmartPagerApplication.getInstance().getPreferences().setAutoResponseEnabled(jsonObject.optBoolean(SharedPreferencesFields.autoResponseEnabled.name()));
			SmartPagerApplication.getInstance().getPreferences().setVoipAllowed(jsonObject.optBoolean(SharedPreferencesFields.voipAllowed.name()));
            if(jsonObject.has(SharedPreferencesFields.callRouterPhoneNumber.name()))
                SmartPagerApplication.getInstance().getPreferences().setCallRouterPhoneNumber(jsonObject.optString(SharedPreferencesFields.callRouterPhoneNumber.name()));
            else
                SmartPagerApplication.getInstance().getPreferences().setCallRouterPhoneNumber("");

            if(jsonObject.has(SharedPreferencesFields.outgoingCallPhoneNumber.name()))
                SmartPagerApplication.getInstance().getPreferences().setOutgoingPhoneCallNumber(jsonObject.optString(SharedPreferencesFields.outgoingCallPhoneNumber.name()));
            else
                SmartPagerApplication.getInstance().getPreferences().setOutgoingPhoneCallNumber("");

			JSONObject forwardPages = jsonObject.optJSONObject(ResponseParts.forwardPages.name());
			if(forwardPages != null){
				SmartPagerApplication.getInstance().getPreferences().setForwardType(forwardPages.optString(SharedPreferencesFields.type.name()));
				SmartPagerApplication.getInstance().getPreferences().setForwardGroupID(forwardPages.optString(SharedPreferencesFields.groupID.name()));
				SmartPagerApplication.getInstance().getPreferences().setForwardContactID(forwardPages.optString(SharedPreferencesFields.contactID.name()));
				SmartPagerApplication.getInstance().getPreferences().setForwardSmartPagerID(forwardPages.optString(SharedPreferencesFields.smartPagerID.name()));
			}
			
			SmartPagerApplication.getInstance().getPreferences().setShowMobileNumbers(jsonObject.optBoolean(SharedPreferencesFields.showMobileNumbers.name()));
			SmartPagerApplication.getInstance().getPreferences().setAllowCallFromContacts(jsonObject.optBoolean(SharedPreferencesFields.allowCallFromContacts.name()));
			
			SmartPagerApplication.getInstance().getPreferences().setCallBlocking(jsonObject.optString(SharedPreferencesFields.callBlocking.name()));
//			Log.e("GetProfileResponse", "jsonObject.optString(SharedPreferencesFields.callBlocking.name()): " + jsonObject.optString(SharedPreferencesFields.callBlocking.name()));
//			Log.e("GetProfileResponse", "SmartPagerApplication.getInstance().getPreferences().getBlockMyNumber()" + SmartPagerApplication.getInstance().getSettingsPreferences().getBlockMyNumber());
			// Update value of existing setting to ensure server defined value is set on phone
			if (jsonObject.optString(SharedPreferencesFields.callBlocking.name()).equalsIgnoreCase("on")) {
				SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.on);
//				Log.e("GetProfileResponse", "Setting value: ON");
			} else if (jsonObject.optString(SharedPreferencesFields.callBlocking.name()).equalsIgnoreCase("off")) {
				SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.off);
//				Log.e("GetProfileResponse", "Setting value: OFF");
			} else if (jsonObject.optString(SharedPreferencesFields.callBlocking.name()).equalsIgnoreCase("ask")) {
				SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.ask);
//				Log.e("GetProfileResponse", "Setting value: ASk");
			}
//			Log.e("GetProfileResponse", "(AFTER) SmartPagerApplication.getInstance().getPreferences().getBlockMyNumber()" + SmartPagerApplication.getInstance().getSettingsPreferences().getBlockMyNumber());
			SmartPagerApplication.getInstance().getPreferences().setRestrictCallBlocking(jsonObject.optBoolean(SharedPreferencesFields.restrictCallBlocking.name(), false));
			
			
		}
	}

	private String parseDepartments(JSONObject jsonObject) {
		
		String depList = "";
		JSONArray array = jsonObject.optJSONArray(SharedPreferencesFields.departments.name());
		depList = JsonParserUtil.getStringFromJsonArray(array, ",\n");
		
		return depList;
	}

}
