/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.smartpager.android.activity.crop;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.media.ExifInterface;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import net.smartpager.android.R;
import net.smartpager.android.utils.PhotoFileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;

/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImageActivity extends Activity {

	// These are various options can be specified in the intent.
	private Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG; // only
																				// used
																				// with
																				// mSaveUri
	private Uri mSaveUri = null;
	private boolean mSetWallpaper = false;
	private int mAspectX, mAspectY;
	private boolean mDoFaceDetection = true;
	private boolean mCircleCrop = false;
	private final Handler mHandler = new Handler();

	// These options specifiy the output image size and whether we should
	// scale the output to fit it (or just crop it).
	private int mOutputX, mOutputY;
	private boolean mScale;
	// private boolean mScaleUp = true;

	boolean mWaitingToPick; // Whether we are wait the user to pick a face.
	boolean mSaving; // Whether the "save" button is already clicked.

	private CropImageView mImageView;

	private Bitmap mBitmap;
	HighlightView mCrop;

	// private PhotoItem mImage;
	String mUri;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cropimagelayout);

		mImageView = (CropImageView) findViewById(R.id.image);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (extras != null) {
			if (extras.getString("circleCrop") != null) {
				mCircleCrop = true;
				mAspectX = 1;
				mAspectY = 1;
			}
			mSaveUri = (Uri) extras.getParcelable(MediaStore.EXTRA_OUTPUT);
			if (mSaveUri != null) {
				String outputFormatString = extras.getString("outputFormat");
				if (outputFormatString != null) {
					mOutputFormat = Bitmap.CompressFormat
							.valueOf(outputFormatString);
				}
			} else {
				mSetWallpaper = extras.getBoolean("setWallpaper");
			}
			mBitmap = (Bitmap) extras.getParcelable("data");
			mAspectX = extras.getInt("aspectX");
			mAspectY = extras.getInt("aspectY");
			mOutputX = extras.getInt("outputX");
			mOutputY = extras.getInt("outputY");
			mScale = extras.getBoolean("scale", true);
			// mScaleUp = extras.getBoolean("scaleUpIfNeeded", true);
			mDoFaceDetection = extras.containsKey("noFaceDetection") ? !extras
					.getBoolean("noFaceDetection") : true;
		}

		if (mBitmap == null && extras != null) {
			mUri = extras.getString("path");
			int exifOrientation = extras.getInt("orientation");
			// mImage = new PhotoItem(new NodeBase);
			// mImage.setUri(uri);
			if (mUri != null) {
				try {
					BitmapFactory.Options optionsChangeSize = new BitmapFactory.Options();
					optionsChangeSize.inSampleSize = 2;
					mBitmap = BitmapFactory.decodeFile(mUri, optionsChangeSize);
					ExifInterface mExif = new ExifInterface(mUri);
					int orientation = mExif.getAttributeInt(
							ExifInterface.TAG_ORIENTATION, -1);
					exifOrientation = 0;
					switch (orientation) {
					case ExifInterface.ORIENTATION_ROTATE_270:
						exifOrientation = 270;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						exifOrientation = 180;
						break;
					case ExifInterface.ORIENTATION_ROTATE_90:
						exifOrientation = 90;
						break;
					case ExifInterface.ORIENTATION_NORMAL:
						exifOrientation = 0;
						break;
					}
					mBitmap = PhotoFileUtils.rotate(mBitmap, exifOrientation);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

		if (mBitmap == null) {
			finish();
			return;
		}

		// Make UI fullscreen.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		findViewById(R.id.discard).setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						setResult(RESULT_CANCELED);
						finish();
					}
				});

		findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onSaveClicked();
			}
		});

		startFaceDetection();
	}

	private void startFaceDetection() {
		if (isFinishing()) {
			return;
		}
		mImageView.setImageBitmapReset(mBitmap, true);
		Thread t = new Thread(new Runnable() {
			public void run() {
				final CountDownLatch latch = new CountDownLatch(1);
				final Bitmap b = mBitmap;
				mHandler.post(new Runnable() {
					public void run() {
						if (b != mBitmap && b != null) {
							// mImageView.setImageBitmapResetBase(b, true);
							mBitmap.recycle();
							mBitmap = b;
						}
						if (mImageView.getScale() == 1F) {
							mImageView.center(true, true);
						}
						latch.countDown();
					}
				});
				try {
					latch.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				mRunFaceDetection.run();
			}
		});
		t.start();
	}

	public static Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, boolean scaleUp, boolean recycle) {
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
			/*
			 * In this case the bitmap is smaller, at least in one dimension,
			 * than the target. Transform it by placing as much of the image as
			 * possible into the target and leaving the top/bottom or left/right
			 * (or both) black.
			 */
			Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
					Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));
			int dstX = (targetWidth - src.width()) / 2;
			int dstY = (targetHeight - src.height()) / 2;
			Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
					- dstY);
			c.drawBitmap(source, src, dst, null);
			if (recycle) {
				source.recycle();
			}
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = (float) targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		} else {
			float scale = targetWidth / bitmapWidthF;
			if (scale < .9F || scale > 1F) {
				scaler.setScale(scale, scale);
			} else {
				scaler = null;
			}
		}

		Bitmap b1;
		if (scaler != null) {
			// this is used for minithumb and crop, so we want to filter here.
			b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
					source.getHeight(), scaler, true);
		} else {
			b1 = source;
		}

		if (recycle && b1 != source) {
			source.recycle();
		}

		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
				targetHeight);

		if (b2 != b1) {
			if (recycle || b1 != source) {
				b1.recycle();
			}
		}

		return b2;
	}

	private void onSaveClicked() {
		if (mCrop == null) {
			return;
		}

		if (mSaving)
			return;
		mSaving = true;

		Bitmap croppedImage;

		// If the output is required to a specific size, create an new image
		// with the cropped image in the center and the extra space filled.
		if (mOutputX != 0 && mOutputY != 0 && !mScale) {
			// Don't scale the image but instead fill it so it's the
			// required dimension
			croppedImage = Bitmap.createBitmap(mOutputX, mOutputY,
					Bitmap.Config.RGB_565);
			Canvas canvas = new Canvas(croppedImage);

			Rect srcRect = mCrop.getCropRect();
			Rect dstRect = new Rect(0, 0, mOutputX, mOutputY);

			int dx = (srcRect.width() - dstRect.width()) / 2;
			int dy = (srcRect.height() - dstRect.height()) / 2;

			// If the srcRect is too big, use the center part of it.
			srcRect.inset(Math.max(0, dx), Math.max(0, dy));

			// If the dstRect is too big, use the center part of it.
			dstRect.inset(Math.max(0, -dx), Math.max(0, -dy));

			// Draw the cropped bitmap in the center
			canvas.drawBitmap(mBitmap, srcRect, dstRect, null);

			// Release bitmap memory as soon as possible
			mImageView.clear();
			mBitmap.recycle();
		} else {
			Rect r = mCrop.getCropRect();

			int width = r.width();
			int height = r.height();

			// If we are circle cropping, we want alpha channel, which is the
			// third param here.
			croppedImage = Bitmap.createBitmap(width, height,
					mCircleCrop ? Bitmap.Config.ARGB_8888
							: Bitmap.Config.RGB_565);

			Canvas canvas = new Canvas(croppedImage);
			Rect dstRect = new Rect(0, 0, width, height);
			canvas.drawBitmap(mBitmap, r, dstRect, null);

			// Release bitmap memory as soon as possible
			mImageView.clear();
			mBitmap.recycle();

			if (mCircleCrop) {
				// OK, so what's all this about?
				// Bitmaps are inherently rectangular but we want to return
				// something that's basically a circle. So we fill in the
				// area around the circle with alpha. Note the all important
				// PortDuff.Mode.CLEAR.
				Canvas c = new Canvas(croppedImage);
				Path p = new Path();
				p.addCircle(width / 2F, height / 2F, width / 2F,
						Path.Direction.CW);
				c.clipPath(p, Region.Op.DIFFERENCE);
				c.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
			}

			// If the required dimension is specified, scale the image.
			/*
			 * if (mOutputX != 0 && mOutputY != 0 && mScale) { croppedImage =
			 * Util.transform(new Matrix(), croppedImage, mOutputX, mOutputY,
			 * mScaleUp, Util.RECYCLE_INPUT);
			 */
		}

		// mImageView.setImageBitmapResetBase(croppedImage, true);
		mImageView.center(true, true);
		mImageView.mHighlightViews.clear();

		final Bitmap b = croppedImage;
		saveOutput(b);

	}

	private void saveOutput(Bitmap croppedImage) {
		if (mSaveUri != null) {
			OutputStream outputStream = null;
			try {
				outputStream = new FileOutputStream(PhotoFileUtils.getPath(this,
						mSaveUri));
				if (outputStream != null) {
					croppedImage.compress(mOutputFormat, 75, outputStream);
				}
			} catch (IOException ex) {
			} finally {
				// Util.closeSilently(outputStream);
				// FIX ?
			}
			Intent resultIntent = new Intent();
			resultIntent.setAction(mSaveUri.toString());
			setResult(Activity.RESULT_OK, resultIntent);
		} else if (mSetWallpaper) {
			try {
				WallpaperManager.getInstance(this).setBitmap(croppedImage);
				setResult(RESULT_OK);
			} catch (IOException e) {
				setResult(RESULT_CANCELED);
			}
		} else {
			Bundle extras = new Bundle();
			extras.putString("rect", mCrop.getCropRect().toString());

			File oldPath = new File(mUri);
			File directory = new File(oldPath.getParent());

			int x = 0;
			String fileName = oldPath.getName();
			fileName = fileName.substring(0, fileName.lastIndexOf("."));

			// Try file-1.jpg, file-2.jpg, ... until we find a fileName which
			// does not exist yet.
			while (true) {
				x += 1;
				String candidate = directory.toString() + "/" + fileName + "-"
						+ x + ".jpg";
				boolean exists = (new File(candidate)).exists();
				if (!exists) {
					break;
				}
			}
		}

		final Bitmap b = croppedImage;
		mHandler.post(new Runnable() {
			public void run() {
				mImageView.clear();
				b.recycle();
			}
		});

		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	Runnable mRunFaceDetection = new Runnable() {
		float mScale = 1F;
		Matrix mImageMatrix;
		FaceDetector.Face[] mFaces = new FaceDetector.Face[3];
		int mNumFaces;

		// For each face, we create a HightlightView for it.
		private void handleFace(FaceDetector.Face f) {
			PointF midPoint = new PointF();

			int r = ((int) (f.eyesDistance() * mScale)) * 2;
			f.getMidPoint(midPoint);
			midPoint.x *= mScale;
			midPoint.y *= mScale;

			int midX = (int) midPoint.x;
			int midY = (int) midPoint.y;

			HighlightView hv = new HighlightView(mImageView);

			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			RectF faceRect = new RectF(midX, midY, midX, midY);
			faceRect.inset(-r, -r);
			if (faceRect.left < 0) {
				faceRect.inset(-faceRect.left, -faceRect.left);
			}

			if (faceRect.top < 0) {
				faceRect.inset(-faceRect.top, -faceRect.top);
			}

			if (faceRect.right > imageRect.right) {
				faceRect.inset(faceRect.right - imageRect.right, faceRect.right
						- imageRect.right);
			}

			if (faceRect.bottom > imageRect.bottom) {
				faceRect.inset(faceRect.bottom - imageRect.bottom,
						faceRect.bottom - imageRect.bottom);
			}

			hv.setup(mImageMatrix, imageRect, faceRect, mCircleCrop,
					mAspectX != 0 && mAspectY != 0);

			mImageView.add(hv);
		}

		// Create a default HightlightView if we found no face in the picture.
		private void makeDefault() {
			HighlightView hv = new HighlightView(mImageView);

			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();

			Rect imageRect = new Rect(0, 0, width, height);

			// make the default size about 4/5 of the width or height
			int cropWidth = Math.min(width, height) * 4 / 5;
			int cropHeight = cropWidth;

			if (mAspectX != 0 && mAspectY != 0) {
				if (mAspectX > mAspectY) {
					cropHeight = cropWidth * mAspectY / mAspectX;
				} else {
					cropWidth = cropHeight * mAspectX / mAspectY;
				}
			}

			int x = (width - cropWidth) / 2;
			int y = (height - cropHeight) / 2;

			RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
			hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop,
					mAspectX != 0 && mAspectY != 0);
			mImageView.add(hv);
		}

		// Scale the image down for faster face detection.
		private Bitmap prepareBitmap() {
			if (mBitmap == null) {
				return null;
			}

			// 256 pixels wide is enough.
			if (mBitmap.getWidth() > 256) {
				mScale = 256.0F / mBitmap.getWidth();
			}
			Matrix matrix = new Matrix();
			matrix.setScale(mScale, mScale);
			Bitmap faceBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
					mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
			return faceBitmap;
		}

		public void run() {
			mImageMatrix = mImageView.getImageMatrix();
			Bitmap faceBitmap = prepareBitmap();

			mScale = 1.0F / mScale;
			if (faceBitmap != null && mDoFaceDetection) {
				FaceDetector detector = new FaceDetector(faceBitmap.getWidth(),
						faceBitmap.getHeight(), mFaces.length);
				mNumFaces = detector.findFaces(faceBitmap, mFaces);
			}

			if (faceBitmap != null && faceBitmap != mBitmap) {
				faceBitmap.recycle();
			}

			mHandler.post(new Runnable() {
				public void run() {
					mWaitingToPick = mNumFaces > 1;
					if (mNumFaces > 0) {
						for (int i = 0; i < mNumFaces; i++) {
							handleFace(mFaces[i]);
						}
					} else {
						makeDefault();
					}
					mImageView.invalidate();
					if (mImageView.mHighlightViews.size() == 1) {
						mCrop = mImageView.mHighlightViews.get(0);
						mCrop.setFocus(true);
					}

					/*
					 * if (mNumFaces > 1) { Toast t =
					 * Toast.makeText(CropImage.this,
					 * R.string.multiface_crop_help, Toast.LENGTH_SHORT);
					 * t.show(); }
					 */
				}
			});
		}
	};
}
