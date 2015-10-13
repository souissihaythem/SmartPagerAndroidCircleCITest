package biz.mobidev.framework.utils.providers;

public class ContactProfile implements IContactProfileContainer{
	private String mDisplayName;
	private String mWorkNumber;
	private String mEmail;
	private String mCompanyName;
	private String mJobTitle;
	
	public ContactProfile(){
		
	}
	
	
	public String getDisplayName() {
		return mDisplayName;
	}
	public void setDisplayName(String displayName) {
		mDisplayName = displayName;
	}
	public String getWorkNumber() {
		return mWorkNumber;
	}
	public void setWorkNumber(String workNumber) {
		mWorkNumber = workNumber;
	}
	public String getEmail() {
		return mEmail;
	}
	public void setEmail(String email) {
		mEmail = email;
	}
	public String getCompanyName() {
		return mCompanyName;
	}
	public void setCompanyName(String companyName) {
		mCompanyName = companyName;
	}
	public String getJobTitle() {
		return mJobTitle;
	}
	public void setJobTitle(String jobTitle) {
		this.mJobTitle = jobTitle;
	}

	public ContactProfile getContactProfile() {

		return this;
	}
}
