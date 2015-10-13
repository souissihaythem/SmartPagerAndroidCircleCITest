package net.smartpager.android.consts;

import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.utils.AudioManagerUtils;

public class SettingsPreferences extends AbstractPreferences{

//	public enum SettingsPreferencesFields {
//		criticalAlert,
//		normalAlert,
//		volume,
//		vibration,
//		blockMyNumber,
//		blockingPrefix,
//		logMyCalls,
//		recordMyCalls,
//		archiveMessages,
//		removeFromArchive,
//		requiredPinToBeEntered,
//		quickResponse,
//		enterPinTriesLeft,
//		pinLastEnteredTime,
//		forwardPagesEnabled
//	}
	
//	public enum CallsSettings {
//		on,
//		off,
//		ask
//	}
	
//	public enum ArchiveMessages {
//		hour,
//		day,
//		week
//	}

//	public enum RemoveFromArchive {
//		week,
//		month
//	}
	
//	public enum RequiredPinToBeEntered {
//		_30Min,
//		_1Hour,
//		_8Hours,
//		always
//	}
	
	private static final String SETTING_PREFERENCES_NAME = "SmartPagerSettingPreferences";

    @Override
	public SharedPreferences getSharedPreferences() {
		return SmartPagerApplication.getInstance().getSharedPreferences(SETTING_PREFERENCES_NAME, 0);
	}

    @Override
	public SharedPreferences.Editor getEditor() {
		return SmartPagerApplication.getInstance()
				.getSharedPreferences(SETTING_PREFERENCES_NAME, 0).edit();
	}
	
	public int getCriticalAlert(){
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getInt(SettingsPreferencesFields.criticalAlert.name(), 0);
	}
	
	public void setCriticalAlert(int position){
		Editor preferences = getEditor();
		preferences.putInt(SettingsPreferencesFields.criticalAlert.name(), position);
		preferences.commit();
	}
	
	public int getNormalAlert(){
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getInt(SettingsPreferencesFields.normalAlert.name(), 0);
	}
	
	public void setNormalAlert(int position){
		Editor preferences = getEditor();
		preferences.putInt(SettingsPreferencesFields.normalAlert.name(), position);
		preferences.commit();
	}
	
	public int getCasualAlert(){
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getInt(SettingsPreferencesFields.casualAlert.name(), 0);
	}
	
	public void setCasualAlert(int position){
		Editor preferences = getEditor();
		preferences.putInt(SettingsPreferencesFields.casualAlert.name(), position);
		preferences.commit();
	}
	
	public int getVolume(){
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getInt(SettingsPreferencesFields.volume.name(), 
				AudioManagerUtils.getStreamMaxVolume());
	}
	
	public void setVolume(int volume){
		Editor preferences = getEditor();
		preferences.putInt(SettingsPreferencesFields.volume.name(), volume);
		preferences.commit();
	}

	public boolean getVibration(){
		SharedPreferences preferences = getSharedPreferences();
		
		return preferences.getBoolean(SettingsPreferencesFields.vibration.name(), true);
	}
	
	public void setVibration(boolean vibration){
		Editor preferences = getEditor();
		preferences.putBoolean(SettingsPreferencesFields.vibration.name(), vibration);
		preferences.commit();
	}
	
	public boolean getForwardPagesEnabled(){
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getBoolean(SettingsPreferencesFields.forwardPagesEnabled.name(), true);
	}
	
	public void setForwardPagesEnabled(boolean forwardPagesEnabled){
		Editor preferences = getEditor();
		preferences.putBoolean(SettingsPreferencesFields.forwardPagesEnabled.name(), forwardPagesEnabled);
		preferences.commit();
	}
	
	public CallsSettings getBlockMyNumber(){
		SharedPreferences preferences = getSharedPreferences();
		String getBlock = preferences.getString(SettingsPreferencesFields.blockMyNumber.name(), CallsSettings.ask.name()); 
		return CallsSettings.valueOf(getBlock);
	}
	
