package net.smartpager.android.activity.settings;

import android.content.ContentValues;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.activity.AbstractQuickResponseActivity;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.database.DatabaseHelper.QuickResponseTable;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.fragment.dialog.QuickResponseEditFragmentDialog;

public class QuickResponsesActivity extends AbstractQuickResponseActivity implements OnDialogDoneListener {

	private int mQuickRespId;

	@Override
	protected boolean isUpNavigationEnabled() {
		
		return true;
	}
	
	@Override
	public void onEditQuickResponse(int id, String msgText) {
		
		mQuickRespId = id;
		
		QuickResponseEditFragmentDialog dialog = QuickResponseEditFragmentDialog.newInstance(
				getString(R.string.dialog_title_edit_quick_resp),
				"",
				msgText, false, this);
		dialog.show(getSupportFragmentManager(), FragmentDialogTag.EditQuickResponse.name());
		
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);		
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);				
	}

	@Override
	public void onDeleteQuickResponse(int id) {
		
		mQuickRespId = id;
		
		AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(
				getApplicationContext(), 
				getString(R.string.dialog_title_delete_quick_resp),
				getString(R.string.dialog_msg_delete_quick_resp),
				"", false, this);
		dialog.show(getSupportFragmentManager(), FragmentDialogTag.DeleteQuickResponse.name());
		
	}

	@Override
	public void onDialogDone(FragmentDialogTag tag, String data) {
		
		if (tag.equals(FragmentDialogTag.DeleteQuickResponse)) {
			String where = QuickResponseTable._id.name() + " = " + mQuickRespId;
			getContentResolver().delete(SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI, where, null);
			getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI, null);
		}
		
		if (tag.equals(FragmentDialogTag.EditQuickResponse)) {
			ContentValues values = new ContentValues();
			values.put(QuickResponseTable.messageText.name(), data);
			String where = QuickResponseTable._id.name() + " = " + mQuickRespId;
			getContentResolver().update(SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI, values, where, null);
			getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI, null);
		}
	}

	@Override
	public void onDialogNo(FragmentDialogTag tag, String data) {
		
	}

	@Override
	protected boolean isForEdit() {
		return true;
	}
}