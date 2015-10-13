package net.smartpager.android.fragment.interfaces;

import net.smartpager.android.model.Recipient;

public interface ContactsListButtonsListener {
	void onPageButtonClick(Recipient recipient);
	void onCallButtonClick(String phoneNumber);
}
