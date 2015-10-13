package net.smartpager.android.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.ChatActivity;
import net.smartpager.android.activity.MuteNotificationsActivity;
import net.smartpager.android.adapters.MessagesListCursorAdapter;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.RequestID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ChatsTable;
import net.smartpager.android.database.ChatsTable.MessageType;
import net.smartpager.android.database.CursorTable;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.service.ArchiveService;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import biz.mobidev.framework.utils.Log;

// Show list of archived messages
public class ArchiveMessagesListFagment extends AbstractFragmentList 
									   implements OnDialogDoneListener,
									   			  LoaderCallbacks<Cursor> {
	
	private String mThreadIdToRemove = "";
	
	@Override
	protected CursorAdapter createAdapter() {
		
		mList.setOnItemClickListener( new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openMessage(position);
			}
			
		});	
		
		mList.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				deleteMessage(position);
				return true;
			}
		});


		createSwipeMenuList();
//		return new MessageCursorAdapter(getActivity(), null, true);
		return new MessagesListCursorAdapter(getActivity(), null, true);
	}

	protected void showDialogDeleteMessage() {
		
		FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
		AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getActivity().getApplicationContext(), getString(R.string.dialog_delete_message_title),
				getString(R.string.dialog_delete_message_msg), "", false, this);
		dialog.show(transaction, FragmentDialogTag.DeleteThisMessageDialog.name());
	}

	@Override
	protected CursorTable createTable() {
		return  new ChatsTable(true);
	}

	@Override
	protected int getFragmentLayout() {
		return R.layout.fragment_messages_list;
	}

	@Override
	protected int getListViewID() {
		return R.id.message_listView;
	}

	@Override
	protected void initView(View rootView) {
		
	}

	@Override
	protected int getLoaderID() {
		return LoaderID.ARCHIVE_MESSAGE_LOADER.ordinal();
	}

	@Override
	public void onDialogDone(FragmentDialogTag tag, String data) {
		Intent intent = new Intent(getActivity(), ArchiveService.class);		
		intent.setAction(ArchiveService.ArchiveActions.removeFromArchive.name());
		intent.putExtra(BundleKey.threadId.name(), mThreadIdToRemove);
		getActivity().startService(intent);
		mThreadIdToRemove = "";
	}

	@Override
	public void onDialogNo(FragmentDialogTag tag, String data) {
		mThreadIdToRemove = "";		
	}

	private void createSwipeMenuList(){
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {

				switch (menu.getViewType()) {
					case 0:
						createMenuSilence(menu);
						break;
					case 1:
						createMenuUnSilence(menu);
						break;
				}
			}
		};

		// set creator
		((SwipeMenuListView) mList).setMenuCreator(creator);

		((SwipeMenuListView) mList).setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
				switch (index) {
					case 0:
						// open
						Cursor cursor = (Cursor) mCursorAdapter.getItem(position);
						String threadId = cursor.getString(DatabaseHelper.MessageTable.threadID.ordinal());
						if (mCursorAdapter.getItemViewType(position) == 0){
							muteNotification(Integer.parseInt(threadId));
						}
						else{
							unmuteNotifications(Integer.parseInt(threadId));
						}
						break;
					case 1:
						// delete
						deleteMessage(position);
						break;
				}
				// false : close the menu; true : not close the menu
				return false;
			}
		});
	}

	private void createMenuSilence(SwipeMenu menu){
		// create "open" item
		SwipeMenuItem openItem = new SwipeMenuItem(
				getActivity().getApplicationContext());

		// set item background
		openItem.setBackground(R.color.gray_midle_background);
		// set item width
		openItem.setWidth(dp2px(90));
		openItem.setTitle("Silence");

		// set item title fontsize
		openItem.setTitleSize(18);
		// set item title font color
		openItem.setTitleColor(Color.WHITE);
		// add to menu
		menu.addMenuItem(openItem);

		// create "delete" item
		SwipeMenuItem deleteItem = new SwipeMenuItem(
				getActivity().getApplicationContext());
		// set item background
		deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
				0x3F, 0x25)));
		// set item width
		deleteItem.setWidth(dp2px(90));
		// set item title
		deleteItem.setTitle("Delete");
		// set item title fontsize
		deleteItem.setTitleSize(18);
		// set item title font color
		deleteItem.setTitleColor(Color.WHITE);
		// add to menu
		menu.addMenuItem(deleteItem);
	}

	private void createMenuUnSilence(SwipeMenu menu){
// create "open" item
		SwipeMenuItem openItem = new SwipeMenuItem(
				getActivity().getApplicationContext());

		// set item background
		openItem.setBackground(R.color.gray_midle_background);
		// set item width
		openItem.setWidth(dp2px(90));

		openItem.setTitle("Unsilence");

		// set item title fontsize
		openItem.setTitleSize(18);
		// set item title font color
		openItem.setTitleColor(Color.WHITE);
		// add to menu
		menu.addMenuItem(openItem);

		// create "delete" item
		SwipeMenuItem deleteItem = new SwipeMenuItem(
				getActivity().getApplicationContext());
		// set item background
		deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
				0x3F, 0x25)));
		// set item width
		deleteItem.setWidth(dp2px(90));
		// set item title
		deleteItem.setTitle("Delete");
		// set item title fontsize
		deleteItem.setTitleSize(18);
		// set item title font color
		deleteItem.setTitleColor(Color.WHITE);
		// add to menu
		menu.addMenuItem(deleteItem);
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}

	private void openMessage(int position){
		CursorAdapter adapter = (CursorAdapter)mCursorAdapter;
		Cursor cursor = (Cursor) adapter.getItem(position);
		int threadId = cursor.getInt(DatabaseHelper.MessageTable.threadID.ordinal());
		String messageId = cursor.getString(DatabaseHelper.MessageTable.id.ordinal());
		/*************/
//				TestLogUtils.showMessageData(cursor);
//				if (true) return;
		/*************/
		if((cursor.getLong(DatabaseHelper.MessageTable.readTime.ordinal())==Constants.DEFAULT_DATE.getTime())&&(cursor.getInt(DatabaseHelper.MessageTable.messageType.ordinal())==MessageType.inbox.ordinal())){
			Bundle extras = new Bundle();
			extras.putString(WebSendParam.messageId.name(), messageId);
			SmartPagerApplication.getInstance().startWebAction(WebAction.markMessageRead, extras);
		}
		Intent intent = new Intent(
				SmartPagerApplication.getInstance().getApplicationContext(),
				ChatActivity.class);
		intent.putExtra(DatabaseHelper.MessageTable.threadID.name(), threadId);
		startActivity(intent);
	}

	private void deleteMessage(int position){
		CursorAdapter adapter = (CursorAdapter)mCursorAdapter;
		Cursor cursor = adapter.getCursor();
		if (cursor.moveToPosition(position)){
			mThreadIdToRemove = cursor.getString(MessageTable.threadID.ordinal());
			showDialogDeleteMessage();
		}
	}

	private void muteNotification(Integer mThreadID){
		Intent intent = new Intent(getActivity(), MuteNotificationsActivity.class);
		intent.putExtra("mThreadID", mThreadID);
		startActivityForResult(intent, RequestID.MUTE_THREAD.ordinal());
	}

	private void unmuteNotifications(final Integer mThreadID) {

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
		client.post(getActivity().getApplicationContext(), Constants.BASE_REST_URL + "/unsuppressThread", entity, "application/json", new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				Log.e("ChatActivityxx", "response: " + response);
				SmartPagerApplication.getInstance().unMuteThread(getActivity().getApplicationContext(), mThreadID);
				mCursorAdapter.notifyDataSetChanged();
				/*mMutedThreadTextView.setVisibility(View.INVISIBLE);
				mMutedThreadTextView.setHeight(0);

				RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mChatListView.getLayoutParams();
				int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 34, getResources().getDisplayMetrics());
				layoutParams.topMargin = height;
				mChatListView.requestLayout();*/
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("Failure: " + responseBody);
			}
		});
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		super.onLoadFinished(loader, cursor);
		mCursorAdapter.notifyDataSetChanged();
	}
}
