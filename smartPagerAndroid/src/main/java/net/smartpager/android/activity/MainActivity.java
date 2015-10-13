package net.smartpager.android.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.rey.material.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.newrelic.agent.android.NewRelic;
import com.rey.material.widget.TabPageIndicator;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;
import net.smartpager.android.R;
import net.smartpager.android.Autostart;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.activity.info.InfoActivity;
import net.smartpager.android.activity.settings.SettingsActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.SyncState;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ContactCursorTable;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.QuickResponseTable;
import net.smartpager.android.fragment.AbstractFragmentList;
import net.smartpager.android.fragment.ArchiveMessagesListFagment;
import net.smartpager.android.fragment.ContactIndexableListFragment;
import net.smartpager.android.fragment.DialerFragment;
import net.smartpager.android.fragment.MessagesListFragment;
import net.smartpager.android.fragment.dialog.AddContactFragmentDialog;
import net.smartpager.android.fragment.dialog.AddContactFragmentDialog.AddContactDialogListener;
import net.smartpager.android.fragment.dialog.AlertFragmentDialog;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.ContactRequestAcceptedFragmentDialog;
import net.smartpager.android.fragment.dialog.ContactRequestAcceptedFragmentDialog.ContactRequestAcceptedDialogListener;
import net.smartpager.android.fragment.dialog.ContactRequestSentFragmentDialog;
import net.smartpager.android.fragment.dialog.ContactRequestSentFragmentDialog.ContactRequestSentDialogListener;
import net.smartpager.android.fragment.dialog.InviteContRecievedFragmentDialog;
import net.smartpager.android.fragment.dialog.InviteContRecievedFragmentDialog.InviteContRecievedDialogListener;
import net.smartpager.android.fragment.dialog.InviteViaSmsFragmentDialog;
import net.smartpager.android.fragment.dialog.InviteViaSmsFragmentDialog.InviteViaSmsDialogListener;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.loader.GCMAsyncTaskLoader;
import net.smartpager.android.model.CreateTimerTask;
import net.smartpager.android.model.PersonalVerificationAlertTimer;
import net.smartpager.android.model.RequestedContactItem;
import net.smartpager.android.notification.Notificator;
import net.smartpager.android.service.ArchiveService;
import net.smartpager.android.service.UnreadAlarmService;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.utils.TelephoneUtils;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.RequestContactResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.utils.Log;

