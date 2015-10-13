package net.smartpager.android.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.activity.ContactDetailsActivity;
import net.smartpager.android.activity.LocalGroupDetailsActivity;
import net.smartpager.android.activity.MainActivity;
import net.smartpager.android.activity.NewMessageActivity;
import net.smartpager.android.adapters.IndexableBaseHolderAdapter;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.GroupDetails;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ContactGroupTable;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.dialer.VoipPhone;
import net.smartpager.android.database.CursorTable;
import net.smartpager.android.fragment.interfaces.ContactsListButtonsListener;
import net.smartpager.android.holder.ContactsListAlphabeticalHolder;
import net.smartpager.android.holder.ContactsListContactHolder;
import net.smartpager.android.holder.ContactsListGroupHolder;
import net.smartpager.android.model.AlphabeticalCaption;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.receivers.OutgoingCallReceiver;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.RecipientsGroupsComparator;
import net.smartpager.android.utils.TelephoneUtils;
import net.smartpager.android.view.indexable.IndexableListView;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

import org.apache.http.Header;
import org.json.JSONObject;

// SHow list of contacts
public class ContactIndexableListFragment extends AbstractFragmentList {
	private static final boolean ALPHABETICAL_PANEL_SHOW_ALWAYS = true;
	private static final boolean ALPHABETICAL_PANEL_ENABLED = true;

	private List<IHolderSource> mRecipientList;
	private IndexableBaseHolderAdapter mAdapter;

