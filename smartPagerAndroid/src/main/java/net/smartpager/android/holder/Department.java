package net.smartpager.android.holder;

import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

public class Department implements IHolderSource {

	private String mStatus;
	private String mName;
	
	public static Department createFromStringArray(String[] input) {
		Department department = new  Department();
		department.setStatus(input[0]);
		department.setName(input[1]);
		return department;
	}
	
	public static Department createFromString(String departmentName) {
		Department department = new  Department();
		department.setName(departmentName);
		//department.setStatus("status");
		return department;
	}
	@Override
	public int getHolderID() {
		return 0;
	}

	public String getStatus() {
		return mStatus;
	}

	public void setStatus(String statuse) {
		mStatus = statuse;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

}
