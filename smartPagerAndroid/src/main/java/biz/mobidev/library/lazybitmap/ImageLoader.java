package biz.mobidev.library.lazybitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.FileNames;
import net.smartpager.android.utils.CipherUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import biz.mobidev.framework.utils.Log;

public class ImageLoader {
	public static final boolean IMAGE_CODING_ENABLED = true;

	protected static final int MAX_THREADS = 1;

	protected static final String PROTOCOL_FILE = "file";

	protected static final String PROTOCOL_HTTP = "http";
	protected static final String PROTOCOL_HTTPS = "https";
	protected static final String PROTOCOL_FTP = "ftp";

	public static int IMAGE_ROUND_TAG = 101;
	public static int IMAGE_SIMPLE_TAG = 102;
	MemoryCache memoryCache = new MemoryCache();
	FileCache fileCache;
	private ConcurrentHashMap<View, String> imageViews = new ConcurrentHashMap<View, String>();
	ExecutorService executorService;
    private Activity m_currActivity = null;

	final int RADIUS = 10;
	private int SIZE = 100;

	public ImageLoader(Context context) {
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(MAX_THREADS);
		SIZE = (int) (SIZE * context.getResources().getDisplayMetrics().density);
	}

    public void resetViews ()
    {
        if(imageViews != null)
            imageViews.clear();
    }

    public void setCurrActivity (Activity activity)
    {
        m_currActivity = activity;
    }

	final int stub_id = R.drawable.def_pic_loading;

	public void displayImage(String imageUri, ImageView imageView, int emptyPlacedHolderId, int loadPlacedHolderId) {
		imageView.setTag(IMAGE_SIMPLE_TAG);
		internalDisplayImage(imageUri, imageView, emptyPlacedHolderId, loadPlacedHolderId, SIZE);
	}

	public void displayFullImage(String imageUri, ImageView imageView, int emptyPlacedHolderId) {
		imageView.setTag(IMAGE_SIMPLE_TAG);
		internalDisplayImage(imageUri, imageView, emptyPlacedHolderId, stub_id, -1);
	}

	public void displayImage(String imageUri, ImageView imageView, int emptyPlacedHolderId) {
		imageView.setTag(IMAGE_SIMPLE_TAG);
		internalDisplayImage(imageUri, imageView, emptyPlacedHolderId, stub_id, SIZE);
	}

	private void internalDisplayImage(String imageUrl, ImageView imageView, int emptyPlacedHolderId,
			int loadPlacedHolderId, int size) {
		imageView.setTag(imageUrl);
		if (!TextUtils.isEmpty(imageUrl)) {
			imageView.setImageResource(loadPlacedHolderId);
			Bitmap bitmap = memoryCache.get(imageUrl);
			if (bitmap != null) {
//				if (Integer.valueOf(imageView.getTag().toString()) == IMAGE_ROUND_TAG) {
//					bitmap = getRoundedCornerBitmap(bitmap);
//				}
				imageView.setImageBitmap(bitmap);
			} else {
                queuePhoto(imageUrl, imageView, emptyPlacedHolderId, loadPlacedHolderId, size);
				if (emptyPlacedHolderId != -1) {
					imageView.setImageResource(loadPlacedHolderId);
				} else {
					imageView.setImageDrawable(null);
				}
				//imageView.setImageResource(emptyPlacedHolderId);
			}
		} else {
			if (emptyPlacedHolderId != -1) {
				imageView.setImageResource(emptyPlacedHolderId);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}

	public void displayImage(String imageUrl, TextView imageView) {
		if (!TextUtils.isEmpty(imageUrl)) {

			imageViews.put(imageView, imageUrl);
			Bitmap bitmap = memoryCache.get(imageUrl);
			if (bitmap != null) {

				imageView.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(SmartPagerApplication
						.getInstance().getResources(), bitmap), null, null, null);
			} else {
				imageView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
				queuePhoto(imageUrl, imageView, -1, stub_id, -1);
				// imageView.setImageResource(emptyPlacedHolderId);
			}
		} else {
			imageView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
	}

	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		// return bitmap;

		final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = (float) (bitmap.getWidth() * 0.1);// MyShoutApplication.getInstance().getResources().getDisplayMetrics().density;
		paint.setAntiAlias(true);
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_4444);
		Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		canvas = null;
		bitmap = null;
		return output;
	}

