package net.smartpager.android.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Date;



public final class IntentFactory {
	
	public static final String CHOOSER_RETURN_DATA = "return-data"; 
	
	public static Pair<Intent,Uri> getPhotoIntent(Context ctx){
		return getPhotoIntent(ctx, false);
	}
	
	public static Pair<Intent,Uri> getPhotoIntent(Context ctx,boolean isFrontface){
		Uri fileuri = getImageUriFromTimestamp(ctx);
		// Gallery Intent		
		//Intent galleryintent = new Intent(Intent.ACTION_GET_CONTENT, null);
		Intent galleryintent = new Intent(Intent.ACTION_PICK, null);
		galleryintent.setType("image/*");
		// Camera Intent
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra( "android.intent.extras.CAMERA_FACING",
				isFrontface
				? android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT
				: android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);		
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileuri);
		cameraIntent.putExtra(CHOOSER_RETURN_DATA, false);
		// Chooser Intent
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INTENT, galleryintent);
		chooser.putExtra(Intent.EXTRA_TITLE, "Select image");
		Intent[] intentArray = { cameraIntent };
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
		return new Pair<Intent, Uri>(chooser, fileuri);
	}
	
	public static Pair<Intent,Uri> getCameraIntent(Context ctx){
		return getCameraIntent(ctx, false);
	}	
	

	public static Pair<Intent,Uri> getCameraIntent(Context ctx,boolean isFrontface){
//		Uri fileuri = getImageUriFromTimestamp(ctx);
//		// Camera Intent
//		Intent cameraIntent = new Intent(
//				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//		cameraIntent.putExtra(
//				"android.intent.extras.CAMERA_FACING", 				
//				isFrontface
//				? 2
//				: android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
//		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileuri);
//		cameraIntent.putExtra(CHOOSER_RETURN_DATA, false);		
		Intent galleryintent = new Intent(Intent.ACTION_GET_CONTENT, null);
		galleryintent.setType("image/*");
		Intent cameraIntent = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		
		cameraIntent .putExtra("android.intent.extras.CAMERA_FACING", isFrontface?2:android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
		ContentValues values = new ContentValues();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp+".jpg");
		Uri fileuri = ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileuri);
		cameraIntent.putExtra("return-data", false);
		Intent chooser = new Intent(Intent.ACTION_CHOOSER);
		chooser.putExtra(Intent.EXTRA_INTENT, galleryintent);
		
		chooser.putExtra(Intent.EXTRA_TITLE, "Select your contact photo");
		Intent[] intentArray = { cameraIntent };
		chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
		return new Pair<Intent, Uri>(cameraIntent, fileuri);
	}
	
	public static Pair<Intent,Uri> getImageIntent(Context ctx){
		Uri fileuri = getImageUriFromTimestamp(ctx);
		// Gallery Intent
		Intent galleryintent = new Intent(Intent.ACTION_PICK);
		galleryintent.setType("image/*");
		galleryintent.putExtra(Intent.EXTRA_TITLE, "Select image");
		return new Pair<Intent, Uri>(galleryintent, fileuri);
	}
	
	public static Uri getImageUriFromTimestamp(Context ctx){
		ContentValues values = new ContentValues();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		values.put(MediaStore.Images.Media.TITLE, "IMG_" + timeStamp + ".jpg");
		return ctx.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}

}

