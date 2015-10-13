package net.smartpager.android.model;

import net.smartpager.android.utils.DateTimeUtils;

import java.io.Serializable;

public class PlayInfo implements Serializable {

	private static final long serialVersionUID = -3207029230264611952L;

	public enum PlayStatus {
		play, stop, pending, update, error, forvard_stop
	}

	private String mUrl;
	private int mMessageIdForPlay;
	private PlayStatus mPlayerComandStatus;
	private PlayStatus mPlayerResponceStatus;
	private long mProgress;
	private long mDuration;
	private String mMessage;

	public static PlayInfo createPlayerInfor(PlayStatus playerComandStatus) {
		return createPlayerInfor(playerComandStatus, -1);
	}
	
	public static PlayInfo createPlayerInfor(PlayStatus playerComandStatus, int messageId) {
		PlayInfo playInfo = new PlayInfo();
		playInfo.setPlayerComandStatus(playerComandStatus);
		playInfo.setPlayerResponceStatus(PlayStatus.stop);
		playInfo.setDuration(0);
		playInfo.setProgress(0);
		playInfo.setMessage("");
		playInfo.setUrl("");
		playInfo.setMessageIdForPlay(messageId);
		return playInfo;
	}

	private PlayInfo() {

	}

	public int getMessageIdForPlay() {
		return mMessageIdForPlay;
	}

	public void setMessageIdForPlay(int messageIdForPlay) {
		mMessageIdForPlay = messageIdForPlay;
	}

	public PlayStatus getPlayerComandStatus() {
		return mPlayerComandStatus;
	}

	public void setPlayerComandStatus(PlayStatus playStatus) {
		mPlayerComandStatus = playStatus;
	}

	public String getUrl() {
		return mUrl;
	}

	public void setUrl(String url) {
		mUrl = url;
	}

	public PlayStatus getPlayerResponceStatus() {
		return mPlayerResponceStatus;
	}

	public void setPlayerResponceStatus(PlayStatus playerResponceStatus) {
		mPlayerResponceStatus = playerResponceStatus;
	}

	public long getProgress() {
		return mProgress;
	}

	public void setProgress(long progress) {
		mProgress = progress;
	}

	public long getDuration() {
		return mDuration;
	}

	public void setDuration(long duration) {
		mDuration = duration;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		mMessage = message;
	}

	public String getProgressString() {
		String duration = "00:00";
		if (mProgress > 0) {
			duration = DateTimeUtils.progressToString(mProgress);
		}
		return duration;
	}

}