	public void setBlockMyNumber(CallsSettings block){
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.blockMyNumber.name(), block.name());
		preferences.commit();
	}
	
	public String getBlockingPrefix(){
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SettingsPreferencesFields.blockingPrefix.name(), "*67"); 
	}
	
	public void setBlockingPrefix(String prefix){
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.blockingPrefix.name(), prefix);
		preferences.commit();
	}
	
	public boolean getVoipToggled() {
		SharedPreferences preferences = getSharedPreferences();
		
		return preferences.getBoolean(SettingsPreferencesFields.voipToggled.name(), false);
	}
	
	public void setVoipToggled(boolean voipToggled) {
		Editor preferences = getEditor();
		preferences.putBoolean(SettingsPreferencesFields.voipToggled.name(), voipToggled);
		preferences.commit();
	}

	public boolean getPreferCellularToggled() {
		SharedPreferences preferences = getSharedPreferences();

		return preferences.getBoolean(SettingsPreferencesFields.preferCellularToggled.name(), false);
	}

	public void setPreferCellularToggled(boolean preferCellular) {
		Editor preferences = getEditor();
		preferences.putBoolean(SettingsPreferencesFields.preferCellularToggled.name(), preferCellular);
		preferences.commit();
	}

	public CallsSettings getLogCalls() {
		SharedPreferences preferences = getSharedPreferences();
		String getCalls = preferences.getString(SettingsPreferencesFields.logMyCalls.name(), CallsSettings.ask.name()); 
		return CallsSettings.valueOf(getCalls);
	}

	public void setLogCalls(CallsSettings log) {
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.logMyCalls.name(), log.name());
		preferences.commit();
	}

	public CallsSettings getRecordCalls() {
		SharedPreferences preferences = getSharedPreferences();
		String getCalls = preferences.getString(SettingsPreferencesFields.recordMyCalls.name(), CallsSettings.ask.name()); 
		return CallsSettings.valueOf(getCalls);
	}

	public void setRecordCalls(CallsSettings record) {
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.recordMyCalls.name(), record.name());
		preferences.commit();
		
	}
	
	public ArchiveMessages getArchiveMessages() {
		SharedPreferences preferences = getSharedPreferences();
		String archive = preferences.getString(SettingsPreferencesFields.archiveMessages.name(), ArchiveMessages.week.name()); 
		return ArchiveMessages.valueOf(archive);
	}
	
	public void setArchiveMessages(ArchiveMessages archive) {
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.archiveMessages.name(), archive.name());
		preferences.commit();
	}
	
	public RemoveFromArchive getRemoveFromArchive() {
		SharedPreferences preferences = getSharedPreferences();
		String archive = preferences.getString(SettingsPreferencesFields.removeFromArchive.name(), RemoveFromArchive.month.name()); 
		return RemoveFromArchive.valueOf(archive);
	}
	
	public void setRemoveFromArchive(RemoveFromArchive archive) {
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.removeFromArchive.name(), archive.name());
		preferences.commit();
	}

	public RequiredPinToBeEntered getRequiredPin() {
		SharedPreferences preferences = getSharedPreferences();
		String required = preferences.getString(SettingsPreferencesFields.requiredPinToBeEntered.name(), RequiredPinToBeEntered._1Hour.name());
		return RequiredPinToBeEntered.valueOf(required);
	}
	
	public void setRequiredPin(RequiredPinToBeEntered required) {
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.requiredPinToBeEntered.name(), required.name());
		preferences.commit();
	}
	
	public int getEnterPinTriesLeft() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getInt(SettingsPreferencesFields.enterPinTriesLeft.name(), 3); 
	}
	
	public void setEnterPinTriesLeft(int number) {
		Editor preferences = getEditor();
		preferences.putInt(SettingsPreferencesFields.enterPinTriesLeft.name(), number);
		preferences.commit();
	}

	public String getQuickResponse() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getString(SettingsPreferencesFields.quickResponse.name(), "No custom response"); 
	}
	
	public void setQuickResponse(String response) {
		Editor preferences = getEditor();
		preferences.putString(SettingsPreferencesFields.quickResponse.name(), response);
		preferences.commit();
	}

	public long getPinEnteredTime() {
		SharedPreferences preferences = getSharedPreferences();
		return preferences.getLong(SettingsPreferencesFields.pinLastEnteredTime.name(), 0);
	}

	public void setPinEnteredTime() {
		Editor preferences = getEditor();
		preferences.putLong(SettingsPreferencesFields.pinLastEnteredTime.name(), System.currentTimeMillis());
		preferences.commit();
	}
	
}
