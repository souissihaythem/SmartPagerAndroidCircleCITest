package net.smartpager.android.model;

import java.io.Serializable;

public class Message implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3281531207549482467L;
	private String mId;
	private boolean mAcceptanceRequested;

	public Message(String id, boolean acceptanceRequested) {
		mId = id;
		mAcceptanceRequested = acceptanceRequested;
	}
	
	public String getId() {
		return mId;
	}
	
	public boolean isAcceptanceRequested() {
		return mAcceptanceRequested;
	}
}
