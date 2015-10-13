package net.smartpager.android.consts;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Preferences extends AbstractPreferences {

//    public enum SharedPreferencesFields {
//		pin,
//		user_id,
//		password,
//		lastName,
//		title,
//		accountName,
//		ringTone,
//		status,
//        outgoingCallPhoneNumber,
//        callRouterPhoneNumber,
//		onlinePagingRecording,
//		onlinePagingRecordingTimestamp,
//		offlinePagingRecording,
//		offlinePagingRecordingTimestamp,
//		skipOfflinePagingRecording,
//		skipOnlinePagingRecording,
//		departments,
//		smartPagerID,
//		firstName,
//		lastUpdate,
//		profilePicture,
//		lastArchiveClean,
//		lastMoveToArchive,
//		pagerNumber,
//		isLicenseAgree,
//		isMobileVerify,
//		isSaveProfile,
//		isSync,
//		quickResponsesInserted,
//		criticalRingTone,
//		canSendCritical,
//		forwardPagesEnabled,
//		forwardName,
//		type,
//		groupID,
//		contactID,
//		forwardSmartPagerID,
//		autoResponseEnabled,
//		autoResponse,
//		loudSpeaker,
//		lock,
//		confirmLockFromService,
//		syncState,
//		checkAlarmCounter,
//		GCMID,
//		pushSessionHashKey,
//        buildVersion,
//        activityToGo,
//        activityAction
//	}
	
//	public enum SyncState {
//		NotSyncronized,
//		InProgress,
//		Syncronized
//	}
	
//	public enum ForwardType {
//		PagingGroup, User
//	}
	
	private static final String SHARED_PREFERENCES_NAME = "SmartPagerSharedPreferences";

    private static final int BUILD_VERSION = 1;

    @Override
	public SharedPreferences getSharedPreferences() {
		return SmartPagerApplication.getInstance().getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
	}

    @Override
	public  SharedPreferences.Editor getEditor() {
		return SmartPagerApplication.getInstance().getSharedPreferences(SHARED_PREFERENCES_NAME, 0).edit();
	}

    @Override
    public void setPersonalVerificationQuestionsSaved(boolean value) {
        Editor preferences = getEditor();
        preferences.putBoolean(SharedPreferencesFields.isPersonalVerificationQuestionsSaved.name(), value);
        preferences.commit();
    }

    @Override
    public boolean isPersonalVerificationQuestionsSaved() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getBoolean(SharedPreferencesFields.isPersonalVerificationQuestionsSaved.name(), false);
    }

    @Override
	public  int getCheckAlarmCounter() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getInt(SharedPreferencesFields.checkAlarmCounter.name(), 0);
	}

    @Override
    public  void setActivityToGo(BaseActivity.ACTIVITY_TAG activityTag, BaseActivity.ACTIVITY_ACTION activityAction) {
        Editor preferences = getEditor();
        preferences.putString(SharedPreferencesFields.activityToGo.name(), activityTag.name());
        preferences.putString(SharedPreferencesFields.activityAction.name(), activityAction.name());
        preferences.commit();
    }

    @Override
    public  BaseActivity.ACTIVITY_ACTION getActivityAction ()
    {
        SharedPreferences preferences = getSharedPreferences();
        return BaseActivity.ACTIVITY_ACTION.valueOf(preferences.getString(SharedPreferencesFields.activityAction.name(), BaseActivity.ACTIVITY_ACTION.NONE.name()));
    }

    @Override
    public  BaseActivity.ACTIVITY_TAG getActivityToGo ()
    {
        SharedPreferences preferences = getSharedPreferences();
        return BaseActivity.ACTIVITY_TAG.valueOf(preferences.getString(SharedPreferencesFields.activityToGo.name(), BaseActivity.ACTIVITY_TAG.NONE.name()));
    }

    @Override
	public  void setCheckAlarmCounter(int count) {
		Editor preferences = getEditor();
		preferences.putInt(SharedPreferencesFields.checkAlarmCounter.name(), count);
		preferences.commit();
	}

    @Override
    public  String getOutgoingPhoneCallNumber() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getString(SharedPreferencesFields.outgoingCallPhoneNumber.name(), "");
    }

    @Override
    public  void setOutgoingPhoneCallNumber(String phone) {
        Editor preferences = getEditor();
        preferences.putString(SharedPreferencesFields.outgoingCallPhoneNumber.name(), phone);
        preferences.commit();
    }

    @Override
    public  String getCallRouterPhoneNumber() {
        SharedPreferences preferences = getSharedPreferences();
        return preferences.getString(SharedPreferencesFields.callRouterPhoneNumber.name(), "");
    }

    @Override
    public  void setCallRouterPhoneNumber(String phone) {
        Editor preferences = getEditor();
        preferences.putString(SharedPreferencesFields.callRouterPhoneNumber.name(), phone);
        preferences.commit();
    }

    @Override
	public  String getUserID() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.user_id.name(), "");
	}

    @Override
	public  void setUserID(String user_id) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.user_id.name(), user_id);
		preferences.commit();
	}

    @Override
	public  String getPushSessionHashKey() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.pushSessionHashKey.name(), "");
	}

    @Override
	public  void setPushSessionHashKey(String pushSessionHashKey) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.pushSessionHashKey.name(), pushSessionHashKey);
		preferences.commit();
	}

    @Override
	public  String getGCMID() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.GCMID.name(), "");
	}

    @Override
	public  void setGCMID(String gcm_id) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.GCMID.name(), gcm_id);
		preferences.commit();
	}

    @Override
	public  String getPassword() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.password.name(), "");
	}

    @Override
	public  void setPassword(String password) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.password.name(), password);
		preferences.commit();
	}

    @Override
	public  String getPin() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.pin.name(), "");
	}

    @Override
	public  void setPin(String pin) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.pin.name(), pin);
		preferences.commit();
	}

    @Override
	public  boolean getSaveProfile() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.isSaveProfile.name(), false);
	}

    @Override
	public  void setSaveProfile(boolean isMobileVerify) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.isSaveProfile.name(), isMobileVerify);
		preferences.commit();
	}

    @Override
	public  boolean getMobileVerify() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.isMobileVerify.name(), false);
	}

    @Override
	public  void setMobileVerify(boolean isMobileVerify) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.isMobileVerify.name(), isMobileVerify);
		preferences.commit();
	}

    @Override
	public  boolean getLicenseAgree() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.isLicenseAgree.name(), false);
	}

    @Override
	public  void setLicenseAgree(boolean isLicenseAgree) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.isLicenseAgree.name(), isLicenseAgree);
		preferences.commit();
	}

    @Override
	public  boolean isSynchronized() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.isSync.name(), false);
	}

    @Override
	public  void setSynchronised(boolean isSync) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.isSync.name(), isSync);
		preferences.commit();
	}

    @Override
	public  String getLastName() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.lastName.name(), "");
	}

    @Override
	public  void setLastName(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.lastName.name(), value);
		preferences.commit();
	}

    @Override
	public  String getTitle() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.title.name(), "");
	}

    @Override
	public  void setTitle(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.title.name(), value);
		preferences.commit();
	}

    @Override
	public  String getAccountName() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.accountName.name(), "");
	}

    @Override
	public  void setAccountName(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.accountName.name(), value);
		preferences.commit();
	}

    @Override
    public String getIncomingCallMode() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.incomingCallMode.name(), "");    	
    }
    
    @Override
    public void setIncomingCallMode(String value) {
    	Editor preferences = getEditor();
    	preferences.putString(SharedPreferencesFields.incomingCallMode.name(), value);
    	preferences.commit();
    }
    
    @Override
    public String getIncomingCallModeFailoverToPageSeconds() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getString(SharedPreferencesFields.incomingCallModeFailoverToPageSeconds.name(), "");
    }
    
    @Override
    public void setIncomingCallModeFailoverToPageSeconds(String value) {
    	Editor preferences = getEditor();
    	preferences.putString(SharedPreferencesFields.incomingCallModeFailoverToPageSeconds.name(), value);
    	preferences.commit();
    }
    
    @Override
	public  String getRingTone() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.ringTone.name(), "");
	}

    @Override
	public  void setRingTone(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.ringTone.name(), value);
		preferences.commit();
	}

    @Override
	public  String getCriticalRingTone() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.criticalRingTone.name(), "");
	}

    @Override
	public  void setCriticalRingTone(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.criticalRingTone.name(), value);
		preferences.commit();
	}
    
    @Override
    public String getCasualRingTone() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getString(SharedPreferencesFields.casualRingTone.name(), "");
    }
    
    @Override
    public void setCasualRingTone(String value) {
    	Editor preferences = getEditor();
    	preferences.putString(SharedPreferencesFields.casualRingTone.name(), value);
    	preferences.commit();
    }
    
    @Override
	public  String getStatus() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.status.name(), ContactStatus.DO_NOT_DISTURB.name());
	}

    @Override
	public  void setStatus(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.status.name(), value);
		preferences.commit();
	}

    @Override
	public  ContactStatus getStatusEnum() {
//		SharedPreferences preferences = getSharedPreferences();
//		String status = preferences.getString(SharedPreferencesFields.status.name(), ContactStatus.DO_NOT_DISTURB.name());
//		return ContactStatus.valueOf(status);
        return null;
	}

    @Override
	public  void setStatusEnum(ContactStatus value) {
//		Editor preferences = getEditor();
//		preferences.putString(SharedPreferencesFields.status.name(), value.name());
//		preferences.commit();
	}

    @Override
	public  boolean getSkipOfflinePagingRecording() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.skipOfflinePagingRecording.name(), false);
	}

    @Override
	public  void setSkipOfflinePagingRecording(boolean value) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.skipOfflinePagingRecording.name(), value);
		preferences.commit();
	}

    @Override
	public  boolean getSkipOnlinePagingRecording() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.skipOnlinePagingRecording.name(), false);
	}

    @Override
	public  void setSkipOnlinePagingRecording(boolean value) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.skipOnlinePagingRecording.name(), value);
		preferences.commit();
	}

    @Override
	public  String getOnlinePagingRecording() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.onlinePagingRecording.name(), "");
	}

    @Override
	public  void setOnlinePagingRecording(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.onlinePagingRecording.name(), value);
		preferences.commit();
	}

    @Override
	public  String getOfflinePagingRecording() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.offlinePagingRecording.name(), "");
	}

    @Override
	public  void setOfflinePagingRecording(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.offlinePagingRecording.name(), value);
		preferences.commit();
	}

    @Override
	public  String getOnlinePagingRecordingTimestamp() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.onlinePagingRecordingTimestamp.name(), "No custom greeting");
	}

    @Override
	public  void setOnlinePagingRecordingTimestamp(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.onlinePagingRecordingTimestamp.name(), value);
		preferences.commit();
	}

    @Override
	public  String getOfflinePagingRecordingTimestamp() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.offlinePagingRecordingTimestamp.name(), "No custom greeting");
	}

    @Override
	public  void setOfflinePagingRecordingTimestamp(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.offlinePagingRecordingTimestamp.name(), value);
		preferences.commit();
	}

    @Override
	public  String getDepartments() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.departments.name(), "");
	}

    @Override
	public  void setDepartments(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.departments.name(), value);
		preferences.commit();
	}

    @Override
	public  String getSmartPagerID() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.smartPagerID.name(), "");
	}

    @Override
	public  void setSmartPagerID(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.smartPagerID.name(), value);
		preferences.commit();
	}

    @Override
	public  String getPagerNumber() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.pagerNumber.name(), "");
	}

    @Override
	public  void setPagerNumber(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.pagerNumber.name(), value);
		preferences.commit();
	}

    @Override
	public  String getFirstName() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.firstName.name(), "");
	}

    @Override
	public  void setFirstName(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.firstName.name(), value);
		preferences.commit();
	}

    @Override
	public  boolean isPinEntered(){
		return !TextUtils.isEmpty(getPin());
	}

    @Override
	public  String getLastUpdate() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.lastUpdate.name(), "");
	}

    @Override
    public  void checkBuildUpdate ()
    {
        SharedPreferences preferences = getSharedPreferences();
        Editor editor = preferences.edit();
        if(preferences.getInt(SharedPreferencesFields.buildVersion.name(), -1) != BUILD_VERSION)
        {
            setLastUpdate("");
            editor.putInt(SharedPreferencesFields.buildVersion.name(), BUILD_VERSION);
            editor.commit();
        }
    }

    @Override
	public  void setLastUpdate(String value) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.lastUpdate.name(), value);
		preferences.commit();
	}

    @Override
	public  void setLastUpdate(Date date) {
		String dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(date);
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.lastUpdate.name(), dateFormat);
		preferences.commit();
	}

    @Override
	public  String getProfilePicture() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.profilePicture.name(), "");
	}

    @Override
	public  void setProfilePicture(String pictureString) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.profilePicture.name(), pictureString);
		preferences.commit();
	}

    @Override
	public  long getLastArchiveClean() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getLong(SharedPreferencesFields.lastArchiveClean.name(), 0);
	}

    @Override
	public  void setLastArchiveClean(long value) {
		Editor preferences = getEditor();
		preferences.putLong(SharedPreferencesFields.lastArchiveClean.name(), value);
		preferences.commit();
	}

    @Override
	public  long getLastMoveToArchive() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getLong(SharedPreferencesFields.lastMoveToArchive.name(), 0);
	}

    @Override
	public  void setLastMoveToArchive(long value) {
		Editor preferences = getEditor();
		preferences.putLong(SharedPreferencesFields.lastMoveToArchive.name(), value);
		preferences.commit();
	}

    @Override
	public  boolean isQuickResponsesInserted() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.quickResponsesInserted.name(), false);
	}

    @Override
	public  void setQuickResponsesInserted(boolean value) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.quickResponsesInserted.name(), value);
		preferences.commit();
	}


    @Override
	public  boolean getCanSendCritical() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.canSendCritical.name(), false);
	}

    @Override
	public  void setCanSendCritical(boolean canSendCritical) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.canSendCritical.name(), canSendCritical);
		preferences.commit();
	}

    @Override
	public  boolean getForwardPagesEnabled() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.forwardPagesEnabled.name(), false);
	}

    @Override
	public  void setForwardPagesEnabled(boolean forwardPagesEnabled) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.forwardPagesEnabled.name(), forwardPagesEnabled);
		preferences.commit();
	}

    @Override
	public  String getForwardType() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.type.name(), ForwardType.User.name());
	}

    @Override
	public  void setForwardType(String type) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.type.name(), type);
		preferences.commit();
	}

    @Override
	public  String getForwardGroupID() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.groupID.name(), "");
	}

    @Override
	public  void setForwardGroupID(String groupID) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.groupID.name(), groupID);
		preferences.commit();
	}

    @Override
	public  String getForwardContactID() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.contactID.name(), "");
	}

    @Override
	public  void setForwardContactID(String contactID) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.contactID.name(), contactID);
		preferences.commit();
	}

    @Override
	public  String getForwardSmartPagerID() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.forwardSmartPagerID.name(), "");
	}

    @Override
	public  void setForwardSmartPagerID(String smartPagerID) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.forwardSmartPagerID.name(), smartPagerID);
		preferences.commit();
	}

    @Override
	public  String getForwardName() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.forwardName.name(), "");
	}

    @Override
	public  void setForwardName(String name) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.forwardName.name(), name);
		preferences.commit();
	}

    @Override
	public  boolean getAutoResponseEnabled() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.autoResponseEnabled.name(), false); 
	}

    @Override
	public  void setAutoResponseEnabled(boolean enabled) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.autoResponseEnabled.name(), enabled);
		preferences.commit();
	}

    @Override
	public  String getAutoResponse() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.autoResponse.name(), ""); 
	}

    @Override
	public  void setAutoResponse(String autoResponse) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.autoResponse.name(), autoResponse);
		preferences.commit();
	}

    @Override
	public  boolean getLoudSpeaker() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.loudSpeaker.name(), true); 
	}

    @Override
	public  void setLoudSpeaker(boolean mute) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.loudSpeaker.name(), mute);
		preferences.commit();
	}

    @Override
	public  boolean isLocked() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.lock.name(), false); 
	}

    @Override
	public  void setLock(boolean lock) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.lock.name(), lock);
		preferences.commit();
	}

    @Override
	public  boolean isConfirmLockFromService() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.confirmLockFromService.name(), false); 
	}

    @Override
	public  void setConfirmLockFromService(boolean lock) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.confirmLockFromService.name(), lock);
		preferences.commit();
	}
    
    @Override
	public boolean getVoipAllowed(){
		SharedPreferences preferences = getSharedPreferences();
		
		return preferences.getBoolean(SharedPreferencesFields.voipAllowed.name(), false);
	}
	
    @Override
	public void setVoipAllowed(boolean voipAllowed){
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.voipAllowed.name(), voipAllowed);
		preferences.commit();
	}

	@Override
	public boolean getPreferCellular() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SharedPreferencesFields.preferCellular.name(), false);
	}

	@Override
	public void setPreferCellular(boolean preferCellular) {
		Editor preferences = getEditor();
		preferences.putBoolean(SharedPreferencesFields.preferCellular.name(), preferCellular);
		preferences.commit();
	}

    @Override
	public  SyncState getSyncState() {
		SharedPreferences preferences = getSharedPreferences();
		String state = preferences.getString(SharedPreferencesFields.syncState.name(), SyncState.NotSyncronized.name()); 
		return SyncState.valueOf(state);
	}

    @Override
	public  void setSyncState(SyncState state) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.syncState.name(), state.name());
		preferences.commit();
	}
    
    @Override
    public String getPinEntryTimeout() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getString(SharedPreferencesFields.pinEntryTimeout.name(), null);	
    }
    
    @Override
    public void setPinEntryTimeout(String timeout) {
    	Editor preferences = getEditor();
        preferences.putString(SharedPreferencesFields.pinEntryTimeout.name(), timeout);
    	preferences.commit();
    }
    
    @Override
    public String getDeviceArchivePeriod() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getString(SharedPreferencesFields.deviceArchivePeriod.name(), "null");
    }
    
    @Override
    public void setDeviceArchivePeriod(String timeout) {
    	Editor preferences = getEditor();
    	preferences.putString(SharedPreferencesFields.deviceArchivePeriod.name(), timeout);
    	preferences.commit();
    }
    
    @Override
    public String getDevicePurgePeriod() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getString(SharedPreferencesFields.devicePurgePeriod.name(), "null");
    }
    
    @Override
    public void setDevicePurgePeriod(String timeout) {
    	Editor preferences = getEditor();
    	preferences.putString(SharedPreferencesFields.devicePurgePeriod.name(), timeout);
    	preferences.commit();
    }
    
    @Override
    public boolean getShowMobileNumbers() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getBoolean(SharedPreferencesFields.showMobileNumbers.name(), false);
    }
    
    @Override
    public void setShowMobileNumbers(boolean value) {
    	Editor preferences = getEditor();
    	preferences.putBoolean(SharedPreferencesFields.showMobileNumbers.name(), value);
    	preferences.commit();
    }
    
    @Override
    public boolean getAllowCallFromContacts() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getBoolean(SharedPreferencesFields.allowCallFromContacts.name(), false);
    }
    
    @Override
    public void setAllowCallFromContacts(boolean value) {
    	Editor preferences = getEditor();
    	preferences.putBoolean(SharedPreferencesFields.allowCallFromContacts.name(), value);
    	preferences.commit();
    }
    
    @Override
    public String getCallBlocking() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getString(SharedPreferencesFields.callBlocking.name(), "null");
    }
    
    @Override
    public void setCallBlocking(String value) {
    	Editor preferences = getEditor();
    	preferences.putString(SharedPreferencesFields.callBlocking.name(), value);
    	preferences.commit();
    }
    
    @Override
    public boolean getRestrictCallBlocking() {
    	SharedPreferences preferences = getSharedPreferences();
    	return preferences.getBoolean(SharedPreferencesFields.restrictCallBlocking.name(), false);
    }
    
    @Override
    public void setRestrictCallBlocking(boolean value) {
    	Editor preferences = getEditor();
    	preferences.putBoolean(SharedPreferencesFields.restrictCallBlocking.name(), value);
    	preferences.commit();
    }

	@Override
	public  String getMessageTypes() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SharedPreferencesFields.messageTypes.name(), null);
	}

	@Override
	public  void setMessageTypes(String messageTypes) {
		Editor preferences = getEditor();
		preferences.putString(SharedPreferencesFields.messageTypes.name(), messageTypes);
		preferences.commit();
	}
}

