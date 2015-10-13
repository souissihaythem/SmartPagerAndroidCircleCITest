package net.smartpager.android.holder.chat;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.QueryMessageThreadTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.model.PlayInfo;
import net.smartpager.android.model.PlayInfo.PlayStatus;
import net.smartpager.android.utils.TelephoneUtils;

import biz.mobidev.framework.adapters.holderadapter.IHolderCursor;
import biz.mobidev.framework.utils.Log;
import biz.mobidev.library.lazybitmap.ImageLoader;

public class ChatItemHolder implements IHolderCursor, OnClickListener {

	private View mPlayerView;
	private TextView mProgressTextView;
	private ImageButton mPlayButton;
	private SeekBar mProgressSeekBar;
	private PlayInfo mPlayInfo;
	protected int mMessageId = -2;
	private String mRecordUrl = "";
	protected ChatItemClickListener mChatItemClickListener;
	private Button mCallButton;
	private FrameLayout mImageAttachLayout;

    protected boolean m_bWasRecipientsShown = false;
    protected int m_nCurrListPosition = -1;
	private String callbackNumber;

	@Override
	public ViewGroup inflateLayout(LayoutInflater inflater, Context context) {
		return null;
	}

	@Override
	public IHolderCursor createHolder(View view) {
		mImageAttachLayout = (FrameLayout) view.findViewById(R.id.item_chat_image_framelayout);
		mImageAttachLayout.setOnClickListener(this);
		mPlayerView = view.findViewById(R.id.player);
		mProgressTextView = (TextView) view.findViewById(R.id.player_progress_text_view);
		mProgressSeekBar = (SeekBar) view.findViewById(R.id.player_seekbar);
		mPlayButton = (ImageButton) view.findViewById(R.id.player_action_button);
		mPlayButton.setOnClickListener(this);
		mCallButton = (Button) view.findViewById(R.id.item_chat_call_button);
		mCallButton.setOnClickListener(this);
		return null;
	}

	@Override
	public void preSetData(Bundle params) {
		if (params != null) {
			final PlayInfo playInfo = (PlayInfo) params.getSerializable(BundleKey.playInfo.name());
			if (playInfo.getMessageIdForPlay() == mMessageId) {
				mPlayInfo = playInfo;
				switch (mPlayInfo.getPlayerResponceStatus()) {
					case forvard_stop:
					case stop:
						stopPlay();
					break;
					case pending:
						// mPlayButton.setEnabled(false);
						mProgressSeekBar.setMax((int) mPlayInfo.getDuration());
						mProgressTextView.setText("loading");
					break;
					case play:
						// mPlayButton.setEnabled(true);
						mProgressSeekBar.setMax((int) mPlayInfo.getDuration());
						mPlayButton.setImageResource(R.drawable.selector_chat_stop_button);
						if (mPlayInfo.getProgress() > 0) {
							mProgressTextView.setText(mPlayInfo.getProgressString());
						}
						mProgressSeekBar.setProgress((int) mPlayInfo.getProgress());
					break;
					case update:
						// mPlayButton.setEnabled(true);
						if (mPlayInfo.getProgress() > 0) {
							mProgressTextView.setText(mPlayInfo.getProgressString());
						}
						mProgressSeekBar.setProgress((int) mPlayInfo.getProgress());
					break;					
					default:
					break;
				}
			}else{
				if(mPlayInfo!=null && mPlayInfo.getPlayerComandStatus()==PlayStatus.play){
					mPlayInfo.setPlayerComandStatus(PlayStatus.stop);
					mPlayInfo.setPlayerResponceStatus(PlayStatus.stop);
					stopPlay();
				}
			}
		}

	}

	private void stopPlay() {
		// mPlayButton.setEnabled(true);
		mPlayButton.setImageResource(R.drawable.selector_chat_play_button);
		mPlayInfo.setProgress(0);
		mProgressSeekBar.setProgress((int) mPlayInfo.getProgress());
		mProgressTextView.setText(mPlayInfo.getProgressString());
	}

	@Override
	public void setData(Cursor cursor) {
		final int messageId = cursor.getInt(MessageTable.id.ordinal());
		setDataToPlayerView(cursor, messageId);
		setDataToCallBackButton(cursor);
		setDataToImageLayout(cursor);
	}

