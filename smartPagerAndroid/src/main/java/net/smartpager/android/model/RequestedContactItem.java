package net.smartpager.android.model;

import org.json.JSONObject;

import java.io.Serializable;

public class RequestedContactItem implements Serializable{
	
	private static final long serialVersionUID = 1654654600L;
	private String smartPagerID;
	private String mobileNumber;
	private String title;
	private String firstName;
	private String lastName;
	private long contactID;

	public RequestedContactItem() {}

	public RequestedContactItem(String firstName, String lastName, String mobileNumber,
			String smartPagerID, String title, long contactID) {
		
		this.firstName = firstName;
		this.lastName = lastName;
		this.mobileNumber = mobileNumber;
		this.smartPagerID = smartPagerID;
		this.title = title;
		this.contactID = contactID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String phoneNumber) {
		this.mobileNumber = phoneNumber;
	}

	public String getSmartPagerID() {
		return smartPagerID;
	}

	public void setSmartPagerID(String smartPagerID) {
		this.smartPagerID = smartPagerID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public long getContactID() {
		return contactID;
	}

	public void setContactID(long contactID) {
		this.contactID = contactID;
	}

	public static RequestedContactItem getFromJSONObject(JSONObject item) {
		
    	RequestedContactItem rci = new RequestedContactItem();
    	
    	rci.setSmartPagerID(item.optString("smartPagerID"));
    	rci.setMobileNumber(item.optString("mobileNumber"));
    	rci.setTitle(item.optString("title"));
    	rci.setFirstName(item.optString("firstName"));
    	rci.setLastName(item.optString("lastName"));
    	rci.setContactID(item.optLong("contactID"));
    	
		return rci;
	}
}
