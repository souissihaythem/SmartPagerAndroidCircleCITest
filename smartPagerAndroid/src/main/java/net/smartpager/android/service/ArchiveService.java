package net.smartpager.android.service;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.database.DatabaseHelper.ImageUrlTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.database.MessagesQueryHelper;
import net.smartpager.android.utils.DateTimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import biz.mobidev.library.lazybitmap.FileCache;

/**
 * Service for archiving messages and removing them  from archive  
 * @author Roman
 */
public class ArchiveService extends Service {

	public enum ArchiveActions {
		addToArchive, 
		removeFromArchive, 
		autoCleanArchive, 
		removeAllFromArchive, 
		archiveAllRead
	}

	private final int THREAD_POOL_SIZE = 2;
	private ExecutorService mExecutorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
            AbstractArchiveRunnable runnable = null;
            //This try/catch block was added to avoid crash if the intent.getAction() return wrong value
            try {
                ArchiveActions action = ArchiveActions.valueOf((intent.getAction()));
                switch (action) {
                    case addToArchive:
                        runnable = new AddToArchiveRunnable(intent);
                        break;
                    case removeFromArchive:
                        runnable = new RemoveFromArchiveRunnable(intent);
                        break;
                    case autoCleanArchive:
                        runnable = new AutoCleanArchiveRunnable(intent);
                        break;
                    case removeAllFromArchive:
                        runnable = new CleanAllArchiveRunnable(intent);
                        break;
                    case archiveAllRead:
                        runnable = new ArchiveAllReadRunnable(intent);
                        break;
                    default:
                        break;
                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }
			if (runnable != null) {
				mExecutorService.execute(runnable);
			}
		}
		return Service.START_STICKY;
	}

	// ---------------------------------------------------------------
	/**
	 * Adds selected messages thread to archive by setting <b>"archived"</b> flag 
	 * @author Roman
	 */
	private class AddToArchiveRunnable extends AbstractArchiveRunnable {

		public AddToArchiveRunnable(Intent intent) {
			super(intent);
		}

		@Override
		public void run() {
			String threadId = intent.getStringExtra(BundleKey.threadId.name());
			Uri uri = SmartPagerContentProvider.CONTENT_MESSAGE_URI;
			ContentValues values = new ContentValues();
			values.put(MessageTable.archived.name(), 1);
			String where = MessageTable.threadID.name() + " = " + threadId;
			getContentResolver().update(uri, values, where, null);
			getContentResolver().notifyChange(uri, null);
			getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_CHATS_URI, null);
		}
	}
	// ---------------------------------------------------------------
	/**
	 * Adds all read messages threads to archive by setting <b>"archived"</b> flag 
	 * @author Roman
	 */
	private class ArchiveAllReadRunnable extends AbstractArchiveRunnable {
		
		public ArchiveAllReadRunnable(Intent intent) {
			super(intent);
		}
		
		@Override
		public void run() {
			Uri uri = SmartPagerContentProvider.CONTENT_MESSAGE_URI;
			ContentValues values = new ContentValues();
			values.put(MessageTable.archived.name(), 1);
			String where = MessagesQueryHelper.selectArchiveAllRead();
			getContentResolver().update(uri, values, where, null);
			getContentResolver().notifyChange(uri, null);
			getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_CHATS_URI, null);			
		}
	}

	// ---------------------------------------------------------------
	/**
	 * Removes selected messages thread from archive and deletes appropriate records from database 
	 * @author Roman
	 */
	private class RemoveFromArchiveRunnable extends AbstractArchiveRunnable {

		public RemoveFromArchiveRunnable(Intent intent) {
			super(intent);
		}

		@Override
		public void run() {
			String threadId = intent.getStringExtra(BundleKey.threadId.name());
			String selection = MessageTable.threadID.name() + " = " + threadId;
			cleanArchive(selection);
		}
	}

	// ---------------------------------------------------------------
	/**
	 * Removes messages from archive by manual request
	 * @author Roman
	 */
	private class CleanAllArchiveRunnable extends AbstractArchiveRunnable {

		public CleanAllArchiveRunnable(Intent intent) {
			super(intent);
		}

		@Override
		public void run() {
			String selection = MessagesQueryHelper.selectArchivedChatTable();
			cleanArchive(selection);
			SmartPagerApplication.getInstance().getPreferences().setLastArchiveClean(System.currentTimeMillis());
		}
	}

	// ---------------------------------------------------------------
	/**
	 * Removes messages from archive once a day according to the expiration time
	 * @author Roman
	 */
	private class AutoCleanArchiveRunnable extends AbstractArchiveRunnable {

		public AutoCleanArchiveRunnable(Intent intent) {
			super(intent);
		}

		@Override
		public void run() {
			long threshold = DateTimeUtils.MS_IN_DAY;
			long lastClean = SmartPagerApplication.getInstance().getPreferences().getLastArchiveClean();
			long now = System.currentTimeMillis();
			if (now - lastClean >= threshold) {
				String selection = MessagesQueryHelper.selectAutoCleanArchive();
				cleanArchive(selection);
				SmartPagerApplication.getInstance().getPreferences().setLastArchiveClean(System.currentTimeMillis());
			}
		}
	}

	// ---------------------------------------------------------------
	private abstract class AbstractArchiveRunnable implements Runnable {
		protected Intent intent;

		public AbstractArchiveRunnable(Intent intent) {
			this.intent = intent;
		}
	}
	// ---------------------------------------------------------------

	/**
	 * Deletes records from database and files from device for selected messages
	 * @param selection
	 */
	private void cleanArchive(String selection) {
		Map<String, String> idRecordMap = selectIdRecords(selection);
		List<String> messageIdList = getMessageId(idRecordMap);
		List<String> recordUrlList = getRecordsUrl(idRecordMap);
		List<String> imageUrlList = getImagesUrl(messageIdList);
		deletePDFs();
		deleteAllRecordFiles(recordUrlList);
		deleteAllImageFiles(imageUrlList);
		deleteImagesFromDb(messageIdList);
		deleteMessagesFromDb(messageIdList);
		notifyChanges();
	}

	/**
	 * Gets map of messageID - recordUrl correspondence via SQL request
	 * @param selection  - WHERE clause to SQL query
	 * @return map <messageID , recordUrl>
	 */
	private Map<String, String> selectIdRecords(String selection) {
		Uri uri = SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI;
		Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
		Map<String, String> idRecords = new HashMap<String, String>();
		if (cursor.moveToFirst()) {
			do {
				String messageId = cursor.getString(MessageTable.id.ordinal());
				String recordUrl = cursor.getString(MessageTable.recordUrl.ordinal());
				idRecords.put(messageId, recordUrl);
			} while (cursor.moveToNext());
		}
		return idRecords;
	}

	/**
	 * Gets messageID list from map of messageID - recordUrl correspondence 
	 * @param idRecordMap
	 * @return  messageID list
	 */
	private List<String> getMessageId(Map<String, String> idRecordMap) {
		return new ArrayList<String>(idRecordMap.keySet());
	}

	/**
	 * Gets recordUrl list from map of messageID - recordUrl correspondence 
	 * @param idRecordMap
	 * @return recordUrl list
	 */
	private List<String> getRecordsUrl(Map<String, String> idRecordMap) {
		List<String> recordUrlList = new ArrayList<String>();
		for (String messageId : idRecordMap.keySet()) {
			String recordUrl = idRecordMap.get(messageId);
			if (!TextUtils.isEmpty(recordUrl)) {
				recordUrlList.add(recordUrl);
			}
		}
		return recordUrlList;
	}

	/**
	 * Gets imageUrl list by messageID list via SQL request
	 * @param messageIdList
	 * @return  imageUrl list
	 */
	private List<String> getImagesUrl(List<String> messageIdList) {
		List<String> imageUrlList = new ArrayList<String>();
		if (messageIdList != null && messageIdList.size() > 0) {
			Uri uri = SmartPagerContentProvider.CONTENT_IMAGE_URL_URI;
			String selection = getWhereStatement(messageIdList);
			Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
			if (cursor.moveToFirst()) {
				do {
					String imageUrl = cursor.getString(ImageUrlTable.ImageUrl.ordinal());
					imageUrlList.add(imageUrl);
				} while (cursor.moveToNext());
			}
		}
		return imageUrlList;
	}

	/**
	 * Constructs WHERE statement for SQL query base on messageID list 
	 * @param messageIdList 
	 * @return WHERE statement
	 */
	private String getWhereStatement(List<String> messageIdList) {
		StringBuilder builder = new StringBuilder(ImageUrlTable.messageId.name());
		builder.append(" IN (");
		for (int i = 0; i < messageIdList.size(); i++) {
			if (i != 0) {
				builder.append(", ");
			}
			builder.append(messageIdList.get(i));
		}
		builder.append(") ");
		String where = builder.toString();
		return where;
	}

	private void deletePDFs() {
		File dir = SmartPagerApplication.getInstance().getExternalFilesDir(null);
		if (dir.exists()) {
			File[] files = dir.listFiles();
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (file.isFile()) {
					file.delete();
				}
			}
		}
	}

