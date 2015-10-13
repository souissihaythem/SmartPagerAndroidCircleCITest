package net.smartpager.android.model;

import android.database.Cursor;
import android.os.Parcel;

import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.utils.CursorTextUtils;

import java.io.Serializable;

import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

public class Recipient implements IHolderSource, Serializable /*, Parcelable*/ {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7345858254891064948L;
	private String mSmartPagerId;
	private String mName;
	private String mTitle;
	private String mStatus;
	private String mImageUrl;
	private String mPhone;
	private String mId;
	private String mIdDb;
	private String mGroupType;
	private boolean mSelected;
	private boolean mGroup;
	private boolean mDisabled;
	private int mIndex;
    private String mLastName;

	public Recipient() {

	}

	public Recipient(Parcel in) {
		boolean[] boolArray = new boolean[3];
		mSmartPagerId = in.readString();
		mName = in.readString();
		mTitle = in.readString();
		mStatus = in.readString();
		mImageUrl = in.readString();
		mPhone = in.readString();
		mId = in.readString();
		mIdDb = in.readString();
		mGroupType = in.readString();
		in.readBooleanArray(boolArray);
		mSelected = boolArray[0];
		mGroup = boolArray[1];
		mDisabled = boolArray[2];
		mIndex = in.readInt();
		mLastName = in.readString();
		
	}

	public static Recipient createFromCursor(Cursor cursor) {
		Recipient recipient = new Recipient();
		recipient.setIdDb(cursor.getString(GroupContactItem._id.ordinal()));
		recipient.mId = cursor.getString(GroupContactItem.id.ordinal());
		recipient.mSmartPagerId = cursor.getString(GroupContactItem.contact_smart_pager_id_or_group_id.ordinal());
		recipient.mGroup = cursor.getInt(GroupContactItem.isGroup.ordinal()) == 1 ? true : false;
		String name = "";
		if (recipient.mGroup) {
			name = cursor.getString(GroupContactItem.name.ordinal());
			recipient.mGroupType = cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
			recipient.mStatus = ContactStatus.ONLINE.name();
		} else {
			recipient.mGroupType = "";						
			name = cursor.getString(GroupContactItem.name.ordinal()) + " "
					+ cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
			
			recipient.mStatus = cursor.getString(cursor.getColumnIndex(ContactTable.status.name()));
		}
		recipient.mLastName = cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
		recipient.mName = name;
		recipient.mTitle = cursor.getString(GroupContactItem.contact_title_or_group_empty.ordinal());

		recipient.mImageUrl = cursor.getString(GroupContactItem.contact_url_or_group_empty.ordinal());
		recipient.setPhone(cursor.getString(GroupContactItem.contact_phone_or_group_empty.ordinal()));	
		return recipient;
	}
	
	public static Recipient createFromDepartmentCursor(Cursor cursor) {
		Recipient recipient = new Recipient();
		recipient.setIdDb(cursor.getString(GroupContactItem._id.ordinal()));
		recipient.mId = cursor.getString(GroupContactItem.id.ordinal());
		recipient.mSmartPagerId = cursor.getString(GroupContactItem.contact_smart_pager_id_or_group_id.ordinal());
		recipient.mGroup = cursor.getInt(GroupContactItem.isGroup.ordinal()) == 1 ? true : false;
		String name = "";
		if (recipient.mGroup) {
			name = cursor.getString(GroupContactItem.name.ordinal());
			recipient.mGroupType = cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
			recipient.mStatus = ContactStatus.ONLINE.name();
		} else {
			recipient.mGroupType = "";
			name = cursor.getString(GroupContactItem.name.ordinal()) + " "
					+ cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
			recipient.mStatus = cursor.getString(cursor.getColumnIndex(ContactTable.status.name()));
		}

        recipient.mLastName = cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
		recipient.mName = name;
		recipient.mTitle = CursorTextUtils.getTitleDepartmentCompostion(cursor);

		recipient.mImageUrl = cursor.getString(GroupContactItem.contact_url_or_group_empty.ordinal());
		recipient.setPhone(cursor.getString(GroupContactItem.contact_phone_or_group_empty.ordinal()));	
		return recipient;
	}

	public static Recipient createFromContactCursor(Cursor cursor) {
		Recipient recipient = new Recipient();
		recipient.setIdDb(cursor.getString(ContactTable._id.ordinal()));
		recipient.mId = cursor.getString(ContactTable.id.ordinal());
		recipient.mSmartPagerId = cursor.getString(ContactTable.smartPagerID.ordinal());
		recipient.mGroup = false;
		recipient.mGroupType = "";
		String name = cursor.getString(ContactTable.firstName.ordinal()) + " "
					+ cursor.getString(ContactTable.lastName.ordinal());
        recipient.mLastName = cursor.getString(ContactTable.lastName.ordinal());
		recipient.mStatus = cursor.getString(cursor.getColumnIndex(ContactTable.status.name()));
		recipient.mName = name;
		recipient.mTitle = cursor.getString(ContactTable.title.ordinal());
		recipient.mImageUrl = cursor.getString(ContactTable.photoUrl.ordinal());
		recipient.setPhone(cursor.getString(ContactTable.phoneNumber.ordinal()));	
		return recipient;
	}

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

    public String getLastName ()
    {
        return mLastName;
    }

    public void setLastName (String lastName)
    {
        mLastName = lastName;
    }

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String status) {
		mStatus = status;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setImageUrl(String imageUrl) {
		mImageUrl = imageUrl;
	}

	public boolean isSelected() {
		return mSelected;
	}

	public void setSelected(boolean selected) {
		mSelected = selected;
	}

	public boolean isGroup() {
		return mGroup;
	}

	public void setGroup(boolean group) {
		mGroup = group;
	}

	@Override
	public int getHolderID() {
		return isGroup() ? 1 : 0;
	}

	public String getSmartPagerId() {
		return mSmartPagerId;
	}

	public void setSmartPagerId(String smartPagerId) {
		mSmartPagerId = smartPagerId;
	}

	public String getContactId() { return mId; }

	public void setContactId(String contactId) { mId = contactId; }

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String phone) {
		mPhone = phone;
	}

	public int getIndex() {
		return mIndex;
	}

	public void setIndex(int index) {
		mIndex = index;
	}

	public String getGroupType() {
		return mGroupType;
	}

	public void setGroupType(String groupType) {
		mGroupType = groupType;
	}

	public String getIdDb() {
		return mIdDb;
	}

	public void setIdDb(String idDb) {
		mIdDb = idDb;
	}

	public boolean isDisabled() {
		return mDisabled;
	}

	public void setDisabled(boolean disabled) {
		mDisabled = disabled;
	}
	
	public String getUniqueKey(){
		return mSmartPagerId + mName + mIdDb + mId;
	}

//	@Override
//	public int describeContents() {
//		return 0;
//	}
//
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(mSmartPagerId);
//		dest.writeString(mName);
//		dest.writeString(mTitle);
//		dest.writeString(mStatus);
//		dest.writeString(mImageUrl);
//		dest.writeString(mPhone);
//		dest.writeString(mId);
//		dest.writeString(mIdDb);
//		dest.writeString(mGroupType);
//		dest.writeBooleanArray( new boolean[] {mSelected, mGroup, mDisabled});
//		dest.writeInt(mIndex);
//	}
//
//	public static final Parcelable.Creator<Recipient> CREATOR = new Parcelable.Creator<Recipient>() {
//		public Recipient createFromParcel(Parcel in) {
//			return new Recipient(in);
//		}
//
//		public Recipient[] newArray(int size) {
//			return new Recipient[size];
//		}
//	};

}
