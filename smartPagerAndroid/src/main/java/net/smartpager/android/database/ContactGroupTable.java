package net.smartpager.android.database;

import android.content.Context;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.PagingGroupTable;
import net.smartpager.android.model.Recipient;

import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.utils.Log;

public class ContactGroupTable extends CursorTable {

	public enum GroupContactItem{
		_id,
		id,
		name,
		contact_lastname_or_group_type,
		contact_title_or_group_empty,
		contact_url_or_group_empty,
		contact_status_or_group_empty,
		contact_phone_or_group_empty,
		contact_smart_pager_id_or_group_id,		
		isGroup,
		contact_depart_or_group_empty
	}
	
	public enum ItemType{
		isContact,
		isGroup
	}
	
	private String mOnlyContacts;
	private String mWithoutLocalGroups;
	
	public ContactGroupTable(){
		this(false, false);
	}

	public ContactGroupTable(boolean onlyContacts){
		this(onlyContacts, false);
	}
	
	public ContactGroupTable(boolean onlyContacts, boolean withoutLocalGroups){
		mOnlyContacts = (onlyContacts) ? BundleKey.onlyContacts.name() : "";
		mWithoutLocalGroups = (withoutLocalGroups) ? BundleKey.withoutLocalGroups.name() : "";
	}
	
	@Override
	public Uri getUri() {
		return SmartPagerContentProvider.CONTENT_CONTACT_GROUP_URI;
	}

	@Override
	public CursorLoader getLoader() {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		CursorLoader cursorLoader = new CursorLoader(context, getUri(), null, null,
				new String[]{"", "", mOnlyContacts, mWithoutLocalGroups}, null);
		return cursorLoader;
	}

	@Override
	public CursorLoader getFilterLoader(String... filterparams) {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		StringBuilder likeContactBuilder = new StringBuilder(" where (");
		String value = DatabaseUtils.sqlEscapeString("%"+filterparams[0]+"%");
		likeContactBuilder.append(ContactTable.firstName.name()).append(" like ").append(value).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.lastName.name()).append(" like ").append(value).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.departments.name()).append(" like ").append(value).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.accountName.name()).append(" like ").append(value).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.title.name()).append(" like ").append(value).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.phoneNumber.name()).append(" like ").append(value).append("").append(") ");
		
		StringBuilder likeGroupBuilder = new StringBuilder(" where (");
		likeGroupBuilder.append(PagingGroupTable.name.name()).append(" like ").append(value).append("").append(") ");
		
		CursorLoader cursorLoader = new CursorLoader(context, getUri(), null, null, 
				new String[]{likeContactBuilder.toString(), likeGroupBuilder.toString(), mOnlyContacts, mWithoutLocalGroups}, null);
		return cursorLoader;
	}

    public CursorLoader getFilterLoader(String likeArgument, List<IHolderSource> recipients) {
        return getFilterLoader(likeArgument, recipients, null);
    }

	public CursorLoader getFilterLoader(String likeArgument, List<IHolderSource> recipients, ContactTable filterColumn) {
		likeArgument = DatabaseUtils.sqlEscapeString("%"+likeArgument+"%");
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		StringBuilder likeContactBuilder = new StringBuilder(" where (");
		StringBuilder notLikeContactBuilder = new StringBuilder();
		
		StringBuilder likeGroupBuilder = new StringBuilder(" where (");
		StringBuilder notLikeGroupBuilder = new StringBuilder();

		
		likeContactBuilder.append(ContactTable.firstName.name()).append(" like ").append(likeArgument).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.lastName.name()).append(" like ").append(likeArgument).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.departments.name()).append(" like ").append(likeArgument).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.accountName.name()).append(" like ").append(likeArgument).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.title.name()).append(" like ").append(likeArgument).append("").append(" OR ");
		likeContactBuilder.append(ContactTable.phoneNumber.name()).append(" like ").append(likeArgument).append("").append(") ");

		likeGroupBuilder.append(PagingGroupTable.name.name()).append(" like ").append(likeArgument).append("").append(") ");
		
        StringBuilder filterValue = new StringBuilder();
        boolean notInIsAdded = false;
        boolean isFirstArgument = true;
		for (IHolderSource item : recipients) {
			Recipient recipient = (Recipient) item;
            if(filterColumn != null) {
                filterValue.setLength(0);
                switch (filterColumn) {
                    case lastName:
                        filterValue.append("'").append(recipient.getId()).append("'");
                        break;
                    default:
                        break;
                }
            }
            if (recipient.isGroup()) {
                if (TextUtils.isEmpty(notLikeGroupBuilder.toString())) {
                    notLikeGroupBuilder.append(" AND ( ").append(PagingGroupTable.name.name()).append(" NOT IN (");
                } else {
                    notLikeGroupBuilder.append(", ");
                }
                notLikeGroupBuilder.append("'").append(recipient.getName()).append("'");
            } else {
                if (filterColumn != null) {
                    if(!notInIsAdded) {
                        notLikeContactBuilder.append(" AND ( ").append(filterColumn.name()).append(" NOT IN (");
                        notInIsAdded = true;
                    }
                    if(!isFirstArgument)
                        notLikeContactBuilder.append(",");
                    isFirstArgument = false;
                    notLikeContactBuilder.append(recipient.getId());
                } else {
                    if (TextUtils.isEmpty(notLikeContactBuilder.toString())) {
                        notLikeContactBuilder.append(" AND ( ").append(ContactTable.phoneNumber.name()).append(" NOT IN (");
                    } else {
                        notLikeContactBuilder.append(", ");
                    }
                    notLikeContactBuilder.append(recipient.getPhone());
                }
            }
        }
		if(!TextUtils.isEmpty(notLikeGroupBuilder.toString())){
			notLikeGroupBuilder.append("))");
		}
		if(!TextUtils.isEmpty(notLikeContactBuilder.toString())){
			notLikeContactBuilder.append("))");
		}
		likeContactBuilder.append(notLikeContactBuilder);
		likeGroupBuilder.append(notLikeGroupBuilder);
//		builder.append("firstName not in (").append(notLikeArgument).append(") OR lastName like ")
//		.append(notLikeArgument).append(" OR phoneNumber like ").append(notLikeArgument).append("");
		CursorLoader cursorLoader = new CursorLoader(context, getUri(), null, null, 
				new String[]{likeContactBuilder.toString(), likeGroupBuilder.toString(), mOnlyContacts, mWithoutLocalGroups}, null);
		return cursorLoader;
	}
}
