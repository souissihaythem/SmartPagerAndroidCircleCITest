package net.smartpager.android.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TableLayout;
import android.widget.TextView;
import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.activity.NewMessageActivity.PrepareLocalGroupTask;
import net.smartpager.android.adapters.ChatCursorAdapter;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.CallsSettings;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.QueryMessageThreadTable;
import net.smartpager.android.consts.RequestID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendMessageParams;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.MessageCursorTable;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.dialer.VoipPhone;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.holder.ContactRecipientHolder;
import net.smartpager.android.holder.ImageHolder;
import net.smartpager.android.holder.MessageRecipientHolder;
import net.smartpager.android.holder.chat.ChatItemClickListener;
import net.smartpager.android.model.ImageSource;
import net.smartpager.android.model.ListSerilizableContainer;
import net.smartpager.android.model.Message;
import net.smartpager.android.model.PlayInfo;
import net.smartpager.android.model.PlayInfo.PlayStatus;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.model.SPair;
import net.smartpager.android.notification.Notificator;
import net.smartpager.android.receivers.OutgoingCallReceiver;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.DateTimeUtils;
import net.smartpager.android.utils.IntentFactory;
import net.smartpager.android.utils.PhotoFileUtils;
import net.smartpager.android.utils.TelephoneUtils;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetInboxResponse;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolder;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.utils.Log;
import biz.mobidev.framework.utils.TimeChecker;
import biz.mobidev.framework.view.ExpandableHeightGridView;
import biz.mobidev.framework.view.ShoutGalleryView;

