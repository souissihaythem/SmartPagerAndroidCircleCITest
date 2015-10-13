package net.smartpager.android.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.rey.material.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ContactCursorTable;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.dialer.VoipPhone;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.holder.Department;
import net.smartpager.android.holder.DepartmentHolder;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.receivers.OutgoingCallReceiver;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.TelephoneUtils;
import net.smartpager.android.web.response.AbstractResponse;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class ContactDetailsActivity extends BaseActivity implements LoaderCallbacks<Cursor> {

	@ViewInjectAnatation(viewID = R.id.contact_details_name_textView)
	private TextView mTxtName;
	@ViewInjectAnatation(viewID = R.id.contact_details_description_textView)
	private TextView mTxtDescription;
	@ViewInjectAnatation(viewID = R.id.contact_details_phone_caption_textView)
	private TextView mTxtPhoneCaption;
	@ViewInjectAnatation(viewID = R.id.contact_details_phone_number_textView)
	private TextView mTxtPhone;
	@ViewInjectAnatation(viewID = R.id.contact_details_phone_line)
	private View mViewPhoneLine;
	@ViewInjectAnatation(viewID = R.id.contact_details_pager_id_textView)
	private TextView mTxtPagerID;
	@ViewInjectAnatation(viewID = R.id.contact_details_organization_textView)
	private TextView mTxtOrganization;
	@ViewInjectAnatation(viewID = R.id.contact_details_departments_listView)
	private ListView mLstDepartmentsList;
	@ViewInjectAnatation(viewID = R.id.contact_details_status_textView)
	private TextView mTxtStatus;
	@ViewInjectAnatation(viewID = R.id.contact_details_photo_imageView)
	private ImageView mImgContactImage;
	@ViewInjectAnatation(viewID = R.id.contact_details_page_button)
	private Button mBtnPageButton;
	@ViewInjectAnatation(viewID = R.id.contact_details_call_button)
	private Button mBtnCallButton;

	private String mId;
	private String mPhoneNumber;
	private String mSmartPagerId;
	private String mName;
	private Recipient mRecipient;
	private boolean mHideMenu = false;
	private AlertFragmentDialogYesNO mDialog;
	private String accountName;
	private boolean mIsFavourite = false;
	
	private AsyncHttpClient httpClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mId = getIntent().getStringExtra(GroupContactItem.id.name());
		setContentView(R.layout.activity_contact_details);
		Injector.doInjection(this);
		getSupportLoaderManager().initLoader(LoaderID.CONTACT_LOADER.ordinal(), null, this);
		
		httpClient = new AsyncHttpClient();
		
		if (SmartPagerApplication.getInstance().getPreferences().getShowMobileNumbers() == false) {
			mTxtPhoneCaption.setVisibility(View.GONE);
			mTxtPhone.setVisibility(View.GONE);
			mViewPhoneLine.setVisibility(View.GONE);
		}
		
		if (SmartPagerApplication.getInstance().getPreferences().getAllowCallFromContacts() == false) {
			mBtnCallButton.setVisibility(View.GONE);
		}
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		Loader<Cursor> loaderCursor = new ContactCursorTable().getFilterLoader(mId);
		return loaderCursor;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		
		if (!cursor.moveToFirst()) {
			return;
		}
		
		// get data from cursor
		String firstName = cursor.getString(DatabaseHelper.ContactTable.firstName.ordinal());
		String lastName = cursor.getString(DatabaseHelper.ContactTable.lastName.ordinal());
		mPhoneNumber = cursor.getString(DatabaseHelper.ContactTable.phoneNumber.ordinal());
		String status = cursor.getString(DatabaseHelper.ContactTable.status.ordinal());
		mSmartPagerId = cursor.getString(DatabaseHelper.ContactTable.smartPagerID.ordinal());
		mName = firstName + " " + lastName;
		String title = cursor.getString(DatabaseHelper.ContactTable.title.ordinal());
		String departments = cursor.getString(DatabaseHelper.ContactTable.departments.ordinal());
		accountName = cursor.getString(DatabaseHelper.ContactTable.accountName.ordinal());
		String photoUrl = cursor.getString(DatabaseHelper.ContactTable.photoUrl.ordinal());
		
		// set text fields
		mTxtName.setText(mName);
		mTxtDescription.setText(title);
		mTxtPhone.setText(TelephoneUtils.format(mPhoneNumber));
		mTxtPagerID.setText(mSmartPagerId);
		mTxtOrganization.setText(accountName);
		
		// set contact image
		SmartPagerApplication.getInstance().getImageLoader()
				.displayImage(photoUrl, mImgContactImage, R.drawable.chat_avatar_no_image);
		
		// set status bar
		switch (ContactStatus.valueOf(status)) {
			case ONLINE:
				mTxtStatus.setBackgroundResource(R.drawable.shape_contact_status);
			break;
			default:
				mTxtStatus.setBackgroundResource(R.drawable.shape_contact_status_offline);
			break;
		}
		
		mRecipient = new Recipient();
		mRecipient.setName(mName);
		mRecipient.setSmartPagerId(mSmartPagerId);
		mRecipient.setGroup(false);
		mRecipient.setImageUrl(photoUrl);
		mRecipient.setSelected(false);
		mRecipient.setStatus(status);
		mRecipient.setTitle(title);
		
		// set departments
		List<IHolderSource> holderSources = new ArrayList<IHolderSource>();
		if (!TextUtils.isEmpty(departments)) {
			String[] departmentsArray = departments.split("\n");
			for (int i = 0; i != departmentsArray.length; i++) {

				holderSources.add(Department.createFromString(departmentsArray[i]));
			}
		}
		BaseHolderAdapter departmentsAdapter = new BaseHolderAdapter(this, holderSources, DepartmentHolder.class);
		mLstDepartmentsList.setAdapter(departmentsAdapter);
		mLstDepartmentsList.setEnabled(false);	// to avoid click on the items
		
		mIsFavourite = SmartPagerApplication.getInstance().isFavourite(getApplicationContext(), mSmartPagerId);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.contact_details, menu);
		
		MenuItem deleteItem = menu.findItem(R.id.item_delete);
