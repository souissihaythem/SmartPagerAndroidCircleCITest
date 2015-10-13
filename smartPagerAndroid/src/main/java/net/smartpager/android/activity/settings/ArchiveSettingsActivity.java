package net.smartpager.android.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.ArchiveMessages;
import net.smartpager.android.consts.RemoveFromArchive;
import net.smartpager.android.consts.RequiredPinToBeEntered;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;

public class ArchiveSettingsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_archive_settings);
		initGui();
	}

	private void initGui() {
		setArchiveMessagesFromServer();
		setRemoveArchiveMessagesFromServer();
		initArchiveMessagesRadiogroup();
		initRemoveRadiogroup();
	}
	
	@Override
	protected boolean isUpNavigationEnabled() {
		
		return true;
	}
	
	public void setArchiveMessagesFromServer() {
		TextView archiveMessagesDisabledTextView = (TextView) findViewById(R.id.settings_archive_messages_disabled);
		
		ArchiveMessages archive = null;
		if (SmartPagerApplication.getInstance().getPreferences().getDeviceArchivePeriod().equals("null")) {
			archiveMessagesDisabledTextView.setVisibility(View.GONE);
		} else {
			archiveMessagesDisabledTextView.setVisibility(View.VISIBLE);
			
			int timeout = Integer.parseInt(SmartPagerApplication.getInstance().getPreferences().getDeviceArchivePeriod());
			switch(timeout) {
				case 3600:
					archive = ArchiveMessages.hour;
					break;
				case 86400:
					archive = ArchiveMessages.day;
					break;
				case 604800:
					archive = ArchiveMessages.week;
					break;
			}
			
			SmartPagerApplication.getInstance().getSettingsPreferences().setArchiveMessages(archive);
			
			archiveMessagesDisabledTextView.setText("Input disabled. Value set on server.");
			
			RadioGroup requiredRadioGroup = (RadioGroup) findViewById(R.id.settings_archive_messages_radiogroup);
			for (int i = 0;i < requiredRadioGroup.getChildCount(); i++) {
				requiredRadioGroup.getChildAt(i).setEnabled(false);
			}
		}
		
	}
	
	public void setRemoveArchiveMessagesFromServer() {
		TextView purgeMessagesDisabledTextView = (TextView) findViewById(R.id.settings_remove_archive_disabled);
		
		RemoveFromArchive archive = null;
		if (SmartPagerApplication.getInstance().getPreferences().getDevicePurgePeriod().equals("null")) {
			purgeMessagesDisabledTextView.setVisibility(View.GONE);
		} else {
			purgeMessagesDisabledTextView.setVisibility(View.VISIBLE);
			
			int timeout = Integer.parseInt(SmartPagerApplication.getInstance().getPreferences().getDevicePurgePeriod());
			switch(timeout) {
				case 86400:
					archive = RemoveFromArchive.day;
					break;
				case 604800:
					archive = RemoveFromArchive.week;
					break;
				case 2592000:
					archive = RemoveFromArchive.month;
					break;
			}
			
			SmartPagerApplication.getInstance().getSettingsPreferences().setRemoveFromArchive(archive);
			
			purgeMessagesDisabledTextView.setText("Input disabled. Value set on server.");
			
			RadioGroup requiredRadioGroup = (RadioGroup) findViewById(R.id.settings_remove_archive_radiogroup);
			for (int i = 0;i < requiredRadioGroup.getChildCount(); i++) {
				requiredRadioGroup.getChildAt(i).setEnabled(false);
			}
		}
	}

	private void initArchiveMessagesRadiogroup() {
		RadioGroup archiveRadioGroup = (RadioGroup) findViewById(R.id.settings_archive_messages_radiogroup);
		int checkedRadiobutton = R.id.settings_archive_messages_1w;
		switch (SmartPagerApplication.getInstance().getSettingsPreferences().getArchiveMessages()) {
			case hour:
				checkedRadiobutton = R.id.settings_archive_messages_1h;
				break;
			case day:
				checkedRadiobutton = R.id.settings_archive_messages_1d;
				break;
			case week:
				checkedRadiobutton = R.id.settings_archive_messages_1w;
				break;
			default:
			break;
		}
		archiveRadioGroup.check(checkedRadiobutton);

		archiveRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				ArchiveMessages archive = ArchiveMessages.week;
				switch (checkedId) {					
					case R.id.settings_archive_messages_1h:
						archive = ArchiveMessages.hour;
						break;
					case R.id.settings_archive_messages_1d:
						archive = ArchiveMessages.day;
						break;
					case R.id.settings_archive_messages_1w:
						archive = ArchiveMessages.week;
						break;					
					default:
					break;
				}
				SmartPagerApplication.getInstance().getSettingsPreferences().setArchiveMessages(archive);
			}
		});
	}

	private void initRemoveRadiogroup() {
		RadioGroup removeRadioGroup = (RadioGroup) findViewById(R.id.settings_remove_archive_radiogroup);
		int checkedRadiobutton = R.id.settings_archive_messages_1w;
		switch (SmartPagerApplication.getInstance().getSettingsPreferences().getRemoveFromArchive()) {
			case day:
				checkedRadiobutton = R.id.settings_remove_archive_messages_1d;
				break;
			case week:
				checkedRadiobutton = R.id.settings_remove_archive_messages_1w;
				break;
			case month:
				checkedRadiobutton = R.id.settings_remove_archive_messages_1m;
				break;
			default:
			break;
		}
		removeRadioGroup.check(checkedRadiobutton);

		removeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RemoveFromArchive archive = RemoveFromArchive.month;
				switch (checkedId) {
					case R.id.settings_remove_archive_messages_1d:
						archive = RemoveFromArchive.day;
						break;
					case R.id.settings_remove_archive_messages_1w:
						archive = RemoveFromArchive.week;
					break;
					case R.id.settings_remove_archive_messages_1m:
						archive = RemoveFromArchive.month;
						break;					
					default:
					break;
				}
				SmartPagerApplication.getInstance().getSettingsPreferences().setRemoveFromArchive(archive);
			}
		});		
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		
	}

}