@SuppressWarnings("deprecation")
public class ChatActivity extends BasePlayerActivity implements LoaderCallbacks<Cursor>, ChatItemClickListener,
        MessageRecipientHolder.OnDeleteRecipientClickListener, OnDialogDoneListener {
	
	private static ChatActivity chatActivity;

    public static final String CHAT_INBOX_ACTION = "net.smartpager.android.ACTION_CHAT_INBOX";

    private static final int MAX_ELEMENTS_TO_BLOCK_EXPAND = 3;
    
    private static final int MUTE_SPEAKER = 0;
    private static final int UNMUTE_SPEAKER = 1;
    private static final int MUTE_NOTIFICATIONS = 2;
    private static final int UNMUTE_NOTIFICATIONS = 3;

    private static final long GET_UPDATES_INTERVAL = 30 * DateTimeUtils.MS_IN_SEC;
    private PopupWindow mPopup;

    // private Recipient mFromRecipient;
    private String mMySmartPagerId = SmartPagerApplication.getInstance().getPreferences().getSmartPagerID();
    private Integer mThreadID;
    private String mGroupsID;
    private String mLastMessageId;
    private String mMessageIdToForward;
    private String mLastMessageSubject;
    private String mFromSmartPagerId;
    private String mCallbackNumber = "";
    private int mCallBackMessageID = -1;
    private ChatCursorAdapter mChatMessagesCursorAdapter;

    // private List<IHolderSource> mRecipients;
    private BaseHolderAdapter mRecipientsBaseHolderAdapter;
    private HashMap<String, Integer> mRecipientPositionMap;

    private Uri mNewImageUri;
    private List<IHolderSource> mImageslist;
    private BaseHolderAdapter mImageAdapter;

    @ViewInjectAnatation(viewID = R.id.chat_footer_add_button)
    private View mAddButton;
    @ViewInjectAnatation(viewID = R.id.chat_footer_message_edit_text)
    private EditText mMessageEditText;
    @ViewInjectAnatation(viewID = R.id.chat_list_view)
    private ListView mChatListView;
    @ViewInjectAnatation(viewID = R.id.sliding_arrow_button)
    private ImageButton mArrowButton;
    @ViewInjectAnatation(viewID = R.id.sliding_drawer)
    private SlidingDrawer mSlidingDrawer;
    @ViewInjectAnatation(viewID = R.id.sliding_participants_counter_text_view)
    private TextView mParticipantsCounter;
    @ViewInjectAnatation(viewID = R.id.chat_images_grid_view)
    private ExpandableHeightGridView mImagesGridView;
    @ViewInjectAnatation(viewID = R.id.sliding_page_container)
    private ShoutGalleryView mContainer;
    @ViewInjectAnatation(viewID = R.id.chat_footer)
    private LinearLayout mChatFooter;
    @ViewInjectAnatation(viewID = R.id.text_view_thread_muted)
    private TextView mMutedThreadTextView;
    
    
    private long mDelay;
    private Timer mTimer;

    @Override
    protected boolean isUpNavigationEnabled() {
        return true;
    }

    DataSetObserver mRecipientsDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            setParticipantsCount(mRecipientsBaseHolderAdapter.getCount());
            super.onChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        chatActivity = this;
        
        setContentView(R.layout.activity_chat);
        Injector.doInjection(this);

        mDelay = GET_UPDATES_INTERVAL;
        // mChatListView.setVisibility(View.INVISIBLE);

        mThreadID = getIntent().getIntExtra(DatabaseHelper.MessageTable.threadID.name(), -1);
        getUnreadMessages(mThreadID.toString());

        // mark message read if we came from notification
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(MessageTable.id.name())) {
            String messageId = getIntent().getStringExtra(MessageTable.id.name());
            startMarkMessageRead(messageId);
        }

        mRecipientPositionMap = new HashMap<String, Integer>();
        mRecipientPositionMap.put(mMySmartPagerId, 0);

        mChatMessagesCursorAdapter = new ChatCursorAdapter(this, null, false, mRecipientPositionMap);
        mChatMessagesCursorAdapter.setChatItemClickListener(this);
        mChatListView.setAdapter(mChatMessagesCursorAdapter);
        mChatListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                CursorAdapter adapter = (CursorAdapter) parent.getAdapter();
                Cursor cursor = adapter.getCursor();
                if (cursor.moveToPosition(position)) {
                    boolean isReplyAllowed = (cursor.getInt(cursor.getColumnIndex(MessageTable.replyAllowed.name())) == 1);
                    // TODO Uncomment this line to make the message not replied if the attribute replyAllowed is false
//                    if(isReplyAllowed) {
                        mMessageIdToForward = cursor.getString(MessageTable.id.ordinal());
                        showDialogForwardMessage();
//                    }
                }
                return true;
            }
        });
        // getSupportLoaderManager().initLoader(RECIPIENT_LOADER_ID, null, this);

        // mMessageEditText.setOnEditorActionListener(mOnSendListener);

        // mRecipientsCursorAdapter = new ChatRecipientsCursorAdapter(this, null, false);
        // mContainer.setAdapter(mRecipientsCursorAdapter);
        // mGallery.setAdapter(mRecipientsCursorAdapter);

        mSlidingDrawer.setOnDrawerOpenListener(mDrawerOpenListener);
        mSlidingDrawer.setOnDrawerCloseListener(mDrawerCloseListener);
        mSeekBar.setEnabled(false);
        mMessageEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mChatListView.setSelection(mChatMessagesCursorAdapter.getCount() - 1);
            }
        });
        createRecipientAdapter();
        initImagesCore();
        
        initMutedThreadTextView();
        
    }
    
    public void initMutedThreadTextView() {
    	if (SmartPagerApplication.getInstance().isThreadMuted(getApplicationContext(), mThreadID)) {
    		mMutedThreadTextView.setVisibility(View.VISIBLE);
    		RelativeLayout.LayoutParams tvLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    		mMutedThreadTextView.setLayoutParams(tvLayout);
    		
    		StringBuilder sb = new StringBuilder();
    		sb.append(getString(R.string.this_thread_is_silenced_until));
    		sb.append(SmartPagerApplication.getInstance().threadMutedUntil(getApplicationContext(), mThreadID));
            mMutedThreadTextView.setText(sb.toString());
    		
    		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mChatListView.getLayoutParams();
    		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34 + mMutedThreadTextView.getHeight(), getResources().getDisplayMetrics());
    		layoutParams.topMargin = height;
    		mChatListView.requestLayout();
    	} else {
    		mMutedThreadTextView.setVisibility(View.INVISIBLE);
    		mMutedThreadTextView.setHeight(0);
    		
    		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mChatListView.getLayoutParams();
    		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34, getResources().getDisplayMetrics());
    		layoutParams.topMargin = height;
    		mChatListView.requestLayout();
    		
    	}
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	System.out.println("************ " + SmartPagerApplication.getInstance().getPreferences().getLoudSpeaker());
    	if (SmartPagerApplication.getInstance().getPreferences().getLoudSpeaker()) {
    		menu.add(0, MUTE_SPEAKER, Menu.NONE, "Turn Speaker Off");
    	} else {
    		menu.add(0, UNMUTE_SPEAKER, Menu.NONE, "Turn Speaker On");
    	}
    	if (SmartPagerApplication.getInstance().isThreadMuted(getApplicationContext(), mThreadID)) {
    		menu.add(0, UNMUTE_NOTIFICATIONS, Menu.NONE, "Unsilence Notifications");
    	} else {
    		menu.add(0, MUTE_NOTIFICATIONS, Menu.NONE, "Silence Notifications");
    	}
    	return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	
    	switch (item.getItemId()) {
			case MUTE_SPEAKER:
				lowSpeaker(); 
				break;
			case UNMUTE_SPEAKER:
				loudSpeaker();
				break;
			case MUTE_NOTIFICATIONS:
				Intent intent = new Intent(ChatActivity.this, MuteNotificationsActivity.class);
				intent.putExtra("mThreadID", mThreadID);
				startActivityForResult(intent, RequestID.MUTE_THREAD.ordinal());
				break;
			case UNMUTE_NOTIFICATIONS:
				unmuteNotifications();
				break;
			default:
				break;
		}
    	
    	return false;
    }

    @Override
    protected void onStart() {
        Notificator.cancelAlarm();
        Notificator.cancelAll();
        getSupportLoaderManager().restartLoader(LoaderID.RECIPIENTS_LOADER.ordinal(), null, this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        stopUpdateTimer();
        super.onStop();
    }

    protected void createRecipientAdapter() {
        List<Class<? extends IHolder>> holderClasses = new ArrayList<Class<? extends IHolder>>();
        holderClasses.add(ContactRecipientHolder.class);
        List<IHolderSource> recipients = new ArrayList<IHolderSource>();
        mRecipientsBaseHolderAdapter = new BaseHolderAdapter(this, new ArrayList<IHolderSource>(recipients),
                holderClasses);
        mRecipientsBaseHolderAdapter.registerDataSetObserver(mRecipientsDataSetObserver);
        mContainer.setAdapter(mRecipientsBaseHolderAdapter);
    }

    private void initImagesCore() {
        mImageslist = new ArrayList<IHolderSource>();
        mImageAdapter = new BaseHolderAdapter(this, mImageslist, ImageHolder.class, new Object[] { this });
        mImagesGridView.setAdapter(mImageAdapter);
        mImagesGridView.setExpanded(true);
    }
    
	private void getUnreadMessages(String threadId) {
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.threadId.name(), threadId);
		getSupportLoaderManager().restartLoader(LoaderID.UNREAD_MESSAGE_LOADER.ordinal(), bundle,  this);
	}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        Loader<Cursor> loader = null;
        switch (LoaderID.values()[id]) {
            case MESSAGE_LOADER:
                loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null,
                        mThreadID.toString(), null, null);
                break;
            case RECIPIENTS_LOADER:
                loader = new CursorLoader(this, SmartPagerContentProvider.CONTENT_RECIPIENTS_THREAD_URI, null,
                        mThreadID.toString(), null, null);
                break;
            case UNREAD_MESSAGE_LOADER:
				loader = new MessageCursorTable().getUnreadMessagesByThread(bundle.getString(BundleKey.threadId.name()));
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
            case MESSAGE_LOADER:
                int oldCount = mChatMessagesCursorAdapter.getCount();
                mChatMessagesCursorAdapter.swapCursor(cursor);
                if (cursor.moveToFirst()) {
                    mLastMessageSubject = cursor.getString(MessageTable.subjectText.ordinal());
                    mLastMessageId = cursor.getString(MessageTable.id.ordinal());
                    mGroupsID = cursor.getString(cursor.getColumnIndex(QueryMessageThreadTable.queryGroupID.name()));
                    setTitle(TextUtils.isEmpty(mLastMessageSubject) ? "Exit"
                            : mLastMessageSubject);
                    mFromSmartPagerId = cursor.getString(MessageTable.fromSmartPagerId.ordinal());
                    Recipient fromRecipient = fillFromRecipient(cursor);

                    // to avoid adding VoicePager recipients
                    boolean notInDatabase = fromRecipient.getName().equals("null null");

                    if (!notInDatabase && !mMySmartPagerId.equalsIgnoreCase(fromRecipient.getSmartPagerId())) {
                        if (putRecipient(fromRecipient.getSmartPagerId())
                                || mRecipientsBaseHolderAdapter.getSourceList().isEmpty()) {
                            mRecipientsBaseHolderAdapter.getSourceList().add(fromRecipient);
                            mRecipientsBaseHolderAdapter.notifyDataSetChanged();
                        }
                    }

                    if (oldCount != mChatMessagesCursorAdapter.getCount()) {
                        mChatListView.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                mChatListView.setSelection(mChatMessagesCursorAdapter.getCount() - 1);
                            }
                        }, 0);
                    }
                    
                    boolean replyAllowedIsNull = cursor.isNull(MessageTable.replyAllowed.ordinal());
                    Integer showChatFooter = cursor.getInt(MessageTable.replyAllowed.ordinal());
                    if (replyAllowedIsNull) {
                    	mChatFooter.setVisibility(View.VISIBLE);
                    } else {
                    	if (showChatFooter == 0) {
                        	mChatFooter.setVisibility(View.GONE);
                        } else {
                        	mChatFooter.setVisibility(View.VISIBLE);
                        }
                    }
                    
                }

                break;
            case RECIPIENTS_LOADER:
                if (cursor.moveToFirst()) {
                    synchronized (mRecipientPositionMap) {
                        mRecipientPositionMap.clear();
                        mRecipientPositionMap.put(mMySmartPagerId, 0);
                        mRecipientsBaseHolderAdapter.updateSours(fillRecipientList(cursor));
                        getSupportLoaderManager().restartLoader(LoaderID.MESSAGE_LOADER.ordinal(), null, this);
                    }
                }
                break;
            case UNREAD_MESSAGE_LOADER:
				if(cursor.moveToFirst()){
					ArrayList<Integer>ids = new ArrayList<Integer>();
					do{
						ids.add(cursor.getInt(MessageTable.id.ordinal()));
					}while(cursor.moveToNext());
					markAsRead(ids);
				}
				
			break;
            default:
                break;
        }
    }
    
    private void markAsRead( ArrayList<Integer>ids) {
        Bundle extras = new Bundle();
		extras.putIntegerArrayList(WebSendParam.messageIdList.name(), ids);
        SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageRead, extras);
	}

    private List<IHolderSource> fillRecipientList(Cursor cursor) {
        List<IHolderSource> recipients = new ArrayList<IHolderSource>();
        if (cursor.moveToFirst()) {
            do {

                Recipient recipient = Recipient.createFromCursor(cursor);
                recipient.setDisabled(true);
                if (!mMySmartPagerId.equalsIgnoreCase(recipient.getSmartPagerId())) {
                    recipients.add(recipient);
                }
                putRecipient(recipient.getSmartPagerId());

            } while (cursor.moveToNext());
        }
        return recipients;
    }

    private boolean putRecipient(String recipientSmartPagerId) {
        boolean isAdded = false;
        if (!mRecipientPositionMap.containsKey(recipientSmartPagerId)) {
            isAdded = true;
            // 0 is special index ONLY for SELF bubble, so we add 1 to result, and remove 1 in numerator and
            // denominator!
            int pos = 1 + (mRecipientPositionMap.size() - 1) % (mChatMessagesCursorAdapter.getViewTypeCount() - 1);
            mRecipientPositionMap.put(recipientSmartPagerId, pos);
        }
        return isAdded;
    }

    private Recipient fillFromRecipient(Cursor cursor) {
        int spin = MessageTable.values().length;
        String id = cursor.getString(spin + ContactTable.id.ordinal());
        String name = cursor.getString(spin + ContactTable.firstName.ordinal()) + " "
                + cursor.getString(spin + ContactTable.lastName.ordinal());
        String phone = cursor.getString(spin + ContactTable.phoneNumber.ordinal());
        String status = cursor.getString(cursor.getColumnIndex(ContactTable.status.name()));
        String title = cursor.getString(spin + ContactTable.title.ordinal());
        String imageUrl = cursor.getString(spin + ContactTable.photoUrl.ordinal());
        Recipient fromRecipient = new Recipient();
        fromRecipient.setId(id);
        fromRecipient.setGroup(false);
        fromRecipient.setImageUrl(imageUrl);
        fromRecipient.setName(name);
        fromRecipient.setPhone(phone);
        fromRecipient.setSmartPagerId(mFromSmartPagerId);
        fromRecipient.setStatus(status);
        fromRecipient.setTitle(title);
        fromRecipient.setDisabled(true);
        return fromRecipient;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        int id = loader.getId();
        switch (LoaderID.values()[id]) {
            case MESSAGE_LOADER:
                // mChatMessagesCursorAdapter.swapCursor(null);
                break;
            case RECIPIENTS_LOADER:
                // mRecipientsCursorAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

    // ChatItemClickListener
    @Override
    public void onImageClick(int messageId) {
        Intent intent = new Intent(this, ImageGalleryActivity.class);
        intent.putExtra(WebSendParam.messageId.name(), messageId);
        startActivity(intent);
    }

    @Override
    public void onCallBackClick(String number, int messageId) {
        mCallBackMessageID = messageId;
        mCallbackNumber = number;
        
        if (SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled()) {
        	makeVoipCall(number);
        	return;
        }
        
        String pagerNumber = SmartPagerApplication.getInstance().getPreferences().getPagerNumber();
        String routingNumber = SmartPagerApplication.getInstance().getPreferences().getCallRouterPhoneNumber();
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.messageId.name(), String.valueOf(messageId));
        SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageReplied, extras);
        switch (SmartPagerApplication.getInstance().getSettingsPreferences().getBlockMyNumber()) {
            case off:
                pushOutgoingCallNumber(mCallbackNumber, mCallBackMessageID);
                TelephoneUtils.dial(mCallbackNumber);
                break;
            case ask:
                showDialogBlockId(mCallbackNumber);
                break;
            case on:
                startArmCallback(mCallbackNumber);
                break;
            default:
                startArmCallback(mCallbackNumber);
                break;
        }
    }
    
    public void makeVoipCall(final String number) {
    	AudioManagerUtils.setLoudSpeaker(false);
		showProgressDialog("Connecting VOIP Call");
    	RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        
        AsyncHttpClient httpClient = new AsyncHttpClient();        
        httpClient.post(this, Constants.BASE_REST_URL + "/getTwilioClientToken", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//				System.out.println("successful response: " + response);
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
    
    public void showAlertFragmentDialogYesNo(String number) {
    	AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.attention), getString(R.string.block_my_number_alert), number, true).show(getSupportFragmentManager(), FragmentDialogTag.AlertBlockMyNumber.name());
    }

    private void pushOutgoingCallNumber (String number, int messageID)
    {
        Intent outgoingReceiver = OutgoingCallReceiver.createOutgoingCallNumberAction(number, messageID != -1 ? String.valueOf(messageID) : null);
        sendBroadcast(outgoingReceiver);
    }

    public void startArmCallback(String number) {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.phoneNumber.name(), number);
        SmartPagerApplication.getInstance().startWebAction(WebAction.armCallback, extras);
        showProgressDialog(getString(R.string.sending_request));
    }

    public void makeArmCallBack() {
        String pagerNumber = SmartPagerApplication.getInstance().getPreferences().getPagerNumber();
        String routerNumber = SmartPagerApplication.getInstance().getPreferences().getCallRouterPhoneNumber();
        
        if (TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routerNumber)) {
        	// !PagerNo + !RouterNo
            showErrorDialog(getString(R.string.your_pager_number_is_invalid));
        } else if (!TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routerNumber)) {
        	// PagerNo + !RouterNo
            pushOutgoingCallNumber(mCallbackNumber, mCallBackMessageID);
            TelephoneUtils.dial(pagerNumber);
        } else if (TextUtils.isEmpty(pagerNumber) && !TextUtils.isEmpty(routerNumber)) {
        	// !PagerNo + RouterNo
        	pushOutgoingCallNumber(mCallbackNumber, mCallBackMessageID);
            TelephoneUtils.dial(routerNumber);
        } else {
        	// PagerNo + RouterNo
        	pushOutgoingCallNumber(mCallbackNumber, mCallBackMessageID);
            TelephoneUtils.dial(pagerNumber);
        }
    }

    @SuppressWarnings("unused")
    private String getSequrityNumber(String number) {
        String phone = SmartPagerApplication.getInstance().getSettingsPreferences().getBlockingPrefix();
        if ((number.length() > 10) && (number.charAt(0) == '1')) {
            phone += number.substring(1);
        } else {
            phone += number;
        }
        return phone;
    }

    @Override
    public void onContactClick(String contactId) {
        Intent intent = new Intent(this, ContactDetailsActivity.class);
        intent.putExtra(GroupContactItem.id.name(), contactId);
        startActivity(intent);
    }

    private void sendMessage(String message) {

        // stopForvardPlayFile();
        // onPlayerStatusChenge(PlayStatus.forvard_stop, PlayInfo.createPlayerInfor(PlayStatus.forvard_stop));
        TimeChecker.SetStartPoint();
        // stopUpdateTimer();
        ArrayList<String> imagesUris = new ArrayList<String>();
        for (IHolderSource item : mImageslist) {
            ImageSource image = (ImageSource) item;
            imagesUris.add(image.getFullImageUri().toString());
        }
        final Bundle extras = new Bundle();
        extras.putString(WebSendMessageParams.text.name(), message);
        extras.putString(WebSendMessageParams.subjectText.name(), mLastMessageSubject);
        extras.putInt(WebSendMessageParams.threadID.name(), mThreadID);
        extras.putStringArrayList(WebSendMessageParams.recipientsGroup.name(), getCurrGroupID());
        extras.putString(WebSendMessageParams.id.name(), mLastMessageId);
        extras.putBoolean(WebSendMessageParams.isFastAdd.name(), true);
        extras.putStringArrayList(WebSendMessageParams.imagesUriToSend.name(), imagesUris);
//		intent.putStringArrayListExtra(WebSendMessageParams.recipients.name(), getRecipientsSmartPagerId());
        extras.putSerializable(WebSendMessageParams.recipients.name(), getRecipientsIds());

        if (new File(mLocalRecordPath).exists()) {
            extras.putString(WebSendMessageParams.recordUriToSend.name(), mRealRecordPath);
        }
        showProgressDialog(getString(R.string.sending_message));
        
		
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
				SmartPagerApplication.getInstance().startWebAction(WebAction.sendMessage, extras);
			}
			

		});
        
        
        
        
        
        
        
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
    	
    	FragmentTransaction transaction = chatActivity.getSupportFragmentManager().beginTransaction();
		AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(chatActivity.getApplicationContext(), "Network Connection Issue",
				"There is an intermittant issue with the" + chatActivity.checkNetworkStatus() + " network.\n\n Would you like to try sending your message again?", "", false);
		dialog.show(transaction, FragmentDialogTag.Retry.name());
    	