// Main view contain a view pager
// MAIN_TAB_MESSAGES
// MAIN_TAB_ARCHIVED
// MAIN_TAB_CONTACTS
// MAIN_TAB_DIALER
// Search page
// Start NewRelic
// startGCMPushSession
public class MainActivity extends BaseActivity implements  OnQueryTextListener,  //ActionBar.TabListener,
		LoaderCallbacks<Cursor>, ContactRequestSentDialogListener, InviteViaSmsDialogListener,
		ContactRequestAcceptedDialogListener, AddContactDialogListener, OnDialogDoneListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will keep every loaded fragment in memory.
	 * If this becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private PopupWindow mPopup;
	private SearchView mSearchView;

	@ViewInjectAnatation(viewID = R.id.main_footer_status_button)
	private FloatingActionMenu mStatusButton;

	private String mRequestedPhoneNumber = "";

	private final static int MSG_SHOW_DIALOG_CONTACT_EXISTS = 1;
	private final static int MSG_SEND_REQUEST_CONTACT_COMMAND = 2;

	private final static int MAIN_TAB_MESSAGES = 0;
	private final static int MAIN_TAB_ARCHIVED = 1;
	private final static int MAIN_TAB_CONTACTS = 2;
	private final static int MAIN_TAB_DIALER = 3;
	public static final String MAIN_INBOX_ACTION = "net.smartpager.android.ACTION_MAIN_INBOX";

	private boolean isNeedCheckGCMRegistration = true;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

				case MSG_SHOW_DIALOG_CONTACT_EXISTS:
					showDialogUserExists(mRequestedPhoneNumber);
				break;

				case MSG_SEND_REQUEST_CONTACT_COMMAND:
					sendRequestContactCommand(mRequestedPhoneNumber);
				break;

				default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		NewRelic.withApplicationToken(
				"AAf0561930808dff9083547447a9bb4e610c70e372"
				).start(this.getApplication());

		setTitle("");

		if (!SmartPagerApplication.getInstance().getPreferences().getLicenseAgree()) {
			startActivity(LicenseActivity.class);
			return;
		}
		if (!SmartPagerApplication.getInstance().getPreferences().getMobileVerify()) {
			startActivity(MobileVerificationActivity.class);
			return;
		}
		if (!SmartPagerApplication.getInstance().getPreferences().isPinEntered()) {
			startActivity(PinCodeActivity.class);
			return;
		}
		if (!SmartPagerApplication.getInstance().getPreferences().isPersonalVerificationQuestionsSaved()) {
			startActivity(SecretQuestionsActivity.class);
			return;
		}
		if (!SmartPagerApplication.getInstance().getPreferences().getSaveProfile()) {
			startActivity(PersonalInfoActivity.class);
			return;
		}
		if (!SmartPagerApplication.getInstance().getPreferences().isQuickResponsesInserted()) {
			insertQuickResponsesDefaults();
			SmartPagerApplication.getInstance().getPreferences().setQuickResponsesInserted(true);
		}
		if (!SmartPagerApplication.getInstance().getPreferences().isPersonalVerificationQuestionsSaved()) {
			SmartPagerApplication.getInstance().startTaskTimer(
					new CreateTimerTask(new PersonalVerificationAlertTimer()));
		}
		
		if (SmartPagerApplication.getInstance().getPreferences().getSmartPagerID() != null) {
			boolean attributeSet = NewRelic.setAttribute("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getSmartPagerID());
			if (attributeSet) {
				System.out.println("MainActivity: SmartPagerID SET: " + SmartPagerApplication.getInstance().getPreferences().getSmartPagerID());
			} else {
				System.out.println("MainActivity: Couldn't set SmartPagerID.");
			}
		}

		setContentView(R.layout.activity_main);
		initGui();
		Injector.doInjection(this);
		showVersion();
		startGCMPushSession();
		startGetUpdates();
		startService(new Intent(this, UnreadAlarmService.class));
		Autostart.startAlarm(this);

		checkForUpdates();
	}

	private void showVersion() {
		if (LOGGER_ENABLED) {
			try {
				((TextView) findViewById(R.id.version_textView)).setText(" v."
						+ getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			findViewById(R.id.version_textView).setVisibility(View.INVISIBLE);
		}
	}

	LoaderCallbacks<String> loaderCallbacks = new RegistrGCMLoaderCallbacks();

	class RegistrGCMLoaderCallbacks implements LoaderCallbacks<String> {

		@Override
		public Loader<String> onCreateLoader(int arg0, Bundle arg1) {
			if (LoaderID.RegistrGCM_Loader.ordinal() == arg0) {
				GCMAsyncTaskLoader mAsyncTaskLoader = new GCMAsyncTaskLoader(MainActivity.this);
				mAsyncTaskLoader.forceLoad();
				return mAsyncTaskLoader;
			}
			return null;
		}

		@Override
		public void onLoadFinished(Loader<String> arg0, String registrationId) {
			if (LoaderID.RegistrGCM_Loader.ordinal() == arg0.getId()) {
				if (TextUtils.isEmpty(registrationId)) {
					Log.e("BLANK registrationId");
					MainActivity.this.getWindow().getDecorView().postDelayed(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							MainActivity.this.getSupportLoaderManager().restartLoader(
									LoaderID.RegistrGCM_Loader.ordinal(), null, RegistrGCMLoaderCallbacks.this);
						}
					}, 1000);
				} else {
					if (TextUtils.isEmpty(SmartPagerApplication.getInstance().getPreferences().getGCMID())) {
						SmartPagerApplication.getInstance().getPreferences().setGCMID(registrationId);
						Log.e("FIRST REGISTRATION");
						sendStartPushSession(registrationId);
					} else if (!SmartPagerApplication.getInstance().getPreferences().getGCMID()
							.equalsIgnoreCase(registrationId)) {
						Log.e("NEW REGISTRATION");
						Bundle extras = new Bundle();
						extras.putString(WebSendParam.pushSessionHashKey.name(), SmartPagerApplication.getInstance()
								.getPreferences().getPushSessionHashKey());
						extras.putBoolean(BundleKey.initiatedByUser.name(), false);
						SmartPagerApplication.getInstance().startWebAction(WebAction.stopPushSession, extras);
						sendStartPushSession(registrationId);
					}
				}
			}
		}

		private void sendStartPushSession(String registrationId) {
			Bundle extras = new Bundle();
			extras.putString(WebSendParam.alias.name(), String.valueOf(System.currentTimeMillis()));
			extras.putString(WebSendParam.androidAPID.name(), registrationId);
			extras.putString(WebSendParam.version.name(), "1");
			extras.putBoolean(BundleKey.initiatedByUser.name(), false);
			SmartPagerApplication.getInstance().startWebAction(WebAction.startPushSession, extras);
		}

		@Override
		public void onLoaderReset(Loader<String> arg0) {
			// TODO Auto-generated method stub

		}
	}

	private void startGCMPushSession() {
		isNeedCheckGCMRegistration = false;
		getSupportLoaderManager().restartLoader(LoaderID.RegistrGCM_Loader.ordinal(), null,
				new RegistrGCMLoaderCallbacks());

	}

	private void insertQuickResponsesDefaults() {

		String[] strArray = getResources().getStringArray(R.array.quick_resp_defaults);
		List<String> defaultsList = Arrays.asList(strArray);

		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		Uri uri = SmartPagerContentProvider.CONTENT_QUICK_RESPONSE_URI;

		for (String item : defaultsList) {
			Builder builder = ContentProviderOperation.newInsert(uri);
			builder.withValue(QuickResponseTable.messageText.name(), item);
			operations.add(builder.build());
		}

		try {
			getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, operations);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected ACTIVITY_TAG getActivityTag() {
		return ACTIVITY_TAG.MainActivity;
	}

	@Override
	protected void notifyActivityToGo(ACTIVITY_ACTION action) {
		if (action == ACTIVITY_ACTION.MainActivity_ShowMessages) selectTab(MAIN_TAB_MESSAGES);
	}

	@Override
	protected void onStart() {
		startAutoCleanArchive();
		Notificator.cancelAlarm();
		Notificator.cancelAll();
		super.onStart();
	}

	private void startAutoCleanArchive() {
		Intent intent = new Intent(this, ArchiveService.class);
		intent.setAction(ArchiveService.ArchiveActions.autoCleanArchive.name());
		startService(intent);
	}

	private void startRemoveAllFromArchive() {
		Intent intent = new Intent(this, ArchiveService.class);
		intent.setAction(ArchiveService.ArchiveActions.removeAllFromArchive.name());
		startService(intent);
	}

	private void startArchiveAll() {
		Intent intent = new Intent(this, ArchiveService.class);
		intent.setAction(ArchiveService.ArchiveActions.archiveAllRead.name());
		startService(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);
	}

	private void startActivity(Class<?> activityClass) {
		Intent intent = new Intent(this, activityClass);
		startActivity(intent);
		finish();
	}

	private int m_nSelectedTab = MAIN_TAB_MESSAGES;

	private void initGui() {
		// TODO:
		// Set up the action bar.
		//final ActionBar actionBar = getActionBar();
		//actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		TabPageIndicator mIndicator = (TabPageIndicator) findViewById(R.id.indicator);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				//actionBar.setSelectedNavigationItem(position);

				if (position == 0) {
					if (mArchiveAllMenuItem != null) {
						mArchiveAllMenuItem.setVisible(true);
					}
				} else {
					if (mArchiveAllMenuItem != null) {
						mArchiveAllMenuItem.setVisible(false);
					}
				}
				if (position == 1) {
					if (mClearMenuItem != null) {
						mClearMenuItem.setVisible(true);
					}
				} else {
					if (mClearMenuItem != null) {
						mClearMenuItem.setVisible(false);
					}
				}

				if (position == 3) {
					if(mSearchMenuItem != null) {
						mSearchMenuItem.setVisible(false);
					}
				} else {
					if (mSearchMenuItem != null) {
						mSearchMenuItem.setVisible(true);
					}
				}

			}
		});


		mIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// When the given tab is selected, switch to the corresponding page in
				// the ViewPager.
				mViewPager.setCurrentItem(position);
				m_nSelectedTab = position;

				try {
					AbstractFragmentList fragment = (AbstractFragmentList) getSupportFragmentManager().findFragmentByTag(
							"android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());

					if (fragment != null) {
						fragment.filter("");
					} else {
						AbstractFragmentList f = (AbstractFragmentList) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
						f.filter("");
					}
					closeSearchView();
				} catch (NullPointerException e) {
					
				} catch (ClassCastException e) {

				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}

		});

		mIndicator.setViewPager(mViewPager);


		// For each of the sections in the app, add a tab to the action bar.
		/*for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.

			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
		}*/

	}

	private void selectTab(int tab) {
		if (tab < 0 || tab > MAIN_TAB_CONTACTS) return;
		mViewPager.setCurrentItem(tab, true);
	}

	MenuItem mClearMenuItem;
	MenuItem mArchiveAllMenuItem;
	MenuItem mSearchMenuItem;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		mSearchView = (SearchView) menu.findItem(R.id.item_search_menu).getActionView();
		mSearchView.setOnQueryTextListener(this);
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

		mSearchMenuItem = menu.findItem(R.id.item_search_menu);
		mSearchMenuItem.setVisible(true);

		mClearMenuItem = menu.findItem(R.id.item_clear_archive);
		mClearMenuItem.setVisible(false);
		mArchiveAllMenuItem = menu.findItem(R.id.item_archive_all);
		mArchiveAllMenuItem.setVisible(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {

			case R.id.item_add_contact:
				String[] items = new String[] { getString(R.string.new_group), getString(R.string.new_contact) };
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setCancelable(true);
				builder.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								createNewGroup();
							break;
							case 1:
								createNewContact();
							break;
						}
					}
				});
				AlertDialog alertDialog = builder.create();
				alertDialog.setCanceledOnTouchOutside(true);
				alertDialog.show();
				break;
			case R.id.item_clear_archive:
				AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(
						getApplicationContext(), getString(R.string.clear_archive), getString(R.string.are_you_sure_you_want_to_clear_archive),
						"", true, this);
				dialog.show(getSupportFragmentManager(), FragmentDialogTag.ClearArchive.name());

				break;
			case R.id.item_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.item_info:
				startActivity(new Intent(this, InfoActivity.class));
				break;
			case R.id.item_archive_all:
				AlertFragmentDialogYesNO dialog1 = AlertFragmentDialogYesNO.newInstance(
						getApplicationContext(), getString(R.string.archive_all), getString(R.string.move_read_and_sent), "", true, this);
				dialog1.show(getSupportFragmentManager(), FragmentDialogTag.ArchiveAllRead.name());
				break;
			default:
				return false;
		}
		return true;
	}

	protected void createNewContact() {
		AddContactFragmentDialog dialog = AddContactFragmentDialog
				.newInstance((AddContactDialogListener) MainActivity.this);
		dialog.show(getSupportFragmentManager(), FragmentDialogTag.AddContactFragmentDialog.name());
	}

	private void createNewGroup() {
		startActivity(new Intent(this, LocalGroupAddActivity.class));
	}

	/*@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		m_nSelectedTab = tab.getPosition();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}*/

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private String[] mTabNameArray;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			mTabNameArray = getResources().getStringArray(R.array.main_activity_tabs);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = null;
			switch (position) {
				case MAIN_TAB_MESSAGES:
					fragment = new MessagesListFragment();
				break;
				case MAIN_TAB_ARCHIVED:
					fragment = new ArchiveMessagesListFagment();
				break;
				case MAIN_TAB_CONTACTS:
					// fragment = new ContactListFagment();
					fragment = new ContactIndexableListFragment();
				// fragment = new ContactsListFragment();
				break;
				case MAIN_TAB_DIALER:
					fragment = new DialerFragment();
					break;
				default:
				break;
			}
			Bundle args = new Bundle();
			args.putString(DummySectionFragment.ARG_SECTION_NUMBER, mTabNameArray[position]);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// // Show 3 total pages.
			return mTabNameArray.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mTabNameArray[position];
		}


	}

	/**
	 * A dummy fragment representing a section of the app, but that simply displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
			TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
			dummyTextView.setText(getArguments().getString(ARG_SECTION_NUMBER));
			return rootView;
		}
	}

	@ClickListenerInjectAnatation(viewID = R.id.main_footer_status_button)
	public void onAttachClick(View view) {
		//showPopup(view);
	}

	private void showPopup(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		Point p = new Point();
		p.x = location[0];
		p.y = location[1];

		int popupWidth = (int) (140 * getResources().getDisplayMetrics().density);
		int popupHeight = (int) (110 * getResources().getDisplayMetrics().density);

		// Inflate the popup_layout.xml
		LinearLayout viewGroup = (LinearLayout) this.findViewById(R.id.popup_container);
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_status, viewGroup);
		// Creating the PopupWindow
		mPopup = new PopupWindow(this);
		mPopup.setBackgroundDrawable(getResources().getDrawable(R.drawable.def_bg_popup_attachment_static));
		mPopup.setContentView(layout);
		mPopup.setWidth(popupWidth);
		mPopup.setHeight(popupHeight);
		mPopup.setFocusable(true);

		int OFFSET_X = 30;
		int OFFSET_Y = -popupHeight;

		layout.findViewById(R.id.popup_set_offline_layout).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!NetworkUtils.isInternetConnectedAsync()) {
					mPopup.dismiss();
					showDialogUnableToChangeStatus();
				} else {
					setStatusService(ContactStatus.DO_NOT_DISTURB, true);
				}
			}
		});

		layout.findViewById(R.id.popup_set_online_layout).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!NetworkUtils.isInternetConnectedAsync()) {
					mPopup.dismiss();
					showDialogUnableToChangeStatus();
				} else if (SmartPagerApplication.getInstance().getPreferences().getSyncState() != SyncState.Syncronized) {
					mPopup.dismiss();
					startSyncronization();
					showSyncInProgressDialog();
				} else {
					setStatusService(ContactStatus.ONLINE, true);
				}
			}
		});

		mPopup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y + OFFSET_Y);
	}

	private void setStatusService(ContactStatus status, boolean byUser) {

		if (mPopup != null) {
			mPopup.dismiss();
		}

		if (NetworkUtils.isInternetConnectedAsync()) {
			Bundle extras = new Bundle();
			extras.putString(WebSendParam.status.name(), status.name());
			SmartPagerApplication.getInstance().startWebAction(WebAction.setStatus, extras);
		} else if (byUser) {

			showDialogUnableToChangeStatus();

		} else {

			showDialogNoInternetConnection();
		}
	}

	private void showDialogNoInternetConnection() {

		AlertFragmentDialog fd = AlertFragmentDialog.newInstance(getString(R.string.dialog_title_no_internet),
				getString(R.string.no_internet_connection), false);
		fd.show(getSupportFragmentManager(), FragmentDialogTag.NoInternetConnection.name());
	}

	private void showDialogUnableToChangeStatus() {

		AlertFragmentDialog fd = AlertFragmentDialog.newInstance(getString(R.string.dialog_title_no_internet),
				getString(R.string.unable_to_change_status), false);
		fd.show(getSupportFragmentManager(), FragmentDialogTag.NoInternetConnection.name());
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// exclude keyboard appearance on volume change
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_MENU:
				return super.onKeyUp(keyCode, event);
			default:
			break;
		}

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, 0);

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onQueryTextChange(String query) {
		AbstractFragmentList fragment = (AbstractFragmentList) getSupportFragmentManager().findFragmentByTag(
				"android:switcher:" + R.id.pager + ":" + mViewPager.getCurrentItem());

		if (fragment != null) {
			fragment.filter(query);
		} else {
			AbstractFragmentList f = (AbstractFragmentList) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
			f.filter(query);
		}
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
		return true;
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {

		// hideProgressDialog();

		String status = SmartPagerApplication.getInstance().getPreferences().getStatus();
		status = status.replace(" ", "_");
		ContactStatus currentStatus = ContactStatus.valueOf(status);
		// currentStatus = ContactStatus.valueOf(status);
		switch (action) {

			case syncronize:
				hideProgressDialog();
				setStatusButton(currentStatus);
			// setStatusService(currentStatus, false);
			break;

			case getPagingGroupDetails:
				// hideProgressDialog();
				Intent intent = new Intent(this, GroupDetailsActivity.class);
				intent.putExtra(WebAction.getPagingGroupDetails.name(), responce);
				startActivity(intent);
			break;

			case requestContact:
				if (responce instanceof RequestContactResponse) {
					RequestContactResponse resp = (RequestContactResponse) responce;
					String name = resp.getFirstName() + " " + resp.getLastName();
					showDialogContactRequestWasSent(name);
				}
			break;

			case acceptContact:
				startGetUpdates();
			break;

			case setStatus:
				hideProgressDialog();
				// Prefferens.setStatus(mDesirableStatus.name());
				setStatusButton(currentStatus);
				if (currentStatus == ContactStatus.ONLINE) {
					startGetUpdates();
				}
			break;

			default:
			break;
		}
	}

	private void setStatusButton(ContactStatus status) {
		FloatingActionButton popup_set_offline_layout = (FloatingActionButton) mStatusButton.findViewById(R.id.popup_set_offline_layout);
		FloatingActionButton popup_set_online_layout = (FloatingActionButton) mStatusButton.findViewById(R.id.popup_set_online_layout);

		popup_set_offline_layout.setColorNormalResId(R.color.colorPrimary);
		popup_set_online_layout.setColorNormalResId(R.color.white);
		int drawableStatus = R.drawable.def_ic_offline;
		switch (status) {
			case ONLINE:
				drawableStatus = R.drawable.def_ic_online;
				popup_set_offline_layout.setColorNormalResId(R.color.white);
				popup_set_online_layout.setColorNormalResId(R.color.colorPrimary);
			break;
			default:
			break;
		}
		mStatusButton.setClosedOnTouchOutside(true);
		mStatusButton.setIconAnimated(false);
		mStatusButton.getMenuIconView().setImageResource(drawableStatus);
		//mStatusButton.setBackgroundResource(drawableStatus);
		popup_set_offline_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("","popup_set_offline_layout");
				mStatusButton.toggle(true);
				if (!NetworkUtils.isInternetConnectedAsync()) {
					mPopup.dismiss();
					showDialogUnableToChangeStatus();
				} else {
					setStatusService(ContactStatus.DO_NOT_DISTURB, true);
				}
			}
		});

		popup_set_online_layout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.e("", "popup_set_online_layout");
				mStatusButton.toggle(true);
				if (!NetworkUtils.isInternetConnectedAsync()) {
					if (mPopup != null) {
						mPopup.dismiss();
					}
					showDialogUnableToChangeStatus();
				} else if (SmartPagerApplication.getInstance().getPreferences().getSyncState() != SyncState.Syncronized) {
					if (mPopup != null) {
						mPopup.dismiss();
					}
					startSyncronization();
					showSyncInProgressDialog();
				} else {
					setStatusService(ContactStatus.ONLINE, true);
				}
			}
		});
	}

	public void closeSearchView() {
		try {
			if (!mSearchView.isIconified()) {
				mSearchView.setIconified(true);
				mSearchView.setIconified(true);
			}
		} catch (NullPointerException e) {

		}
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mNewInboxReceiver);
		super.onPause();

		closeSearchView();

		UpdateManager.unregister();
	}

	@Override
	protected void onResume() {
		showSyncInProgressDialog();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MAIN_INBOX_ACTION);
		registerReceiver(mNewInboxReceiver, intentFilter);
		if (isNeedCheckGCMRegistration) {
			// final String regId = GCMRegistrar.getRegistrationId(this);
			// if (regId.equals("")) {
			// Log.CRASH();
			// GCMRegistrar.register(this, BuildSettings.GCM_SENDER_ID);
			// }
		}
		
		super.onResume();

		checkForCrashes();
	}

	private void checkForCrashes() {
		CrashManager.register(this, "d38d9d493d70479e35dd29392b07f2a8");
	}

	private void checkForUpdates() {
		// Remove this for store / production builds!
		UpdateManager.register(this, "d38d9d493d70479e35dd29392b07f2a8");
	}

	private void showSyncInProgressDialog() {

		String status = SmartPagerApplication.getInstance().getPreferences().getStatus();
		status = status.replace(" ", "_");
		ContactStatus currentStatus = ContactStatus.valueOf(status);
		currentStatus = ContactStatus.valueOf(status);
		setStatusButton(currentStatus);

		switch (SmartPagerApplication.getInstance().getPreferences().getSyncState()) {

			case InProgress:
				if (NetworkUtils.isInternetConnectedAsync()) {
					showProgressDialog(R.string.synchronizing);
				} else {
					showDialogNoInternetConnection();
				}
			break;

			case NotSyncronized:
				if (NetworkUtils.isInternetConnectedAsync()) {
					showProgressDialog(R.string.synchronizing);
					startSyncronization();
				} else {
					showDialogNoInternetConnection();
				}
			break;

			case Syncronized:
				hideProgressDialog();
			// setStatusService(currentStatus, false);
			break;

			default:
				hideProgressDialog();
			break;
		}
	}

	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse responce) {

		hideProgressDialog();

		switch (action) {
			case getPagingGroupDetails:
				showToast("Loading data Error");
			break;
			case requestContact:
				showDialogInviteViaSms();
			break;
			default:
				super.onErrorResponse(action, responce);
			break;
		}
	}

	private void showDialogContactRequestWasSent(String name) {

		ContactRequestSentFragmentDialog fd = ContactRequestSentFragmentDialog.newInstance(name, false);
		fd.show(getSupportFragmentManager(), FragmentDialogTag.ContactRequestWasSentDialog.name());
	}

	private void showDialogInviteViaSms() {

		InviteViaSmsFragmentDialog fd = InviteViaSmsFragmentDialog.newInstance(false);
		fd.show(getSupportFragmentManager(), FragmentDialogTag.InviteViaSmsDialog.name());
	}

	@ClickListenerInjectAnatation(viewID = R.id.main_footer_new_message_button)
	public void onNewMessageClick(View view) {
		Intent intent = new Intent(this, NewMessageActivity.class);
		startActivity(intent);
	}

	@Override
	public void onAddNowClick(String number) {
		mRequestedPhoneNumber = number;
		getSupportLoaderManager().restartLoader(LoaderID.CONTACT_EXISTS_LOADER.ordinal(), null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

		Loader<Cursor> loader = null;

		switch (LoaderID.values()[id]) {

			case CONTACT_EXISTS_LOADER:

				loader = new ContactCursorTable().getPhoneFilterLoader(mRequestedPhoneNumber);
			break;

			default:
			break;
		}

		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

		switch (LoaderID.values()[loader.getId()]) {

			case CONTACT_EXISTS_LOADER:

				boolean contactExist = cursor.moveToFirst();
				if (contactExist) {
					mHandler.sendEmptyMessage(MSG_SHOW_DIALOG_CONTACT_EXISTS);
				} else {
					mHandler.sendEmptyMessage(MSG_SEND_REQUEST_CONTACT_COMMAND);
				}

			break;

			default:
			break;
		}
	}

	private void sendRequestContactCommand(final String number) {

		new AlertDialog.Builder(MainActivity.this)
				.setTitle("Unknown Contact")
				.setMessage("This number is not registered in SmartPager.\n\n Would you like to invite them?")
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse("sms:" + number));
						intent.putExtra("sms_body", "I just started using SmartPager and would like to add you as a contact.\n\nGo to http://tmiq.it/sp to get the app for your iPhone or Android device.");
						startActivity(intent);
					}
				})
				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

					}
				})
				.show();

//		Bundle extras = new Bundle();
//		extras.putString(WebSendParam.contactMobileNumber.name(), number);
//		SmartPagerApplication.getInstance().startWebAction(WebAction.requestContact, extras);

	}

	private void showDialogUserExists(String number) {

		AlertFragmentDialog fd = AlertFragmentDialog.newInstance(getString(R.string.warning),
				getString(R.string.contact_already_exists) + "\n" + TelephoneUtils.format(number));
		fd.setCancelable(false);
		fd.show(getSupportFragmentManager(), FragmentDialogTag.AlertUserExistsDialog.name());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {

	}

	@Override
	public void onAddAnotherClick() {
		createNewContact();
	}

	@Override
	public void onDoneClick() {
		startGetUpdates();
	}

	@Override
	public void onSendSmsInviteClick() {

		String number = mRequestedPhoneNumber.substring(1);
		Intent sendIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
		sendIntent.putExtra("sms_body", getResources().getString(R.string.i_just_started_using_smartpager));
		sendIntent.putExtra("sms_number", number);
		startActivity(sendIntent);
	}

	@Override
	public void onCancelSmsInviteClick() {

	}

	@Override
	public void onContactRequestAcceptedClick() {
		showToast("Contact Request Accepted");
	}

	// ----------------- Invite dialog -------------------------------
	@Override
	protected void onContactRequests(List<RequestedContactItem> contactItems) {
		if (contactItems != null) {
			for (RequestedContactItem item : contactItems) {
				String name = item.getFirstName() + " " + item.getLastName();
				String photoUrl = getPhotoUrl(item);
				final String number = item.getMobileNumber();
				InviteContRecievedFragmentDialog fd = InviteContRecievedFragmentDialog.newInstance(name, photoUrl,
						false);
				fd.show(getSupportFragmentManager(), FragmentDialogTag.InviteContactRequestedDialog.name());

				fd.setDialogListener(new InviteContRecievedDialogListener() {
					@Override
					public void onConfirmContactRequestClick() {
						startAcceptContact(number);
					}

					@Override
					public void onCancelContactRequestClick() {
						startRejectContact(number);
					}
				});
			}
		}
	}

	private void startRejectContact(String number) {
		Bundle extras = new Bundle();
		extras.putString(WebSendParam.contactMobileNumber.name(), number);
		SmartPagerApplication.getInstance().startWebAction(WebAction.rejectContact, extras);
	}

	private void startAcceptContact(String number) {
		Bundle extras = new Bundle();
		extras.putString(WebSendParam.contactMobileNumber.name(), number);
		SmartPagerApplication.getInstance().startWebAction(WebAction.acceptContact, extras);
	}

	@SuppressWarnings("unused")
	private void showDialogContactRequestAccepted(List<RequestedContactItem> contactItems) {

		for (RequestedContactItem item : contactItems) {
			String name = item.getFirstName() + " " + item.getLastName();
			String photoUrl = getPhotoUrl(item);
			ContactRequestAcceptedFragmentDialog fd = ContactRequestAcceptedFragmentDialog.newInstance(name, photoUrl,
					false);
			fd.show(getSupportFragmentManager(), FragmentDialogTag.ContactRequestAcceptedDialog.name());
		}
	}

	private String getPhotoUrl(RequestedContactItem item) {
		StringBuilder builder = new StringBuilder();
		builder.append(Constants.BASE_REST_URL).append("/");
		builder.append(WebAction.getProfilePicture.name()).append("?");
		builder.append(WebSendParam.smartPagerID.name()).append("=");
		builder.append(SmartPagerApplication.getInstance().getPreferences().getUserID()).append("&");
		builder.append(WebSendParam.uberPassword.name()).append("=");
		builder.append(SmartPagerApplication.getInstance().getPreferences().getPassword()).append("&");
		builder.append(WebSendParam.contactSmartPagerID.name()).append("=");
		builder.append(item.getContactID());
		return builder.toString();
	}

	@Override
	public void onDialogDone(FragmentDialogTag tag, String data) {
		switch (tag) {
			case ClearArchive:
				startRemoveAllFromArchive();
			break;
			case ArchiveAllRead:
				startArchiveAll();
			break;
			case NoInternetConnection:
				hideProgressDialog();
			break;
			default:
			break;
		}

	}

	@Override
	public void onDialogNo(FragmentDialogTag tag, String data) {

	}

	public SearchView getSearchView() {
		return mSearchView;
	}

	@Override
	public void onBackPressed() {
		if (mSearchView != null && !mSearchView.isIconified()) {
			mSearchView.onActionViewCollapsed();
			mSearchView.setQuery("", true);
		} else {
			super.onBackPressed();
		}
	}

	// ---- New Inbox in Main Mode ------------------------------------------------------
	private BroadcastReceiver mNewInboxReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MAIN_INBOX_ACTION)) {
				if (m_nSelectedTab == MAIN_TAB_MESSAGES) {
					Notificator.notifyInboxChatSelfThread(intent.getExtras());
				} else {
					Notificator.notifyInboxChatAnotherThread(intent.getExtras());
				}
			}
		}
	};

}
