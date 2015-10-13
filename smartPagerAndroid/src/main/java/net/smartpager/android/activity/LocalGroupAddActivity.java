package net.smartpager.android.activity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
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
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class LocalGroupAddActivity extends BaseActivity implements LoaderCallbacks<Cursor>,
		MessageRecipientHolder.OnDeleteRecipientClickListener {

	private List<IHolderSource> mRecipients;
	private BaseHolderAdapter mRecipientsAdapter;

	private HashSet<String> mGroupNames;

	@ViewInjectAnatation(viewID = R.id.group_details_name_editText)
	private EditText mName;
	@ViewInjectAnatation(viewID = R.id.new_group_members_listView)
	private ListView mRecipientsListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_local_group);
		Injector.doInjection(this);

		mGroupNames = new HashSet<String>();
		getSupportLoaderManager().initLoader(LoaderID.GROUP_LOADER.ordinal(), null, this);

		mRecipients = new ArrayList<IHolderSource>();
		List<Class<? extends IHolder>> holderClasses = new ArrayList<Class<? extends IHolder>>();
		holderClasses.add(MessageRecipientHolder.class);
		mRecipientsAdapter = new BaseHolderAdapter(this, mRecipients, holderClasses, new Object[] { this });

		mRecipientsListView.setAdapter(mRecipientsAdapter);
		addMember();
	}

	@Override
	public void onResume() {
		super.onResume();

		TimerTask tt = new TimerTask() {

			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(mName, InputMethodManager.SHOW_IMPLICIT);
			}
		};

		final Timer timer = new Timer();
		timer.schedule(tt, 200);
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
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				}
			break;
			default:
			break;
		}


	}

	@ClickListenerInjectAnatation(viewID = R.id.new_group_create_button)
	public void onGroupCreate(View view) {
		if (TextUtils.isEmpty(mName.getText().toString()) || mRecipients.size() == 0) {
			showErrorDialog(getString(R.string.group_name_is_empty));
		} else if (mGroupNames.contains(mName.getText().toString())) {
			showErrorDialog(getString(R.string.group_the_same_name));
		} else {
			AddLocalGroupTask task = new AddLocalGroupTask();
			task.execute();
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
	class AddLocalGroupTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Local Group Addition");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			
			// Generate random number for the Local Group ID
			int min = 10000;
			int max = 20000;
			Random r = new Random();
			int localGroupID = r.nextInt(max - min) + min;
			
			ContentValues values = new ContentValues();
			values.put(PagingGroupTable.id.name(), localGroupID);
			values.put(PagingGroupTable.name.name(), mName.getText().toString());
			values.put(PagingGroupTable.type.name(), LocalGroupDetailsActivity.LOCAL);
			Uri groupsUri = SmartPagerContentProvider.CONTENT_PAGINGROUP_URI;
			Uri newGroupUri = getContentResolver().insert(groupsUri, values);

			if (newGroupUri != null) {
				getContentResolver().notifyChange(groupsUri, null);
				String newId = newGroupUri.getLastPathSegment();
				// fill LocalGroupCorrespondence Table
				ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
				Uri correspondenceUri = SmartPagerContentProvider.CONTENT_LOCAL_GROUP_CORRESPONDENCE_URI;

				for (IHolderSource item : mRecipients) {
					Recipient recipient = (Recipient) item;
					Builder builder = ContentProviderOperation.newInsert(correspondenceUri);
					builder.withValue(LocalGroupCorrespondenceTable.localGroupId.name(), newId);
					builder.withValue(LocalGroupCorrespondenceTable.contactId.name(), recipient.getId());
					operations.add(builder.build());
				}
				try {
					getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, operations);
					getContentResolver().notifyChange(correspondenceUri, null);
				} catch (RemoteException e) {

				} catch (OperationApplicationException e) {

				}
				return newId;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			hideProgressDialog();
			if (TextUtils.isEmpty(result)) {
				Context context = SmartPagerApplication.getInstance().getApplicationContext();
				showErrorDialog(context.getResources().getString(R.string.error_creating_local_group));
			} else {
				finish();
			}
			super.onPostExecute(result);
		}
	}

	// --------------------------------------------------------------------

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

		Loader<Cursor> loader = null;
		switch (LoaderID.values()[id]) {
			case GROUP_LOADER:
				loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_PAGINGROUP_URI, null, null, null,
						null);
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
			case GROUP_LOADER:
				fillGroupNamesSet(cursor);
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
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (LoaderID.values()[id]) {
			case GROUP_LOADER:
			break;
			default:
			break;
		}
	}
	
	@Override
	protected boolean isUpNavigationEnabled() {
		return true;
	}

}