//		AlertDialog.Builder dialog = new AlertDialog.Builder(chatActivity);
//		dialog.setTitle("Network Connection Issue");
//		dialog.setMessage("There is an intermittant network issue.\n\n Would you like to try sending your message again?");
//		dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				View sendButton = (View) chatActivity.findViewById(R.id.page_send_button);
//				sendButton.performClick();
//			}
//		});
//		dialog.setNegativeButton("No", null);
//		dialog.show();
		
	}
    
    private void unmuteNotifications() {
		
		JSONObject jsonParams = new JSONObject();
		try {
			jsonParams.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
			jsonParams.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
			jsonParams.put("threadId", mThreadID);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringEntity entity = null;
		try {
			entity = new StringEntity(jsonParams.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		RequestParams params = new RequestParams();
//		params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
//		params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
//		params.put("threadId", threadId);
//		params.put("expiry", calculateExpiryDate(hourValues.get(position)));
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(getApplicationContext(), Constants.BASE_REST_URL + "/unsuppressThread", entity, "application/json", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.e("ChatActivityxx", "response: " + response);
				SmartPagerApplication.getInstance().unMuteThread(getApplicationContext(), mThreadID);
				
				mMutedThreadTextView.setVisibility(View.INVISIBLE);
	    		mMutedThreadTextView.setHeight(0);
	    		
	    		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mChatListView.getLayoutParams();
	    		int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34, getResources().getDisplayMetrics());
	    		layoutParams.topMargin = height;
	    		mChatListView.requestLayout();
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("Failure: " + responseBody);
			}
		});
    }

    private ArrayList<String> getCurrGroupID ()
    {
        ArrayList<String> res = new ArrayList<String>();
        if(mGroupsID == null)
            return res;
        String[] groups = mGroupsID.split(",");
        for(String group : groups)
            res.add(group);
        return res;
    }

    @Override
    public void onSuccessResponse(WebAction action, AbstractResponse responce) {

        switch (action) {
            case sendMessage:
                hideProgressDialog();
                // Log.END();
                getWindow().getDecorView().post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        TimeChecker.WriteTimeSpendInMiliSeconds();
                        Notificator.notifySent();
                        mMessageEditText.setText("");
                        mImageslist.clear();
                        notifyImageAdapter();
                        File voiceMessage = new File(getActivityRecordFilePath());
                        if (voiceMessage.exists()) {
                            voiceMessage.delete();
                        }
                        mPlayerView.setVisibility(View.GONE);
                        // startUpdateTimer();
                        getSupportLoaderManager().restartLoader(LoaderID.RECIPIENTS_LOADER.ordinal(), null,
                                ChatActivity.this);
                    }
                });
                // getSupportLoaderManager().restartLoader(MESSAGE_LOADER_ID, null, this);
                break;
            case getInbox:
                GetInboxResponse inboxResponce = (GetInboxResponse) responce;
                // for (SPair<Integer, String> item : inboxResponce.getIds()) {
                for (SPair<Integer, Message> item : inboxResponce.getIds()) {
                    if (mThreadID.intValue() == item.first.intValue()) {
                        // startMarkMessageRead(item.second);
                        if (!item.second.isAcceptanceRequested()) {
                            startMarkMessageRead(item.second.getId());
                        }
                    }
                }
                getSupportLoaderManager().restartLoader(LoaderID.RECIPIENTS_LOADER.ordinal(), null, this);
                break;
            case armCallback:
                hideProgressDialog();
                makeArmCallBack();
                break;
            default:
                break;
        }
    }

    private void startMarkMessageRead(String messageId) {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.messageId.name(), messageId);
        SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageRead, extras);
    }

    private void startMarkMessageCalled (String messageID) {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.messageId.name(), messageID);
        SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageCalled, extras);
    }

    @Override
    protected void onErrorResponse(WebAction action, AbstractResponse response) {
        hideProgressDialog();
        if (action == WebAction.armCallback) {
            showDialogUnableBlock(mCallbackNumber);
        } else {
            super.onErrorResponse(action, response);
        }
    }

    private SPair<ArrayList<String>, ArrayList<String>> getRecipientsIds() {

        ArrayList<String> smpIdsList = new ArrayList<String>();
        ArrayList<String> cntIdsList = new ArrayList<String>();

        for (IHolderSource source : mRecipientsBaseHolderAdapter.getSourceList()) {
            Recipient recipient = (Recipient) source;
            String smpId = recipient.getSmartPagerId();
            if (!smpId.equals(mMySmartPagerId)) {
                String cntId = recipient.getId();
                smpIdsList.add(smpId);
                cntIdsList.add(cntId);
            }
        }

        return new SPair<ArrayList<String>, ArrayList<String>>(smpIdsList, cntIdsList);
    }

