package net.smartpager.android.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.SyncState;
import net.smartpager.android.consts.RequiredPinToBeEntered;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.fragment.dialog.AlertFragmentDialog;
import net.smartpager.android.model.AbstractTaskTimer;
import net.smartpager.android.model.RequestedContactItem;
import net.smartpager.android.receivers.TaskTimerReceiver;
import net.smartpager.android.service.WebService;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.DateTimeUtils;
import net.smartpager.android.utils.EmailUtils;
import net.smartpager.android.utils.log.LogcatContext;
import net.smartpager.android.utils.log.LogcatProcessor;
import net.smartpager.android.web.command.AbstractCommand;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetUpdatesResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.mobidev.framework.utils.Log;

public abstract class BaseActivity extends FragmentActivity {
	
	public Activity theActivity;
	
    public interface IOnProgressDialogCancelledListener {
        public void onProgressDialogCancelled();
    }
    public enum ACTIVITY_TAG {
        NONE, MainActivity
    }

    public enum ACTIVITY_ACTION {
        NONE, MainActivity_ShowMessages
    }

	/**
	 * Enables or disables LogCat permanent logging
	 *
	 * @see LogcatProcessor
	 * @see #startLogcatProcessor()
	 * @see #stopLogcatProcessor()
	 */
	protected static final boolean LOGGER_ENABLED = false;
	protected static final int INTENT_FLAGS = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK
			| Intent.FLAG_ACTIVITY_TASK_ON_HOME;

	protected static ProgressDialog mProgressDialog;
	protected static AlertFragmentDialog mFragmentDialog;
	private boolean isLocPin = false;
    private boolean m_bWasInstanceSaved = false;

    protected final String EXTRA_FIELD_BACK_ACTIVITY_TAG = "ACTIVITY_TAG";
    private ACTIVITY_TAG m_currActivityTag;

	/**
	 * Receives server responses from {@linkplain WebService} for further processing
	 *
	 * @see {@link net.smartpager.android.service.WebService.ExecuteCommand ExecuteCommand}
	 * @see #onSuccessResponse
	 * @see #onErrorResponse
	 */
	private BroadcastReceiver mBaseReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			WebAction action = WebAction.valueOf(SmartPagerApplication.getExtractActionName(intent.getAction()));
			AbstractResponse response = (AbstractResponse) intent.getExtras().get(BundleKey.response.name());
            boolean commandCancelled = intent.getExtras().getBoolean(BundleKey.commandCancelled.name());

			if (response != null && response.isSuccess()) {
				onSuccessResponse(action, response);
			}