	private AsyncHttpClient httpClient;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		httpClient = new AsyncHttpClient();
		
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	protected BaseAdapter createAdapter() {
		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				BaseHolderAdapter adapter = (BaseHolderAdapter) parent.getAdapter();
				if (adapter.getItem(position) instanceof Recipient) {
					Recipient recipient = (Recipient) adapter.getItem(position);
					if (!recipient.isGroup()) {
						startContactDetails(recipient);
					} else {
						startGroupDetails(recipient);
					}
				}
			}
		});

		List<Class<? extends IHolder>> holderClasses = new ArrayList<Class<? extends IHolder>>();
		holderClasses.add(ContactsListContactHolder.class);
		holderClasses.add(ContactsListGroupHolder.class);
		holderClasses.add(ContactsListAlphabeticalHolder.class);
		mRecipientList = new ArrayList<IHolderSource>();
		mAdapter = new IndexableBaseHolderAdapter(getActivity(), mRecipientList, holderClasses, mButtonsListener);
		return mAdapter;
	}

	private void startContactDetails(Recipient recipient) {

		String id = recipient.getId();
		String smartPagerId = recipient.getSmartPagerId();

		Intent intent = new Intent(SmartPagerApplication.getInstance().getApplicationContext(),
				ContactDetailsActivity.class);
		intent.putExtra(GroupContactItem.id.name(), id);
		intent.putExtra(GroupContactItem.contact_smart_pager_id_or_group_id.name(), smartPagerId);
		startActivity(intent);
	}

	private void startGroupDetails(Recipient recipient) {

		String groupType = recipient.getGroupType();

		if (groupType.equalsIgnoreCase(LocalGroupDetailsActivity.LOCAL)) {
			startLocalGroupDetails(recipient);
		} else {
			startGroupDetailsService(recipient);
		}
	}

	private void startLocalGroupDetails(Recipient recipient) {

		Intent intent = new Intent(getActivity(), LocalGroupDetailsActivity.class);
		intent.putExtra(BundleKey.recipient.name(), recipient);
		getActivity().startActivity(intent);
	}

	private void startGroupDetailsService(Recipient recipient) {

		String id = recipient.getId();
		String smartPagerId = recipient.getSmartPagerId();
		String name = recipient.getName();

		((BaseActivity) getActivity()).showProgressDialog(getActivity().getString(R.string.processing));
		Bundle extras = new Bundle();
		extras.putString(WebSendParam.groupId.name(), id);
		extras.putString(GroupDetails.name.name(), name);
		extras.putString(GroupContactItem.contact_smart_pager_id_or_group_id.name(), smartPagerId);
		SmartPagerApplication.getInstance().startWebAction(WebAction.getPagingGroupDetails, extras);
	}

	public String getActionName(String actionName) {
		return SmartPagerApplication.getInstance().getPackageName() + "." + actionName;
	}

	@Override
	protected CursorTable createTable() {
		return new ContactGroupTable();
	}

	@Override
	protected int getFragmentLayout() {
		return (ALPHABETICAL_PANEL_ENABLED) ? R.layout.fragment_contact_indexable_list : R.layout.fragment_contact_list;
	}

	@Override
	protected int getListViewID() {
		return (ALPHABETICAL_PANEL_ENABLED) ? R.id.contact_indexable_listView : R.id.contact_listView;
	}

	@Override
	protected void initView(View rootView) {

	}

	@Override
	protected int getLoaderID() {

		return LoaderID.CONTACT_LOADER.ordinal();
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		List<IHolderSource> holderSources = new ArrayList<IHolderSource>();
		LinkedHashMap<String, List<IHolderSource>> tableData = getIndexableContactsList(cursor);
		HashMap<String, Integer> sectionIndex = new HashMap<String, Integer>();

		int index = 0;
		for (LinkedHashMap.Entry<String, List<IHolderSource>> entry : tableData.entrySet()) {
			holderSources.addAll(entry.getValue());

			if (!entry.getValue().isEmpty()) {
				AlphabeticalCaption header = new AlphabeticalCaption(entry.getKey());
				holderSources.add(index, header);
				// Increment index to account for section header
				index++;

				sectionIndex.put(entry.getKey(), index);
				index += entry.getValue().size();
			}
		}

		mList.setFastScrollAlwaysVisible(ALPHABETICAL_PANEL_SHOW_ALWAYS);
		mList.setFastScrollEnabled(ALPHABETICAL_PANEL_ENABLED);
		mList.setVerticalScrollBarEnabled(false);

		if (ALPHABETICAL_PANEL_ENABLED) {
			mAdapter.setPositionMap(sectionIndex);
			((IndexableListView) mList).setSearchView(((MainActivity) getActivity()).getSearchView());
		}
		mAdapter.updateSours(holderSources);
	}

	private LinkedHashMap<String, List<IHolderSource>> getIndexableContactsList(Cursor cursor) {
		String[] sectionTitles = { "Favorites", "Groups", "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K",
				"L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
		LinkedHashMap<String, List<IHolderSource>> tableDataMap = new LinkedHashMap<String, List<IHolderSource>>();
		for (String title : sectionTitles) {
			tableDataMap.put(title, new ArrayList<IHolderSource>());
		}

		if (cursor.moveToFirst()) {
			do {
				Recipient recipient = Recipient.createFromDepartmentCursor(cursor);
				// Don't add currently logged in user as a contact
				if(recipient.getSmartPagerId().equalsIgnoreCase(SmartPagerApplication.getInstance().getPreferences().getSmartPagerID()))
                	continue;

				if (recipient.isGroup()) {
					tableDataMap.get("Groups").add(recipient);
				} else {
					// trim() to account for any last names that have a leading space
					String firstChar = recipient.getLastName().trim().substring(0, 1).toUpperCase();
					if (Arrays.asList(sectionTitles).contains(firstChar)) {
						tableDataMap.get(firstChar).add(recipient);
					}
				}

				if (SmartPagerApplication.getInstance().isFavourite(getActivity().getApplicationContext(),
						recipient.getSmartPagerId())) {
					tableDataMap.get("Favorites").add(recipient);
				}

				if (recipient.getGroupType().equalsIgnoreCase("LOCAL")) {
					tableDataMap.get("Favorites").add(recipient);
				}

			} while (cursor.moveToNext());
		}

		// Only sort Favorites and Groups, the rest of the sections appear to sort properly.
		Collections.sort(tableDataMap.get("Favorites"), new RecipientsGroupsComparator());
		Collections.sort(tableDataMap.get("Groups"), new RecipientsGroupsComparator());
		
		return tableDataMap;
	}

	ContactsListButtonsListener mButtonsListener = new ContactsListButtonsListener() {
		@Override
		public void onPageButtonClick(Recipient recipient) {
			Intent intent = new Intent(getActivity(), NewMessageActivity.class);
			intent.putExtra(BundleKey.recipient.name(), recipient);
			startActivity(intent);
		}

		@Override
		public void onCallButtonClick(String phoneNumber) {
			if (SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled()) {
				makeVoipCall(phoneNumber);
				return;
			}
			
			if (SmartPagerApplication.getInstance().getPreferences().getShowMobileNumbers() == false) {
				startArmCallback(phoneNumber);
				return;
			}
			
			TelephoneUtils.dial(phoneNumber);
		}
	};
	
	public void makeVoipCall(final String number) {
		AudioManagerUtils.setLoudSpeaker(false);		
		((BaseActivity) getActivity()).showProgressDialog("Connecting VOIP Call");
    	RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        
        AsyncHttpClient httpClient = new AsyncHttpClient();        
        httpClient.post(getActivity().getApplicationContext(), Constants.BASE_REST_URL + "/getTwilioClientToken", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				((BaseActivity) getActivity()).hideProgressDialog();
				
				VoipPhone voipPhone = new VoipPhone(((BaseActivity) getActivity()).getApplicationContext(), response.optString("token"));
				voipPhone.connect(number);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				((BaseActivity) getActivity()).hideProgressDialog();
			}
        	
        });
    }
	
	public void startArmCallback(final String number) {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.phoneNumber.name(), number);
