package net.smartpager.android.database;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.ChatsTable.MessageType;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.utils.DateTimeUtils;

public class MessagesQueryHelper {

	public static String selectNonArchivedChatTable() {
		// (non-read OR (time-not-exceeded AND non-archived))		
		return "(NOT " + read() + " OR ( " + isNotYetArchiveTime() + " AND NOT " + archived() + "))";
	}

	public static String selectArchivedChatTable() {
		// ( read AND (time-exceeded  OR ( archived AND not-time-to-archive)))
		return "(" + read() + " AND (" + isPutToArchiveTime() +  " OR (" + archived() + " AND " + isNotYetArchiveTime() + ")))" ;
	}
	
	public static String selectArchiveAllRead() {
		// ( read )
		return read();
	}
	
	public static String selectByMessageId(String msgId) {
		return MessageTable.id.name() + " == " + msgId;
	}
	
	public static String selectAutoCleanArchive() {
		// (time-to-remove AND  read )
		return "(" + isRemoveFromArchiveTime() + " AND " + read() + ")";  
//		return "(" + isRemoveFromArchiveTime() + " AND (" + read() + " OR " + archived() + "))";  
	}
	
	public static String selectUnreadCritical(){
		// (non-read AND critical)		
		return "(NOT " + read() + " AND " + critical() + ") GROUP BY threadID";
	}

	public static String selectUnread(){
		// (non-read)		
		return "(NOT " + read() + ") GROUP BY threadID";
	}
	
	public static String selectUnreadCasual() {
		return "(NOT " + read() + ") AND threadID != id GROUP BY threadID";
	}
	public static String selectUnreadByThread(String threadID){
		// (non-read)		
		return "(NOT " + read() + " AND threadID = "+threadID+")";
	}
	//---------------------------------------------------------------
	private static String read(){
		/*return String.format( "((readTime >'%s' AND messageType ='%s') OR (messageType ='%s'))",
				Constants.DEFAULT_DATE.getTime(), MessageType.inbox.ordinal(), MessageType.sent.ordinal());*/
		//return String.format( "(readTime != '%s' OR messageType != '%s')",
		//		Constants.DEFAULT_DATE.getTime(), MessageType.inbox.ordinal());
		
		StringBuilder readStatusList = new StringBuilder();
		for (MessageStatus status : MessageStatus.values()) {
			if (status.ordinal() >= MessageStatus.Read.ordinal()) {
				readStatusList.append("'"+status.name()+"'");
				readStatusList.append(",");
			}
		}
		readStatusList.setLength(readStatusList.length() - 1);
		
		return String.format("(messageStatus in (%s) OR messageType == '%s')", readStatusList, MessageType.sent.ordinal());
	}
	
	private static String critical(){
		return  "(critical != '0')";
	}
	
	private static String archived(){
		return "(archived IS NOT NULL AND archived != '0')";		
	}
	
	private static String isNotYetArchiveTime(){
		long putToArchive = DateTimeUtils.getTimeInMillis(SmartPagerApplication.getInstance().getSettingsPreferences().getArchiveMessages());
		return String.format("(lastUpdate > '%s')", putToArchive);
	}
	
	private static String isPutToArchiveTime(){
		long putToArchive = DateTimeUtils.getTimeInMillis(SmartPagerApplication.getInstance().getSettingsPreferences().getArchiveMessages());
		long removeFromArchive = DateTimeUtils.getTimeInMillis(SmartPagerApplication.getInstance().getSettingsPreferences().getRemoveFromArchive());
		return String.format("(lastUpdate <= '%s' AND lastUpdate > '%s')", 
				putToArchive, removeFromArchive);
	}
	
	private static String isRemoveFromArchiveTime(){
		long removeFromArchive = DateTimeUtils.getTimeInMillis(SmartPagerApplication.getInstance().getSettingsPreferences().getRemoveFromArchive());
		return String.format("(lastUpdate <='%s')", removeFromArchive);  
	}

}
