package net.smartpager.android.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.rey.material.widget.EditText;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.adapters.QuickResponseCursorAdapter;
import net.smartpager.android.adapters.QuickResponseCursorAdapter.QuickResponseChangeListener;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.database.DatabaseHelper.QuickResponseTable;
import net.smartpager.android.web.response.AbstractResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public abstract class AbstractQuickResponseActivity extends BaseActivity 
													implements QuickResponseChangeListener, LoaderCallbacks<Cursor> {
	
	
	@ViewInjectAnatation(viewID = R.id.quick_responses_new_edit_text)
	EditText mNewTextView;
	@ViewInjectAnatation(viewID = R.id.quick_responses_show_list_view)
	ListView mShowListView;
	
	QuickResponseCursorAdapter mResponseAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quick_responses);
		Injector.doInjection(this);
		
		mResponseAdapter = new QuickResponseCursorAdapter(this, null, true);
		if (isForEdit()) {
			mResponseAdapter.setChangeListener(this);
			mResponseAdapter.setEditableMode(true);
		} else {
			mResponseAdapter.setEditableMode(false);
		}
		mShowListView.setAdapter(mResponseAdapter);
		
		getSupportLoaderManager().initLoader(LoaderID.RESPONSES_LOADER.ordinal(), null, this);
	}
	
	protected abstract boolean isForEdit();

	protected void setOnItemClickListener(OnItemClickListener listener) {
		mShowListView.setOnItemClickListener(listener);
	};

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {

	}

	@ClickListenerInjectAnatation(viewID = R.id.quick_responses_add_button)
	public void onAddButtonClick(View view) {
		String text = mNewTextView.getText().toString().trim();
		if (!TextUtils.isEmpty(text)){
			ContentValues values = new ContentValues();
			values.put(QuickResponseTable.messageText.name(), text);
			getContentResolver().insert(SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI, values);
			mNewTextView.setText("");
			getSupportLoaderManager().restartLoader(LoaderID.RESPONSES_LOADER.ordinal(), null, this);
		} else {
			showToast(getString(R.string.quick_response_text_empty));
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {		
		return new CursorLoader(this, SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI, null,
				null, null, null);
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
	

}
