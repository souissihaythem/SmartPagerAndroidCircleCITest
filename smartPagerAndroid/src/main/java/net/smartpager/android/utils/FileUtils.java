package net.smartpager.android.utils;

import android.os.Environment;

import net.smartpager.android.SmartPagerApplication;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class FileUtils {

	private static File cacheDir;

	static {
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
			cacheDir = new File(new File(dataDir, SmartPagerApplication.getInstance().getApplicationContext()
					.getPackageName()), "cache");
		} else {
			cacheDir = SmartPagerApplication.getInstance().getApplicationContext().getCacheDir();
		}
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
	}

	public static File getCacheDir() {
		return cacheDir;
	}

	public static void copy(File source, File target) throws IOException {
		if (source.exists()) {
			InputStream in = null;
			OutputStream out = null;
			try {
				if (target.exists()) {
					target.delete();
				}
				in = new FileInputStream(source);
				out = new FileOutputStream(target, true);
				IOUtils.copy(in, out);
			} catch (IOException e) {
				throw new IOException(e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
	}

	public static void copy(String sourcePath, String targetPath) throws IOException {
		File source = new File(sourcePath);
		File target = new File(targetPath);
		copy(source, target);
	}

	public static void writeToFile(String filename, Object object) throws Exception {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;

		try {
			fos = new FileOutputStream(new File(filename));
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}

	public static Object readFromFile(String filename) throws Exception {
		FileInputStream fos = null;
		ObjectInputStream oos = null;
		Object obj = null;
		try {
			fos = new FileInputStream(new File(filename));
			oos = new ObjectInputStream(fos);
			obj = oos.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (oos != null) {
				oos.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
		return obj;
	}

}
