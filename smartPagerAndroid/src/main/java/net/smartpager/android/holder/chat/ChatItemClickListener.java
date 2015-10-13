package net.smartpager.android.holder.chat;

import net.smartpager.android.model.PlayInfo;

public interface ChatItemClickListener {
	public void onImageClick(int messageId);
	public void onCallBackClick(String number, int messageId);
	public void onContactClick(String contactId);
	public void onChatItemSwitchPlayMode(PlayInfo playInfo);
	public void onRejectAcceptanceReq(long msgId);
	public void onAcceptAcceptanceReq(long msgId);
	public void onResponseOptionClick(String respText);
    public void onRecipientsClick(int listPosition, boolean isShown);
}