	private void queuePhoto(String url, View imageView, int emptyPlacedHolderId, int loadPlacedHolderId, int size) {
        if(!memoryCache.hasID(url))
        {
            imageViews.putIfAbsent(imageView, url);
            memoryCache.put(url, null);
		    PhotoToLoad p = new PhotoToLoad(url, imageView, emptyPlacedHolderId, loadPlacedHolderId, size);
		    executorService.submit(new PhotosLoader(p));
        }
	}

	protected Bitmap getBitmap(String url, int size) {
//		File f = fileCache.getFile(url);
        File f = new File(url);
        if(!f.exists())
            f = FileCache.getFile(url);
		// from SD cache
		Bitmap bitmap = decodeEncryptedFile(f, size);
		if (bitmap == null) {

			// from web
			try {
				URI imageUri = URI.create(url);
				String scheme = imageUri.getScheme();
				// InputStream inputStream=null;
				if (PROTOCOL_HTTP.equals(scheme) || PROTOCOL_HTTPS.equals(scheme) || PROTOCOL_FTP.equals(scheme)) {
					bitmap = getStreamFromNetwork(imageUri, f, size);
				} else {
					File newFile = new File(url);
					if (PROTOCOL_FILE.equals(newFile.toURI().getScheme())) {
						bitmap = decodeEncryptedFile(newFile, size);
					}
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
				if (ex instanceof OutOfMemoryError) memoryCache.clear();
				return null;
			}
		}
		int rotation = getOrientation(f.toURI().getPath());
		if (rotation > 0) {
			Matrix transformMatrix = new Matrix();
			transformMatrix.postRotate(rotation);
			Bitmap transformedBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
					transformMatrix, true);
			bitmap.recycle();
			bitmap = transformedBmp;
		}
		return bitmap;
	}

	public static int getOrientation(String str) {
		ExifInterface exif;
		int tag = -1;
		try {
			exif = new ExifInterface(str);
			String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
			tag = Integer.parseInt(exifOrientation);
		} catch (IOException e) {
			return 0;
		} // Since API Level 5
		switch (tag) {
			case 1:
				return 0;
			case 3:
				return 180;
			case 8:
				return 270;
			case 6:
				return 90;
			default:
				return 0;
		}
	}

	private Bitmap getStreamFromNetwork(URI imageUri, File f, int size) throws MalformedURLException, IOException {
		
//		URL url = imageUri.toURL();
//	    HttpURLConnection connRedirect = (HttpURLConnection) url.openConnection();
//	    connRedirect.connect();
//	    int responseCode = connRedirect.getResponseCode();
//	    if ( responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM )
//	    {
//	       String location = connRedirect.getHeaderField("Location");
//	       imageUri = URI.create(location);
//	    }
	    
		HttpURLConnection conn = (HttpURLConnection) imageUri.toURL().openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		conn.setInstanceFollowRedirects(true);
		InputStream is = conn.getInputStream();
		OutputStream os = new FileOutputStream(f);
		Utils.CopyStream(is, os);
		os.close();
		Bitmap bitmap = decodeFile(f, size);
		try {
			if (IMAGE_CODING_ENABLED) {
				CipherUtils.encryptFile(SmartPagerApplication.getInstance(), f);
			}
		} catch (Exception e) {
			return null;
		}
		return bitmap;
	}

