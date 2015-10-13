package net.smartpager.android.utils;

import android.content.Context;

import net.smartpager.android.SmartPagerApplication;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import biz.mobidev.framework.utils.Log;

public class FfmpegUtils {

	/**
	 * Converts .3gp file to .wav via ffmpeg library
	 * @param fileName source .3gp fileName
	 * @return destination .wav fileName
	 * @throws Exception
	 */
	public static String convertToWav(String fileName) throws Exception {

        Context mContext = SmartPagerApplication.getInstance().getApplicationContext();
        if (mContext != null && fileName.indexOf(".3gp") != -1) {
            Clip in = new Clip();
            Clip out = new Clip();

            in.path = fileName;
            out.path = String.format("%s.wav", fileName.substring(0, fileName.indexOf(".3gp")));

            int rand = new Random().nextInt(10000-1000) + 1000;
            File tempFile = File.createTempFile(new String().valueOf(rand), "wav");
            FfmpegController controller = new FfmpegController(mContext, tempFile);
            final int[] ret = {0};
            controller.processVideo(in, out, true, new ShellUtils.ShellCallback() {
				@Override
				public void shellOut(String shellLine) {
                    Log.e("FfmpegUtils", "shellLine: " + shellLine);
                }

				@Override
				public void processComplete(int exitValue) {
                    Log.e("FfmpegUtils", "processComplete: exitValue = " + exitValue);
                    ret[0] = exitValue;
                }
			});
            if (ret[0] == 0) {
                fileName = out.path;

                Log.e("FfmpegUtils", "ret = 0, fileName: " + fileName);
            } else {
                File del = new File(out.path);
                del.delete();
                Log.e("FfmpegUtils", "ret != 0, delete file: " + del.getPath());
            }
            tempFile.delete();
        }

		return fileName;
	}

	/**
	 * Converts audiofile to .3gp via ffmpeg library
	 * @param fileForSave
	 * @param file3gp
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws Exception
	 */
	public static File convertTo3gp(File fileForSave, final File file3gp) throws FileNotFoundException, IOException,
			Exception {

        Context mContext = SmartPagerApplication.getInstance().getApplicationContext();
        Clip in = new Clip();
        Clip out = new Clip();

        in.path = fileForSave.getAbsolutePath();
        out.path = file3gp.getAbsolutePath();

        int rand = new Random().nextInt(10000-1000) + 1000;
        File tempFile = File.createTempFile(new String().valueOf(rand), "wav");
        FfmpegController controller = new FfmpegController(mContext, tempFile);
        final int[] ret = {0};

        controller.processVideo(in, out, true, new ShellUtils.ShellCallback() {
            @Override
            public void shellOut(String shellLine) {
                Log.e("FfmpegUtils", "shellLine: " + shellLine);
            }

            @Override
            public void processComplete(int exitValue) {
                Log.e("FfmpegUtils", "processComplete: exitValue = " + exitValue);
                ret[0] = exitValue;
            }
        });
        if (ret[0] == 0) {
            fileForSave.delete();
            fileForSave = file3gp;
        } else {
            file3gp.delete();
        }
        tempFile.delete();
		return fileForSave;
	}

}
