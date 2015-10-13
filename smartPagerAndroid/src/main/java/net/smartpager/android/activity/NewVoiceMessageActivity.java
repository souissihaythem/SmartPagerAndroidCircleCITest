package net.smartpager.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.sound.SoundRecorder;
import net.smartpager.android.web.response.AbstractResponse;

import java.io.IOException;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class NewVoiceMessageActivity extends BaseActivity {

	@ViewInjectAnatation(viewID = R.id.recorder_action_button)
	private ImageButton mBtnRecord;
	@ViewInjectAnatation(viewID = R.id.recorder_description_text_view)
	private TextView mTxtHint;
	@ViewInjectAnatation(viewID = R.id.recorder_progress_text_view)
	private TextView mTxtProgress;
	
	private String mRecordPath;
	private SoundRecorder mRecorder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_voice_message);
		Injector.doInjection(this);
		
		mRecordPath = getIntent().getExtras().getString(BundleKey.recordPath.name());
		mRecorder = new SoundRecorder(mTxtProgress);
	}

	@ClickListenerInjectAnatation(viewID = R.id.recorder_action_button)
	public void startRecordNewGreeting(View v) {
		if (mRecorder.isRecording()) {
			stopRecord();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mBtnRecord.post(new Runnable() {

			@Override
			public void run() {
				if (!mRecorder.isRecording()) {
					try {
						startRecord();
					} catch (IOException e) {
						showErrorDialog(e.getMessage());
					}
				}
			}
		});
	}

	private void startRecord() throws IOException {
		mRecorder.start(mRecordPath);
		mBtnRecord.setImageResource(R.drawable.voice_dialog_ic_stop);
		mBtnRecord.setBackgroundResource(R.drawable.selector_round_button_red);
		mTxtHint.setBackgroundResource(R.color.settings_seekbar_progress);
		mTxtHint.setTextColor(getResources().getColor(R.color.white_text));
		mTxtHint.setText(R.string.recording_);
	}

	private void stopRecord() {
		mRecorder.stop();
		mBtnRecord.setImageResource(R.drawable.voice_dialog_ic_record);
		mBtnRecord.setBackgroundResource(R.drawable.selector_round_button);
		mTxtHint.setBackgroundResource(R.color.recorder_layout_color);
		mTxtHint.setTextColor(getResources().getColor(R.color.recorder_text_color));
		mTxtHint.setText(R.string.type_record_when_ready);
		finish();
	}

	@Override
	public void finish() {
		Intent intent = new Intent();
		intent.putExtra(BundleKey.recordPath.name(), mRecordPath);
		setResult(RESULT_OK, intent);
		super.finish();
	}
	
	@Override
	protected void onPause() {
		stopRecord();
		super.onPause();
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {

	}

}