	// decodes image and scales it to reduce memory consumption
	protected Bitmap decodeFile(File f, int size) {
		try {
			// decode image size

			// Find the correct scale value. It should be the power of 2.
			// if (size == 0) {
			// size = 300;
			// }
			FileInputStream stream2 = new FileInputStream(f);
			Bitmap bitmap;
			if (size != -1) {
				BitmapFactory.Options o = new BitmapFactory.Options();
				o.inJustDecodeBounds = true;
				FileInputStream stream1 = new FileInputStream(f);
				BitmapFactory.decodeStream(stream1, null, o);
				stream1.close();
				final int REQUIRED_SIZE = size;
				int width_tmp = o.outWidth, height_tmp = o.outHeight;
				int scale = 1;
				while (true) {
					if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) break;
					width_tmp /= 2;
					height_tmp /= 2;
					scale *= 2;
				}

				// decode with inSampleSize
				BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = scale;
				bitmap = BitmapFactory.decodeStream(stream2, null, o2);
			} else {
				bitmap = BitmapFactory.decodeStream(stream2);
			}
			stream2.close();
			return bitmap;
		} catch (FileNotFoundException e) {} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected Bitmap decodeEncryptedFile(File f, int size) {
		if (IMAGE_CODING_ENABLED) {
			Context context = SmartPagerApplication.getInstance().getApplicationContext();
			Bitmap bitmap = null;
			try {
				// decode file
				boolean isEncrypted = f.getAbsolutePath().contains(FileNames.CACHE_DIR.getPath());
				if (isEncrypted) {
					String decryptedFilename = CipherUtils.decryptFile(context, f);
					File decrypted = new File(decryptedFilename);
					bitmap = decodeFile(decrypted, size);
					if (decrypted.exists()) {
						decrypted.delete();
					}
				} else {
					bitmap = decodeFile(f, size);
				}
			} catch (Exception e) {
				// FIXME 14-06-2013
				// if file isn't encoded
				// bitmap = decodeFile(f, size);
			}
			return bitmap;
		} else {
			return decodeFile(f, size);
		}
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
//		public View imageView;
		public int size;
		@SuppressWarnings("unused")
		public int loadPlacedHolderId = -1;
		public int emptyPlacedHolderId = -1;
//		@SuppressWarnings("unused")
//		public int hashCode;

//		@SuppressWarnings("unused")
		public PhotoToLoad(String url, View i, int emptyPlacedHolderId, int loadPlacedHolderId) {
			this(url, i, emptyPlacedHolderId, loadPlacedHolderId, -1);
		}

		public PhotoToLoad(String url, View i, int emptyPlacedHolderId, int loadPlacedHolderId, int size) {
//			imageView = i;
			this.emptyPlacedHolderId = emptyPlacedHolderId;
			this.loadPlacedHolderId = loadPlacedHolderId;
			this.url = url;
			this.size = size;
//			this.hashCode = imageView.hashCode();
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
//			if (imageViewReused(photoToLoad)) return;
			Bitmap bmp = getBitmap(photoToLoad.url, photoToLoad.size);
			memoryCache.put(photoToLoad.url, bmp);
//			if (imageViewReused(photoToLoad)) return;
            ArrayList<View> views = new ArrayList<View>();
            Iterator iterator = imageViews.entrySet().iterator();
            while (iterator.hasNext())
            {
                Map.Entry<View, String> entry = (Map.Entry)iterator.next();
                if(entry.getValue().equalsIgnoreCase(photoToLoad.url))
                    views.add(entry.getKey());
            }
			BitmapDisplayer bd = new BitmapDisplayer(bmp, views);
            if(m_currActivity != null)
                m_currActivity.runOnUiThread(bd);
		}
	}

//	boolean imageViewReused(PhotoToLoad photoToLoad) {
//		String tag = imageViews.get(photoToLoad.imageView);
//		if (tag == null || !tag.equals(photoToLoad.url)) return true;
//		return false;
//	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
//		PhotoToLoad photoToLoad;
        private ArrayList<View> viewToUpdate = null;

		public BitmapDisplayer(Bitmap b, ArrayList<View> views) {
			bitmap = b;
//			photoToLoad = p;
            viewToUpdate = views;
		}

        public void run() {
            for (View view : viewToUpdate) {
                if (bitmap != null) {
                    if (view instanceof ImageView) {
                        ((ImageView) view).setImageBitmap(bitmap);
                    } else if (view instanceof TextView){
                        ((TextView) view).setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(
                                SmartPagerApplication.getInstance().getResources(), bitmap), null, null, null);
                    }
                }
            }
        }

//		public void run() {
//			if (imageViewReused(photoToLoad) || ((photoToLoad.imageView.getTag() == null) || !photoToLoad.imageView.getTag().toString().equalsIgnoreCase(photoToLoad.url) )) {
//				if (photoToLoad.imageView instanceof ImageView) {
//				    ((ImageView) photoToLoad.imageView).setImageResource(photoToLoad.emptyPlacedHolderId);
//				}else{
//					((TextView) photoToLoad.imageView).setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//				}
//				return;
//			}
//			if (bitmap != null) {
//				if (photoToLoad.imageView instanceof ImageView) {
//					((ImageView) photoToLoad.imageView).setImageBitmap(bitmap);
//				} else {
//					((TextView) photoToLoad.imageView).setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(
//							SmartPagerApplication.getInstance().getResources(), bitmap), null, null, null);
//				}
//			}
//		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	public void clearCacheFor(String imageUrl) {

		File file = FileCache.getFile(imageUrl);
		if (file.exists()) file.delete();
        memoryCache.remove(imageUrl);
//		Bitmap bitmap = memoryCache.get(imageUrl);
//		if (bitmap != null) memoryCache.put(imageUrl, null);
	}

}