//        SmartPagerApplication.getInstance().startWebAction(WebAction.armCallback, extras);
        ((BaseActivity)getActivity()).showProgressDialog(getString(R.string.sending_request));
        
        RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        params.put("phoneNumber", number);
        
        httpClient.post(getActivity().getApplicationContext(), Constants.BASE_REST_URL + "/armCallback", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				((BaseActivity)getActivity()).hideProgressDialog();
				makeArmCallBack(number);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				((BaseActivity)getActivity()).hideProgressDialog();
			}
        	
        });
        
    }
	
	public void makeArmCallBack(String number) {
        String pagerNumber = SmartPagerApplication.getInstance().getPreferences().getPagerNumber();
        String routerNumber = SmartPagerApplication.getInstance().getPreferences().getCallRouterPhoneNumber();
        
        if (TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routerNumber)) {
        	// !PagerNo + !RouterNo
        	((BaseActivity)getActivity()).showErrorDialog(R.string.your_pager_number_is_invalid);
        } else if (!TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routerNumber)) {
        	// PagerNo + !RouterNo
            pushOutgoingCallNumber(number);
            TelephoneUtils.dial(pagerNumber);
        } else if (TextUtils.isEmpty(pagerNumber) && !TextUtils.isEmpty(routerNumber)) {
        	// !PagerNo + RouterNo
        	pushOutgoingCallNumber(number);
            TelephoneUtils.dial(routerNumber);
        } else {
        	// PagerNo + RouterNo
        	pushOutgoingCallNumber(number);
            TelephoneUtils.dial(pagerNumber);
        }
    }
	
	private void pushOutgoingCallNumber (String number)
    {
        Intent outgoingReceiver = OutgoingCallReceiver.createOutgoingCallNumberAction(number, null);
        getActivity().getApplicationContext().sendBroadcast(outgoingReceiver);
    }

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		// hide index scroller to prevent sticky central letter effect
		if (mList != null && ALPHABETICAL_PANEL_ENABLED && !ALPHABETICAL_PANEL_SHOW_ALWAYS) {
			((IndexableListView) mList).hideIndexScroller();
		}
	}
}