			switch (action) {
				case deviceRegistrationStep1:
				break;
				case putFile:
				case sendMessage:
//					if (response.isSuccess()) {
//						showToast(getString(R.string.message_was_sent));
//					}
					if (!response.isSuccess()) {
						hideProgressDialog();
						
						ActivityManager am = (ActivityManager) theActivity.getSystemService(ACTIVITY_SERVICE);
					    List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
					    System.out.println("CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
					    ComponentName componentInfo = taskInfo.get(0).topActivity;
					    componentInfo.getPackageName();
					    
					    if (taskInfo.get(0).topActivity.getClassName().equalsIgnoreCase("net.smartpager.android.activity.NewMessageActivity")) {
					    	NewMessageActivity.showBadConnectionMessage();
					    } else if (taskInfo.get(0).topActivity.getClassName().equalsIgnoreCase("net.smartpager.android.activity.ChatActivity")) {
					    	ChatActivity.showBadConnectionMessage();
					    }
					}
				break;
				case getUpdates:
					if (response.isSuccess()) {
						// SmartPagerApplication.tryUnlockDevice();
						GetUpdatesResponse updateResponse = (GetUpdatesResponse) response;
						List<RequestedContactItem> contactItems = updateResponse.getRequestedContacts();
						if (contactItems != null && contactItems.size() > 0) {
							onContactRequests(contactItems);
						}
					}
				break;
				case getInbox:

				break;
				default:
				break;
			}
		}
	};

    private BroadcastReceiver m_taskTimerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AbstractTaskTimer task = null;
            if(intent.getExtras().containsKey(BundleKey.taskTimer.name()))
                task = (AbstractTaskTimer) intent.getExtras().get(BundleKey.taskTimer.name());
            onTaskTimerScheduled(task);
            if(task != null)
            {
                switch (task.getTaskAction())
                {
                    case personalVerificationReminder:
                        if(!SmartPagerApplication.isApplicationInBackground() && !SmartPagerApplication.getInstance().getPreferences().isPersonalVerificationQuestionsSaved())
                        {
                            BaseActivity.this.showErrorDialog(R.string.personal_verification_reminder_message);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

//    private BroadcastReceiver mSIPReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//        }
//    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        m_bWasInstanceSaved = false;
        m_currActivityTag = getActivityTag();
        
        
        theActivity = (Activity) this;
        
        resetActivityToGo();
		if (isUpNavigationEnabled()) {
			final ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
	}

    private void resetActivityToGo()
    {
        SmartPagerApplication.getInstance().getPreferences().setActivityToGo(ACTIVITY_TAG.NONE, ACTIVITY_ACTION.NONE);
    }

    private boolean checkActivityToGo ()
    {
        ACTIVITY_TAG activityToGo = SmartPagerApplication.getInstance().getPreferences().getActivityToGo();
        if(activityToGo != ACTIVITY_TAG.NONE)
        {
            if (activityToGo != m_currActivityTag) {
                finish();
                return false;
            } else
            {
                notifyActivityToGo(SmartPagerApplication.getInstance().getPreferences().getActivityAction());
                resetActivityToGo();
            }
        }
        return true;
    }

    protected void notifyActivityToGo (ACTIVITY_ACTION action)
    {
    }

    protected ACTIVITY_TAG getActivityTag ()
    {
        return ACTIVITY_TAG.NONE;
    }

    protected void backToRootActivity ()
    {
        backToActivity(ACTIVITY_TAG.MainActivity);
    }

    protected void backToActivity (ACTIVITY_TAG activityTag)
    {
        backToActivity(activityTag, ACTIVITY_ACTION.NONE);
    }

    protected void backToActivity (ACTIVITY_TAG activityTag, ACTIVITY_ACTION activityAction)
    {
        SmartPagerApplication.getInstance().getPreferences().setActivityToGo(activityTag, activityAction);
    }

	@Override
	protected void onStart() {
		if (LOGGER_ENABLED) {
			startLogcatProcessor();
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AudioManagerUtils.stopExternalMusic(this);
        checkActivityToGo();
        registerBaseReceiver();
        registerTaskTimerReceiver();
//        registerSIPReceiver();
        if (!checkIsDeviceLocked()) {
            if (!checkIsPinEnterNeed()) {
                startIfNeedUpdate();
            }
        }
    }

    private void registerTaskTimerReceiver()
    {
        IntentFilter intentFilter = new IntentFilter(TaskTimerReceiver.TASK_TIMER_ACTION_CALLBACK);
        registerReceiver(m_taskTimerReceiver, intentFilter);
    }

	@Override
	protected void onPause() {
		unregisterReceiver(mBaseReceiver);
        unregisterReceiver(m_taskTimerReceiver);
//        unregisterReceiver(mSIPReceiver);
		if (!isLocPin) {
			SmartPagerApplication.getInstance().onStopActivity();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		if (LOGGER_ENABLED) {
			stopLogcatProcessor();
		}
		super.onStop();
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		/**
		 * Adds extra actionBar item to all Activities who extends BaseActivity for sending logs via email purpose
		 */
		if (LOGGER_ENABLED) {
			inflater.inflate(R.menu.send_logs, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.item_send_logs:
				saveCurrentLog();
			break;
			case android.R.id.home:
				if (isUpNavigationEnabled()) {
					/**
					 * By default chevron behavior equivalents onBackPressed
					 */
					onBackPressed();
					// NavUtils.navigateUpFromSameTask(this);
					return true;
				}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

    @Override
	protected void onSaveInstanceState(Bundle outState) {
        m_bWasInstanceSaved = true;
		outState.putBoolean(BundleKey.locPin.name(), isLocPin);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null && savedInstanceState.containsKey(BundleKey.locPin.name())) {
			isLocPin = savedInstanceState.getBoolean(BundleKey.locPin.name());
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * Enables or disables chevron button at left-side of ActionBar
	 *
	 * @return
	 */
	protected boolean isUpNavigationEnabled() {
		return false;
	}

	/**
	 * Success responses handler method
	 *
	 * @see #mBaseReceiver
	 */
	protected abstract void onSuccessResponse(WebAction action, AbstractResponse response);

    /**
     * Task timer scheduled handler method
     */
    protected void onTaskTimerScheduled(AbstractTaskTimer task)
    {

    }

    /**
     * Cancelled web action handler method
     *
     * @see #mBaseReceiver
     */
    protected void onCancelledWebAction(WebAction action, AbstractResponse response)
    {
        showToast(String.format(getString(R.string.web_action_cancelled), action.name()));
    }

	/**
	 * Error responses handler method
	 *
	 * @see #mBaseReceiver
	 */
	protected void onErrorResponse(WebAction action, AbstractResponse response) {
		// String message = action.name() + " error";
		// if (!SmartPagerApplication.isNetworkAvailable()){
		// message = getString(R.string.no_internet_connection);
		// }

		if (response.isInitiatedByUser()) {
			showErrorDialog(response.getMessage());
		}
	}

	/**
	 * Starts {@linkplain WebService} with {@linkplain AbstractCommand} according to {@linkplain WebAction} parameter
	 *
	 * @param action
	 *            name for AbstractCommand that shall be started
	 */
	protected void sendSingleComand(WebAction action) {
        SmartPagerApplication.getInstance().startWebAction(action, null);
	}

	/**
	 * Hides software keyboard for specified view
	 *
	 * @param view
	 */
	public void hideSoftKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}

	/**
	 * Called if somebody has invited you to his contact list
	 *
	 * @param contactItems
	 */
	protected void onContactRequests(List<RequestedContactItem> contactItems) {

	}

	/**
	 * Called in {@linkplain #onResume()} method and checks if device is locked , and if it is - starts
	 * {@linkplain LockActivity}
	 */
	private boolean checkIsDeviceLocked() {
		boolean result = false;
		if (SmartPagerApplication.getInstance().getPreferences().isLocked() && !SmartPagerApplication.isActivityOnFront(LockActivity.class)) {
			Intent lockIntent = new Intent(BaseActivity.this, LockActivity.class);
			lockIntent.setFlags(INTENT_FLAGS);
			startActivity(lockIntent);
			result = true;
		}
		return result;
	}

//    private void registerSIPReceiver() {
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("android.SipDemo.INCOMING_CALL");
//        this.registerReceiver(mSIPReceiver, filter);
//    }
	/**
	 * Registers BaseReceiver in {@linkplain #onResume()}
	 *
	 * @see #mBaseReceiver
	 */
	private void registerBaseReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		for (WebAction action : WebAction.values()) {
			intentFilter.addAction(SmartPagerApplication.getActionName(action.name()));
		}
		registerReceiver(mBaseReceiver, intentFilter);
	}

	// ------ PIN CODE REQUEST ----------------------------------------------------

	/**
	 * Checks if current activity can be blocked by {@linkplain CheckPinActivity}
	 *
	 * @return
	 */
	protected boolean needCheckPin() {
		return true;
	}

	/**
	 * Sets ability to skip PIN veification
	 *
	 * @param isLocPin
	 *            - if true - PIN verification will be skipped for current activity
	 */
	public void setLocPin(boolean isLocPin) {
		this.isLocPin = isLocPin;
	}

	private void startIfNeedUpdate() {
		if (SmartPagerApplication.getInstance().isFromBackground()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
			try {
				Date lastUpdate = dateFormat.parse(SmartPagerApplication.getInstance().getPreferences().getLastUpdate());
				Calendar calendar = Calendar.getInstance();

				long diffInMis = Math.abs(lastUpdate.getTime() - calendar.getTimeInMillis());
				long diff = TimeUnit.MILLISECONDS.toMinutes(diffInMis);

				startGetUpdates();
				if (diff > 3) {
					sendSingleComand(WebAction.getProfile);
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static String getActionName(String actionName) {
		return SmartPagerApplication.getInstance().getApplicationContext().getPackageName() + "." + actionName;
	}

	protected void startGetUpdates() {

		if (SmartPagerApplication.getInstance().getPreferences().getSyncState() == SyncState.Syncronized) {
            Bundle extras = new Bundle();
            extras.putBoolean(BundleKey.initiatedByUser.name(), false);
            SmartPagerApplication.getInstance().startWebAction(WebAction.getUpdates, extras);
		}
	}

	/**
	 * Helper method - checks if it is time to request PIN code enter<br>
	 * Time to pin verification is set in application settings
	 *
	 * @return
	 * @see #checkIsPinEnterNeed
	 */
	private boolean needShowPin() {

		if (!needCheckPin()) {
			return false;
		}
		long timeDiff = System.currentTimeMillis() - SmartPagerApplication.getInstance().getSettingsPreferences().getPinEnteredTime();
		RequiredPinToBeEntered verifyPinSettings = SmartPagerApplication.getInstance().getSettingsPreferences().getRequiredPin();
		switch (verifyPinSettings) {
			case always:
				if (SmartPagerApplication.getInstance().isFromBackground()) {
					return true;
				}
			break;
			case _30Min:
				if (timeDiff > 30 * DateTimeUtils.MS_IN_MIN) {
					return true;
				}
			break;
			case _1Hour:
				if (timeDiff > DateTimeUtils.MS_IN_HOUR) {
					return true;
				}
			break;
			case _8Hours:
				if (timeDiff > 8 * DateTimeUtils.MS_IN_HOUR) {
					return true;
				}
			break;
			default:
			break;
		}
		return false;
	}

	/**
	 * Called in {@linkplain #onResume()} method and checks if it is time to request PIN code , and if it is - starts
	 * {@linkplain CheckPinActivity}
	 */
	private boolean checkIsPinEnterNeed() {
		boolean result = false;
		if (!isLocPin && !SmartPagerApplication.getInstance().getPreferences().isLocked()) {
			if (needShowPin()) {
				if (SmartPagerApplication.getInstance().getPreferences().getPin().length() == 0) {
					return result;
				}
				Intent checkPin = new Intent(BaseActivity.this, CheckPinActivity.class);
				checkPin.setFlags(INTENT_FLAGS);
				isLocPin = true;
				startActivity(checkPin);
				result = true;
			}
		} else {
			isLocPin = false;
		}
		return result;
	}

	// ------ DIALOG TOOLS ----------------------------------------------------

    public void showProgressDialog(int resID)
    {
        showProgressDialog(getString(resID));
    }

	public void showProgressDialog(String message) {
		try {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMessage(message);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
			}
		} catch (Exception e) {
			mProgressDialog = null;
		}
	}

	public void hideProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.hide();
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

    public boolean isProgressDialogShowing ()
    {
        return mProgressDialog != null && mProgressDialog.isShowing();
    }

    //----- CANCELABLE PROGRESS DIALOG

    public void showCancellableProgressDialog (int resID, IOnProgressDialogCancelledListener listener)
    {
        showCancellableProgressDialog(getString(resID), listener);
    }

    public void showCancellableProgressDialog (String message, final IOnProgressDialogCancelledListener listener)
    {
        try {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setCancelable(false);
                View progressDialogView = ((LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                        R.layout.dialog_layout_cancellable_progress_dialog, null);
                progressDialogView.findViewById(R.id.progress_dialog_bt_cancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null)
                            listener.onProgressDialogCancelled();
                        hideProgressDialog();
                    }
                });
                ((TextView)progressDialogView.findViewById(R.id.progress_dialog_title)).setText(message);
                mProgressDialog.show();
                mProgressDialog.setContentView(progressDialogView);
            }
        } catch (Exception e) {
            mProgressDialog = null;
        }
    }

    public void showErrorDialog(int resID)
    {
        showErrorDialog(getString(resID));
    }

	public void showErrorDialog(String message) {
		if (mFragmentDialog != null && !m_bWasInstanceSaved) {
			mFragmentDialog.dismiss(); // to avoid multiple dialogs at same time
		};
		mFragmentDialog = AlertFragmentDialog.newErrorInstance(message);
		mFragmentDialog.show(getSupportFragmentManager(), FragmentDialogTag.AlertInforDialog.name());
	}

    public void showToast(int resID)
    {
        showToast(getString(resID));
    }

	public void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	// ------- LOGGER TOOLS ----------------------------------------------------
	protected Process mLogcatProc;
	protected LogcatProcessor mLogcatter;
	protected LogcatContext mContext = new LogcatContext(5);

	private static final int MAX_LINES = 250;
	private static final int MSG_NEWLINE = 1;
	private static final String[] LINES_ARRAY = new String[MAX_LINES];
	private int mCurrentLogLine = 0;

	/**
	 * Fills array by commands from {@linkplain #mLogcatter} line by line
	 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_NEWLINE:
					handleMessageNewline(msg);
				break;
				default:
					super.handleMessage(msg);
			}
		}
	};

	/**
	 * Puts single log line into cyclic array. If reaches the end of array - starts from the first element
	 *
	 * @param msg
	 */
	private void handleMessageNewline(Message msg) {
		String line = (String) msg.obj;
		LINES_ARRAY[mCurrentLogLine] = line;
		mCurrentLogLine = ++mCurrentLogLine % MAX_LINES;
	}

	/**
	 * Sends logs array starting from current line to email application
	 */
	// TODO - refactor to normal 'for' iteration to avoid this SuppressWarnings
	@SuppressWarnings("unused")
	protected void saveCurrentLog() {
		String subject = "LOGS: " + new Date().toString();
		StringBuilder builder = new StringBuilder();
		int lineIndex = mCurrentLogLine;

		for (String _ : LINES_ARRAY) {
			String str = LINES_ARRAY[lineIndex];
			if (!TextUtils.isEmpty(str)) {
				builder.append(str).append("\n");
			}
			lineIndex = ++lineIndex % MAX_LINES;
		}
		EmailUtils.sendEmail(this, subject, builder.toString());
	}

	/**
	 * Starts {@linkplain LogcatProcessor} instance
	 */
	private void startLogcatProcessor() {
		mLogcatter = new LogcatProcessor() {
			public void onError(final String msg, Throwable e) {
				runOnUiThread(new Runnable() {
					public void run() {
						showToast(msg);
					}
				});
			}

			public void onNewline(String line) {
				Message msg = mHandler.obtainMessage(MSG_NEWLINE);
				msg.obj = line;
				mHandler.sendMessage(msg);
			}
		};
		mLogcatter.start();
	}

	/**
	 * Stops {@linkplain LogcatProcessor} instance
	 */
	private void stopLogcatProcessor() {
		if (mLogcatter != null) {
			mLogcatter.stopCatter();
			mLogcatter = null;
		}
	}

	protected void startSyncronization() {
//		Intent intent = new Intent(this, WebService.class);
//		intent.setAction(SmartPagerApplication.getActionName(WebAction.syncronize.name()));
//		startService(intent);
        sendSingleComand(WebAction.syncronize);
        SmartPagerApplication.getInstance().getPreferences().setSyncState(SyncState.InProgress);
		hideProgressDialog();
	}
}
