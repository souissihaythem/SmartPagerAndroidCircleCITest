package net.smartpager.android.activity;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.rey.material.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.RequestID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.database.DatabaseHelper.LocalGroupCorrespondenceTable;
import net.smartpager.android.database.DatabaseHelper.PagingGroupTable;
import net.smartpager.android.holder.MessageRecipientHolder;
import net.smartpager.android.model.ImageSource;
import net.smartpager.android.model.ListSerilizableContainer;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.web.response.AbstractResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class LocalGroupEditActivity extends BaseActivity implements
		MessageRecipientHolder.OnDeleteRecipientClickListener, LoaderCallbacks<Cursor> {
	
	private List<IHolderSource> mRecipients;
	private BaseHolderAdapter mRecipientsAdapter;
	
	private HashSet<String> mGroupNames;

	@ViewInjectAnatation(viewID = R.id.group_details_name_editText)
	private EditText mEdtName;
	@ViewInjectAnatation(viewID = R.id.new_group_members_listView)
	private ListView mLstRecipientsList;
	@ViewInjectAnatation(viewID = R.id.new_group_create_button)
	private Button mBtnSave;

	private String m_id;
	private String mName;
	private String mContactWhere;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_local_group);
		Injector.doInjection(this);
		
		mBtnSave.setText(R.string.save_changes);
		
		// set type and name
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			m_id = bundle.getString(PagingGroupTable._id.name());
			mName = bundle.getString(PagingGroupTable.name.name());
			mEdtName.setText(mName);
		}
		
		mGroupNames = new HashSet<String>();

		
		getSupportLoaderManager().initLoader(LoaderID.LOCAL_GROUP_CORRESPONDENCE_LOADER.ordinal(), null, this);
		getSupportLoaderManager().initLoader(LoaderID.GROUP_LOADER.ordinal(), null, this);

		mRecipients = new ArrayList<IHolderSource>();
		List<Class<? extends IHolder>> holderClasses = new ArrayList<Class<? extends IHolder>>();
		holderClasses.add(MessageRecipientHolder.class);
		mRecipientsAdapter = new BaseHolderAdapter(this, mRecipients, holderClasses, new Object[] { this });
		mLstRecipientsList.setAdapter(mRecipientsAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.chat_add_contact:
				addMember();
			break;
			default:
				return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (RequestID.values()[requestCode]) {
			case ADD_RECIPIENT:
				if (resultCode == RESULT_OK && data.getExtras().containsKey(BundleKey.recipient.name())) {
					mRecipients.clear();
					ListSerilizableContainer listSerilizableContainer = (((ListSerilizableContainer) data
							.getSerializableExtra(BundleKey.recipient.name())));
					mRecipients.addAll(listSerilizableContainer.getList());
					mRecipientsAdapter.notifyDataSetChanged();
				}
			break;
			default:
			break;
		}
	}

	@ClickListenerInjectAnatation(viewID = R.id.new_group_create_button)
	public void onGroupSave(View view) {
		if (TextUtils.isEmpty(mEdtName.getText().toString()) || mRecipients.size() == 0) {
			showErrorDialog(getString(R.string.group_name_and_contacts_list_shouldn_t_be_empty));
		} else if (mGroupNames.contains(mEdtName.getText().toString())) {
			showErrorDialog(getString(R.string.group_the_same_name));
		} else {
			new EditLocalGroupTask().execute();			
		}

	}

	public void addMember() {
		Intent intent = new Intent(this, RecipientListActivity.class);
		ListSerilizableContainer listSerilizableContainer = new ListSerilizableContainer(mRecipients);
		intent.putExtra(BundleKey.recipient.name(), listSerilizableContainer);
		intent.putExtra(BundleKey.onlyContacts.name(), true);
		startActivityForResult(intent, RequestID.ADD_RECIPIENT.ordinal());
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {}

	@Override
	public void onDeleteRecipientClick(Recipient deleteRecipient) {
		mRecipients.remove(deleteRecipient);
		mRecipientsAdapter.notifyDataSetChanged();
	}

	
	
	@Override
	public void onDeleteImageClick(ImageSource imageSource) {
		
	}

	// --------------------------------------------------------
	class EditLocalGroupTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Save Local Group...");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			// update group name
			ContentValues values = new ContentValues();			
			values.put(PagingGroupTable.name.name(), mEdtName.getText().toString());			
			Uri groupsUri = SmartPagerContentProvider.CONTENT_PAGINGROUP_URI;
			getContentResolver().update(groupsUri, values, "_id = " + m_id, null);
			// delete all old relations
			Uri correspondenceUri = SmartPagerContentProvider.CONTENT_LOCAL_GROUP_CORRESPONDENCE_URI;
			String where = LocalGroupCorrespondenceTable.localGroupId.name() + " = " + m_id;			
			getContentResolver().delete(correspondenceUri, where, null);
			// fill LocalGroupCorrespondence Table
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();			
			for (IHolderSource item : mRecipients) {
				Recipient recipient = (Recipient) item;
				Builder builder = ContentProviderOperation.newInsert(correspondenceUri);
				builder.withValue(LocalGroupCorrespondenceTable.localGroupId.name(), m_id);
				builder.withValue(LocalGroupCorrespondenceTable.contactId.name(), recipient.getId());
				operations.add(builder.build());
			}
			try {
				getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, operations);
				getContentResolver().notifyChange(correspondenceUri, null);
				getContentResolver().notifyChange(groupsUri, null);
			} catch (RemoteException e) {

			} catch (OperationApplicationException e) {

			}

			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			hideProgressDialog();			
			LocalGroupEditActivity.this.setResult(RESULT_OK);			
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
				String where = LocalGroupCorrespondenceTable.localGroupId.name() + " = " + m_id;
				loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_LOCAL_GROUP_CORRESPONDENCE_URI, null,
						where, null, null);
			break;
			case RECIPIENTS_LOADER:
				loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_CONTACT_URI, null, mContactWhere,
						null, null);
			break;
			case GROUP_LOADER:
				loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_PAGINGROUP_URI, null,
						null, null, null);
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
				getSupportLoaderManager().initLoader(LoaderID.RECIPIENTS_LOADER.ordinal(), null, this);
			break;
			case RECIPIENTS_LOADER:
				fillMembersList(cursor);
			break;
			case GROUP_LOADER:
				fillGroupNamesSet(cursor);
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
			case RECIPIENTS_LOADER:
			break;
			case GROUP_LOADER:
			break;
			default:
			break;
		}
	}
	
	private void fillGroupNamesSet(Cursor cursor) {
		
		mGroupNames.clear();
		if (cursor.moveToFirst()) {
			do {
				mGroupNames.add(cursor.getString(PagingGroupTable.name.ordinal()));
			} while (cursor.moveToNext());
		}
		// exclude the current group name from the set
		mGroupNames.remove(mEdtName.getText().toString());
	}

	private void fillMembersList(Cursor cursor) {
		mRecipients.clear();
		if (cursor.moveToFirst()) {
			do {
				mRecipients.add(Recipient.createFromContactCursor(cursor));
			} while (cursor.moveToNext());
		}
		mRecipientsAdapter.notifyDataSetChanged();
	}

	private String generateContactSelection(Cursor cursor) {
		StringBuilder builder = new StringBuilder(" ");
		builder.append(" id IN  ");
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



}