//	/**
//	 * Deletes pdf files from the device
//	 * @param threadID of files to be deleted
//	 */
//	private void deleteAllPdfFilesForThread(String threadID) {
//		File dir = SmartPagerApplication.getInstance().getExternalFilesDir(null);
//		if (dir.exists()) {
//			File[] files = dir.listFiles();
//			for (int i = 0; i < files.length; i++) {
//				File file = files[i];
//				String fileName = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("/") + 1);
//				if (file.isFile()) {
//					if (fileName.indexOf("-") != -1 && fileName.indexOf("pdf") != -1) {
//						if (threadID.equalsIgnoreCase(fileName.substring(0, fileName.indexOf("-")))) {
//							file.delete();
//						}
//					}
//				}
//			}
//		}
//	}

	/**
	 * Deletes audio files from the device
	 * @param recordUrlList of files to be deleted
	 * @see #deleteAll
	 */
	private void deleteAllRecordFiles(List<String> recordUrlList) {
		deleteAll(recordUrlList);
	}

	/**
	 * Deletes images files from the device
	 * @param imageUrlList of files to be deleted
	 * @see #deleteAll
	 */
	private void deleteAllImageFiles(List<String> imageUrlList) {
		deleteAll(imageUrlList);
	}

	/**
	 * Deletes files from the device
	 * @param urlList of files to be deleted
	 * @return count of deleted files
	 */
	private int deleteAll(List<String> urlList) {
		int count = 0;
		for (String url : urlList) {
			File file = FileCache.getFile(url);
			if (file.exists()) {
				file.delete();
				count++;
			}
		}
		return count;
	}

	/**
	 * Deletes records from the <i><b>ImageUrl</b></i> table in database according to messageID
	 * @param messageIdList
	 */
	private void deleteImagesFromDb(List<String> messageIdList) {
		if (messageIdList != null && messageIdList.size() > 0) {
			Uri uri = SmartPagerContentProvider.CONTENT_IMAGE_URL_URI;
			String where = getWhereStatement(messageIdList);
			getContentResolver().delete(uri, where, null);
		}
	}

	/**
	 * Deletes records from the <i><b>Message</b></i> table in database
	 * @param messageIdList
	 */
	private void deleteMessagesFromDb(List<String> messageIdList) {
		if (messageIdList != null && messageIdList.size() > 0) {
			Uri uri = SmartPagerContentProvider.CONTENT_MESSAGE_URI;
			StringBuilder builder = new StringBuilder(MessageTable.id.name());
			builder.append(" IN (");
			for (int i = 0; i < messageIdList.size(); i++) {
				if (i != 0) {
					builder.append(", ");
				}
				builder.append(messageIdList.get(i));
			}
			builder.append(") ");
			String where = builder.toString();
			getContentResolver().delete(uri, where, null);
		}
	}

	private void notifyChanges() {
		getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_CHATS_URI, null);
		getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_URI, null);
		getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_IMAGE_URL_URI, null);
	}

}
