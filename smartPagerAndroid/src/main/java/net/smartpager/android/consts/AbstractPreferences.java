package net.smartpager.android.consts;

import android.content.SharedPreferences;

import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.database.ContactCursorTable;
import net.smartpager.android.utils.AudioManagerUtils;

import java.util.Date;

/**
 * Created by dmitriy on 2/21/14.
 */
public abstract class AbstractPreferences {

    public abstract SharedPreferences getSharedPreferences();

    public abstract SharedPreferences.Editor getEditor();

    public void setPersonalVerificationQuestionsSaved(boolean value)
    {

    }

    public boolean isPersonalVerificationQuestionsSaved()
    {
        return false;
    }

    public int getCheckAlarmCounter() {
        return -1;
    }

    public void setActivityToGo(BaseActivity.ACTIVITY_TAG activityTag, BaseActivity.ACTIVITY_ACTION activityAction) {
    }

    public BaseActivity.ACTIVITY_ACTION getActivityAction() {
        return null;
    }

    public BaseActivity.ACTIVITY_TAG getActivityToGo() {
        return null;
    }

    public void setCheckAlarmCounter(int count) {
    }

    public String getOutgoingPhoneCallNumber() {
        return null;
    }

    public void setOutgoingPhoneCallNumber(String phone) {
    }

    public String getCallRouterPhoneNumber() {
        return null;
    }

    public void setCallRouterPhoneNumber(String phone) {
    }

    public String getUserID() {
        return null;
    }

    public void setUserID(String user_id) {
    }

    public String getPushSessionHashKey() {
        return null;
    }

    public void setPushSessionHashKey(String pushSessionHashKey) {
    }

    public String getGCMID() {
        return null;
    }

    public void setGCMID(String gcm_id) {
    }

    public String getPassword() {
        return null;
    }

    public void setPassword(String password) {
    }

    public String getPin() {
        return null;
    }

    public void setPin(String pin) {
    }

    public boolean getSaveProfile() {
        return false;
    }

    public void setSaveProfile(boolean isMobileVerify) {
    }

    public boolean getMobileVerify() {
        return false;
    }

    public void setMobileVerify(boolean isMobileVerify) {
    }

    public boolean getLicenseAgree() {
        return false;
    }

    public void setLicenseAgree(boolean isLicenseAgree) {
    }

    public boolean isSynchronized() {
        return false;
    }

    public void setSynchronised(boolean isSync) {
    }

    public String getLastName() {
        return null;
    }

    public void setLastName(String value) {
    }

    public String getTitle() {
        return null;
    }

    public void setTitle(String value) {
    }

    public String getAccountName() {
        return null;
    }

    public void setAccountName(String value) {
    }
    
    public String getIncomingCallMode() {
    	return null;
    }
    
    public void setIncomingCallMode(String value) {
    }
    
    public String getIncomingCallModeFailoverToPageSeconds() {
    	return null;
    }
    
    public void setIncomingCallModeFailoverToPageSeconds(String value) {
    }

    public String getRingTone() {
        return null;
    }

    public void setRingTone(String value) {
    }

    public String getCriticalRingTone() {
        return null;
    }

    public void setCriticalRingTone(String value) {
    }
    
    public String getCasualRingTone() {
    	return null;
    }
    
    public void setCasualRingTone(String value) {
    }

    public String getStatus() {
        return null;
    }

    public void setStatus(String value) {
    }

    public ContactCursorTable.ContactStatus getStatusEnum() {
        return null;
    }

    public void setStatusEnum(ContactCursorTable.ContactStatus value) {
    }

    public boolean getSkipOfflinePagingRecording() {
        return false;
    }

    public void setSkipOfflinePagingRecording(boolean value) {
    }

    public boolean getSkipOnlinePagingRecording() {
        return false;
    }

    public void setSkipOnlinePagingRecording(boolean value) {
    }

    public String getOnlinePagingRecording() {
        return null;
    }

    public void setOnlinePagingRecording(String value) {
    }

    public String getOfflinePagingRecording() {
        return null;
    }

    public void setOfflinePagingRecording(String value) {
    }

    public String getOnlinePagingRecordingTimestamp() {
        return null;
    }

    public void setOnlinePagingRecordingTimestamp(String value) {
    }

    public String getOfflinePagingRecordingTimestamp() {
        return null;
    }

    public void setOfflinePagingRecordingTimestamp(String value) {
    }

    public String getDepartments() {
        return null;
    }

    public void setDepartments(String value) {
    }

    public String getSmartPagerID() {
        return null;
    }

    public void setSmartPagerID(String value) {
    }

    public String getPagerNumber() {
        return null;
    }

    public void setPagerNumber(String value) {
    }

    public String getFirstName() {
        return null;
    }

    public void setFirstName(String value) {
    }

    public boolean isPinEntered() {
        return false;
    }

    public String getLastUpdate() {
        return null;
    }

    public void checkBuildUpdate() {
    }

    public void setLastUpdate(String value) {
    }

    public void setLastUpdate(Date date) {
    }

    public String getProfilePicture() {
        return null;
    }

    public void setProfilePicture(String pictureString) {
    }

    public long getLastArchiveClean() {
        return -1;
    }

    public void setLastArchiveClean(long value) {
    }

    public long getLastMoveToArchive() {
        return -1;
    }

