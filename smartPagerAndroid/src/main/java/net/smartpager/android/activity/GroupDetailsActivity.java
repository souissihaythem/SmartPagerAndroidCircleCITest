package net.smartpager.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.rey.material.widget.Button;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.holder.Department;
import net.smartpager.android.holder.GroupMemberHolder;
import net.smartpager.android.model.Recipient;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetPagingGroupDetailsResponse;

import java.util.ArrayList;
import java.util.List;

import biz.mobidev.framework.adapters.holderadapter.BaseHolderAdapter;
import biz.mobidev.framework.adapters.holderadapter.IHolderSource;
import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class GroupDetailsActivity extends BaseActivity {

	@ViewInjectAnatation(viewID = R.id.group_details_name_textView)
	private TextView mTxtName;
	@ViewInjectAnatation(viewID = R.id.group_details_description_textView)
	private TextView mTxtDescription;
	@ViewInjectAnatation(viewID = R.id.group_details_type_textView)
	private TextView mTxtType;
	@ViewInjectAnatation(viewID = R.id.group_details_members_listView)
	private ListView mLstMembersList;
	@ViewInjectAnatation(viewID = R.id.group_details_page_button)
	private Button mBtnPageButton;
	@ViewInjectAnatation(viewID = R.id.group_details_imageView)
	private ImageView mGroupImage;

	private String mSmartPagerId;
	private String mName;
	private String mGroupType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_details);
		Injector.doInjection(this);
		
		// set type and name
		GetPagingGroupDetailsResponse responce = (GetPagingGroupDetailsResponse) getIntent().getSerializableExtra(
				WebAction.getPagingGroupDetails.name());
		mSmartPagerId = responce.getGroupId();
		mName = responce.getGroupName();
		mGroupType = responce.getGroupType();
		mTxtName.setText(mName);
		mTxtType.setText(mGroupType);
		mTxtDescription.setVisibility(View.GONE);

		if (responce.getGroupType() != null && responce.getGroupType().equalsIgnoreCase("SCHEDULE")) {
			mGroupImage.setImageResource(R.drawable.contacts_ic_schedule_group_static);
		} else {
			mGroupImage.setImageResource(R.drawable.contacts_ic_group_static);
		}
		
		// set members list
		List<String[]> users = responce.getUsers();
		List<IHolderSource> holderSources = new ArrayList<IHolderSource>();
		for (String[] data : users) {
			holderSources.add(Department.createFromStringArray(data));
		}
		BaseHolderAdapter membersAdapter = new BaseHolderAdapter(this, holderSources, GroupMemberHolder.class);
		mLstMembersList.setAdapter(membersAdapter);
		mLstMembersList.setEnabled(false);	// to avoid click on the items
	}

	// BUTTON LISTENERS
	@ClickListenerInjectAnatation(viewID = R.id.group_details_page_button)
	public void onPageToGroupClick(View view) {
		
		Recipient recipient = new Recipient();
		recipient.setName(mName);
		recipient.setGroupType(mGroupType);
		recipient.setSmartPagerId(mSmartPagerId);
		recipient.setGroup(true);
		recipient.setStatus(ContactStatus.ONLINE.name());

		Intent intent = new Intent(this, NewMessageActivity.class);
		intent.putExtra(BundleKey.recipient.name(), recipient);
		startActivity(intent);
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {

	}
	
	@Override
	protected boolean isUpNavigationEnabled() {
		return true;
	}

}
