package net.smartpager.android;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import net.smartpager.android.activity.LocalGroupDetailsActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.QueryMessageThreadTable;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.PagingGroupTable;
import net.smartpager.android.database.DatabaseHelper.RecipientTable;
import net.smartpager.android.database.DatabaseHelper.Tables;

import biz.mobidev.framework.utils.Log;

public class SmartPagerContentProvider extends ContentProvider {

	public final static String AUTHORITY = "net.smartpager.android";
	public final static Uri CONTENT_CONTACT_URI;
	public final static Uri CONTENT_MESSAGE_URI;
	public final static Uri CONTENT_PAGINGROUP_URI;
	public final static Uri CONTENT_CONTACT_GROUP_URI;
	public final static Uri CONTENT_RECIPIENTS_URI;
	public final static Uri CONTENT_RECIPIENTS_FULLINFO_URI;
	public final static Uri CONTENT_RECIPIENTS_THREAD_URI;
	public final static Uri CONTENT_IMAGE_URL_URI;
	public final static Uri CONTENT_MESSAGE_THREADS_URI;
	public final static Uri CONTENT_SIMPLE_MESSAGE_URI;
	public final static Uri CONTENT_QUICK_RESPONSE_URI;
	public final static Uri CONTENT_CHATS_URI;
	public final static Uri CONTENT_LOCAL_GROUP_CORRESPONDENCE_URI;
	public final static Uri CONTENT_LOCAL_GROUP_CONTACTS_URI;
    public final static Uri CONTENT_THREAD_GROUP_CORRESPONDENCE_URI;

	private static final String CONTENT_BASE_URI = "content://net.smartpager.android/";
	public static final String DATABASE_NAME = "SmartPagerDB.db";
	public static final int DATABASE_VERSION = 4;

	private SQLiteDatabase mSmartPagerDB;

	static {
		CONTENT_CONTACT_URI = Uri.parse(CONTENT_BASE_URI + Tables.Contacts.name());
		CONTENT_MESSAGE_URI = Uri.parse(CONTENT_BASE_URI + Tables.Message.name());
		CONTENT_CHATS_URI = Uri.parse(CONTENT_BASE_URI + Tables.Chats.name());
		CONTENT_PAGINGROUP_URI = Uri.parse(CONTENT_BASE_URI + Tables.PagingGroups.name());
		CONTENT_CONTACT_GROUP_URI = Uri.parse(CONTENT_BASE_URI + Tables.ContactGroup.name());
		CONTENT_RECIPIENTS_URI = Uri.parse(CONTENT_BASE_URI + Tables.Recipients.name());
		CONTENT_IMAGE_URL_URI = Uri.parse(CONTENT_BASE_URI + Tables.ImageUrl.name());
		CONTENT_MESSAGE_THREADS_URI = Uri.parse(CONTENT_BASE_URI + Tables.MessageThreads.name());
		CONTENT_RECIPIENTS_FULLINFO_URI = Uri.parse(CONTENT_BASE_URI + Tables.RecipientFullInfo.name());
		CONTENT_RECIPIENTS_THREAD_URI = Uri.parse(CONTENT_BASE_URI + Tables.RecipientThread.name());
		CONTENT_QUICK_RESPONSE_URI = Uri.parse(CONTENT_BASE_URI + Tables.QuickResponse.name());
		CONTENT_SIMPLE_MESSAGE_URI = Uri.parse(CONTENT_BASE_URI + Tables.SimpleMessage.name());
		CONTENT_LOCAL_GROUP_CORRESPONDENCE_URI = Uri.parse(CONTENT_BASE_URI + Tables.LocalGroupCorrespondence.name());
		CONTENT_LOCAL_GROUP_CONTACTS_URI = Uri.parse(CONTENT_BASE_URI + Tables.LocalGroupContacts.name());
        CONTENT_THREAD_GROUP_CORRESPONDENCE_URI = Uri.parse(CONTENT_BASE_URI + Tables.ThreadGroupCorrespondence.name());
	}

	public SmartPagerContentProvider() {}

