package net.smartpager.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.SyncState;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.web.RequestSender;
import net.smartpager.android.web.command.*;
import net.smartpager.android.web.response.AbstractResponse;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import biz.mobidev.framework.utils.Log;

/**
 * Low-coupling service for all commands to server
 * 
 * @author Roman
 */
public class WebService extends Service {

	private static final int THREAD_POOL_SIZE = 2;
    private static final int SEND_REQUEST_DELAY_MS = 1000;
	ExecutorService mExecutorService;
	ExecutorService mSendMessageExecutorService;
    private List<ExecuteCommand> m_commandsPool = null;

	/**
	 * Action-commands correspondence map for WebService low coupling purpose
	 */
	HashMap<WebAction, Class<? extends AbstractCommand>> mComandMap;

	public WebService() {
		mExecutorService = Executors.newSingleThreadExecutor();
		mSendMessageExecutorService = Executors.newSingleThreadExecutor();
        m_commandsPool = Collections.synchronizedList(new ArrayList<ExecuteCommand>());
		mComandMap = new HashMap<WebAction, Class<? extends AbstractCommand>>();
		mComandMap.put(WebAction.deviceRegistrationStep1, VerifyPhoneCommand.class);
		mComandMap.put(WebAction.deviceRegistrationStep2, VerifySMSCommand.class);
		mComandMap.put(WebAction.getProfile, GetProfileCommand.class);
		mComandMap.put(WebAction.setProfile, SetProfileCommand.class);
		mComandMap.put(WebAction.setProfilePicture, SetProfilePictureCommand.class);
		mComandMap.put(WebAction.getProfilePicture, GetProfilePictureCommand.class);
		mComandMap.put(WebAction.getContacts, GetContactsCommand.class);
		mComandMap.put(WebAction.getPagingGroups, GetPagingGroupsCommand.class);
		mComandMap.put(WebAction.getPagingGroupDetails, GetPagingGroupDetailsCommand.class);
		mComandMap.put(WebAction.getInbox, GetInboxCommand.class);
		mComandMap.put(WebAction.getSentMessages, GetSentMessagesCommand.class);
		mComandMap.put(WebAction.putFile, PutFileCommand.class);
		mComandMap.put(WebAction.startPushSession, StartPushSessionCommand.class);
		mComandMap.put(WebAction.getUpdates, GetUpdatesCommand.class);
		mComandMap.put(WebAction.sendMessage, SendMessageCommand.class);
		mComandMap.put(WebAction.markMessageDelivered, MarkMessageDeliveredCommand.class);
		mComandMap.put(WebAction.markMessageReplied, MarkMessageRepliedCommand.class);
		mComandMap.put(WebAction.markMessageRead, MarkMessageReadCommand.class);
		mComandMap.put(WebAction.requestContact, RequestContactCommand.class);
		mComandMap.put(WebAction.setStatus, SetStatusCommand.class);
		mComandMap.put(WebAction.rejectContact, RejectContactCommand.class);
		mComandMap.put(WebAction.acceptContact, AcceptContactCommand.class);
		mComandMap.put(WebAction.removeContact, RemoveContactCommand.class);
		mComandMap.put(WebAction.setForward, SetForwardCommand.class);
		mComandMap.put(WebAction.markMessageAccepted, MarkMessageAcceptedCommand.class);
		mComandMap.put(WebAction.markMessageRejected, MarkMessageRejectedCommand.class);
		mComandMap.put(WebAction.lock, LockCommand.class);
		mComandMap.put(WebAction.armCallback, ArmCallbackCommand.class);
		mComandMap.put(WebAction.stopPushSession, StopPushSessionCommand.class);
        mComandMap.put(WebAction.markMessageCalled, MarkMessageCalledCommand.class);
        mComandMap.put(WebAction.getSecretQuestions, GetSecretQuestionsCommand.class);
        mComandMap.put(WebAction.setSecretQuestions, SetSecretQuestionsCommand.class);
        mComandMap.put(WebAction.getSecretQuestion, GetSecretQuestionCommand.class);
        mComandMap.put(WebAction.checkSecretQuestion, CheckSecretQuestionCommand.class);
		// 'syncronize' used here to distinguish 'true setStatus' against setStatus after sync
		mComandMap.put(WebAction.syncronize, SyncronizeCommand.class);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			WebAction action = WebAction.valueOf(SmartPagerApplication.getExtractActionName(intent.getAction()));
			Bundle extras = intent.getExtras();
            if(action == WebAction.cancelWebService)
            {
                WebAction actionToCancel = null;
                if(extras.containsKey(BundleKey.canceledWebAction.name()))
                {
                    try
                    {
                        actionToCancel = WebAction.valueOf(extras.getString(BundleKey.canceledWebAction.name()));
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                if(actionToCancel != null)
                {
                    Iterator<ExecuteCommand> iterator = m_commandsPool.iterator();
                    while(iterator.hasNext())
                    {
                        ExecuteCommand executeCommand = iterator.next();
                        if(executeCommand != null && executeCommand.isWorking() && executeCommand.getWebAction() == actionToCancel)
                        {
                            executeCommand.stopWorking();
                        }
                    }
                }
                return Service.START_NOT_STICKY;
            }
			AbstractCommand command = mComandMap.get(action).newInstance();
			command.setArguments(extras);
			if ((extras != null) && extras.containsKey(BundleKey.initiatedByUser.name())) {
				boolean byUser = extras.getBoolean(BundleKey.initiatedByUser.name());
				if (!byUser) {
					command.setInitiatedByUser(byUser);
				}
			}
            ExecuteCommand executeCommand = null;
			switch (action) {
				case sendMessage:
                    executeCommand = new SendMessageExecuteComand(command);
					mSendMessageExecutorService.submit(executeCommand);
				break;

				default:
                    executeCommand = new DefaultExecuteComand(command);
					mExecutorService.submit(executeCommand);
				break;
			}
            m_commandsPool.add(executeCommand);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Service.START_STICKY;
	}

	class DefaultExecuteComand extends ExecuteCommand {

		public DefaultExecuteComand(AbstractCommand comand) {
			super(comand);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void handelResult(RequestSender requestSender, AbstractResponse abstractResponce) {

			for (AbstractCommand comand : abstractResponce.getPostComandList()) {
				doCommand(requestSender, comand);
			}

		}

	}

	class SendMessageExecuteComand extends ExecuteCommand {

		public SendMessageExecuteComand(AbstractCommand comand) {
			super(comand);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void handelResult(RequestSender requestSender, AbstractResponse abstractResponce) {
			for (AbstractCommand command : abstractResponce.getPostComandList()) {
				// doCommand(requestSender, comand);
				mExecutorService.submit(new DefaultExecuteComand(command));
			}

		}

	}

	/**
	 * Runnable for executing current command in multithreading mode
	 * 
	 * @author Roman <br>
	 */
	abstract class ExecuteCommand implements Runnable {
		private AbstractCommand mCommand;
        private volatile boolean m_bIsWorking = false;

		/**
		 * Constructor
		 * 
		 * @param comand
		 * @see AbstractCommand
		 */
		public ExecuteCommand(AbstractCommand comand) {
			mCommand = comand;
		}

		/**
		 * Runnable method implementation<br>
		 * Calls {@linkplain #doCommand}
		 */
		@Override
		public void run() {
			RequestSender requestSender = new RequestSender();
            m_bIsWorking = true;
			doCommand(requestSender, mCommand);
		}

		/**
		 * Recursive method for sequential execution all of pre-commands, the command itself and all of post-commands
		 * for current command <br>
		 * Number of tries for each command sets in {@linkplain }
		 * 
		 * @param requestSender
		 * @param abstractComand
		 * @return list
		 */
		protected ArrayList<String> doCommand(RequestSender requestSender, AbstractCommand abstractComand) {
			ArrayList<String> results = new ArrayList<String>();
			ArrayList<String> preComandResults = new ArrayList<String>();
			AbstractResponse errorResponce = null;
			int internetCheckMaxTries = 3;
			int trial = 0;
			do {

				if (NetworkUtils.isInternetConnected()) {
					break;
				}

				trial++;
				if (trial == internetCheckMaxTries) {
					errorResponce = abstractComand.getErrorResponce(getString(R.string.data_network_timed_out));
					sendResult(abstractComand, errorResponce);
					return results;
				}
				try {
					Log.POINT();
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} while (trial >= internetCheckMaxTries);

			for (int i = 0; i < Constants.SEND_REQUEST_MAX_COUNT; i++) {
				try {
					for (AbstractCommand comand : abstractComand.getPreCommandList()) {
						ArrayList<String> result = doCommand(requestSender, comand);
						if (result.isEmpty()) {
							if (abstractComand.getResponseAction() == WebAction.syncronize) {
                                SmartPagerApplication.getInstance().getPreferences().setSyncState(SyncState.NotSyncronized);
							}
							return result;
						}
						preComandResults.addAll(result);
					}
					abstractComand.setPreCommandResults(preComandResults);
					Log.e(abstractComand.getWebAction());
					//TimeChecker.SetStartPoint();

                    if(!m_bIsWorking)
                    {
                        cancelCommand(abstractComand);
                        break;
                    }
					String result = requestSender.execRaw(abstractComand);

					//TimeChecker.WriteTimeSpendInMiliSeconds();
                    if(!m_bIsWorking)
                    {
                        cancelCommand(abstractComand);
                        break;
                    }
					results.add(result);

                    if(!m_bIsWorking)
                    {
                        cancelCommand(abstractComand);
                        break;
                    }
					AbstractResponse abstractResponce = abstractComand.getResponse(result);

                    if(!m_bIsWorking)
                    {
                        cancelCommand(abstractComand);
                        break;
                    }
                    abstractResponce.processResponse(abstractResponce.getJsonObject());

					// mirror 'byUser' from command into corresponding response
					boolean byUser = abstractComand.isInitiatedByUser();
					if (!byUser) {
						abstractResponce.setInitiatedByUser(byUser);
					}

                    if(!m_bIsWorking)
                    {
                        cancelCommand(abstractComand);
                        break;
                    }
					handelResult(requestSender, abstractResponce);
                    if(!m_bIsWorking)
                    {
                        cancelCommand(abstractComand);
                        break;
                    }
					sendResult(abstractComand, abstractResponce);
					errorResponce = null;
					break;
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					errorResponce = abstractComand.getErrorResponce("ClientProtocolException");
				} catch (UnknownHostException e) {
					e.printStackTrace();
					Context context = SmartPagerApplication.getInstance().getApplicationContext();
					errorResponce = abstractComand.getErrorResponce(context.getString(R.string.no_internet_connection));
					break;
				} catch (ConnectTimeoutException e) {
					e.printStackTrace();
					errorResponce = abstractComand.getErrorResponce("Error");
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					errorResponce = abstractComand.getErrorResponce("Error");
				} catch (IOException e) {
					e.printStackTrace();
					// to avoid dummy notifications
					// errorResponce = abstractComand.getErrorResponce("IOException");
				} catch (Exception e) {
					e.printStackTrace();
					// to avoid dummy notifications
					// errorResponce = abstractComand.getErrorResponce("Exception");
				}
				try {
					Log.POINT();
					Thread.sleep(SEND_REQUEST_DELAY_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (errorResponce != null) {
				if (abstractComand.getResponseAction() == WebAction.syncronize) {
                    SmartPagerApplication.getInstance().getPreferences().setSyncState(SyncState.NotSyncronized);
				}
				sendResult(abstractComand, errorResponce);
			}
            if(abstractComand.getWebAction() == mCommand.getWebAction())
            {
                boolean wasRemoved = m_commandsPool.remove(this);
                if(wasRemoved)
                    ((ArrayList<ExecuteCommand>)m_commandsPool).trimToSize();
                m_bIsWorking = false;
            }
			return results;
		}

		abstract protected void handelResult(RequestSender requestSender, AbstractResponse abstractResponce);
		// private void handelResult(RequestSender requestSender, AbstractResponse abstractResponce) {
		// for (AbstractCommand comand : abstractResponce.getPostComandList()) {
		// doCommand(requestSender, comand);
		// }
		// }

        private void cancelCommand(AbstractCommand abstractCommand)
        {
            abstractCommand.cancel();
            if(abstractCommand.getWebAction() == mCommand.getWebAction())
                sendResult(abstractCommand, null);
        }

        public void stopWorking ()
        {
            m_bIsWorking = false;
        }

        public boolean isWorking ()
        {
            return m_bIsWorking;
        }

        public WebAction getWebAction ()
        {
            return mCommand.getWebAction();
        }
	}

	/**
	 * Sends server response results to {@linkplain BaseActivity}'s broadcast receiver
	 * 
	 * @param abstractCommand
	 * @param errorResponse
	 */
	private void sendResult(AbstractCommand abstractCommand, AbstractResponse errorResponse) {
		Intent intent = new Intent(SmartPagerApplication.getActionName(abstractCommand.getResponseAction().name()));
		intent.putExtra(BundleKey.response.name(), errorResponse);
        intent.putExtra(BundleKey.commandCancelled.name(), abstractCommand.wasCancelled());
		sendBroadcast(intent);
	}
}