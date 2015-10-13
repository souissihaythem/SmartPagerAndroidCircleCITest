package net.smartpager.android.model;

import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

/**
 * Model class for alphabetical bars in contact list 
 * @author Roman
 */
public class AlphabeticalCaption implements IHolderSource {

	private String mName;
		
	public AlphabeticalCaption(){
		mName = "";
	}

	public AlphabeticalCaption(String name){
		mName = name;
	}
	
	@Override
	public int getHolderID() {
		return 2;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

}
