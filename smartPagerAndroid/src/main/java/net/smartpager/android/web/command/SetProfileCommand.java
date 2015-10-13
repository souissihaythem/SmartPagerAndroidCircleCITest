package net.smartpager.android.web.command;

import android.os.Bundle;
import android.text.TextUtils;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *  Allows an authenticated user to set any subset of profile properties. 
 *  Values which are not inserted into the JSON will not be set, only those defined 
 *  variables with values, will be saved into the profile. This means that 
 *  if you only want to set the firstName, you can omit all the other properties 
 *  without the threat of them being set to a null value.<br>
*<b>Request JSON</b><br>
*<tt>{<br>
*&nbsp;     smartPagerID: &lt;string&gt;,<br> 
*&nbsp;     uberPassword: &lt;string&gt;,<br>
*&nbsp;     firstName: &lt;string(optional)&gt;,<br>  
*&nbsp;     lastName: &lt;string(optional)&gt;,  <br>
*&nbsp;     title: &lt;string(optional)&gt;, <br>
*&nbsp;     forwardPagesEnabled: &lt;Boolean(optional)&gt;,<br> 
*&nbsp;     autoResponseEnabled: &lt;Boolean(optional)&gt;, <br>
*&nbsp;     autoResponse: &lt;String(optional)&gt;, <br>
*&nbsp;     skipOfflinePagingRecording: &lt;boolean(optional)&gt;,<br>  
*&nbsp;     skipOnlinePagingRecording: &lt;boolean(optional)&gt;,<br>  
*&nbsp;     offlineSmsPagingMessage: &lt;string(optional)&gt;,  <br>
*&nbsp;     onlineSmsPagingMessage: &lt;string(optional)&gt;, <br> 
*&nbsp;     offlinePagingRecording: &lt;string(URL, optional)&gt;, &nbsp; 
*&nbsp;     <i>//the url for this recording should be publically available.  If using putFile, ensure that the savePublic property is set to true for recordings which will be used in this field.</i><br> 
*&nbsp;     onlinePagingRecording: &lt;string(URL, optional)&gt;, &nbsp; 
*&nbsp;     <i>//the url for this recording should be publically available.  If using putFile, ensure that the savePublic property is set to true for recordings which will be used in this field. </i><br>
*&nbsp;     onlinePagingRecordingTimestamp: &lt;string(date yyy-MM-dd, optional)&gt;,<br>
*&nbsp;     offlinePagingRecordingTimestamp: &lt;string(date yyy-MM-dd, optional)&gt;<br>
*&nbsp;     incomingCallMode: &lt;string&gt;,<br>
*&nbsp;     incomingCallModeFailoverToPageSeconds: &lt;string&gt;,<br>
*}</tt><br>
*<b>Response JSON</b><br>
*<tt>{ <br>
*&nbsp;    success: &lt;boolean&gt;,<br> 
*&nbsp;    errorCode: &lt;integer&gt;, <br>
*&nbsp;    errorMessage: &lt;string&gt;<br>
*}</tt><br>
 * @author Roman
 */
public class SetProfileCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 5878865097767915650L;

	private Bundle mArguments;
	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.lastName.name(), mArguments.get(WebSendParam.lastName.name()));
		root.put(WebSendParam.firstName.name(), mArguments.get(WebSendParam.firstName.name()));
		root.put(WebSendParam.title.name(), mArguments.get(WebSendParam.title.name()));
		root.put(WebSendParam.skipOfflinePagingRecording.name(), SmartPagerApplication.getInstance().getPreferences().getSkipOfflinePagingRecording());
		root.put(WebSendParam.skipOnlinePagingRecording.name(), SmartPagerApplication.getInstance().getPreferences().getSkipOnlinePagingRecording());
		root.put(WebSendParam.incomingCallMode.name(), SmartPagerApplication.getInstance().getPreferences().getIncomingCallMode());
		root.put(WebSendParam.incomingCallModeFailoverToPageSeconds.name(), SmartPagerApplication.getInstance().getPreferences().getIncomingCallModeFailoverToPageSeconds());		
		root.put(WebSendParam.pinEntryTimeout.name(), SmartPagerApplication.getInstance().getPreferences().getPinEntryTimeout());
		root.put(WebSendParam.deviceArchivePeriod.name(), SmartPagerApplication.getInstance().getPreferences().getDeviceArchivePeriod());
		root.put(WebSendParam.devicePurgePeriod.name(), SmartPagerApplication.getInstance().getPreferences().getDevicePurgePeriod());
		root.put(WebSendParam.autoResponseEnabled.name(), SmartPagerApplication.getInstance().getPreferences().getAutoResponseEnabled());
		root.put(WebSendParam.autoResponse.name(), SmartPagerApplication.getInstance().getPreferences().getAutoResponse());
		root.put(WebSendParam.callBlocking.name(), SmartPagerApplication.getInstance().getPreferences().getCallBlocking());
				
		ArrayList<String> records = parsePutFileResults();					
		
		if(records != null && records.size() > 0){
			String greetingUrl = records.get(0);	// can set only one greeting a time
			if (mArguments.containsKey(WebSendParam.onlinePagingRecording.name())){
				SmartPagerApplication.getInstance().getPreferences().setOnlinePagingRecording(greetingUrl);
				root.put(WebSendParam.onlinePagingRecording.name(), greetingUrl);
			}
			if (mArguments.containsKey(WebSendParam.offlinePagingRecording.name())){
				SmartPagerApplication.getInstance().getPreferences().setOfflinePagingRecording(greetingUrl);
				root.put(WebSendParam.offlinePagingRecording.name(), greetingUrl);
			}
		}
		
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new BaseResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return  WebAction.setProfile;
	}

	@Override
	public void setArguments(Bundle bundle) {
		mArguments = bundle;
		
		if (mArguments.containsKey(WebSendParam.onlinePagingRecording.name())) {
			String onGreetingUri = mArguments.getString(WebSendParam.onlinePagingRecording.name());
			addGreetingPreCommand(onGreetingUri);
		}
		if (mArguments.containsKey(WebSendParam.offlinePagingRecording.name())) {
			String offGreetingUri = mArguments.getString(WebSendParam.offlinePagingRecording.name());
			addGreetingPreCommand(offGreetingUri);
		}
	}

	private void addGreetingPreCommand(String uriToSend) {
		if (!TextUtils.isEmpty(uriToSend)){
	    	PutFileCommand comand = new PutFileCommand();
	    	comand.setFileName(uriToSend);
	    	comand.setSavePublic(true);
	    	comand.setMimeType(PutFileCommand.AUDIO_MPEG);
	    	this.addPreCommand(comand);
	    }
	}
	
	/**
	 * Parses {@linkplain PutFileCommand} pre-commands results to obtain audio records URLs
	 * @return
	 */
	private ArrayList<String> parsePutFileResults(){
		ArrayList<String> uriList = new ArrayList<String>();
		for(String result : mPreCommandResultList){
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
				String url = jsonObject.optString("url");
				boolean isAudio = (url.indexOf(".3gp")!= -1) || (url.indexOf(".wav")!= -1) || (url.indexOf(".mp3")!= -1);
				if (isAudio){
					uriList.add(url);
				}				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}		
		return uriList;
	}
	
	

}
