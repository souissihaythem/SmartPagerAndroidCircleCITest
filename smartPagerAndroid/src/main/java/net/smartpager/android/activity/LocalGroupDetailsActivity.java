package net.smartpager.android.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.rey.material.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.RequestID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.LocalGroupCorrespondenceTable;
import net.smartpager.android.database.DatabaseHelper.PagingGroupTable;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.holder.Department;
import net.smartpager.android.holder.GroupMemberHolder;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.web.response.AbstractResponse;

import java.util.ArrayList;
import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class LocalGroupDetailsActivity extends BaseActivity implements LoaderCallbacks<Cursor>{

	public static final String LOCAL = "LOCAL";
	public static final String ONLINE = "ONLINE";
	private static final String STATUS_ON_CALL = "On Call -";
	private static final String STATUS_OFFLINE = "";
	
	@ViewInjectAnatation(viewID = R.id.group_details_name_textView)
	private TextView mTxtName;
	@ViewInjectAnatation(viewID = R.id.group_details_description_textView)
	private TextView mTxtDescription;
	@ViewInjectAnatation(viewID = R.id.group_details_type_textView)
	private TextView mTxtType;
	@ViewInjectAnatation(viewID = R.id.group_details_members_listView)
	private ListView mLstMembersList;
	@ViewInjectAnatation(viewID = R.id.group_details_page_button)
	private Button mBtnPageButton;

	
	private AlertFragmentDialogYesNO mDialog;
	private String mContactWhere;
	private Recipient mRecipient;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_details);
		Injector.doInjection(this);
		
		// set type and name
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mRecipient = (Recipient) bundle.getSerializable(BundleKey.recipient.name()); 
			mTxtName.setText(mRecipient.getName());
			mTxtType.setText(mRecipient.getGroupType());
		}
		
		mTxtDescription.setVisibility(View.GONE);
				
		getSupportLoaderManager().initLoader(LoaderID.LOCAL_GROUP_CORRESPONDENCE_LOADER.ordinal(), null, this);						
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.group_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.item_delete:
				showDeleteDialog();
			break;
			case R.id.item_edit:
				editGroup();
				break;
		}
		return true;
	}
	
	private void editGroup() {
		Intent intent = new Intent(this, LocalGroupEditActivity.class);
		intent.putExtra(PagingGroupTable._id.name(), mRecipient.getIdDb());
		intent.putExtra(PagingGroupTable.id.name(), mRecipient.getId());
		intent.putExtra(PagingGroupTable.name.name(), mRecipient.getName());
		intent.putExtra(PagingGroupTable.type.name(), mRecipient.getGroupType());
		startActivityForResult(intent, RequestID.EDIT_LOCAL_GROUP.ordinal());
		
	}

	private void showDeleteDialog() {
		mDialog = AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.remove_local_group), 
				getString(R.string.this_local_group_will_be_removed), "",
				true, new OnDialogDoneListener() {
					@Override
					public void onDialogNo(FragmentDialogTag tag, String data) {
						mDialog.dismiss();
					}

					@Override
					public void onDialogDone(FragmentDialogTag tag, String data) {
						DeleteLocalGroupTask deleteTask = new DeleteLocalGroupTask();
						deleteTask.execute();
						mDialog.dismiss();
					}
				});
		mDialog.show(getSupportFragmentManager(), FragmentDialogTag.AlertInforDialogYesNo.name());
	}
	
	// BUTTON LISTENERS
	@ClickListenerInjectAnatation(viewID = R.id.group_details_page_button)
	public void onPageToGroupClick(View view) {
		Intent intent = new Intent(this, NewMessageActivity.class);
		intent.putExtra(BundleKey.recipient.name(), mRecipient);
		startActivity(intent);
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {

	}

	// --------------------------------------------------------
	class DeleteLocalGroupTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Local Group Deletion");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Uri correspondenceUri = SmartPagerContentProvider.CONTENT_LOCAL_GROUP_CORRESPONDENCE_URI;
			Uri groupsUri = SmartPagerContentProvider.CONTENT_PAGINGROUP_URI;
			
			String whereCorrespondence = LocalGroupCorrespondenceTable.localGroupId.name() + " = " + mRecipient.getIdDb();
			String whereGroup = PagingGroupTable._id.name() + " = " + mRecipient.getIdDb();
			
			getContentResolver().delete(correspondenceUri, whereCorrespondence, null);
			getContentResolver().delete(groupsUri, whereGroup, null);
			
			getContentResolver().notifyChange(groupsUri, null);
			getContentResolver().notifyChange(correspondenceUri, null);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			hideProgressDialog();
			finish();
			super.onPostExecute(result);
		}
	}
	// --------------------------------------------------------------------

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		Loader<Cursor> loader = null;
		switch (LoaderID.values()[id]) {
			case LOCAL_GROUP_CORRESPONDENCE_LOADER:
				String where = LocalGroupCorrespondenceTable.localGroupId.name() + " = " + mRecipient.getIdDb();
				loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_LOCAL_GROUP_CORRESPONDENCE_URI, 
						null, where, null, null);
			break;
			case CONTACT_LOADER:
				loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_CONTACT_URI, 
						null, mContactWhere, null, null);
			break;
			default:
			break;
		}
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId(); 
		switch (LoaderID.values()[id]) {
			case LOCAL_GROUP_CORRESPONDENCE_LOADER:
				mContactWhere = generateContactSelection(cursor);
				getSupportLoaderManager().initLoader(LoaderID.CONTACT_LOADER.ordinal(), null, this);
			break;
			case CONTACT_LOADER:
				fillMembersList(cursor);
			break;
			default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId(); 
		switch (LoaderID.values()[id]) {
			case LOCAL_GROUP_CORRESPONDENCE_LOADER:
			break;
			case CONTACT_LOADER:
			break;
			default:
			break;
		}	
	}
	
	private void fillMembersList(Cursor cursor) {
		
		List<IHolderSource> holderSources = new ArrayList<IHolderSource>();
		if (cursor.moveToFirst()) {
			do {
				String statusFromTable = cursor.getString(ContactTable.status.ordinal());
//				String status = statusFromTable.equalsIgnoreCase(ONLINE) ? STATUS_ON_CALL : STATUS_OFFLINE;
				String firstName = cursor.getString(ContactTable.firstName.ordinal()); 
				String lastName = cursor.getString(ContactTable.lastName.ordinal()); 
				String name = firstName + " " + lastName;
				String[] data = {STATUS_OFFLINE, name};
				holderSources.add(Department.createFromStringArray(data));
			} while (cursor.moveToNext());
		}
		
		BaseHolderAdapter membersAdapter = new BaseHolderAdapter(this, holderSources, GroupMemberHolder.class);		
		mLstMembersList.setAdapter(membersAdapter);
		mLstMembersList.setEnabled(false);	// to avoid click on the items
		membersAdapter.notifyDataSetChanged();
	}

	private String generateContactSelection(Cursor cursor) {
		
		StringBuilder builder = new StringBuilder(" ");
		builder.append(ContactTable.id.name()).append(" IN ");
		builder.append(" ( ");
		boolean isFirst = true;
		
		if (cursor.moveToFirst()) {
			do {
				if (!isFirst) {
					builder.append(" , ");
				}
				String contactId = cursor.getString(LocalGroupCorrespondenceTable.contactId.ordinal()); 
				builder.append(contactId);						
				isFirst = false;
			} while (cursor.moveToNext());
		}
		builder.append(" ) ");
		return builder.toString();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RequestID.EDIT_LOCAL_GROUP.ordinal() && resultCode == RESULT_OK){
			finish();
		}
		
	}
	
	@Override
	protected boolean isUpNavigationEnabled() {
		return true;
	}
	
}
