package net.smartpager.android.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.widget.Gallery;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.adapters.ImageGalleryCursorAdapter;
import net.smartpager.android.consts.LoaderID;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.view.HackyViewPager;
import net.smartpager.android.web.response.AbstractResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

@SuppressWarnings("deprecation")
public class ImageGalleryActivity extends BaseActivity implements LoaderCallbacks<Cursor>{

	private int mMessageId;
	private ImageGalleryCursorAdapter mImageAdapter;
	
	@ViewInjectAnatation(viewID = R.id.image_gallery)
	private HackyViewPager mGallery;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_gallery);
		Injector.doInjection(this);
		
		mMessageId = getIntent().getIntExtra(WebSendParam.messageId.name(), 0);

		getSupportLoaderManager().initLoader(LoaderID.MESSAGE_IMAGE_LOADER.ordinal(), null, this);
		mImageAdapter = new ImageGalleryCursorAdapter();
		mImageAdapter.setCursor(null);
		mGallery.setAdapter(mImageAdapter);
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Loader<Cursor> loader = null;
		switch (LoaderID.values()[id]) {
		case MESSAGE_IMAGE_LOADER:
			String where = DatabaseHelper.ImageUrlTable.messageId.name() +  " = " + mMessageId;
			loader =  new CursorLoader(this, 
					SmartPagerContentProvider.CONTENT_IMAGE_URL_URI, 
					null,
					where,
					null,
					null);
			break;
		default:
			break;
		}		
		return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId();
		switch (LoaderID.values()[id]) {
		case MESSAGE_IMAGE_LOADER:
			//mImageAdapter.swapCursor(cursor);
			mImageAdapter.setCursor(cursor);
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		int id = loader.getId();
		switch (LoaderID.values()[id]) {
		case MESSAGE_IMAGE_LOADER:
			//mImageAdapter.swapCursor(null);
			mImageAdapter.setCursor(null);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected boolean isUpNavigationEnabled() {
		return true;
	}
}