	@Override
	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
		mSmartPagerDB = dbHelper.getWritableDatabase();
		return (mSmartPagerDB == null) ? false : true;
	}

	@Override
	public String getType(Uri uri) {
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String currentTable = uri.getPathSegments().get(0);
		int count = mSmartPagerDB.delete(currentTable, selection, selectionArgs);
		switch (Tables.valueOf(currentTable)) {
			case Message:
				getContext().getContentResolver().notifyChange(CONTENT_CHATS_URI, null);
				getContext().getContentResolver().notifyChange(CONTENT_MESSAGE_THREADS_URI, null);
			break;
			case Contacts:
				getContext().getContentResolver().notifyChange(CONTENT_CONTACT_URI, null);
				getContext().getContentResolver().notifyChange(CONTENT_PAGINGROUP_URI, null);
				getContext().getContentResolver().notifyChange(CONTENT_CONTACT_GROUP_URI, null);
			break;
			default:
				getContext().getContentResolver().notifyChange(uri, null);
			break;
		}
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String currentTable = uri.getPathSegments().get(0);
        long rowID = mSmartPagerDB.replace(currentTable, "quake", values);
		// long rowID = mSmartPagerDB.insert(currentTable, "quake", values);
		if (rowID > 0) {
			Uri newUri = ContentUris.withAppendedId(uri, rowID);
			// getContext().getContentResolver().notifyChange(newUri, null);
			// switch (Tables.valueOf(currentTable)) {
			// case Message:
			// getContext().getContentResolver().notifyChange(CONTENT_MESSAGE_URI, null);
			// getContext().getContentResolver().notifyChange(CONTENT_MESSAGE_THREADS_URI, null);
			// break;
			// case Contacts:
			// getContext().getContentResolver().notifyChange(CONTENT_CONTACT_URI, null);
			// getContext().getContentResolver().notifyChange(CONTENT_PAGINGROUP_URI, null);
			// break;
			// default:
			// //getContext().getContentResolver().notifyChange(newUri, null);
			// break;
			// }
			return newUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
		String currentTable = uri.getPathSegments().get(0);
		int count = mSmartPagerDB.update(currentTable, values, whereClause, whereArgs);
		// Log.e( currentTable, values, whereClause, whereArgs[0], count);
		if (count > 0) {
			// getContext().getContentResolver().notifyChange(uri, null);
			// switch (Tables.valueOf(currentTable)) {
			// case Message:
			// getContext().getContentResolver().notifyChange(CONTENT_MESSAGE_URI, null);
			// getContext().getContentResolver().notifyChange(CONTENT_MESSAGE_THREADS_URI, null);
			// break;
			// case Contacts:
			// getContext().getContentResolver().notifyChange(CONTENT_CONTACT_URI, null);
			// getContext().getContentResolver().notifyChange(CONTENT_PAGINGROUP_URI, null);
			// break;
			// default:
			// getContext().getContentResolver().notifyChange(uri, null);
			// break;messageId
			// }
		}
		return count;
		// throw new SQLException("Failed to update " + uri);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String currentTable = uri.getPathSegments().get(0);
		Cursor cursor = null;
		switch (Tables.valueOf(currentTable)) {
			case Contacts:
				cursor = queryContact(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case ContactGroup:
				cursor = queryContactGroup(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case Message:
				cursor = queryMessage(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case Chats:
				cursor = queryThread(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case MessageThreads:
				cursor = queryMessageThread(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case SimpleMessage:
				cursor = querySimpleMessage(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case Recipients:
				cursor = queryRecipients(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case RecipientFullInfo:
				cursor = queryRecipientsFullInfo(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case RecipientThread:
				cursor = queryRecipientsThread(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case ImageUrl:
				cursor = queryImageUrl(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			case LocalGroupCorrespondence:
				cursor = queryLocalGroupCorrespondence(uri, projection, selection, selectionArgs, sortOrder,
						currentTable);
			break;
			case LocalGroupContacts:
				cursor = queryLocalGroupContacts(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
			default:
				cursor = queryContacts(uri, projection, selection, selectionArgs, sortOrder, currentTable);
			break;
		}
		return cursor;
	}

	private Cursor queryLocalGroupContacts(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT DISTINCT * ");
		query.append(" FROM  	Contacts  ");
		query.append(" WHERE	  ");
		query.append("	  Contacts.id IN (");
		query.append("					SELECT DISTINCT LocalGroupCorrespondence.contactId ");
		query.append("					FROM LocalGroupCorrespondence  ");
		if (!TextUtils.isEmpty(selection)) {
			query.append("				WHERE LocalGroupCorrespondence.localGroupId IN ( ");
			query.append(selection).append(" )");
		}
		query.append("				);");
		Cursor cursor = mSmartPagerDB.rawQuery(query.toString(), null);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryLocalGroupCorrespondence(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder, String currentTable) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT * FROM ").append(Tables.LocalGroupCorrespondence.name());
		if (!TextUtils.isEmpty(selection)) {
			query.append(" WHERE ").append(selection);
		}
		query.append(";");
		Cursor cursor = mSmartPagerDB.rawQuery(query.toString(), null);
		return cursor;
	}

	private Cursor querySimpleMessage(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT * FROM ").append(Tables.Message.name());
		if (!TextUtils.isEmpty(selection)) {
			query.append(" WHERE ").append(selection);
		}
		query.append(";");
		Cursor cursor = mSmartPagerDB.rawQuery(query.toString(), null);
		return cursor;
	}

	private Cursor queryRecipientsThread(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT ");
		query.append("		Contacts._id 	   AS _id,");
		query.append("		Contacts.id	   AS id, ");
		query.append("		Contacts.firstName AS name,");
		query.append("		Contacts.lastName  AS contact_lastname_or_group_type,");
		query.append("		Contacts.title	   AS contact_title_or_group_empty, ");
		query.append("		Contacts.photoUrl  AS contact_url_or_group_empty, ");
		query.append("		Contacts.status	   ,	 ");
		query.append("		Contacts.phoneNumber, ");
		query.append("		Contacts.smartPagerID AS contact_smart_pager_id_or_group_id,");
		query.append("		'0' isGroup ");
		query.append("		");
		query.append(" FROM  	Contacts  ");
		query.append(" WHERE	  ");
		// query.append("		AND  messageId  = ").append(selection);
		query.append("		  Contacts.id IN (");
		// query.append("				(");
		query.append("					SELECT DISTINCT Recipients.contactId ");
		query.append("					FROM Recipients  ");
		if (!TextUtils.isEmpty(selection)) {
			query.append("				WHERE Recipients.messageId IN (SELECT id FROM Message WHERE threadID = ")
					.append(selection).append(")");
		}
		query.append("				);");
		Cursor cursor = mSmartPagerDB.rawQuery(query.toString(), null);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryImageUrl(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT * FROM ").append(DatabaseHelper.Tables.ImageUrl);
		if (!TextUtils.isEmpty(selection)) {
			builder.append(" WHERE ").append(selection);
		}
		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryRecipients(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder builder = new StringBuilder();
		// builder.append(" SELECT DISTINCT contactId, smartPagerID, messageId ");
		builder.append(" SELECT * ");
		builder.append(" FROM ").append(DatabaseHelper.Tables.Recipients);
		if (!TextUtils.isEmpty(selection)) {
			builder.append(" WHERE messageId = ").append(selection);
		}
		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryRecipientsFullInfo(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder builder = new StringBuilder();
		builder.append("select ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable._id.name()).append(" as ").append(GroupContactItem._id.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.id.name()).append(" as ").append(GroupContactItem.id.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.firstName.name()).append(" as ").append(GroupContactItem.name.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.lastName.name()).append(" as ").append(GroupContactItem.contact_lastname_or_group_type.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.title.name()).append(" as ").append(GroupContactItem.contact_title_or_group_empty.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.photoUrl.name()).append(" as ").append(GroupContactItem.contact_url_or_group_empty.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.status.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.phoneNumber.name()).append(", ");
		builder.append(Tables.Contacts.name()).append(".").append(ContactTable.smartPagerID.name()).append(" as ").append(GroupContactItem.contact_smart_pager_id_or_group_id.name()).append(", ");
		builder.append("'0' isGroup, ");
		builder.append(Tables.Recipients.name()).append(".* ");
		builder.append("from ").append(Tables.Recipients.name()).append(", ").append(Tables.Contacts.name());

		builder.append(" where ").append(Tables.Recipients.name()).append(".").append(RecipientTable.contactId.name())
				.append(" = ").append(Tables.Contacts.name()).append(".").append(ContactTable.id.name());
		builder.append(" And ").append(RecipientTable.messageId.name()).append(" = ").append(selection);


		// if (!TextUtils.isEmpty(selection)) {
		// builder.append(" where Contacts.firstName like '%").append(selection).append("%' OR lastName like '%").append(selection)
		// .append("%' OR name like '%").append(selection).append("%'");
		// builder.append(" where name like '%").append(selection).append("%'");
		// }
		// _id, id, firstname, lastname, 'user' f_type, photo from users union all select id, groupname, null, 'group',
		// null from fromGroupId;
		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		// SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// queryBuilder.setTables(currentTable);
		// Cursor cursor = queryBuilder.query(mSmartPagerDB, projection, selection, selectionArgs, null, null,
		// sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryContact(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		// StringBuilder builder = new StringBuilder();
		// builder.append(" SELECT * ");
		// builder.append(" FROM ").append(DatabaseHelper.Tables.Contacts);
		// if (!TextUtils.isEmpty(selection)) {
		// builder.append(" WHERE id = ").append(selection);
		// selection =builder.toString();
		// }
		Cursor cursor = mSmartPagerDB.query(DatabaseHelper.Tables.Contacts.name(), null, selection, selectionArgs,
				null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryContacts(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(currentTable);
		Cursor cursor = queryBuilder.query(mSmartPagerDB, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryThread(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {

		String filter = "";
		String dateSelect = "";
		if (selectionArgs != null) {
			filter = DatabaseUtils.sqlEscapeString("%" + selectionArgs[0] + "%");

			dateSelect = selectionArgs[1];
		}
		StringBuilder builder = new StringBuilder();
		if (TextUtils.isEmpty(filter)) {
			filter = "";
		}

		builder.append("SELECT ");
		builder.append("x.*, ");
		builder.append("contacts.*, ");
		builder.append("(SELECT COUNT(*) FROM (SELECT DISTINCT contactId FROM Recipients WHERE messageId = x.id)) ").append(QueryMessageThreadTable.numRecipients.name()).append(",  ");
		builder.append("(SELECT COUNT(*) FROM ImageUrl WHERE messageId = x.id) ").append(QueryMessageThreadTable.numImages.name()).append(",  ");
        builder.append("(SELECT Group_Concat(name) FROM PagingGroups WHERE id IN (SELECT groupID FROM ThreadGroupCorrespondence WHERE threadID = x.threadID)) ").append(QueryMessageThreadTable.groupName.name()).append(",  ");

        // Name to show on a message summary
        builder.append("(SELECT firstName FROM Contacts a, Recipients b WHERE a.id = b.contactId AND b.messageId = x.id LIMIT 1) ").append(QueryMessageThreadTable.firstName.name()).append(",  ");
		builder.append("(SELECT lastName FROM Contacts a, Recipients b WHERE a.id = b.contactId AND b.messageId = x.id LIMIT 1) ").append(QueryMessageThreadTable.lastName.name()).append(",  ");

        // Name to show on next 2 lines was added to resolve task SPANDROID-180:
		// In a group message, the name of the sender is present in the message summary.
        builder.append("(SELECT firstName FROM Contacts a WHERE a.id = x.fromContactId LIMIT 1) senderFirstName,  ");
        builder.append("(SELECT lastName FROM Contacts a WHERE a.id = x.fromContactId LIMIT 1) senderLastName  ");

		builder.append("FROM Message x ");
		//        builder.append("LEFT OUTER JOIN group ON x.id = group.smartPagerId ");
		builder.append("LEFT OUTER JOIN contacts ON x.fromSmartPagerId = contacts.smartPagerId ");
		builder.append("WHERE  ");
		builder.append(dateSelect);
		builder.append(" AND (text LIKE  " + filter + " OR subjectText LIKE " + filter + " OR firstName LIKE " + filter
				+ " OR lastName LIKE " + filter + " ) ");
		builder.append("GROUP BY x.threadId ");
		//		builder.append("HAVING MAX(lastUpdate) ORDER BY timeSent DESC; ");
		builder.append("HAVING MAX(x.id) ORDER BY x.id DESC; ");

		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryContactGroup(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {

		StringBuilder builder = new StringBuilder();
		
		builder.append("select ");
		builder.append(ContactTable._id.name()).append(", ");
		builder.append(ContactTable.id.name()).append(", ");
		builder.append(ContactTable.firstName.name()).append(" as ").append(GroupContactItem.name.name()).append(", ");
		builder.append(ContactTable.lastName.name()).append(" as ")
				.append(GroupContactItem.contact_lastname_or_group_type.name()).append(", ");
		builder.append(ContactTable.title.name()).append(" as ")
				.append(GroupContactItem.contact_title_or_group_empty.name()).append(", ");
		builder.append(ContactTable.photoUrl.name()).append(" as ")
				.append(GroupContactItem.contact_url_or_group_empty.name()).append(", ");
		builder.append(ContactTable.status.name()).append(", ");
		builder.append(ContactTable.phoneNumber.name()).append(", ");
		builder.append(ContactTable.smartPagerID.name()).append(" as ")
				.append(GroupContactItem.contact_smart_pager_id_or_group_id.name()).append(", ");
		builder.append("'0' isGroup").append(", ");
		builder.append(ContactTable.departments.name()).append(" as ")
				.append(GroupContactItem.contact_depart_or_group_empty.name());
		builder.append(" from ").append(Tables.Contacts.name());
		if (selectionArgs != null) {
			builder.append(selectionArgs[0]);
		}

		if (selectionArgs == null || !selectionArgs[2].contains(BundleKey.onlyContacts.name())) {
			builder.append(" union all select ");
			builder.append(PagingGroupTable._id.name()).append(", ");
			builder.append(PagingGroupTable.id.name()).append(", ");
			builder.append(PagingGroupTable.name.name()).append(", ");
			builder.append(PagingGroupTable.type.name()).append(", ");
			builder.append("null").append(", ");
			builder.append("null").append(", ");
			builder.append("null").append(", ");
			builder.append("null").append(", ");
			builder.append(PagingGroupTable.id.name()).append(", ");
			builder.append("'1' isGroup").append(", ");
			builder.append("null ");
			builder.append("from ").append(Tables.PagingGroups.name());
			if (selectionArgs != null) {
				builder.append(selectionArgs[1]);
			}
			if (selectionArgs != null && selectionArgs[3].contains(BundleKey.withoutLocalGroups.name())) {
				String prefix = (TextUtils.isEmpty(selectionArgs[1])) ? " WHERE (" : " AND (";
				builder.append(prefix).append(PagingGroupTable.type.name()).append(" NOT LIKE '");
				builder.append(LocalGroupDetailsActivity.LOCAL).append("' )");
			}
		}

//		  builder.append(" order by ").append(GroupContactItem.name.name());
        builder.append(" order by ").append(GroupContactItem.contact_lastname_or_group_type.name());
	    builder.append(" COLLATE NOCASE ");
		
		
		// _id, id, firstname, lastname, 'user' f_type, photo from users union all select id, groupname, null, 'group',
		// null from fromGroupId;
		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		// SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		// queryBuilder.setTables(currentTable);
		// Cursor cursor = queryBuilder.query(mSmartPagerDB, projection, selection, selectionArgs, null, null,
		// sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryMessageThread(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder builder = new StringBuilder();
		if (TextUtils.isEmpty(selection)) {
			selection = "";
		}
		// builder.append(" SELECT _id, subjectText ");
		// builder.append(" FROM message ");
		// builder.append(" WHERE threadID = " + selection);
		// builder.append(" ORDER BY M.timeSent DESC");
		// builder.append("select x.*, contacts.*, (select count(*) from (select distinct contactId from Recipients where messageId = x.id)) numRecipients, (select imageUrl from ImageUrl where messageId = x.id limit 1) image, (select count(*) from ImageUrl where messageId = x.id) numImages from (select * from message where text like '%"+selection+"%' or subjectText like '%"+selection+"%' group by threadId having MAX(lastUpdate) order by lastUpdate DESC) x, contacts where x.fromSmartPagerId like contacts.smartPagerId");
		builder.append("select ");
		builder.append("a.*,  ");
		builder.append("b.*,  ");
		builder.append("(select count(*) from ImageUrl where messageId = a.id) ").append(QueryMessageThreadTable.numImages.name()).append(",  ");
        builder.append("(SELECT Group_Concat(groupID) FROM ThreadGroupCorrespondence WHERE threadID = a.threadID) ").append(QueryMessageThreadTable.queryGroupID.name()).append(",  ");
		builder.append("(select ImageUrl from ImageUrl where messageId = a.id limit 1) ").append(QueryMessageThreadTable.imageUrl.name()).append(" from Message a  ");
		builder.append("Left outer join Contacts b on a.fromSmartPagerId = b.smartPagerId where a.threadId = '"+selection+"' order by a.id ");
		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	private Cursor queryMessage(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String currentTable) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("SELECT  * FROM Message a ");
		builder.append("LEFT OUTER JOIN ");
		builder.append("Contacts b on a.fromSmartPagerId = b.smartPagerId "); 
		builder.append("WHERE a.id = '" + selectionArgs[0] + "' order by a.id; ");
		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		
//		builder.append("Select a.*, b.* from ");
//		builder.append(Tables.Message.name()).append(" a, ").append(Tables.Contacts.name()).append(" b");
//		builder.append(" where a.").append(MessageTable.fromSmartPagerId.name()).append(" = b.")
//				.append(ContactTable.smartPagerID.name());
//		builder.append(" AND a.").append(MessageTable.id.name()).append(" = '").append(selectionArgs[0]).append("'");
//		Cursor cursor = mSmartPagerDB.rawQuery(builder.toString(), null);
		// Cursor cursor = mSmartPagerDB.query(currentTable, projection, selection, selectionArgs, null, null,
		// sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

}
