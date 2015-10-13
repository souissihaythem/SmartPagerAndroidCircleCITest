package net.smartpager.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.database.DatabaseHelper;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.view.ViewGroup.LayoutParams;

public class ImageGalleryCursorAdapter extends PagerAdapter {

	private LayoutInflater mInflater;
	private PhotoViewAttacher mAttacher;
	private Cursor cursor;
	
	/*public ImageGalleryCursorAdapter(Context context , boolean autoRequery) {
		super(context, autoRequery);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}*/

	public void setCursor(Cursor c){
		cursor = c;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (cursor == null) return 0;
		return cursor.getCount();
	}

	@Override
	public View instantiateItem(ViewGroup container, int position) {
		PhotoView photoView = new PhotoView(container.getContext());
		cursor.moveToPosition(position);
		String imageUri = cursor.getString(DatabaseHelper.ImageUrlTable.ImageUrl.ordinal());
		SmartPagerApplication.getInstance().getImageLoader()
				.displayFullImage(imageUri, photoView, R.drawable.chat_avatar_no_image);
		// Now just add PhotoView to ViewPager and return it
		container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		return photoView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
	
	/*@Override
	public void bindView(View view, Context context) {
		ImageView imageView = (ImageView) view.findViewById(R.id.gallery_image_view);
		String imageUri = cursor.getString(DatabaseHelper.ImageUrlTable.ImageUrl.ordinal());
		SmartPagerApplication.getInstance().getImageLoader()
			.displayFullImage(imageUri, imageView, R.drawable.chat_avatar_no_image);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewGroup view = new RelativeLayout(context);
		mInflater.inflate(R.layout.item_image_gallery, view, true);
		return view;
	}*/

}
