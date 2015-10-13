package net.smartpager.android.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;
import com.rey.material.widget.ImageButton;
import android.widget.LinearLayout;
import com.rey.material.widget.ListView;

import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.adapters.MessageTypeAdapter;
import net.smartpager.android.adapters.RecipientsCursorAdapter;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.RequestID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendMessageParams;
import net.smartpager.android.database.ContactGroupTable;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.database.MessageCursorTable;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.holder.ImageHolder;
import net.smartpager.android.holder.MessageRecipientHolder;
import net.smartpager.android.model.ImageSource;
import net.smartpager.android.model.ListSerilizableContainer;
import net.smartpager.android.model.PlayInfo;
import net.smartpager.android.model.PlayInfo.PlayStatus;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.model2.BaseMessaging;
import net.smartpager.android.model2.Field;
import net.smartpager.android.model2.MessageType;
import net.smartpager.android.utils.IntentFactory;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.utils.PhotoFileUtils;
import net.smartpager.android.view.SafeAutoCompleteTextView;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.webservices.Reachability;
import net.smartpager.android.webservices.RestClient;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.utils.Log;
import biz.mobidev.framework.view.ExpandableHeightGridView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class NewMessageActivity extends BasePlayerActivity implements
		MessageRecipientHolder.OnDeleteRecipientClickListener, LoaderManager.LoaderCallbacks<Cursor>,
		OnDialogDoneListener {
	
	private static NewMessageActivity newMessageActivity;

	private String mCallbackNumber = "";
	private String mResponseOptions = "";
	private boolean mOptUrgent = false;
	private boolean mOptAcknowledgement = false;
	private Integer mThreadID;
	
	private List<IHolderSource> mImageslist;
	private List<IHolderSource> mRecipients;
	private List<IHolderSource> mHiddenRecipients;
	private List<IHolderSource> mVisibleRecipients;
	private ArrayList<String> mContactIDs;
	private ArrayList<String> mGroupIDs;
	private ArrayList<String> mLocalGroupIDs;

	private Uri mNewImageUri;
	private BaseHolderAdapter mImageAdapter;
	@ViewInjectAnatation(viewID = R.id.page_to_text)
	private SafeAutoCompleteTextView mAutoComplTxtToContacts;
	@ViewInjectAnatation(viewID = R.id.page_subject_edit_text)
	private android.widget.EditText mEdtSubject;
	@ViewInjectAnatation(viewID = R.id.page_message_edit_text)
	private android.widget.EditText mEdtText;
	@ViewInjectAnatation(viewID = R.id.page_add_button)
	private Button mBtnAddContact;
	@ViewInjectAnatation(viewID = R.id.page_record_button)
	private Button mBtnRecord;
	@ViewInjectAnatation(viewID = R.id.page_attach_button)
	private Button mBtnAttach;
	@ViewInjectAnatation(viewID = R.id.page_send_button)
	private ListView mLstSend;
	@ViewInjectAnatation(viewID = R.id.page_images_grid_view)
	private ExpandableHeightGridView mExpGrdImages;
	@ViewInjectAnatation(viewID = R.id.page_recipients_gridView)
	private ExpandableHeightGridView mExpGrdRecipients;
	@ViewInjectAnatation(viewID = R.id.message_type_add_button)
	private ImageButton mMessageTypeAddButton;
	@ViewInjectAnatation(viewID = R.id.call_back_number)
	private android.widget.EditText mCallBackNumberEditText;
	@ViewInjectAnatation(viewID = R.id.fields)
	private LinearLayout mFields;

	protected boolean isUpNavigationEnabled() {
		
		return true;
	};
	
	TextWatcher mFilterTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void afterTextChanged(Editable s) {
			Bundle bundle = new Bundle();
			bundle.putString(BundleKey.filter.name(), s.toString());
			NewMessageActivity.this.getSupportLoaderManager().restartLoader(LoaderID.RECIPIENTS_LOADER.ordinal(), bundle,
					NewMessageActivity.this);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_message);
		Injector.doInjection(this);
		
		newMessageActivity = this;

		mImageslist = new ArrayList<IHolderSource>();
		mRecipients = new ArrayList<IHolderSource>();
		mHiddenRecipients = new ArrayList<IHolderSource>();
		mVisibleRecipients = new ArrayList<IHolderSource>();
		
		if (getIntent().getExtras() != null ){
			if (getIntent().getExtras().containsKey(BundleKey.recipientList.name())) {
				mHiddenRecipients = (((ListSerilizableContainer) getIntent().getSerializableExtra(BundleKey.recipientList.name()))
						.getList());
				mRecipients.addAll(mHiddenRecipients);
			}
			if (getIntent().getExtras().containsKey(BundleKey.recipient.name())) {
				mVisibleRecipients.add((IHolderSource) getIntent().getExtras().getSerializable(BundleKey.recipient.name()));
				mRecipients.addAll(mVisibleRecipients);
			}
			if (getIntent().getExtras().containsKey(WebSendMessageParams.threadID.name())) {
				mThreadID = getIntent().getExtras().getInt(WebSendMessageParams.threadID.name());
			}
			if (getIntent().getExtras().containsKey(WebSendMessageParams.subjectText.name())) {
				mEdtSubject.setText( getIntent().getExtras().getString(WebSendMessageParams.subjectText.name()));
			}
		}
		
		
		List<Class<? extends IHolder>> holderClasses = new ArrayList<Class<? extends IHolder>>();
		holderClasses.add(MessageRecipientHolder.class);
		holderClasses.add(MessageRecipientHolder.class);
		BaseHolderAdapter resipientsAdapter = new BaseHolderAdapter(this, mVisibleRecipients, holderClasses,
				new Object[] { this });
		mExpGrdRecipients.setAdapter(resipientsAdapter);
		mImageAdapter = new BaseHolderAdapter(this, mImageslist, ImageHolder.class, new Object[] { this });
		mExpGrdImages.setAdapter(mImageAdapter);
		RecipientsCursorAdapter completeAdapter = new RecipientsCursorAdapter(this, null, true);
		mAutoComplTxtToContacts.setAdapter(completeAdapter);
		mAutoComplTxtToContacts.addTextChangedListener(mFilterTextWatcher);
		mAutoComplTxtToContacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long count) {
				Cursor cursor = (Cursor) adapterView.getAdapter().getItem(position);
                Recipient selectedRecipient = null;
                if(cursor != null)
                {
                    selectedRecipient = Recipient.createFromCursor(cursor);
                }
                if(selectedRecipient != null) {
                    mRecipients.add(selectedRecipient);
                    mVisibleRecipients.add(selectedRecipient);
    				((BaseHolderAdapter) mExpGrdRecipients.getAdapter()).notifyDataSetChanged();
                }
				mAutoComplTxtToContacts.setText("");
				mEdtSubject.requestFocus();

			}
		});
		getSupportLoaderManager().initLoader(LoaderID.RECIPIENTS_LOADER.ordinal(), null, this);
		mExpGrdImages.setExpanded(true);
		mExpGrdRecipients.setExpanded(true);
		mSeekBar.setEnabled(false);
		
		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(BundleKey.messageId.name())) {
			getSupportLoaderManager().initLoader(LoaderID.FORWARD_MESSAGE_LOADER.ordinal(), getIntent().getExtras(), this);
			getSupportLoaderManager().initLoader(LoaderID.MESSAGE_IMAGE_LOADER.ordinal(), getIntent().getExtras(), this);			
		}


		mCallBackNumberEditText.setVisibility(View.GONE);
		mFields.setVisibility(View.GONE);
	}

	public int countGroupRecipients() {
		int numOfRecipients = 0;

		System.out.println("recipients: " + mRecipients);
		for (Object o : mRecipients) {
			Recipient r = (Recipient)o;

			if (r.getGroupType() != null && (r.getGroupType().equals("BROADCAST") || r.getGroupType().equals("SCHEDULE"))) {
				numOfRecipients += 1;
			}

		}
		return numOfRecipients;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@ClickListenerInjectAnatation(viewID = R.id.page_send_button)
	public void onSendMessageClick(View view) {


		if (countGroupRecipients() >= 1) {
			String msg = "";
			if (countGroupRecipients() == 1) {
				msg = "You are about to send a message to a group.  Do you wish to continue?";
			} else {
				msg = "You are about to send a message to " + countGroupRecipients() + " groups.  Do you wish to continue?";
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(NewMessageActivity.this);
			builder.setTitle("Info")
					.setMessage(msg)
					.setPositiveButton("Send Message", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							sendMessage();
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			AlertDialog alert = builder.create();
			alert.show();


		} else {
			sendMessage();
		}
	}

	private void sendMessage() {
		showProgressDialog("Sending message");

		AsyncHttpClient client = new AsyncHttpClient();
		client.get(Constants.BASE_URL, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				System.out.println("xxFailure");
				hideProgressDialog();
				showErrorDialog(getString(R.string.no_internet_connection));
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				System.out.println("xxSuccess");


				if (mRecipients.size() == 0) {
					showErrorDialog(getString(R.string.recipient_list_should_not_be_empty));
					hideProgressDialog();
					return;
				}
				fillContactsAndGroups();
				PrepareLocalGroupTask task = new PrepareLocalGroupTask();
				task.execute();

			}


		});

		if (!NetworkUtils.isInternetConnectedAsync()) {
			showErrorDialog(getString(R.string.no_internet_connection));
			return;
		}
	}

	private void fillContactsAndGroups() {
		mContactIDs = new ArrayList<String>();
		mGroupIDs = new ArrayList<String>();
		mLocalGroupIDs = new ArrayList<String>();

		Recipient recipient = null;
		for (IHolderSource item : mRecipients) {
			recipient = (Recipient) item;
			if (recipient.isGroup() && recipient.getGroupType().equalsIgnoreCase(LocalGroupDetailsActivity.LOCAL)) {
				mLocalGroupIDs.add(recipient.getIdDb());
			} else if (recipient.isGroup()) {
				mGroupIDs.add(recipient.getSmartPagerId());
			} else {
//				mContactIDs.add(recipient.getSmartPagerId());
				mContactIDs.add(recipient.getContactId());
			}
		}
	}
	
	public String checkNetworkStatus() {
		
		String networkType = new String();
		
	    final ConnectivityManager connMgr = (ConnectivityManager)
	    this.getSystemService(Context.CONNECTIVITY_SERVICE);
	    final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (wifi.isAvailable()) {
	        networkType = " wifi";
	    } else if (mobile.isAvailable()) {
	    	TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    	networkType = " " + manager.getNetworkOperatorName();
	    } else {
	       networkType = "";
	    }
	    
	    return networkType;
	}
	
	public static void showBadConnectionMessage() {
		
		
		FragmentTransaction transaction = newMessageActivity.getSupportFragmentManager().beginTransaction();
		AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(newMessageActivity.getApplicationContext(), "Network Connection Issue",
				"There is an intermittant issue with the" + newMessageActivity.checkNetworkStatus() + " network.\n\n Would you like to try sending your message again?", "", false);
		dialog.show(transaction, FragmentDialogTag.Retry.name());
		
		
		
		
		
//		AlertDialog.Builder dialog = new AlertDialog.Builder(newMessageActivity);
//		dialog.set
//		dialog.setTitle("Network Connection Issue");
//		dialog.setMessage("There is an intermittant network issue.\n\n Would you like to try sending your message again?");
//		dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				View sendButton = (View) newMessageActivity.findViewById(R.id.page_send_button);
//				sendButton.performClick();
//			}
//		});
//		dialog.setNegativeButton("No", null);
//		dialog.show();
		
	}

	private void startSendMessage() {
		ArrayList<String> imagesUris = new ArrayList<String>();
		for (IHolderSource item : mImageslist) {
			ImageSource image = (ImageSource) item;
			// Set the full image uri here so the app doesn't crash when forwarding an image (which clearly doesn't exist on the local device)
			image.setFullImageUri(image.getPreviewImageUri());
			imagesUris.add(image.getFullImageUri().toString());	
		}
		Bundle bundle = new Bundle();

		String bodyText = mEdtText.getText().toString().trim();

		StringBuilder bodyTextStringBuilder = new StringBuilder();
		if (mFieldsEditText != null) {
			for (int i = 0; i < mFieldsEditText.size(); i++) {

				String label = mFieldsEditTextLabels.get(i);
				String value = mFieldsEditText.get(i).getText().toString();
//				bundle.putString(label, mFieldsEditText.get(i).getText().toString());
				bodyTextStringBuilder.append(label + ": " + value + "\n");
			}
		}

		if (mFieldsSpinner != null) {
			for (int i = 0; i < mFieldsSpinner.size(); i++) {
				String label = mFieldsSpinnerLabels.get(i);
				String value = mFieldsSpinner.get(i).getSelectedItem().toString();
//				bundle.putString(label, mFieldsSpinner.get(i).getSelectedItem().toString());
				bodyTextStringBuilder.append(label + ": " + value + "\n");
			}
		}

		bodyTextStringBuilder.append("\n" + bodyText);

		bundle.putString(WebSendMessageParams.text.name(), bodyTextStringBuilder.toString());

		bundle.putBoolean(WebSendMessageParams.requestAcceptance.name(), mOptAcknowledgement);
		bundle.putBoolean(WebSendMessageParams.critical.name(), mOptUrgent);
		bundle.putString(WebSendMessageParams.callbackNumber.name(), mCallbackNumber);

		// FIXME responseTemplate is probably incorrect key!!!!
		bundle.putString(WebSendMessageParams.responseTemplate.name(), mResponseOptions);
		
		bundle.putString(WebSendMessageParams.subjectText.name(), mEdtSubject.getText().toString().trim());
		if (new File(mLocalRecordPath).exists()) {
			bundle.putString(WebSendMessageParams.recordUriToSend.name(), mRealRecordPath);
		}
		
		if (mThreadID != null){
			bundle.putInt(WebSendMessageParams.threadID.name(), mThreadID);
		}
		bundle.putStringArrayList(WebSendMessageParams.recipients.name(), mContactIDs);
		bundle.putStringArrayList(WebSendMessageParams.recipientsGroup.name(), mGroupIDs);
		bundle.putStringArrayList(WebSendMessageParams.imagesUriToSend.name(), imagesUris);

		if (mCallBackNumberEditText.getText().toString().length() > 0) {
			bundle.putString(WebSendMessageParams.callbackNumber.name(), mCallBackNumberEditText.getText().toString());
		}

		SmartPagerApplication.getInstance().startWebAction(WebAction.sendMessage, bundle);
	}
	// --------------------------------------------------------
	class PrepareLocalGroupTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			showProgressDialog("Sending message");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (mLocalGroupIDs.size() > 0) {
				// query for all contactId
				StringBuilder selection = new StringBuilder();
				for (int i = 0; i < mLocalGroupIDs.size(); i++) {
					if (i > 0) {
						selection.append(" , ");
					}
					selection.append(mLocalGroupIDs.get(i));
				}
				Uri uri = SmartPagerContentProvider.CONTENT_LOCAL_GROUP_CONTACTS_URI;
				Cursor cursor = getContentResolver().query(uri, null, selection.toString(), null, null);
				// get all smartPagerID from cursor
				if (cursor != null && cursor.moveToFirst()) {
					do {
						String smartPagerId = cursor.getString(ContactTable.smartPagerID.ordinal());
						mContactIDs.add(smartPagerId);
					} while (cursor.moveToNext());
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			startSendMessage();
			super.onPostExecute(result);
		}
	}

	// --------------------------------------------------------------------

	@ClickListenerInjectAnatation(viewID = R.id.page_add_button)
	public void onAddRecipientsClick(View view) {
		Intent intent = new Intent(this, RecipientListActivity.class);
		ListSerilizableContainer listSerilizableContainer = new ListSerilizableContainer(mRecipients);
		intent.putExtra(BundleKey.recipient.name(), listSerilizableContainer);
		startActivityForResult(intent, RequestID.ADD_RECIPIENT.ordinal());
	}

	@ClickListenerInjectAnatation(viewID = R.id.message_type_add_button)
	public void onAddMessageTypeClick(View view) {

		String messageTypes = SmartPagerApplication.getInstance().getPreferences().getMessageTypes();
		if (messageTypes != null){
			Gson gson = new Gson();
			BaseMessaging baseMessaging = gson.fromJson(messageTypes, BaseMessaging.class);
			pickMessageType(baseMessaging);
		}
		else{


		if (!Reachability.getInstance(this).isConnected()){
			Toast.makeText(this, R.string.not_connected_to_internet, Toast.LENGTH_LONG).show();
		}
		else{
			final ProgressDialog dialog = ProgressDialog.show(this, "",
					getString(R.string.loading_mesages_types), true);

			String smartPagerID = SmartPagerApplication.getInstance().getPreferences().getUserID();
			String uberPassword = SmartPagerApplication.getInstance().getPreferences().getPassword();
			RestClient.getApiService().getMessageTypes(smartPagerID, uberPassword,
					new Callback<BaseMessaging>() {

						@Override
						public void success(BaseMessaging baseMessaging, Response response) {
							if (!isFinishing()) {
								dialog.dismiss();
								Gson gson = new Gson();
								String json = gson.toJson(baseMessaging);
								SmartPagerApplication.getInstance().getPreferences().setMessageTypes(json);

								pickMessageType(baseMessaging);
							}
						}

						@Override
						public void failure(RetrofitError error) {
							dialog.dismiss();
						}
					});

		}
		}
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (WebAction.sendMessage == action) {
			File voiceMessage = new File(getActivityRecordFilePath());
			if (voiceMessage.exists()) {
				voiceMessage.delete();
			}
			hideProgressDialog();
            backToActivity(ACTIVITY_TAG.MainActivity, ACTIVITY_ACTION.MainActivity_ShowMessages);
			finish();
		}
	}

	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		super.onErrorResponse(action, responce);
	}

	@ClickListenerInjectAnatation(viewID = R.id.page_record_button)
	public void onClickNewVoiceMessage(View view) {
		Intent intent = new Intent(this, NewVoiceMessageActivity.class);
		intent.putExtra(BundleKey.recordPath.name(), getActivityRecordFilePath());
		startActivityForResult(intent, RequestID.ADD_VOICE_RECORD.ordinal());
	}

	@ClickListenerInjectAnatation(viewID = R.id.page_attach_button)
	public void onClickNewPhotoButton(View view) {
		try {
			Pair<Intent, Uri> chooser = IntentFactory.getPhotoIntent(this);
			mNewImageUri = chooser.second;
			setLocPin(true);
			startActivityForResult(chooser.first, RequestID.PICK_PHOTO.ordinal());
		} catch (Exception e) {
			showToast(e.getMessage());
		}
	}

	@ClickListenerInjectAnatation(viewID = R.id.page_quick_button)
	public void onClickQuickButton(View view) {
		Intent data = new Intent(this, QuickResponseChooserActivity.class);
		startActivityForResult(data, RequestID.CHOOSE_QUICK_RESPONSE.ordinal());
	}

	@ClickListenerInjectAnatation(viewID = R.id.page_options_button)
	public void onClickOptionButton(View view) {
		Intent data = new Intent(this, MessageOptionsActivity.class);
		data.putExtra(BundleKey.optUrgent.name(), mOptUrgent);
		data.putExtra(BundleKey.optAcknowledgement.name(), mOptAcknowledgement);
		startActivityForResult(data, RequestID.CHOOSE_OPTIONS.ordinal());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (RequestID.values()[requestCode]) {
			case PICK_PHOTO:
                boolean isCameraImage = true;
				if (resultCode == RESULT_OK) {
					if (data != null && data.getData() != null) {
						mNewImageUri = data.getData();
                        isCameraImage = false;
					}
					try {
						String photoPath = PhotoFileUtils.getPath(NewMessageActivity.this, mNewImageUri);
						Bitmap scaledBmp = PhotoFileUtils.getScaledBmpFromFile(photoPath);
						if (null != scaledBmp) {
							// Save scaled bitmap to File and get path
							String newImageFileName = PhotoFileUtils.saveScaledImage(NewMessageActivity.this, scaledBmp, !isCameraImage);
                            ImageSource imageSource = new ImageSource(Uri.parse(newImageFileName));
                            imageSource.setFullImageUri(Uri.parse(newImageFileName));
                            
							mImageslist.add(imageSource);
							mImageAdapter.notifyDataSetChanged();
							
							if (isCameraImage) {
								File file = new File(photoPath);
								if (!file.exists()) {
									return;
								}
								file.delete();
							}
						} else {
							showToast("No Such File or Directory");
						}
					} catch (Exception e) {
						mNewImageUri = null;
						showToast("Please select either jpg or png file");
					}
				}
			break;
			case ADD_RECIPIENT:
				if (resultCode == RESULT_OK && data.getExtras().containsKey(BundleKey.recipient.name())) {
					mRecipients.clear();
					ListSerilizableContainer listSerilizableContainer = (((ListSerilizableContainer) data
							.getSerializableExtra(BundleKey.recipient.name())));
					mRecipients.addAll(listSerilizableContainer.getList());
					mVisibleRecipients.clear();
					for(IHolderSource item : mRecipients){
						if (!((Recipient)item).isDisabled() ){
							mVisibleRecipients.add(item);
						}
					}
					((BaseHolderAdapter) mExpGrdRecipients.getAdapter()).notifyDataSetChanged();
					mExpGrdRecipients.setVisibility(View.GONE);
					mExpGrdRecipients.setVisibility(View.VISIBLE);
				}
			break;
			case CHOOSE_QUICK_RESPONSE:
				if (resultCode == RESULT_OK && data != null
						&& data.getExtras().containsKey(BundleKey.quickResponce.name())) {
					String quickResponce = data.getStringExtra(BundleKey.quickResponce.name());
					if (!TextUtils.isEmpty(quickResponce)) {
						String newLine = (TextUtils.isEmpty(mEdtText.getText().toString())) ? "" : "\n";
						mEdtText.append(newLine + quickResponce);
					}
				}
			break;
			case CHOOSE_OPTIONS:
				if (resultCode == RESULT_OK && data != null) {
					mOptUrgent = data.getBooleanExtra(BundleKey.optUrgent.name(), false);
					mOptAcknowledgement = data.getBooleanExtra(BundleKey.optAcknowledgement.name(), false);
				}
			break;
			case ADD_VOICE_RECORD:
				if (resultCode == RESULT_OK && data != null) {
					mRealRecordPath = mLocalRecordPath = data.getStringExtra(BundleKey.recordPath.name());
					if (new File(mLocalRecordPath).exists()) {
						mPlayerView.setVisibility(View.VISIBLE);
					} else {
						mPlayerView.setVisibility(View.GONE);
					}
				}
			break;
			default:
			break;
		}
	}

	@Override
	public void onDeleteRecipientClick(Recipient deleteRecipient) {
        mRecipients.remove(deleteRecipient);
        mVisibleRecipients.remove(deleteRecipient);
		((BaseHolderAdapter) mExpGrdRecipients.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int index, Bundle bundle) {
		CursorLoader result = null;
		switch (LoaderID.values()[index]) {
			case RECIPIENTS_LOADER:
				if (bundle != null && bundle.containsKey(BundleKey.filter.name())) {
					result = new ContactGroupTable().getFilterLoader(bundle.getString(BundleKey.filter.name()),
							mRecipients, ContactTable.id);
				} else {
					result = new ContactGroupTable().getLoader();
				}
			break;
			case MESSAGE_IMAGE_LOADER:
				String where = DatabaseHelper.ImageUrlTable.messageId.name() + " = '"
						+ bundle.getString(BundleKey.messageId.name()) + "'";
				result = new CursorLoader(this, SmartPagerContentProvider.CONTENT_IMAGE_URL_URI, null, where, null,
						null);
			break;
			case FORWARD_MESSAGE_LOADER:
				String messageID = bundle.getString(BundleKey.messageId.name());
				result = new MessageCursorTable().getFilterLoader(new String[] { messageID });
//				result =  new CursorLoader(this, 
//						SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI, null, 
//						MessageTable.id.name() + " = " + messageID, null, null);
			break;
			default:
			break;
		}
		return result;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (LoaderID.values()[loader.getId()]) {
			case RECIPIENTS_LOADER:
				((SimpleCursorAdapter) mAutoComplTxtToContacts.getAdapter()).swapCursor(cursor);
			break;
			case MESSAGE_IMAGE_LOADER:
				if (cursor.moveToFirst()) {
					do {
						boolean isAlreadyInGallery = false;
						Uri uri = Uri.parse(cursor.getString(DatabaseHelper.ImageUrlTable.ImageUrl.ordinal()));
						for (IHolderSource source :  mImageslist){
							ImageSource image = (ImageSource) source;
							if (uri.equals( image.getPreviewImageUri())){
								isAlreadyInGallery = true;
								break;
							}
						}
						if (! isAlreadyInGallery){
							mImageslist.add(new ImageSource( uri ));
						}
					} while (cursor.moveToNext());
					mImageAdapter.notifyDataSetChanged();
				}
			break;
			case FORWARD_MESSAGE_LOADER:
				if (cursor.moveToFirst()) {
//					int spin = MessageTable.values().length;
					mCallbackNumber = cursor.getString(MessageTable.callbackNumber.ordinal());
					mResponseOptions = cursor.getString(MessageTable.responseTemplate.ordinal());
					Integer requestAcceptance = cursor.getInt(MessageTable.requestAcceptance.ordinal()); 
					mOptAcknowledgement =  requestAcceptance != null && requestAcceptance != 0;
					Integer critical = cursor.getInt(MessageTable.critical.ordinal());
					mOptUrgent = critical != null && critical != 0;
					
//					mSubject.setText("Fwd: " + cursor.getString(MessageTable.subjectText.ordinal()));
					mEdtSubject.setText(cursor.getString(MessageTable.subjectText.ordinal()));					
					StringBuilder build = new StringBuilder();
//					build.append("\n______\n");
//					build.append(cursor.getString(spin + ContactTable.firstName.ordinal())).append(" ");
//					build.append(cursor.getString(spin + ContactTable.lastName.ordinal())).append(" ");
//					build.append(getString(R.string.wrote_on_));
//					build.append(DateTimeUtils.fullDateFormat(cursor.getLong(MessageTable.timeSent.ordinal()))).append(
//							"\n");
					build.append(cursor.getString(MessageTable.text.ordinal()));
					mEdtText.setText(build.toString());
					String voicePath = cursor.getString(MessageTable.recordUrl.ordinal());
					if (!TextUtils.isEmpty(voicePath)) {
						setExternalRecordUrl(voicePath);
						if (new File(mLocalRecordPath).exists()) {
							mPlayerView.setVisibility(View.VISIBLE);
						} else {
							mPlayerView.setVisibility(View.GONE);
						}
					}
				}
			break;
			default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (LoaderID.values()[loader.getId()]) {
			case RECIPIENTS_LOADER:
				((SimpleCursorAdapter) mAutoComplTxtToContacts.getAdapter()).swapCursor(null);
			break;
			default:
			break;
		}
	}

	@Override
	public void onDeleteImageClick(ImageSource imageSource) {
		mImageslist.remove(imageSource);
		mImageAdapter.notifyDataSetChanged();

	};

	@Override
	protected void onPause() {

		super.onPause();
	}

	@Override
	public void onBackPressed() {
		File voiceMessage = new File(mLocalRecordPath);
		if (voiceMessage.exists() || !TextUtils.isEmpty(mEdtText.getText().toString().trim())
				|| !TextUtils.isEmpty(mEdtSubject.getText().toString().trim()) || mRecipients.size() > 0
				|| mImageslist.size() > 0) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.close),
					getString(R.string.are_you_sure_you_want_to_cancel_this_message), "", false);
			dialog.show(transaction, FragmentDialogTag.IsExit.name());
		} else {
			super.onBackPressed();
		}
	};

	// Audio Processing
	@ClickListenerInjectAnatation(viewID = R.id.player_action_button)
	public void startPlayVoiceMessage(View v) {
		switchPlayMode();
	}

	@Override
	protected void onPlayerStatusChange(PlayStatus status, PlayInfo playInfo) {

	};

	@Override
	public void onDialogDone(FragmentDialogTag tag, String data) {
		switch (tag) {
			case IsExit:
				File voiceMessage = new File(getActivityRecordFilePath());
				if (voiceMessage.exists()) {
					voiceMessage.delete();
				}
				finish();
				break;
			case Retry:
				View sendButton = (View) newMessageActivity.findViewById(R.id.page_send_button);
				sendButton.performClick();
				break;
			default:
			break;
		}

	}

	@Override
	public void onDialogNo(FragmentDialogTag tag, String data) {

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mNewImageUri != null) {
			outState.putString(BundleKey.imageUri.name(), mNewImageUri.toString());
		}
		outState.putString(BundleKey.localRecordPath.name(), mLocalRecordPath);
		if (!TextUtils.isEmpty(mRealRecordPath)) {
			outState.putString(BundleKey.realRecordPath.name(), mRealRecordPath);
		}
		// images -----------------------------------------------------------
		ListSerilizableContainer imagesContainer = new ListSerilizableContainer(mImageslist);
		outState.putSerializable(BundleKey.imagesList.name(), imagesContainer);
		// recipients-----------------------------------------------------------
		outState.putSerializable(BundleKey.recipientList.name(), new ListSerilizableContainer(mRecipients));
		outState.putSerializable(BundleKey.hiddenRecipientList.name(), new ListSerilizableContainer(mHiddenRecipients));
		outState.putSerializable(BundleKey.visibleRecipientList.name(), new ListSerilizableContainer(mVisibleRecipients));
		// -----------------------------------------------------------
		outState.putBoolean(BundleKey.optUrgent.name(), mOptUrgent);
		outState.putBoolean(BundleKey.optAcknowledgement.name(), mOptAcknowledgement);
		outState.putString(MessageTable.callbackNumber.name(), mCallbackNumber);
		outState.putString(MessageTable.responseTemplate.name(), mResponseOptions);
		
		
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		String uri = savedInstanceState.getString(BundleKey.imageUri.name());		
		if (!TextUtils.isEmpty(uri)) {
			mNewImageUri = Uri.parse(uri);
		}
		mLocalRecordPath = savedInstanceState.getString(BundleKey.localRecordPath.name());
		mRealRecordPath = savedInstanceState.getString(BundleKey.realRecordPath.name());

		// images --------------------------------------
		ListSerilizableContainer imagesContainer = (((ListSerilizableContainer) savedInstanceState
				.getSerializable(BundleKey.imagesList.name())));
		mImageslist.addAll(imagesContainer.getList());
		mImageAdapter.notifyDataSetChanged();
		// recipients--------------------------------------
		ListSerilizableContainer recipientsContainer = (((ListSerilizableContainer) savedInstanceState
				.getSerializable(BundleKey.recipientList.name())));
		ListSerilizableContainer hiddenContainer = (((ListSerilizableContainer) savedInstanceState
				.getSerializable(BundleKey.hiddenRecipientList.name())));
		ListSerilizableContainer visibleContainer = (((ListSerilizableContainer) savedInstanceState
				.getSerializable(BundleKey.visibleRecipientList.name())));
		mRecipients.addAll(recipientsContainer.getList());
		mHiddenRecipients.addAll(hiddenContainer.getList());
		mVisibleRecipients.addAll(visibleContainer.getList());
		((BaseHolderAdapter) mExpGrdRecipients.getAdapter()).notifyDataSetChanged();
		mExpGrdRecipients.setVisibility(View.GONE);
		mExpGrdRecipients.setVisibility(View.VISIBLE);
		// --------------------------------------
		mOptUrgent = savedInstanceState.getBoolean(BundleKey.optUrgent.name());
		mOptAcknowledgement = savedInstanceState.getBoolean(BundleKey.optAcknowledgement.name());
		mCallbackNumber = savedInstanceState.getString(MessageTable.callbackNumber.name());
		mResponseOptions = savedInstanceState.getString(MessageTable.responseTemplate.name());
	}

	private ArrayList<android.widget.EditText> mFieldsEditText;
	private ArrayList<Boolean> mFieldsEditTextRequired;
	private ArrayList<String> mFieldsEditTextLabels;
	private ArrayList<Spinner> mFieldsSpinner;
	private ArrayList<String> mFieldsSpinnerLabels;
	private void pickMessageType(BaseMessaging baseMessaging){
		final ArrayList<MessageType> messagesType = new ArrayList<MessageType>();
		List<MessageType> sortedMessageTypes = baseMessaging.getMessageTypes();
		Collections.sort(sortedMessageTypes, new Comparator<MessageType>() {
			@Override
			public int compare(MessageType lhs, MessageType rhs) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			}
		});
		messagesType.addAll(sortedMessageTypes);
		final MessageTypeAdapter adapter = new MessageTypeAdapter(NewMessageActivity.this, messagesType);
		AlertDialog.Builder builder = new AlertDialog.Builder(NewMessageActivity.this);
		builder.setTitle(R.string.select_message_type);
		builder.setAdapter(adapter,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
										int item) {
						MessageType messageType = adapter.getItem(item);

						mEdtSubject.setText(messageType.getName());
						if (messageType.getShowCallbackNumber()){
							mCallBackNumberEditText.setVisibility(View.VISIBLE);
							if (messageType.getRequireCallbackNumber()){
								mCallBackNumberEditText.setHint(R.string.call_back_number_required);
							}
							else{
								mCallBackNumberEditText.setHint(R.string.call_back_number_not_required);
							}
						}
						else{
							mCallBackNumberEditText.setVisibility(View.GONE);
						}
						if (messageType.getBodyLabel() != null){
							mEdtText.setHint(messageType.getBodyLabel());
						}
						else{
							mEdtText.setHint(R.string.hint_type_new_message);
						}
						if (messageType.getBodyText() != null){
							mEdtText.setText(messageType.getBodyText());
						}
						else{
							mEdtText.setText("");
						}


						List<Field> fields = messageType.getFields();
						mFieldsEditText = new ArrayList<android.widget.EditText>();
						mFieldsEditTextRequired = new ArrayList<Boolean>();
						mFieldsSpinner = new ArrayList<Spinner>();
						mFieldsEditTextLabels = new ArrayList<String>();
						mFieldsSpinnerLabels = new ArrayList<String>();
						mFields.removeAllViews();
						if(fields.size() > 0){
							mFields.setVisibility(View.VISIBLE);
						}
						else{
							mFields.setVisibility(View.GONE);
						}

						for (int i = 0; i < fields.size(); i++){
							Field field = fields.get(i);
							if (field.getFieldType().equals("char")){
								android.widget.EditText myEditText = (android.widget.EditText) getLayoutInflater().inflate(R.layout.edit_text_field, null);
								myEditText.setId((int)System.currentTimeMillis());;
								myEditText.setLayoutParams(new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
								String hint = field.getLabel();
								mFieldsEditTextLabels.add(hint);
								if (field.getRequired()){
									hint = hint + getString(R.string.required);
									mFieldsEditTextRequired.add(true);
								}
								else{
									hint = hint + ":";
									mFieldsEditTextRequired.add(false);
								}
								myEditText.setHint(hint);
								if (field.getDefaultValue() != null){
									myEditText.setText(field.getDefaultValue());
								}
								else{
									myEditText.setText("");

								}
								//myEditText.setId(field.getSlug());
								Log.e("add edit text");
								mFields.addView(myEditText);
								mFieldsEditText.add(myEditText);
							}
							else if (field.getFieldType().equals("choice")){

								ArrayList<String> spinnerArray = new ArrayList<String>();
								List<Object> choices = field.getChoices();
								for(int j = 0; j < choices.size(); j++){
									spinnerArray.add(choices.get(j).toString());
								}

                                TextView label = new TextView(NewMessageActivity.this);
                                label.setText(field.getLabel());
                                label.setPadding(10, 5, 10, -5);
                                mFields.addView(label);

								Spinner spinner = new Spinner(NewMessageActivity.this);
								ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(NewMessageActivity.this, R.layout.spinner_dropdown_item, spinnerArray);
								spinner.setAdapter(spinnerArrayAdapter);

                                mFieldsSpinnerLabels.add(field.getLabel());
								mFields.addView(spinner);
								mFieldsSpinner.add(spinner);
							}
						}

					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
