package net.smartpager.android.activity.settings;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.rey.material.widget.EditText;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.adapters.AutoResponseCursorAdapter;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.DatabaseHelper.QuickResponseTable;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.web.response.AbstractResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class AutoTextResponseActivity extends BaseActivity implements LoaderCallbacks<Cursor>{

	@ViewInjectAnatation(viewID = R.id.auto_responses_new_edit_text)
	EditText mNewTextView;
	@ViewInjectAnatation(viewID = R.id.auto_responses_custom_radiobutton)
	RadioButton mCustomRadioButton;
	@ViewInjectAnatation(viewID = R.id.auto_responses_list_view)
	ListView mListView;
	
	CursorAdapter mResponseAdapter;
	
	@Override
	protected boolean isUpNavigationEnabled() {

		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auto_text_response);
		Injector.doInjection(this);
		initAdapter();
		initEnableSwitch();
	}
	
	@Override
	public void onBackPressed() {
		if (NetworkUtils.isInternetConnectedAsync()){
			startSetProfile();
		} 
		super.onBackPressed();
	}

	private void startSetProfile() {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.firstName.name(), SmartPagerApplication.getInstance().getPreferences().getFirstName());
        extras.putString(WebSendParam.lastName.name(), SmartPagerApplication.getInstance().getPreferences().getLastName());
        extras.putString(WebSendParam.title.name(), SmartPagerApplication.getInstance().getPreferences().getTitle());
        SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, extras);
	}

	private void initEnableSwitch() {
		
		final Switch enableSwitch = (Switch) findViewById(R.id.settings_autoresponse_switch);
		enableSwitch.setChecked(SmartPagerApplication.getInstance().getPreferences().getAutoResponseEnabled());
		enableSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!NetworkUtils.isInternetConnectedAsync()){
					showErrorDialog(getString(R.string.connection_is_required));
					// set old position
					enableSwitch.post( new Runnable() {						
						@Override
						public void run() {
							enableSwitch.setChecked( SmartPagerApplication.getInstance().getPreferences().getAutoResponseEnabled());
						}
					});
				} else {
					showProgressDialog(getString(R.string.saving));
					SmartPagerApplication.getInstance().getPreferences().setAutoResponseEnabled(isChecked);
					startSetProfile();
				}
			}
		});
	}

	private void initAdapter() {
		mResponseAdapter = new AutoResponseCursorAdapter(this, null, false);
		mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mListView.setAdapter(mResponseAdapter);
		getSupportLoaderManager().initLoader(LoaderID.RESPONSES_LOADER.ordinal(), null, this);
		mListView.setOnItemClickListener( mOnListViewItemClickListener );
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {		
		return new CursorLoader(this, SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI, 
				null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(cursor != null && !cursor.isClosed())
		    mResponseAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mResponseAdapter.swapCursor(null);		
	}

	@ClickListenerInjectAnatation(viewID = R.id.auto_responses_custom_radiobutton)
	public void onCutomRadiobuttonClick(View v){
		if (!NetworkUtils.isInternetConnectedAsync()){
			showErrorDialog(getString(R.string.connection_is_required));
			mCustomRadioButton.setChecked(false);
			return;
		}
		String messageText = mNewTextView.getText().toString().trim();
		if (!TextUtils.isEmpty( messageText )) {
			SmartPagerApplication.getInstance().getPreferences().setAutoResponse(messageText);
			mCustomRadioButton.setChecked(true);
			mResponseAdapter.notifyDataSetInvalidated();
		} else {
			mCustomRadioButton.setChecked(false);
			showToast(getString(R.string.quick_response_text_empty));
		}
	}
	
	private OnItemClickListener mOnListViewItemClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			if (!NetworkUtils.isInternetConnectedAsync()){
				showErrorDialog(getString(R.string.connection_is_required));
				return;
			}
			AutoResponseCursorAdapter adapter = (AutoResponseCursorAdapter) parent.getAdapter();
			Cursor cursor = (Cursor) adapter.getCursor();
			int columnIndex = cursor.getColumnIndex( QuickResponseTable.messageText.name() );
			cursor.moveToPosition(position); // !!!
			String messageText = cursor.getString(columnIndex);
			mCustomRadioButton.setChecked(false);
			adapter.setSelectedIndex(position);
			SmartPagerApplication.getInstance().getPreferences().setAutoResponse(messageText);
			adapter.notifyDataSetChanged();
		}
	};
	
	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.setProfile){
			hideProgressDialog();
		}
	}
	
	@Override
		protected void onErrorResponse(WebAction action, AbstractResponse response) {
			hideProgressDialog();
			super.onErrorResponse(action, response);
		}
}
