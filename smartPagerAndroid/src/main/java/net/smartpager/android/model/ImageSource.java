package net.smartpager.android.model;

import android.net.Uri;

import java.io.Serializable;

import biz.mobidev.framework.adapters.holderadapter.IHolderSource;

public class ImageSource implements IHolderSource, Serializable {

	private static final long serialVersionUID = 1482537941381425604L;

	private String mPreviewImageUri;
    private String mFullImageUri;

    public ImageSource(Uri previewUri, Uri fullUri)
    {
        mPreviewImageUri = previewUri.toString();
        mFullImageUri = fullUri.toString();
    }
	
	public ImageSource(Uri imageUri) {
		mPreviewImageUri = imageUri.toString();
	}
	
	@Override
	public int getHolderID() {
		return 0;
	}

	public Uri getPreviewImageUri() {
		return Uri.parse(mPreviewImageUri);
	}

	public void setPreviewImageUri(Uri imageUri) {
		mPreviewImageUri = imageUri.toString();
	}


    public Uri getFullImageUri() {
        return Uri.parse(mFullImageUri);
    }

    public void setFullImageUri(Uri imageUri) {
        mFullImageUri = imageUri.toString();
    }

}
