package net.smartpager.android.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.smartpager.android.SmartPagerApplication;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import biz.mobidev.library.lazybitmap.FileCache;
import biz.mobidev.library.lazybitmap.ImageLoader;

public class PhotoFileUtils {
	
	public static final int PHOTO_PROCESSING_DEALY_S = 1;
	public static final int MAX_RESOLUTION = 1024;

	public static String getPath(Context context, Uri uri) {
		Cursor cursor = null;
		int column_index = 0;
		String path = null;
		try {
			String[] projection = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(uri, projection, null, null, null);
			column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			path = cursor.getString(column_index);
			// ctx.stopManagingCursor(cursor);
		} finally {
			if (cursor != null) cursor.close();
		}
		return path;
	}
	
	public static long getID(Context context, Uri uri) {
		long id = 0L;
		Cursor cursor = null;
		try {
			String[] projection = { MediaStore.Images.ImageColumns._ID };
			cursor = context.getContentResolver().query(uri, projection, null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID);
			cursor.moveToFirst();
			id = cursor.getLong(column_index);
		} catch (Exception e) {

		} finally {
			if (cursor != null)
				cursor.close();
		}
		return id;
	}

    public static Bitmap getBmpFromFile (String photoPath)
    {
        return getBmpFromFile(photoPath, false, -1, -1);
    }
	
	public static Bitmap getBmpFromFile(String photoPath, boolean scale, int maxWidth, int maxHeight) {
		if (TextUtils.isEmpty(photoPath)) {
			return null;
		}
		Bitmap originalBitmap = null;
		try {
			originalBitmap = BitmapFactory.decodeFile(photoPath);
            if(scale)
            {
                float scaleFactor = calculateScaleFactor(originalBitmap, maxWidth, maxHeight);
                int newHeight = (int)(originalBitmap.getHeight() * scaleFactor);
                int newWidth = (int)(originalBitmap.getWidth() * scaleFactor);
    			originalBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, false);
            }
			// check correct orientation and rotate bitmap
			int rotation = PhotoFileUtils.getOrientation(photoPath);
			if (rotation > 0) {
				Bitmap rotateBitmap = PhotoFileUtils.checkOrientation(originalBitmap, rotation);
				originalBitmap = rotateBitmap;
			}
		} catch (Exception e) {
			return null;
		}
		return originalBitmap;
	}

	public static Bitmap getScaledBmpFromFile(String photoPath) {
		return getBmpFromFile(photoPath, true, MAX_RESOLUTION, MAX_RESOLUTION);
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int maxWidth, int maxHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > maxHeight || width > maxWidth) {
			final int heightRatio = Math.round((float) height / (float) maxHeight);
			final int widthRatio = Math.round((float) width / (float) maxWidth);
			inSampleSize = Math.min(heightRatio, widthRatio);
		}
		return inSampleSize;
	}
	
	public static float calculateScaleFactor(Bitmap bmp, int maxWidth, int maxHeight) {
		// Raw height and width of image
		final int height = bmp.getHeight();
		final int width = bmp.getWidth();
		float scaleFactor = 1f;
		
		if (height > maxHeight || width > maxWidth) {
			float heightRatio = (float) maxHeight / (float) height;
			float widthRatio = (float) maxWidth / (float) width;
			scaleFactor = Math.min(heightRatio, widthRatio);
		}
		return scaleFactor;
	}	

	public static Bitmap checkOrientation(Bitmap bitmap, int rotate) {
		if (rotate != -1) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			Bitmap rotateBitmap = Bitmap
					.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			matrix = null;
			bitmap.recycle();
			bitmap = rotateBitmap;
		}
		return bitmap;
	}

	public static int getOrientation(String str) {
		ExifInterface exif;
		int tag = 0;
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

	// Rotates the bitmap by the specified degree.
	// If a new bitmap is created, the original bitmap is recycled.
	public static Bitmap rotate(Bitmap b, int degrees) {
		return rotateAndMirror(b, degrees, false);
	}

	// Rotates and/or mirrors the bitmap. If a new bitmap is created, the
	// original bitmap is recycled.
	public static Bitmap rotateAndMirror(Bitmap b, int degrees, boolean mirror) {
		if ((degrees != 0 || mirror) && b != null) {
			Matrix m = new Matrix();
			// Mirror first.
			// horizontal flip + rotation = -rotation + horizontal flip
			if (mirror) {
				m.postScale(-1, 1);
				degrees = (degrees + 360) % 360;
				if (degrees == 0 || degrees == 180) {
					m.postTranslate(b.getWidth(), 0);
				} else if (degrees == 90 || degrees == 270) {
					m.postTranslate(b.getHeight(), 0);
				} else {
					throw new IllegalArgumentException("Invalid degrees=" + degrees);
				}
			}
			if (degrees != 0) {
				// clockwise
				m.postRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
			}

			try {
				Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
				if (b != b2) {
					b.recycle();
					b = b2;
				}
			} catch (OutOfMemoryError ex) {
				// We have no memory to rotate. Return the original bitmap.
			}
		}
		return b;
	}

    public static String saveScaledImage(Context context, Bitmap bitmap)
    {
        return saveScaledImage(context, bitmap, false);
    }

	public static String saveScaledImage(Context context, Bitmap bitmap, boolean useCacheDir) {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String filename = "IMG_" + timeStamp + "_SMALL.jpg";
		File rootsd = Environment.getExternalStorageDirectory();
		File file = useCacheDir ? FileCache.getFile(filename) : new File(rootsd.getAbsolutePath() + "/Android/data/net.smartpager.android/" + filename);
		FileOutputStream ostream;
		try {
			file.createNewFile();
			ostream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, 75, ostream);
            try {
                if (useCacheDir && ImageLoader.IMAGE_CODING_ENABLED) {
                    CipherUtils.encryptFile(SmartPagerApplication.getInstance(), file);
                }
            } catch (Exception e) {
            }
			ostream.flush();
			ostream.close();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		return file.getAbsolutePath();
	}
	
	public static boolean isFrontCameraAvailable(){
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();		
		int cameraCount = Camera.getNumberOfCameras();
	    for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
	        Camera.getCameraInfo( camIdx, cameraInfo );
	        if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
	            return true;
	        }
	    }
	    return false;
	}

}
