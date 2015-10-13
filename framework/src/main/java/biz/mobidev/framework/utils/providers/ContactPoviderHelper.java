package biz.mobidev.framework.utils.providers;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import biz.mobidev.framework.validate.Validator;

public class ContactPoviderHelper {

	static public boolean isPhoneNumberExistsInContactList(ContentResolver contentResolver, String phoneNumber) {
		boolean result = false;
		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
		Cursor cur = contentResolver.query(lookupUri, null, null, null, null);
		try {
			if (cur.moveToFirst()) {
				result = true;
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		return result;
	}

	static public boolean isEmailExistsInContactList(ContentResolver contentResolver, String email) {
		boolean result = false;
		Cursor cur = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.DATA + " = ?", new String[] { email }, null);
		try {
			if (cur.moveToFirst()) {
				result = true;
			}
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		return result;
	}

	
	static public void addContact(ContentResolver contentResolver, ArrayList <ContentProviderOperation>operationList) throws Exception{
		try
		{
			contentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
		}catch (Exception e) {
			throw e;
		}
	}
	
	static public void addContact(ContentResolver contentResolver, IContactProfileContainer contactProfileContainer) throws Exception{
		try
		{
			addContact(contentResolver, (ArrayList<ContentProviderOperation>) getAddedContactOperation(contactProfileContainer));
		}catch (Exception e) {
			throw e;
		}
	}
	
	static public List<ContentProviderOperation> getAddedContactOperation(IContactProfileContainer contactProfileContainer) {
		ContactProfile contactProfile = contactProfileContainer.getContactProfile();
		List<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		if (Validator.notNull(contactProfile)) {
			operationList.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
					.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
					.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
			operationList =addNameToOperation(operationList, contactProfile);
			operationList =addWorkNumber(operationList, contactProfile);
			operationList = addEmailNumber(operationList, contactProfile);
			operationList = addCompany(operationList, contactProfile);
		}
		return operationList;
	}

	private static List<ContentProviderOperation>  addCompany(List<ContentProviderOperation> operationList, ContactProfile contactProfile) {
		if (Validator.notEmtyOrNull(contactProfile.getCompanyName())
				&& Validator.notEmtyOrNull(contactProfile.getJobTitle())) {
			operationList.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contactProfile.getCompanyName())
					.withValue(ContactsContract.CommonDataKinds.Organization.TYPE,
							ContactsContract.CommonDataKinds.Organization.TYPE_WORK)
					.withValue(ContactsContract.CommonDataKinds.Organization.TITLE, contactProfile.getJobTitle())
					.withValue(ContactsContract.CommonDataKinds.Organization.TYPE,
							ContactsContract.CommonDataKinds.Organization.TYPE_WORK).build());
		}
		return operationList;
	}

	private static List<ContentProviderOperation> addEmailNumber(List<ContentProviderOperation> operationList,
			ContactProfile contactProfile) {
		if (Validator.notEmtyOrNull(contactProfile.getEmail())) {
			operationList
					.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.Email.DATA, contactProfile.getEmail())
							.withValue(ContactsContract.CommonDataKinds.Email.TYPE,
									ContactsContract.CommonDataKinds.Email.TYPE_WORK).build());
		}
		return operationList;
	}

	private static List<ContentProviderOperation> addWorkNumber(List<ContentProviderOperation> operationList,
			ContactProfile contactProfile) {
		if (Validator.notEmtyOrNull(contactProfile.getWorkNumber())) {
			operationList
					.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
							.withValue(ContactsContract.Data.MIMETYPE,
									ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
							.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactProfile.getWorkNumber())
							.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
									ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
		}
		return operationList;
	}

	private static List<ContentProviderOperation> addNameToOperation(List<ContentProviderOperation> operationList,
			ContactProfile contactProfile) {
		if (Validator.notEmtyOrNull(contactProfile.getDisplayName())) {
			operationList.add(ContentProviderOperation
					.newInsert(ContactsContract.Data.CONTENT_URI)
					.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
					.withValue(ContactsContract.Data.MIMETYPE,
							ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
					.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
							contactProfile.getDisplayName()).build());
		}
		return operationList;
	}
}