//		mHideMenu = SmartPagerApplication.getInstance().getPreferences().getAccountName().equalsIgnoreCase(accountName);
//		if (mHideMenu) {
			deleteItem.setVisible(false);
//		}
		
		MenuItem addFavouriteItem = menu.findItem(R.id.item_add_favourite);
        MenuItem removeFavouriteItem = menu.findItem(R.id.item_remove_favourite);

        if (mIsFavourite) {
            addFavouriteItem.setVisible(false);
            removeFavouriteItem.setVisible(true);
        } else {
            addFavouriteItem.setVisible(true);
            removeFavouriteItem.setVisible(false);
        }
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.item_delete:
				showAlertDialog();
				break;
			case R.id.item_add_favourite:
			case R.id.item_remove_favourite:
				favouriteItemClicked();
				break;
			default:
				break;
		}
		return true;
	}

	public void favouriteItemClicked() {
		mIsFavourite = !mIsFavourite;
		invalidateOptionsMenu();
		
		if (mIsFavourite) {
			Toast.makeText(getApplicationContext(), "Favourite Added", Toast.LENGTH_SHORT).show();
			SmartPagerApplication.getInstance().addFavorite(getApplicationContext(), mSmartPagerId);
		} else {
			Toast.makeText(getApplicationContext(), "Favourite Removed", Toast.LENGTH_SHORT).show();
			SmartPagerApplication.getInstance().removeFavorite(getApplicationContext(), mSmartPagerId);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}
	public void startArmCallback(final String number) {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.phoneNumber.name(), number);
//        SmartPagerApplication.getInstance().startWebAction(WebAction.armCallback, extras);
        showProgressDialog(getString(R.string.sending_request));
        
        RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        params.put("phoneNumber", number);
        
        httpClient.post(getApplicationContext(), Constants.BASE_REST_URL + "/armCallback", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				hideProgressDialog();
				makeArmCallBack(number);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				hideProgressDialog();
			}
        	
        });
        
    }
	
	public void makeArmCallBack(String number) {
        String pagerNumber = SmartPagerApplication.getInstance().getPreferences().getPagerNumber();
        String routerNumber = SmartPagerApplication.getInstance().getPreferences().getCallRouterPhoneNumber();
        
        if (TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routerNumber)) {
        	// !PagerNo + !RouterNo
            showErrorDialog(getString(R.string.your_pager_number_is_invalid));
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
        getApplicationContext().sendBroadcast(outgoingReceiver);
    }

	// BUTTON LISTENERS
	@ClickListenerInjectAnatation(viewID = R.id.contact_details_call_button)
	public void onContactCallClick(View view) {
		
		if (SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled()) {
			makeVoipCall(mPhoneNumber);
			return;
		}

		if (SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled()) {
			makeVoipCall(mPhoneNumber);
		} else {
			TelephoneUtils.dial(mPhoneNumber);
		}
		
	}

	@ClickListenerInjectAnatation(viewID = R.id.contact_details_page_button)
	public void onPageClick(View view) {
		Intent intent = new Intent(this, NewMessageActivity.class);
		intent.putExtra(BundleKey.recipient.name(), mRecipient);
		startActivity(intent);
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.removeContact) {
			showProgressDialog("Removing contact");
			startGetUpdates();
		}
		if (action == WebAction.getUpdates) {
			hideProgressDialog();
			finish();
		}
	}

	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		super.onErrorResponse(action, responce);
	}

	private void startRemoveContact(String contactId) {
        Bundle extras = new Bundle();
		extras.putString(WebSendParam.contactId.name(), contactId);
        SmartPagerApplication.getInstance().startWebAction(WebAction.removeContact, extras);
	}



	private void showAlertDialog() {
		mDialog = AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.remove_contact), 
				getString(R.string.this_contact_will_be_removed), "",
				true, new OnDialogDoneListener() {
					@Override
					public void onDialogNo(FragmentDialogTag tag, String data) {
						mDialog.dismiss();
					}

					@Override
					public void onDialogDone(FragmentDialogTag tag, String data) {
						startRemoveContact(mId);
						mDialog.dismiss();
					}
				});
		mDialog.show(getSupportFragmentManager(), FragmentDialogTag.AlertInforDialogYesNo.name());
	}
	
	@Override
	protected boolean isUpNavigationEnabled() {
		return true;
	}
	
	public void makeVoipCall(final String number) {
		AudioManagerUtils.setLoudSpeaker(false);
		showProgressDialog("Connecting VOIP Call");
    	RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        
        AsyncHttpClient httpClient = new AsyncHttpClient();        
        httpClient.post(getApplicationContext(), Constants.BASE_REST_URL + "/getTwilioClientToken", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				hideProgressDialog();
				
				VoipPhone voipPhone = new VoipPhone(getApplicationContext(), response.optString("token"));
				voipPhone.connect(number);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				hideProgressDialog();
			}
        	
        });
    }
}
