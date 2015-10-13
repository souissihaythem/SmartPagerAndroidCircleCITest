package net.smartpager.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.database.DatabaseHelper.QuickResponseTable;

public class QuickResponseChooserActivity extends AbstractQuickResponseActivity implements LoaderCallbacks<Cursor> {
	
	@Override
	protected boolean isUpNavigationEnabled() {
		
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setOnItemClickListener(mOnItemClickListener);
	}
	
	protected OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
			CursorAdapter adapter = (CursorAdapter)parent.getAdapter();
			Cursor cursor = (Cursor) adapter.getItem(position);
			int columnIndex = cursor.getColumnIndex( QuickResponseTable.messageText.name() );
			String messageText = cursor.getString(columnIndex); 
			Intent data = new Intent();
			data.putExtra(BundleKey.quickResponce.name(), messageText);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
	};
	
	@Override
	public void onEditQuickResponse(int id, String msgText) {
		
	}

	@Override
	public void onDeleteQuickResponse(int id) {
		
	}

	
	@Override
	protected boolean isForEdit() {
		return false;
	}
}