package net.smartpager.android.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.database.ContactGroupTable;
import net.smartpager.android.database.CursorTable;
import net.smartpager.android.holder.ContactHolder;
import net.smartpager.android.holder.GroupHolder;
import net.smartpager.android.model.ListSerilizableContainer;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.utils.RecipientsGroupsComparator;
import net.smartpager.android.web.response.AbstractResponse;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

public class RecipientListActivity extends BaseActivity implements 	LoaderCallbacks<Cursor>, 
																	ContactHolder.OnRecipientCheckedChangeListener {

	protected CursorTable mCursorTable;
	protected ListView mList;

	private BaseHolderAdapter mAdapter;
	private List<IHolderSource> mRecipientList;
	private List<IHolderSource> mSelectedRecipients;
	private HashSet<String> mSelectedSet;
	private HashSet<String> mDisabledSet;
	private boolean mOnlyContactsFilter = false;
	private SearchView mSearchView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_contact_list);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey(BundleKey.onlyContacts.name())) {
			mOnlyContactsFilter = bundle.getBoolean(BundleKey.onlyContacts.name());
		}

		mCursorTable = new ContactGroupTable(mOnlyContactsFilter);

		mList = (ListView) findViewById(R.id.contact_listView);
		createAdapter();

		mSelectedSet = new HashSet<String>();
		mDisabledSet = new HashSet<String>();

		mSelectedRecipients = (((ListSerilizableContainer) getIntent().getSerializableExtra(BundleKey.recipient.name()))
				.getList());
		for (IHolderSource item : mSelectedRecipients) {
			Recipient recipient = (Recipient) item;

			mSelectedSet.add(recipient.getUniqueKey());

			if (recipient.isDisabled()) {
				mDisabledSet.add(recipient.getUniqueKey());
			}
		}
		getSupportLoaderManager().initLoader(LoaderID.CONTACT_LOADER.ordinal(), null, this);
	}

	// search menu -----------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.recipient_list, menu);
		mSearchView = (SearchView) menu.findItem(R.id.item_search).getActionView();
		mSearchView.setOnQueryTextListener(onQueryListener);
		mSearchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		int id = mSearchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		TextView mSearchViewText = (TextView) mSearchView.findViewById(id);
		mSearchViewText.setTextColor(Color.WHITE);
		mSearchViewText.setHint("");
		int closeButtonId = mSearchView.getContext().getResources().getIdentifier("android:id/search_close_btn", null, null);
		ImageView closeButton = (ImageView) mSearchView.findViewById(closeButtonId);
		closeButton.setImageResource(R.drawable.action_close);

		int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
		ImageView v = (ImageView) mSearchView.findViewById(searchImgId);
		v.setImageResource(R.drawable.action_search);

		return true;
	}

	OnQueryTextListener onQueryListener = new OnQueryTextListener() {

		@Override
		public boolean onQueryTextSubmit(String query) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(RecipientListActivity.this.getCurrentFocus().getWindowToken(), 0);
			return false;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			filter(newText);
			return false;
		}
	};

	private void filter(String filterString) {
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.filter.name(), filterString);
		getSupportLoaderManager().restartLoader(LoaderID.CONTACT_LOADER.ordinal(), bundle, this);
	}

	// -----------------------------------------------------------------------

	protected void createAdapter() {
		List<Class<? extends IHolder>> holderClasses = new ArrayList<Class<? extends IHolder>>();
		holderClasses.add(ContactHolder.class);
		holderClasses.add(GroupHolder.class);
		mRecipientList = new ArrayList<IHolderSource>();
		mAdapter = new BaseHolderAdapter(this, mRecipientList, holderClasses, this);
		mList.setAdapter(mAdapter);
	}

	@Override
	public void onBackPressed() {

		if (mSearchView != null && !mSearchView.isIconified()) {
			mSearchView.onActionViewCollapsed();
			mSearchView.setQuery("", true);
		} else {
			Recipient recipient = null;
			mSelectedRecipients.clear();
			for (IHolderSource item : mRecipientList) {
				recipient = (Recipient) item;
				if (recipient.isSelected()) {
					mSelectedRecipients.add(recipient);
				}
			}
			Intent intent = new Intent();
			intent.putExtra(BundleKey.recipient.name(), new ListSerilizableContainer(mSelectedRecipients));

			setResult(RESULT_OK, intent);
			finish();
		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
		}
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (bundle != null && bundle.containsKey(BundleKey.filter.name())) {
			return mCursorTable.getFilterLoader(bundle.getString(BundleKey.filter.name()));
		}
		return mCursorTable.getLoader();
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		List<IHolderSource> holderSources = new ArrayList<IHolderSource>();
        holderSources.addAll(getContactsList(cursor, true));
        holderSources.addAll(getContactsList(cursor, false));
//		if (cursor.moveToFirst()) {
//			do {
//				Recipient recipient = Recipient.createFromCursor(cursor);
//				Recipient recipient = Recipient.createFromDepartmentCursor(cursor);
//				recipient.setSelected(mSelectedSet.contains(recipient.getUniqueKey()));
//				recipient.setDisabled(mDisabledSet.contains(recipient.getUniqueKey()));
//				holderSources.add(recipient);
//			} while (cursor.moveToNext());
//		}
		mAdapter.updateSours(holderSources);
	}

    private List<IHolderSource>  getContactsList (Cursor cursor, boolean isGruop)
    {
        List<IHolderSource> res = new ArrayList<IHolderSource>();
        if (cursor.moveToFirst()) {
            do {
                Recipient recipient = Recipient.createFromDepartmentCursor(cursor);
                
                // Don't add currently logged in user as a contact
				if(recipient.getSmartPagerId().equalsIgnoreCase(SmartPagerApplication.getInstance().getPreferences().getSmartPagerID()))
                	continue;
                
                if(mSelectedSet != null && mSelectedSet.size() > 0 && mSelectedSet.contains(recipient.getUniqueKey()))
                    recipient.setSelected(true);
                if((isGruop && !recipient.isGroup()) || (!isGruop && recipient.isGroup())) {
                	Collections.sort(res, new RecipientsGroupsComparator());
                	continue;
                }
                String firstChar = String.valueOf(recipient.getName().charAt(0)).toUpperCase();
                res.add(recipient);
            } while (cursor.moveToNext());
        }
        
        return res;
    }

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {

	}

	@Override
	public void onRecipientCheckedChange(Recipient recipient) {
		String key = recipient.getUniqueKey();
		if ( mSelectedSet.contains(key) && !recipient.isSelected()){
			mSelectedSet.remove(key);
		}else if ( !mSelectedSet.contains(key) && recipient.isSelected()){
			mSelectedSet.add(key);
		}	
	}

}