    public void setLastMoveToArchive(long value) {
    }

    public boolean isQuickResponsesInserted() {
        return false;
    }

    public void setQuickResponsesInserted(boolean value) {
    }

    public boolean getCanSendCritical() {
        return false;
    }

    public void setCanSendCritical(boolean canSendCritical) {
    }

    public boolean getForwardPagesEnabled() {
        return false;
    }

    public void setForwardPagesEnabled(boolean forwardPagesEnabled) {
    }

    public String getForwardType() {
        return null;
    }

    public void setForwardType(String type) {
    }

    public String getForwardGroupID() {
        return null;
    }

    public void setForwardGroupID(String groupID) {
    }

    public String getForwardContactID() {
        return null;
    }

    public void setForwardContactID(String contactID) {
    }

    public String getForwardSmartPagerID() {
        return null;
    }

    public void setForwardSmartPagerID(String smartPagerID) {
    }

    public String getForwardName() {
        return null;
    }

    public void setForwardName(String name) {
    }

    public boolean getAutoResponseEnabled() {
        return false;
    }

    public void setAutoResponseEnabled(boolean enabled) {
    }

    public String getAutoResponse() {
        return null;
    }

    public void setAutoResponse(String autoResponse) {
    }

    public boolean getLoudSpeaker() {
        return false;
    }

    public void setLoudSpeaker(boolean mute) {
    }

    public boolean isLocked() {
        return false;
    }

    public void setLock(boolean lock) {
    }

    public boolean isConfirmLockFromService() {
        return false;
    }

    public void setConfirmLockFromService(boolean lock) {
    }

    public SyncState getSyncState() {
        return null;
    }

    public void setSyncState(SyncState state) {
    }

    //The SettingsPreferencesPart goes here

    public int getCriticalAlert(){
        return -1;
    }

    public void setCriticalAlert(int position){
    }

    public int getNormalAlert(){
        return -1;
    }

    public void setNormalAlert(int position){
    }
    
    public int getCasualAlert() {
    	return -1;
    }
    
    public void setCasualAlert(int position){
    }

    public int getVolume(){
        return -1;
    }

    public void setVolume(int volume){
    }

    public boolean getVibration(){
        return false;
    }

    public void setVibration(boolean vibration){
    }
//
//    public boolean getForwardPagesEnabled() {
//        return false;
//    }
//
//    public void setForwardPagesEnabled(boolean forwardPagesEnabled) {
//    }

    public CallsSettings getBlockMyNumber(){
        return null;
    }

    public void setBlockMyNumber(CallsSettings block){
    }

    public String getBlockingPrefix(){
        return null;
    }

    public void setBlockingPrefix(String prefix){
    }
    
    public boolean getVoipAllowed(){
        return false;
    }

    public void setVoipAllowed(boolean voipAllowed){
    }
    
    public boolean getVoipToggled() {
    	return false;
    }
    
    public void setVoipToggled(boolean voipToggled) {
    }

    public boolean getPreferCellular() {
        return false;
    }

    public void setPreferCellular(boolean preferCellular) {
    }

    public boolean getPreferCellularToggled() {
        return false;
    }

    public void setPreferCellularToggled(boolean preferCellularToggled) {
    }

    public CallsSettings getLogCalls() {
        return null;
    }

    public void setLogCalls(CallsSettings log) {
    }

    public CallsSettings getRecordCalls() {
        return null;
    }

    public void setRecordCalls(CallsSettings record) {

    }

    public ArchiveMessages getArchiveMessages() {
        return null;
    }

    public void setArchiveMessages(ArchiveMessages archive) {
    }

    public RemoveFromArchive getRemoveFromArchive() {
        return null;
    }

    public void setRemoveFromArchive(RemoveFromArchive archive) {
    }

    public RequiredPinToBeEntered getRequiredPin() {
        return null;
    }

    public void setRequiredPin(RequiredPinToBeEntered required) {
    }

    public int getEnterPinTriesLeft() {
        return -1;
    }

    public void setEnterPinTriesLeft(int number) {
    }

    public String getQuickResponse() {
        return null;
    }

    public void setQuickResponse(String response) {
    }

    public long getPinEnteredTime() {
        return -1;
    }

    public void setPinEnteredTime() {
    }
    
    public String getPinEntryTimeout() {
    	return null;
    }
    
    public void setPinEntryTimeout(String timeout) {
    }
    
    public String getDeviceArchivePeriod() {
    	return null;
    }
    
    public void setDeviceArchivePeriod(String timeout) {
    }
    
    public String getDevicePurgePeriod() {
    	return null;
    }
    
    public void setDevicePurgePeriod(String timeout) {
    }
    
    public boolean getShowMobileNumbers() {
    	return false;
    }
    
    public void setShowMobileNumbers(boolean value) {
    }
    
    public boolean getAllowCallFromContacts() {
    	return false;
    }
    
    public void setAllowCallFromContacts(boolean value) {
    }
    
    public String getCallBlocking() {
    	return null;
    }
    
    public void setCallBlocking(String value) {
    }
    
    public boolean getRestrictCallBlocking() {
    	return false;
    }
    
    public void setRestrictCallBlocking(boolean value) {
    }

    public String getMessageTypes() {
        return null;
    }

    public void setMessageTypes(String messageTypes) {
    }
}
