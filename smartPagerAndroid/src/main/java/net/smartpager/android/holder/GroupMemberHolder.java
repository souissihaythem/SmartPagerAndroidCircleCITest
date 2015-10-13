package net.smartpager.android.holder;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class GroupMemberHolder implements IHolder {
	
	TextView mStatus;
	TextView mName;
	
	@Override
	public IHolder createHolder(View view) {
		mStatus = (TextView)view.findViewById(R.id.department_state_textView);
		mName = (TextView)view.findViewById(R.id.department_name_textView);
		return this;
	}

	@Override
	public ViewGroup getInflateView(Context context) {
		
		return new RelativeLayout(context);
	}

	@Override
	public int getLayoutResorsID() {
		return R.layout.item_department;
	}

	@Override
	public void preSetData(Bundle arg0) {

	}

	@Override
	public void setData(Object data) {	
		Department department = (Department) data;					
		mStatus.setText(department.getStatus());
		mName.setText(department.getName());
		
		int colorGreen = mStatus.getContext().getResources().getColor(R.color.green_button);
		int colorGrey = mStatus.getContext().getResources().getColor(R.color.gray_lite_background);
		int color = colorGrey;
		if(!TextUtils.isEmpty(department.getStatus())){
			color = colorGreen; 			
		}
		mStatus.setTextColor(color);
		mName.setTextColor(color);
	}

	@Override
	public void setListeners(Object... arg0) {

	}

}