//	private ArrayList<String> getRecipientsSmartPagerId() {
//		ArrayList<String> smartPagerIdList = new ArrayList<String>();
//		for (IHolderSource recipient : mRecipientsBaseHolderAdapter.getSourceList()) {
//			String spId = ((Recipient) recipient).getSmartPagerId();
//			if (!spId.equals(mMySmartPagerId)) {
//				smartPagerIdList.add(spId);
//			}
//		}
//		return smartPagerIdList;
//	}

    private void setParticipantsCount(int count) {
        String suffix = (count == 1) ? "" : "s";
        mParticipantsCounter.setText(String.valueOf(count) + " Participant" + suffix);
    }

    @ClickListenerInjectAnatation(viewID = R.id.page_send_button)
    public void onSendMessageClick(View view) {
        String messageText = mMessageEditText.getText().toString();
        sendMessage(messageText);
        ChatActivity.this.hideSoftKeyboard(mMessageEditText);
    }

    private OnDrawerOpenListener mDrawerOpenListener = new OnDrawerOpenListener() {
        @Override
        public void onDrawerOpened() {
            mArrowButton.setImageResource(R.drawable.chat_ic_slider_up);
        }
    };

    private OnDrawerCloseListener mDrawerCloseListener = new OnDrawerCloseListener() {
        @Override
        public void onDrawerClosed() {
            mArrowButton.setImageResource(R.drawable.chat_ic_slider_down);
        }
    };

    // ----------------------------------------------------------------------------------------
    // Popup window processing
    @ClickListenerInjectAnatation(viewID = R.id.chat_footer_add_button)
    public void onAttachClick(View view) {
        showPopup(view);
    }

    private void showPopup(View view) {

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Point p = new Point();
        p.x = location[0];
        p.y = location[1];

        int visibleMenuItems = 5; // check this if you want add/remove popp menu item
        int menuItemHeight = 40;
        int height = visibleMenuItems * menuItemHeight;

        int popupWidth = (int) (200 * getResources().getDisplayMetrics().density);
        int popupHeight = (int) (height * getResources().getDisplayMetrics().density);

        // Inflate the popup_layout.xml
        LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popup_container);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.popup_chat, viewGroup);

        // Creating the PopupWindow
        mPopup = new PopupWindow(this);
        mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.def_bg_popup_attachment_static));
        mPopup.setContentView(layout);
        mPopup.setWidth(popupWidth);
        mPopup.setHeight(popupHeight);
        mPopup.setFocusable(true);

        int OFFSET_X = 30;
        int OFFSET_Y = -popupHeight;

        // FIXME according to SPANDROID-44 Add Users Vs Forward
        layout.findViewById(R.id.popup_forward_layout).setVisibility(View.GONE);
        layout.findViewById(R.id.popup_forward_divider).setVisibility(View.GONE);
        layout.findViewById(R.id.popup_quick_responce_layout).setOnClickListener(mQuickResponceListener);
        layout.findViewById(R.id.popup_forward_layout).setOnClickListener(mForwardListener);
        layout.findViewById(R.id.popup_select_image_layout).setOnClickListener(mAttachImageListener);
        layout.findViewById(R.id.popup_take_photo_layout).setOnClickListener(mAttachPhotoListener);
        layout.findViewById(R.id.popup_record_audio_layout).setOnClickListener(mAttachAudioListener);
        layout.findViewById(R.id.popup_add_user_layout).setOnClickListener(mAddUserListener);

        mPopup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
    }

    private OnClickListener mAddUserListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent intent = new Intent(ChatActivity.this, NewMessageActivity.class);
            ListSerilizableContainer listSerilizableContainer = new ListSerilizableContainer(
                    mRecipientsBaseHolderAdapter.getSourceList());
            intent.putExtra(BundleKey.recipientList.name(), listSerilizableContainer);
            intent.putExtra(BundleKey.onlyContacts.name(), true);
            intent.putExtra(WebSendMessageParams.threadID.name(), mThreadID);
            intent.putExtra(WebSendMessageParams.subjectText.name(), mLastMessageSubject);
            startActivity(intent);

            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    };

    private OnClickListener mQuickResponceListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent data = new Intent(ChatActivity.this, QuickResponseChooserActivity.class);
            startActivityForResult(data, RequestID.CHOOSE_QUICK_RESPONSE.ordinal());
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    };

    private OnClickListener mForwardListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mPopup != null) {
                mPopup.dismiss();
            }
            Intent intent = new Intent(ChatActivity.this, NewMessageActivity.class);
            final Cursor cursor = mChatMessagesCursorAdapter.getCursor();
            cursor.moveToLast();
            intent.putExtra(BundleKey.messageId.name(), cursor.getString(MessageTable.id.ordinal()));
            startActivity(intent);
        }
    };

    private OnClickListener mAttachImageListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                Pair<Intent, Uri> chooser = IntentFactory.getImageIntent(ChatActivity.this);
                mNewImageUri = chooser.second;
                setLocPin(true);
                startActivityForResult(chooser.first, RequestID.PICK_PHOTO.ordinal());
            } catch (Exception e) {
                showToast(e.getMessage());
            }
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    };

    private OnClickListener mAttachPhotoListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Pair<Intent, Uri> chooser = IntentFactory.getCameraIntent(ChatActivity.this, false);
                mNewImageUri = chooser.second;
                setLocPin(true);
                startActivityForResult(chooser.first, RequestID.PICK_PHOTO.ordinal());
            } catch (Exception e) {}
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    };

    private OnClickListener mAttachAudioListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            stopForvardPlayFile();
            onPlayerStatusChange(PlayStatus.forvard_stop, PlayInfo.createPlayerInfor(PlayStatus.forvard_stop));
            Intent intent = new Intent(ChatActivity.this, NewVoiceMessageActivity.class);
            intent.putExtra(BundleKey.recordPath.name(), getActivityRecordFilePath());
            startActivityForResult(intent, RequestID.ADD_VOICE_RECORD.ordinal());
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    };

    @Override
    public void onDeleteImageClick(ImageSource imageSource) {
        mImageslist.remove(imageSource);
        notifyImageAdapter();
    };

    // ----------------------------------------------------------------------------------------
    private void notifyImageAdapter() {
        boolean expanded = mImageslist.size() < MAX_ELEMENTS_TO_BLOCK_EXPAND;
        mImagesGridView.setExpanded(expanded);
        mImageAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (RequestID.values()[requestCode]) {
            case PICK_PHOTO:
                if (resultCode == RESULT_OK) {
                    processGetImageFromCamera(data);
                }
                break;
            case ADD_RECIPIENT:
                if (resultCode == RESULT_OK && data.getExtras().containsKey(BundleKey.recipient.name())) {
                    ListSerilizableContainer listSerilizableContainer = (((ListSerilizableContainer) data
                            .getSerializableExtra(BundleKey.recipient.name())));
                    mRecipientsBaseHolderAdapter.updateSours(listSerilizableContainer.getList());
                    // Log.e(" ===================== Got activity result!");
                    // getSupportLoaderManager().restartLoader(RECIPIENT_LOADER_ID, null, this);
                }
                break;
            case CHOOSE_QUICK_RESPONSE:
                if (resultCode == RESULT_OK && data != null
                        && data.getExtras().containsKey(BundleKey.quickResponce.name())) {
                    String quickResponce = data.getStringExtra(BundleKey.quickResponce.name());
                    if (!TextUtils.isEmpty(quickResponce)) {
                        String newLine = (TextUtils.isEmpty(mMessageEditText.getText().toString())) ? "" : "\n";
                        mMessageEditText.append(newLine + quickResponce);
                    }
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
            case MUTE_THREAD:
            	if (resultCode == RESULT_OK) {
            		Log.e("ChatActivity", "MUTE_THREAD result ok");
            		initMutedThreadTextView();
            	}
            default:
                break;
        }
    }

    private void processGetImageFromCamera(Intent data) {
        try {
            boolean isCameraImage = true;
            if (data != null && data.getData() != null) {
                mNewImageUri = data.getData();
                isCameraImage = false;
            }
            String photoPath = PhotoFileUtils.getPath(this, mNewImageUri);
            Bitmap scaledBmp = PhotoFileUtils.getScaledBmpFromFile(photoPath);
            if (null != scaledBmp) {
                // Save scaled bitmap to File and get path
                String newImageFileName = PhotoFileUtils.saveScaledImage(this, scaledBmp, !isCameraImage);
                ImageSource imageSource = new ImageSource(Uri.parse(newImageFileName));
                imageSource.setFullImageUri(Uri.parse(newImageFileName));
                
                mImageslist.add(imageSource);
                notifyImageAdapter();
                
                if (isCameraImage) {
					File file = new File(photoPath);
					if (!file.exists()) {
						return;
					}
					file.delete();
				}
                
            } else {
                showToast(getString(R.string.no_such_file_or_directory));
            }
        } catch (Exception e) {
            mNewImageUri = null;
            showToast(e.getMessage());
            // showToast(getString(R.string.please_select_either_jpg_or_png_file));
        }finally {
        }
    }

    @Override
    public void onDeleteRecipientClick(Recipient deleteRecipient) {

    }

    // ----------------------------------------------------------------------------------------
    // Audio Processing
    @ClickListenerInjectAnatation(viewID = R.id.player_action_button)
    public void startPlayVoiceMessage(View v) {
        switchPlayMode();
    };

    // ----------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        //Next line is commented due to fix BUG SPANDROID-162: App is crazy chatty!!!
//		startUpdateTimer();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CHAT_INBOX_ACTION);
        registerReceiver(mNewInboxReceiver, intentFilter);
        pushOutgoingCallNumber(null, -1);
        initMutedThreadTextView();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mNewInboxReceiver);
        super.onPause();
    }

    private void startUpdateTimer() {

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                Bundle extras = new Bundle();
                extras.putBoolean(BundleKey.initiatedByUser.name(), false);
                SmartPagerApplication.getInstance().startWebAction(WebAction.getUpdates, extras);
            }
        }, mDelay, GET_UPDATES_INTERVAL);
        mDelay = 0;
    }

    private void stopUpdateTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
    }

    @Override
    public void onBackPressed() {
        File voiceMessage = new File(mLocalRecordPath);
        if (!TextUtils.isEmpty(mMessageEditText.getText().toString().trim()) || mImageslist.size() > 0
                || voiceMessage.exists()) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.close),
                    getString(R.string.are_you_sure_you_want_to_cancel_this_message), "", true);
            dialog.show(transaction, FragmentDialogTag.IsExit.name());
        } else {
            super.onBackPressed();
        }
    };

    @Override
    public void onDialogDone(FragmentDialogTag tag, String data) {
        switch (tag) {
            case AlertInforDialogYesNo:
                startArmCallback(data);
                break;
            case IsExit:
                File voiceMessage = new File(getActivityRecordFilePath());
                if (voiceMessage.exists()) {
                    voiceMessage.delete();
                }
                finish();
                break;
            case ForwardMessage:
                Intent intent = new Intent(this, NewMessageActivity.class);
                intent.putExtra(BundleKey.messageId.name(), mMessageIdToForward);
                startActivity(intent);
                break;
            case UnableToBlockId:
                pushOutgoingCallNumber(mCallbackNumber, mCallBackMessageID);
                TelephoneUtils.dial(data);
                break;
            case AlertBlockMyNumber:
                SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.off);
                showToast(R.string.block_my_number_turned_off);
                startArmCallback(data);
                break;
            case Retry:
				View sendButton = (View) chatActivity.findViewById(R.id.page_send_button);
				sendButton.performClick();
				break;
            default:
                break;
        }

    }

    @Override
    public void onDialogNo(FragmentDialogTag tag, String data) {
        switch (tag) {
            case AlertInforDialogYesNo:
                pushOutgoingCallNumber(mCallbackNumber, mCallBackMessageID);
                TelephoneUtils.dial(data);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPlayerStatusChange(PlayStatus status, PlayInfo playInfo) {

        Bundle bundle = new Bundle();
        bundle.putSerializable(BundleKey.playInfo.name(), playInfo);
        mChatMessagesCursorAdapter.setPresetData(bundle);
        mChatMessagesCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onChatItemSwitchPlayMode(PlayInfo playInfo) {

        sendComandToPlayServer(playInfo);
    }

    @Override
    public void onRejectAcceptanceReq(long msgId) {
        ContentValues values = new ContentValues();
        values.put(MessageTable.requestAcceptanceShow.name(), 0);
        getContentResolver().update(SmartPagerContentProvider.CONTENT_MESSAGE_URI, values,
                MessageTable.id.name() + " = '" + msgId + "'", null);
        getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.messageId.name(), String.valueOf(msgId));
        SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageRejected, extras);
    }

    @Override
    public void onAcceptAcceptanceReq(long msgId) {
        ContentValues values = new ContentValues();
        values.put(MessageTable.requestAcceptanceShow.name(), 0);
        getContentResolver().update(SmartPagerContentProvider.CONTENT_MESSAGE_URI, values,
                MessageTable.id.name() + " = '" + msgId + "'", null);
        getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.messageId.name(), String.valueOf(msgId));
        SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageAccepted, extras);
    }

    // ---- DIALOGS ----------------------------------------------------------------------
    protected void showDialogForwardMessage() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.forward_message),
                getString(R.string.do_you_want_to_forward_this_message_), "", false, this);
        dialog.show(transaction, FragmentDialogTag.ForwardMessage.name());
    }

    public void showDialogBlockId(String number) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.security),
                getString(R.string.do_you_want_to_block_your_id), number, true);
        dialog.show(transaction, FragmentDialogTag.AlertInforDialogYesNo.name());
    }

    protected void showDialogUnableBlock(String number) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getApplicationContext(), getString(R.string.security),
                getString(R.string.we_are_unable_to_block), number, false, this);
        dialog.show(transaction, FragmentDialogTag.UnableToBlockId.name());
    }

    // ---------------------------------------------------------------------------------

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mNewImageUri != null) {
            outState.putString(BundleKey.imageUri.name(), mNewImageUri.toString());
        }
        if (mLocalRecordPath != null) {
            outState.putString(BundleKey.localRecordPath.name(), mLocalRecordPath);
        }
        if (mRealRecordPath != null) {
            outState.putString(BundleKey.realRecordPath.name(), mRealRecordPath);
        }
        // images -----------------------------------------------------------
        ListSerilizableContainer imagesContainer = new ListSerilizableContainer(mImageslist);
        outState.putSerializable(BundleKey.imagesList.name(), imagesContainer);
        // recipients-----------------------------------------------------------
        ListSerilizableContainer listSerilizableContainer = new ListSerilizableContainer(
                mRecipientsBaseHolderAdapter.getSourceList());
        outState.putSerializable(BundleKey.recipient.name(), listSerilizableContainer);
        // -----------------------------------------------------------

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
        ListSerilizableContainer listSerilizableContainer = (((ListSerilizableContainer) savedInstanceState
                .getSerializable(BundleKey.recipient.name())));
        mRecipientsBaseHolderAdapter.updateSours(listSerilizableContainer.getList());
        // --------------------------------------
    }

    @Override
    public void onResponseOptionClick(String respText) {
        // Toast.makeText(this, "Responce Options Click stub: " + respText, Toast.LENGTH_SHORT).show();
        sendMessage(respText);
    }

    @Override
    public void onRecipientsClick(int listPosition, boolean isShown) {

    }

    // ---- New Inbox in Chat Mode ------------------------------------------------------
    private BroadcastReceiver mNewInboxReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CHAT_INBOX_ACTION)) {
                int threadID = intent.getIntExtra(MessageTable.threadID.name(), -1);
                if (threadID == mThreadID) {
                    Notificator.notifyInboxChatSelfThread(intent.getExtras());
                } else {
                    Notificator.notifyInboxChatAnotherThread(intent.getExtras());
                    showToast(getString(R.string.new_inbox));
                }
            }
        }
    };
}