	private void setDataToImageLayout(Cursor cursor) {
		String attachedImageUri = cursor.getString(cursor.getColumnIndex(QueryMessageThreadTable.imageUrl.name()));
		if (!TextUtils.isEmpty(attachedImageUri)) {
			if (mImageAttachLayout.getVisibility() != View.VISIBLE) {
				mImageAttachLayout.setVisibility(View.VISIBLE);
			}
			ImageLoader imageLoader = SmartPagerApplication.getInstance().getImageLoader();
			imageLoader.displayImage(attachedImageUri, (ImageView) mImageAttachLayout.getChildAt(2),
					R.drawable.def_pic_no_image);
            int imagesCount = cursor.getInt(cursor.getColumnIndex(QueryMessageThreadTable.numImages.name()));
			if ( imagesCount > 1) {
                setImage(attachedImageUri, imageLoader, (ImageView) mImageAttachLayout.getChildAt(1));
                if(imagesCount > 2)
    				setImage(attachedImageUri, imageLoader, (ImageView) mImageAttachLayout.getChildAt(0));
                else
                    mImageAttachLayout.getChildAt(0).setVisibility(View.GONE);
			} else {
				if (mImageAttachLayout.getChildAt(0).getVisibility() != View.GONE
						&& mImageAttachLayout.getChildAt(1).getVisibility() != View.GONE) {
					mImageAttachLayout.getChildAt(0).setVisibility(View.GONE);
					mImageAttachLayout.getChildAt(1).setVisibility(View.GONE);
				}
			}
		} else if (mImageAttachLayout.getVisibility() != View.GONE) {
			mImageAttachLayout.setVisibility(View.GONE);
		}
	}

	private void setImage(String attachedImageUri, ImageLoader imageLoader, ImageView imageView) {
		if (imageView.getVisibility() != View.VISIBLE) {
			imageView.setVisibility(View.VISIBLE);
		}
		imageLoader.displayImage(attachedImageUri, imageView, R.drawable.def_pic_no_image);
	}

	private void setDataToCallBackButton(Cursor cursor) {
		callbackNumber = cursor.getString(MessageTable.callbackNumber.ordinal());
		if (TextUtils.isEmpty(callbackNumber)) {
			if (mCallButton.getVisibility() != View.GONE) {
				mCallButton.setVisibility(View.GONE);
			}
		} else {
			mCallButton.setText("Call: "+  TelephoneUtils.format(callbackNumber));
			if (mCallButton.getVisibility() != View.VISIBLE) {
				mCallButton.setVisibility(View.VISIBLE);
			}
		}
	}

	private void setDataToPlayerView(Cursor cursor, final int messageId) {
		mMessageId = messageId;
        mRecordUrl = cursor.getString(MessageTable.recordUrl.ordinal());
		if (!TextUtils.isEmpty(mRecordUrl)) {
			if (mPlayerView.getVisibility() != View.VISIBLE) {
				mPlayerView.setVisibility(View.VISIBLE);
			}
		} else {
			if (mPlayerView.getVisibility() != View.GONE) {
				mPlayerView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.player_action_button:
				processPlayButtonClick();
			break;
			case R.id.item_chat_call_button:
				processCallBackButtonClick();
			break;
			case R.id.item_chat_image_framelayout:
				processImageClick();
			break;
			default:
			break;
		}
	}

	private void processImageClick() {
		if (mChatItemClickListener != null) {
			mChatItemClickListener.onImageClick(mMessageId);
		}
	}

	private void processCallBackButtonClick() {
		if (mChatItemClickListener != null) {
			mChatItemClickListener.onCallBackClick(callbackNumber, mMessageId);
		}
	}

	private void processPlayButtonClick() {
		if (mPlayInfo == null) {
			mPlayInfo = PlayInfo.createPlayerInfor(PlayStatus.play, mMessageId);
		}

		if (mPlayInfo.getPlayerResponceStatus() == PlayStatus.play
				|| mPlayInfo.getPlayerResponceStatus() == PlayStatus.update) {
			mPlayInfo.setPlayerComandStatus(PlayStatus.stop);
		}
		if (mPlayInfo.getPlayerResponceStatus() == PlayStatus.stop) {
			mPlayInfo.setPlayerComandStatus(PlayStatus.play);
		}
		mPlayInfo.setMessageIdForPlay(mMessageId);
		mPlayInfo.setUrl(mRecordUrl);
		
		mChatItemClickListener.onChatItemSwitchPlayMode(mPlayInfo);
	}

	@Override
	public void setListeners(Object... listeners) {
		mChatItemClickListener = (ChatItemClickListener) listeners[0];
	}

    public void setCurrListPosition (int pos)
    {
        m_nCurrListPosition = pos;
    }

    public void setWasRecipientsShown (boolean state)
    {
        m_bWasRecipientsShown = state;
    }
}
