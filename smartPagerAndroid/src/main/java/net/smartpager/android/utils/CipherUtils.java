package net.smartpager.android.utils;

import android.content.Context;

import net.smartpager.android.consts.FileNames;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtils {

	private static Key mEncryptKey;

	static {
		try {
			File f = new File(FileNames.APP_ABSOLUTE_PATH + "/secretkey.dat");
			if (!f.exists()) {
				// Length is 16 byte
				SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
				mEncryptKey = sks;
				FileUtils.writeToFile(f.getAbsolutePath(), mEncryptKey);
			} else {
				mEncryptKey = (Key) FileUtils.readFromFile(f.getAbsolutePath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static final InputStream createCypherInStream(File infile) throws NoSuchAlgorithmException,
			NoSuchPaddingException, FileNotFoundException, InvalidKeyException {

		// if (FilenameUtils.getFullPathNoEndSeparator(infile.getAbsolutePath()).equals(FileNames.APP_ABSOLUTE_PATH)) {
		// return new FileInputStream(infile);
		// }
		// if (!FilenameUtils.getFullPathNoEndSeparator(infile.getAbsolutePath()).contains(FileNames.APP_ABSOLUTE_PATH))
		// {
		// return new FileInputStream(infile);
		// }
		Cipher cipher = Cipher.getInstance("AES");

		cipher.init(Cipher.DECRYPT_MODE, mEncryptKey);
		CipherInputStream cis = new CipherInputStream(new FileInputStream(infile), cipher);
		return cis;
	}

	public static final OutputStream createCypherOutStream(File outfile) throws NoSuchAlgorithmException,
			NoSuchPaddingException, FileNotFoundException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, mEncryptKey);
		CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(outfile), cipher);
		return cos;
	}

	public static final String decryptFile(Context ctx, File inFile) throws NoSuchAlgorithmException,
			NoSuchPaddingException, IOException, InvalidKeyException {
		InputStream s = CipherUtils.createCypherInStream(inFile);
		if (s instanceof CipherInputStream) {

			File outFile = File
					.createTempFile("encrypt",
							String.format(".%s", FilenameUtils.getExtension(inFile.getAbsolutePath())),
							FileUtils.getCacheDir());
			try {
				FileOutputStream os = new FileOutputStream(outFile);
				int len = 0;
				byte[] b = new byte[1024 * 1024 * 1];
				while ((len = s.read(b)) > 0) {
					os.write(b, 0, len);
				}
				s.close();
				os.close();
			} catch (Exception e) {
				if (outFile.exists()) {
					outFile.delete();
				}
				throw new IOException(e.getMessage());
			}
			return outFile.getAbsolutePath();
		} else {
			return inFile.getAbsolutePath();
		}
	}

	public static final String encryptFile(Context ctx, File inFile, File newPath) throws NoSuchAlgorithmException,
			NoSuchPaddingException, IOException, InvalidKeyException {
		InputStream s = new FileInputStream(inFile);
		File outFile = File.createTempFile("encrypt",
				String.format(".%s", FilenameUtils.getExtension(inFile.getAbsolutePath())), FileUtils.getCacheDir());
		OutputStream os = CipherUtils.createCypherOutStream(outFile);
		int len = 0;
		byte[] b = new byte[1024 * 10];
		while ((len = s.read(b)) > 0) {
			os.write(b, 0, len);
		}
		s.close();
		os.close();

		inFile.delete();
		outFile.renameTo(newPath);
		return inFile.getAbsolutePath();
	}

	public static final String encryptFile(Context ctx, File inFile) throws NoSuchAlgorithmException, NoSuchPaddingException,
			IOException, InvalidKeyException {		
		return encryptFile(ctx, inFile, inFile);
	}

}
