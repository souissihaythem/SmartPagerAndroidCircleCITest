package net.smartpager.android.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import biz.mobidev.framework.utils.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public enum Tables {
        Contacts, Message, Chats, PagingGroups, ContactGroup, Recipients, ImageUrl, MessageThreads, RecipientFullInfo, RecipientThread, QuickResponse, SimpleMessage, LocalGroupCorrespondence, LocalGroupContacts, ThreadGroupCorrespondence
    }

    public enum ThreadGroupCorrespondence {
        _id, threadID, groupID
    }

    public enum RecipientTable {
        _id, messageId, contactId, readTime, deliveredTime, status, repliedTime, smartPagerID, lastUpdate
    }

    public enum RecipientFullInfoTable {
        _id, messageId, contactId, readTime, deliveredTime, status, repliedTime, smartPagerID, lastUpdate
    }

    public enum PagingGroupTable {
        _id, id, name, type

    }

    public enum ContactTable {
        _id, id, firstName, lastName, phoneNumber, status, smartPagerID, title, departments, accountName, photoUrl, markedAsDeleted // not
        // used
    }

    public enum MessageTable {
        _id, id, threadID, status, type, text, fromGroupId, subjectColor, bodyColor, fromNumber, archived, message_order, fromSmartPagerId, subjectText, callbackNumber, fromColor, timeSent, toGroupId, subjectIconURL, recordUrl, readTime, repliedTime, deliveredTime, messageType, lastUpdate, messageStatus, fromContactId, requestAcceptance, critical, responseTemplate, requestAcceptanceShow, replyAllowed, pdfUrls
    }

    public enum ImageUrlTable {
        _id, messageId, ImageUrl, imageIndex
    }

    public enum QuickResponseTable {
        _id, messageText
    }

    public enum LocalGroupCorrespondenceTable {
        _id, localGroupId, contactId
    }

    private static final String CREATE_TABLE_CONTACTS;
    private static final String CREATE_TABLE_MESSAGE;
    private static final String CREATE_TABLE_PAGINGGROUP;
    private static final String CREATE_TABLE_RECIPIENTS;
    private static final String CREATE_TABLE_IMAGE_URL;
    private static final String CREATE_TABLE_QUICK_RESPONSE_URL;
    private static final String CREATE_TABLE_LOCAL_GROUP_CORRESPONDENCE_URL;
    private static final String CREATE_TABLE_THREAD_GROUP_CORRESPONDENCE;

    private static final String QR_CREATE_TABLE = "create table ";
    private static final String QR_CREATE_TABLE_IF_NOT_EXISTS = "create table if not exists ";
    private static final String QR_DEFAULT = " DEFAULT ";
    private static final String QR_PRIMARY_KEY = " integer primary key autoincrement ";
    private static final String QR_TEXT = " TEXT ";
    private static final String QR_INTEGER = " INTEGER ";
    // private static final String QR_FLOAT = " FLOAT ";
    private static final String QR_UNIQUE = " UNIQUE ";
    // private static final String QR_INDEX = " INDEX ";
    // private static final String QR_FOREIGN_KEY;

    private static final String CREATE_INDEXES_BATCH;

    static {
        CREATE_TABLE_CONTACTS = createTableContacts();
        CREATE_TABLE_MESSAGE = createTableMessage();
        CREATE_TABLE_PAGINGGROUP = createTablePagingGroup();
        CREATE_TABLE_RECIPIENTS = createTableRecipients();
        CREATE_TABLE_IMAGE_URL = createTableImageUrl();
        CREATE_TABLE_QUICK_RESPONSE_URL = createTableQuickResponse();
        CREATE_TABLE_LOCAL_GROUP_CORRESPONDENCE_URL = createTableLocalGroupCorrespondence();
        CREATE_TABLE_THREAD_GROUP_CORRESPONDENCE = createTableThreadGroupCorrespondence();

        CREATE_INDEXES_BATCH = createIndexesBatch();
    }

    private static String createTableThreadGroupCorrespondence ()
    {
        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE_IF_NOT_EXISTS);
        builder.append(Tables.ThreadGroupCorrespondence.name()).append(" (");
        builder.append(ThreadGroupCorrespondence._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(ThreadGroupCorrespondence.threadID.name()).append(QR_INTEGER).append(", ");
        builder.append(ThreadGroupCorrespondence.groupID.name()).append(QR_INTEGER).append(", ");
        builder.append(QR_UNIQUE).append("( ").append(ThreadGroupCorrespondence.threadID.name()).append(", ");
        builder.append(ThreadGroupCorrespondence.groupID.name()).append(") );");
        builder.append(");");
        return builder.toString();
    }

    private static String createTablePagingGroup() {

        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE);
        builder.append(Tables.PagingGroups.name()).append(" (");
        builder.append(PagingGroupTable._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(PagingGroupTable.id.name()).append(QR_INTEGER).append(", ");
        builder.append(PagingGroupTable.name.name()).append(QR_TEXT).append(", ");
        builder.append(PagingGroupTable.type.name()).append(QR_TEXT).append(", ");
        builder.append(QR_UNIQUE).append("( ").append(PagingGroupTable.id.name()).append(") );");

        return builder.toString();
    }

    private static String createTableLocalGroupCorrespondence() {
        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE);
        builder.append(Tables.LocalGroupCorrespondence.name()).append(" (");
        builder.append(LocalGroupCorrespondenceTable._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(LocalGroupCorrespondenceTable.localGroupId.name()).append(QR_INTEGER).append(", ");
        builder.append(LocalGroupCorrespondenceTable.contactId.name()).append(QR_INTEGER);
        builder.append(");");
        return builder.toString();
    }

    private static String createTableImageUrl() {
        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE);
        builder.append(Tables.ImageUrl.name()).append(" (");
        builder.append(ImageUrlTable._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(ImageUrlTable.messageId.name()).append(QR_INTEGER).append(", ");
        builder.append(ImageUrlTable.ImageUrl.name()).append(QR_TEXT).append(", ");
        builder.append(ImageUrlTable.imageIndex.name()).append(QR_INTEGER).append(",");
        builder.append(QR_UNIQUE).append("( ").append(ImageUrlTable.messageId.name()).append(", ");
        builder.append(ImageUrlTable.imageIndex.name()).append(") );");
        return builder.toString();
    }

    private static String createTableRecipients() {
        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE);
        builder.append(Tables.Recipients.name()).append(" (");
        builder.append(RecipientTable._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(RecipientTable.messageId.name()).append(QR_INTEGER).append(", ");
        builder.append(RecipientTable.contactId.name()).append(QR_INTEGER).append(", ");
        builder.append(RecipientTable.readTime.name()).append(QR_INTEGER).append(", ");
        builder.append(RecipientTable.deliveredTime.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.status.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.repliedTime.name()).append(QR_INTEGER).append(", ");
        builder.append(ContactTable.smartPagerID.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.lastUpdate.name()).append(QR_INTEGER).append(", ");
        builder.append(QR_UNIQUE).append("( ").append(RecipientTable.contactId.name()).append(", ");
        builder.append(RecipientTable.messageId.name()).append(") );");
        return builder.toString();
    }

    private static String createTableMessage() {

        Log.e("DatabaseHelper", "createTableMessage");

        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE);
        builder.append(Tables.Message.name()).append(" (");

        builder.append(MessageTable._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(MessageTable.id.name()).append(QR_INTEGER).append(QR_UNIQUE).append(", ");
        builder.append(MessageTable.threadID.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.status.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.type.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.text.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.fromGroupId.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.subjectColor.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.bodyColor.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.fromNumber.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.archived.name()).append(QR_INTEGER).append(QR_DEFAULT).append(0).append(", ");
        builder.append(MessageTable.message_order.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.fromSmartPagerId.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.subjectText.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.callbackNumber.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.fromColor.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.timeSent.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.toGroupId.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.subjectIconURL.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.recordUrl.name()).append(QR_TEXT).append(", ");
        builder.append(MessageTable.readTime.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.repliedTime.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.deliveredTime.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.messageType.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.lastUpdate.name()).append(QR_INTEGER).append(", ");
        builder.append(MessageTable.messageStatus.name()).append(QR_TEXT).append(",");
        builder.append(MessageTable.fromContactId.name()).append(QR_INTEGER).append(",");
        builder.append(MessageTable.requestAcceptance.name()).append(QR_INTEGER).append(",");
        builder.append(MessageTable.critical.name()).append(QR_INTEGER).append(QR_DEFAULT).append(0).append(",");
        builder.append(MessageTable.responseTemplate.name()).append(QR_TEXT).append(QR_DEFAULT).append("''");
        builder.append(");");
        return builder.toString();
    }

    private static String createTableContacts() {

        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE);
        builder.append(Tables.Contacts.name()).append(" (");
        builder.append(ContactTable._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(ContactTable.id.name()).append(QR_INTEGER).append(QR_UNIQUE).append(", ");
        builder.append(ContactTable.firstName.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.lastName.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.phoneNumber.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.status.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.smartPagerID.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.title.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.departments.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.accountName.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.photoUrl.name()).append(QR_TEXT).append(", ");
        builder.append(ContactTable.markedAsDeleted.name()).append(QR_INTEGER).append(QR_DEFAULT).append(0);
        builder.append(" );");
        return builder.toString();
    }

    private static String createTableQuickResponse() {
        StringBuilder builder = new StringBuilder(QR_CREATE_TABLE);
        builder.append(Tables.QuickResponse.name()).append(" (");
        builder.append(QuickResponseTable._id.name()).append(QR_PRIMARY_KEY).append(", ");
        builder.append(QuickResponseTable.messageText.name()).append(QR_TEXT).append(QR_UNIQUE).append(");");
        return builder.toString();
    }

    public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        // Drop table to get rid of misplaced replyAllowed column.
        if (database.getVersion() == 3) {
            database.execSQL("DROP TABLE IF EXISTS " + Tables.Message.name() +";");
        }

        database.execSQL(CREATE_TABLE_CONTACTS);
        database.execSQL(CREATE_TABLE_MESSAGE);
        database.execSQL(CREATE_TABLE_PAGINGGROUP);
        database.execSQL(CREATE_TABLE_RECIPIENTS);
        database.execSQL(CREATE_TABLE_IMAGE_URL);
        database.execSQL(CREATE_TABLE_QUICK_RESPONSE_URL);
        database.execSQL(CREATE_TABLE_LOCAL_GROUP_CORRESPONDENCE_URL);
        database.execSQL(CREATE_TABLE_THREAD_GROUP_CORRESPONDENCE);
        database.execSQL(CREATE_INDEXES_BATCH);

        if (database.getVersion() == 0) {
            onUpgrade(database, database.getVersion(), 1);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.POINT();
        switch (newVersion) {
            case 1:
                Log.e("DatabaseHelper", "case 1");
                final String addRequestAcceptanceShowToMessageTable = "ALTER TABLE " + Tables.Message.name() + " ADD COLUMN "
                        + MessageTable.requestAcceptanceShow.name() + " INTEGER NOT NULL DEFAULT 0";
                database.execSQL(addRequestAcceptanceShowToMessageTable);
            case 2:
                Log.e("DatabaseHelper", "case 2");
                database.execSQL(CREATE_TABLE_THREAD_GROUP_CORRESPONDENCE);
                final String createUniqueIndexInPagingGroupTable = "create unique index " + Indexes.PagingGroupTable_GroupID_Idx.name() +" on " + Tables.PagingGroups.name() + "(" + PagingGroupTable.id.name() + ");";
                database.execSQL(createUniqueIndexInPagingGroupTable);
            case 3:
                Log.e("DatabaseHelper", "case 3");
                final String addReplyAllowedToMessageTable = "ALTER TABLE " + Tables.Message.name() + " ADD COLUMN "
                        + MessageTable.replyAllowed.name() + " INTEGER NOT NULL DEFAULT 0";
                database.execSQL(addReplyAllowedToMessageTable);
            case 4:
                Log.e("DatabaseHelper", "case 4");
            	final String addPdfUrlsToMessageTable = "ALTER TABLE " + Tables.Message.name() + " ADD COLUMN "
            			+ MessageTable.pdfUrls.name() + " TEXT NOT NULL DEFAULT ''";
            	database.execSQL(addPdfUrlsToMessageTable);
            	break;
            default:
                System.out.println("onUpgrade() with unknown oldVersion: " + oldVersion);
                break;
        }
        database.setVersion(newVersion);
    }

    private static String createIndexStatement(String idxName, String tblName, String fldName) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE INDEX ");
        builder.append(idxName);
        builder.append(" ON ");
        builder.append(tblName).append(" (").append(fldName).append(")");
        builder.append(";");
        return builder.toString();
    }

    public enum Indexes {
        RecipientTable_MessageId_Idx, RecipientTable_ContactId_Idx, RecipientTable_SmartPagerID_Idx, PagingGroupTable_Id_Idx, MessageTable_Id_Idx, MessageTable_ThreadId_Idx, MessageTable_Status_Idx, MessageTable_FromSmartPagerId_Idx, MessageTable_FromContactId_Idx, MessageTable_FromGroupId_Idx, ContactTable_Id_Idx, ContactTable_SmartPagerID_Idx, ImageUrlTable_MessageId_Idx, LocalGroupCorrespondenceTable_LocalGroupId_Idx, LocalGroupCorrespondenceTable_ContactId_Idx, PagingGroupTable_GroupID_Idx
    }

    private static String createIndexesBatch() {

        StringBuilder builder = new StringBuilder();

        // ======== RecipientTable ========
        builder.append(createIndexStatement(Indexes.RecipientTable_MessageId_Idx.name(), Tables.Recipients.name(),
                RecipientTable.messageId.name()));
        builder.append(createIndexStatement(Indexes.RecipientTable_ContactId_Idx.name(), Tables.Recipients.name(),
                RecipientTable.contactId.name()));
        builder.append(createIndexStatement(Indexes.RecipientTable_SmartPagerID_Idx.name(), Tables.Recipients.name(),
                RecipientTable.smartPagerID.name()));

        // ======== PagingGroupTableTable =
        builder.append(createIndexStatement(Indexes.PagingGroupTable_Id_Idx.name(), Tables.PagingGroups.name(),
                PagingGroupTable.id.name()));

        // ======== MessageTable ==========
        builder.append(createIndexStatement(Indexes.MessageTable_Id_Idx.name(), Tables.Message.name(),
                MessageTable.id.name()));
        builder.append(createIndexStatement(Indexes.MessageTable_ThreadId_Idx.name(), Tables.Message.name(),
                MessageTable.threadID.name()));
        builder.append(createIndexStatement(Indexes.MessageTable_Status_Idx.name(), Tables.Message.name(),
                MessageTable.status.name()));
        builder.append(createIndexStatement(Indexes.MessageTable_FromSmartPagerId_Idx.name(), Tables.Message.name(),
                MessageTable.fromSmartPagerId.name()));
        builder.append(createIndexStatement(Indexes.MessageTable_FromContactId_Idx.name(), Tables.Message.name(),
                MessageTable.fromContactId.name()));
        builder.append(createIndexStatement(Indexes.MessageTable_FromGroupId_Idx.name(), Tables.Message.name(),
                MessageTable.fromGroupId.name()));

        // ======== ContactTable ==========
        builder.append(createIndexStatement(Indexes.ContactTable_Id_Idx.name(), Tables.Contacts.name(),
                ContactTable.id.name()));
        builder.append(createIndexStatement(Indexes.ContactTable_SmartPagerID_Idx.name(), Tables.Contacts.name(),
                ContactTable.smartPagerID.name()));

        // ======== ImageUrlTableTable ====
        builder.append(createIndexStatement(Indexes.ImageUrlTable_MessageId_Idx.name(), Tables.ImageUrl.name(),
                ImageUrlTable.messageId.name()));

        // ======== LocalGroupCorrespondenceTable
        builder.append(createIndexStatement(Indexes.LocalGroupCorrespondenceTable_LocalGroupId_Idx.name(),
                Tables.LocalGroupCorrespondence.name(), LocalGroupCorrespondenceTable.localGroupId.name()));
        builder.append(createIndexStatement(Indexes.LocalGroupCorrespondenceTable_ContactId_Idx.name(),
                Tables.LocalGroupCorrespondence.name(), LocalGroupCorrespondenceTable.contactId.name()));

        return builder.toString();
    }
}
