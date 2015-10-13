package net.smartpager.android.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import com.rey.material.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.crop.CropImageActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.RequestID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.utils.IntentFactory;
import net.smartpager.android.utils.PhotoFileUtils;
import net.smartpager.android.utils.TelephoneUtils;
import net.smartpager.android.web.response.AbstractResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

import biz.mobidev.library.lazybitmap.ImageLoader;

public abstract class AbstractProfileActivity extends BaseActivity {

	protected ImageView mPhotoImageView;
	protected EditText mFirstNameEditText;
	protected EditText mLastNameEditText;
	protected EditText mTitleEditText;
	protected TextView mPhoneTextView;
	protected TextView mPagerTextView;
	protected TextView mOrganizationTextView;
	protected TextView mDepartmentsTextView;
	protected Button mNewPhotoButton;

	protected boolean mReadProfileData;
	protected Uri mNewImageUri;
	protected String mNewImageFileName;

	protected void initGui() {
		mPhotoImageView = (ImageView) findViewById(R.id.personal_info_photo_imageView);
		mFirstNameEditText = (EditText) findViewById(R.id.personal_info_first_name_editText);
		mLastNameEditText = (EditText) findViewById(R.id.personal_info_last_name_editText);
		mTitleEditText = (EditText) findViewById(R.id.personal_info_title_editText);
		mPhoneTextView = (TextView) findViewById(R.id.personal_info_phone_textView);
		mPagerTextView = (TextView) findViewById(R.id.personal_info_pager_textView);
		mOrganizationTextView = (TextView) findViewById(R.id.personal_info_organization_textView);
		mDepartmentsTextView = (TextView) findViewById(R.id.personal_info_departments_textView);
		mNewPhotoButton = (Button) findViewById(R.id.personal_info_new_photo_button);
		mNewPhotoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pickNewPhoto();
			}
		});
		mReadProfileData = false;
	}

	protected void setDataFromPrefferences() {
		mFirstNameEditText.setText(SmartPagerApplication.getInstance().getPreferences().getFirstName());
		mLastNameEditText.setText(SmartPagerApplication.getInstance().getPreferences().getLastName());
		mTitleEditText.setText(SmartPagerApplication.getInstance().getPreferences().getTitle());
		mPhoneTextView.setText( TelephoneUtils.format(SmartPagerApplication.getInstance().getPreferences().getUserID()));
		mPagerTextView.setText((!TextUtils.isEmpty(SmartPagerApplication.getInstance().getPreferences().getPagerNumber())) 
				? TelephoneUtils.format(SmartPagerApplication.getInstance().getPreferences().getPagerNumber())
				: getString(R.string.none_provided));
		mOrganizationTextView.setText(SmartPagerApplication.getInstance().getPreferences().getAccountName());
		mDepartmentsTextView.setText(SmartPagerApplication.getInstance().getPreferences().getDepartments());
	}

	protected void getProfilePicture() {
		StringBuilder builder = new StringBuilder();
		builder.append(Constants.BASE_REST_URL).append("/");
		builder.append(WebAction.getProfilePicture.name()).append("?");
		builder.append(WebSendParam.smartPagerID.name()).append("=");
		builder.append(SmartPagerApplication.getInstance().getPreferences().getUserID()).append("&");
		builder.append(WebSendParam.uberPassword.name()).append("=");
		builder.append(SmartPagerApplication.getInstance().getPreferences().getPassword());
		String photoUrl = builder.toString();
		displayPhoto(photoUrl);
	}

	protected void pickNewPhoto() {
		try {
			boolean isFrontface = PhotoFileUtils.isFrontCameraAvailable();
			Pair<Intent, Uri> chooser = IntentFactory.getPhotoIntent(this, isFrontface);
			mNewImageUri = chooser.second;
			startActivityForResult(chooser.first, RequestID.PICK_PHOTO.ordinal());			
		} catch (Exception e) {
			showToast(e.getMessage());
		}
	}

	protected void saveData() {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.firstName.name(), mFirstNameEditText.getText().toString());
        extras.putString(WebSendParam.lastName.name(), mLastNameEditText.getText().toString());
        extras.putString(WebSendParam.title.name(), mTitleEditText.getText().toString());
        SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, extras);

		SmartPagerApplication.getInstance().getPreferences().setFirstName(mFirstNameEditText.getText().toString());
		SmartPagerApplication.getInstance().getPreferences().setLastName(mLastNameEditText.getText().toString());
		SmartPagerApplication.getInstance().getPreferences().setTitle(mTitleEditText.getText().toString());
		
		if (!TextUtils.isEmpty(mNewImageFileName)) {
            extras = new Bundle();
			extras.putString(WebSendParam.fileName.name(), mNewImageFileName);
            SmartPagerApplication.getInstance().startWebAction(WebAction.setProfilePicture, extras);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mNewImageUri != null) {
			outState.putString(BundleKey.imageUri.toString(), mNewImageUri.toString());
		}
		outState.putBoolean(BundleKey.readProfile.toString(), mReadProfileData);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		String uri = savedInstanceState.getString(BundleKey.imageUri.toString());
		if (!TextUtils.isEmpty(uri)) {
			mNewImageUri = Uri.parse(uri);
			displayPhoto(uri);
		}
		mReadProfileData = savedInstanceState.getBoolean(BundleKey.readProfile.toString(), false);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK){
			switch (RequestID.values()[requestCode]) {
				case PICK_PHOTO:
					if (data != null && data.getData() != null) {
						mNewImageUri = data.getData();
					}
					try {
						startCropPhoto(mNewImageUri);
					} catch (Exception e) {
						mNewImageUri = null;
						mNewImageFileName = "";
						showToast(getString(R.string.please_select_either_jpg_or_png_file));
					}
				break;
				case CROP_PHOTO:
					mNewImageUri = Uri.parse(data.getAction());
					long id = PhotoFileUtils.getID(this, mNewImageUri);
					Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(getContentResolver(), id,
							MediaStore.Images.Thumbnails.MINI_KIND, null);
					displayPhoto(mNewImageUri.toString());
					mPhotoImageView.setImageBitmap(bmp);
					mNewImageFileName = PhotoFileUtils.saveScaledImage(this, bmp);
				break;
				default:
				break;
			}
		}
	}

	private void displayPhoto(String photoUrl) {
		ImageLoader loader = SmartPagerApplication.getInstance().getImageLoader();
		loader.clearCacheFor(photoUrl);
		loader.displayImage(photoUrl, mPhotoImageView, R.drawable.chat_avatar_no_image);
	}

	private void startCropPhoto(Uri contactImageUri) {
		Intent intent = new Intent(this, CropImageActivity.class);
		// this will open all images in the Galery
		intent.putExtra("path", PhotoFileUtils.getPath(this, contactImageUri));
		intent.putExtra("crop", "true");
		intent.putExtra("orientation", PhotoFileUtils.getOrientation(contactImageUri.toString()));
		// true to return a Bitmap, false to directly save the cropped image
		intent.putExtra("return-data", false);
		// save output image in uri
		ContentValues values = new ContentValues();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");
		Uri fileuri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, fileuri);
		startActivityForResult(intent, RequestID.CROP_PHOTO.ordinal());
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		switch (action) {
			case getProfile:
				if (!mReadProfileData) {
					setDataFromPrefferences();
					mReadProfileData = true;
				}
			break;
			default:
			break;
		}
	}

	@Override
	protected void onResume() {
		if (!mReadProfileData) {
			sendSingleComand(WebAction.getProfile);
		}
		super.onResume();
	}
}
