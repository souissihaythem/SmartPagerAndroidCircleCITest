package net.smartpager.android.activity.settings;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Switch;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.ForwardType;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.database.ContactGroupTable;
import net.smartpager.android.database.CursorTable;
import net.smartpager.android.holder.ContactForwardHolder;
import net.smartpager.android.holder.GroupForwardHolder;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.web.response.AbstractResponse;

import java.util.ArrayList;
import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class ForwardPagesActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

	protected CursorTable mCursorTable;
	protected ListView mList;
	
	private SearchView mSearchView;
	private BaseHolderAdapter mAdapter;
	private List<IHolderSource> mRecipientList;
	private boolean mOnlyContactsFilter = false;
	private boolean mWithoutLocalGroupsFilter = true;
	@ViewInjectAnatation(viewID = R.id.forward_switch)
	private Switch mEnableForvard;
	@ViewInjectAnatation(viewID = R.id.forward_to_textView)
	private TextView mForwardTo;
	
	@Override
	protected boolean isUpNavigationEnabled() {

		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forward_pages);
		Injector.doInjection(this);
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey(BundleKey.onlyContacts.name())){	
			mOnlyContactsFilter = bundle.getBoolean(BundleKey.onlyContacts.name());
		}
		
		if (!TextUtils.isEmpty(SmartPagerApplication.getInstance().getPreferences().getForwardName())){
			mForwardTo.setVisibility(View.VISIBLE);
			mForwardTo.setText( getString(R.string.forward_to_) + SmartPagerApplication.getInstance().getPreferences().getForwardName());
		} else {
			mForwardTo.setVisibility(View.GONE);
		}
		mCursorTable = new ContactGroupTable(mOnlyContactsFilter, mWithoutLocalGroupsFilter);
		
		mList = (ListView) findViewById(R.id.contact_listView);
		mList.setOnItemClickListener(mOnListViewItemClickListener);
		createAdapter();
		getSupportLoaderManager().initLoader(LoaderID.CONTACT_LOADER.ordinal(), null, this);
		// switch
		mEnableForvard.setChecked( SmartPagerApplication.getInstance().getPreferences().getForwardPagesEnabled());
		mEnableForvard.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!NetworkUtils.isInternetConnectedAsync()){
					showErrorDialog(getString(R.string.connection_is_required));		
					// set old position
					mEnableForvard.post( new Runnable() {						
						@Override
						public void run() {
							mEnableForvard.setChecked( SmartPagerApplication.getInstance().getPreferences().getForwardPagesEnabled());
						}
					});
				} else {
					showProgressDialog(getString(R.string.saving));
					SmartPagerApplication.getInstance().getPreferences().setForwardPagesEnabled(isChecked);
					startSetForward();
				}
			}
		});
	}

	private OnItemClickListener mOnListViewItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id){
			if (!NetworkUtils.isInternetConnectedAsync()){
				showErrorDialog(getString(R.string.connection_is_required));
				return;
			}
			BaseHolderAdapter adapter = (BaseHolderAdapter) parent.getAdapter();
			Recipient recipient =  (Recipient) adapter.getItem(position);
			
			Bundle  bundle = new Bundle();
			bundle.putString(BundleKey.recipientID.name(), recipient.getId());
			adapter.setPreSetDataParams(bundle);
			adapter.notifyDataSetChanged();
			
			if (recipient.isGroup()){
				SmartPagerApplication.getInstance().getPreferences().setForwardType(ForwardType.PagingGroup.name());
				SmartPagerApplication.getInstance().getPreferences().setForwardGroupID(recipient.getId());
			}else{
				SmartPagerApplication.getInstance().getPreferences().setForwardType(ForwardType.User.name());
				SmartPagerApplication.getInstance().getPreferences().setForwardContactID(recipient.getId());
			}
			SmartPagerApplication.getInstance().getPreferences().setForwardName(recipient.getName());
			
			mForwardTo.setVisibility(View.VISIBLE);
			mForwardTo.setText( getString(R.string.forward_to_) + recipient.getName());	
		}
	};
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
			imm.hideSoftInputFromWindow(ForwardPagesActivity.this.getCurrentFocus().getWindowToken(), 0);
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
		holderClasses.add(ContactForwardHolder.class);
		holderClasses.add(GroupForwardHolder.class);
		mRecipientList = new ArrayList<IHolderSource>();
//		mSelectedRecipients = new ArrayList<IHolderSource>();
		mAdapter = new BaseHolderAdapter(this, mRecipientList, holderClasses);
		mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mList.setAdapter(mAdapter);
		// pre-set checked user
		String id = "";
		if ( SmartPagerApplication.getInstance().getPreferences().getForwardType().equalsIgnoreCase( ForwardType.PagingGroup.name() )){
			id = SmartPagerApplication.getInstance().getPreferences().getForwardGroupID();
		}else if ( SmartPagerApplication.getInstance().getPreferences().getForwardType().equalsIgnoreCase( ForwardType.User.name() )){
			id = SmartPagerApplication.getInstance().getPreferences().getForwardContactID();
		}
		if (!TextUtils.isEmpty(id)){
			Bundle  bundle = new Bundle();
			bundle.putString(BundleKey.recipientID.name(), id);
			mAdapter.setPreSetDataParams(bundle);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onBackPressed() {
		if (mSearchView != null && !mSearchView.isIconified()){
			mSearchView.onActionViewCollapsed();			
			mSearchView.setQuery("", true);
		} else {
			startSetForward();
			super.onBackPressed();
		}
		
	}

	private void startSetForward() {
        sendSingleComand(WebAction.setForward);
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
		if (cursor.moveToFirst()) {
			do {
//				Recipient recipient = Recipient.createFromCursor(cursor);
				Recipient recipient = Recipient.createFromDepartmentCursor(cursor);
				holderSources.add(recipient);
			} while (cursor.moveToNext());
		}
		mAdapter.updateSours(holderSources);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.setForward){
			hideProgressDialog();
		}
	}
	
	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse response) {
		hideProgressDialog();
		super.onErrorResponse(action, response);
	}


}
