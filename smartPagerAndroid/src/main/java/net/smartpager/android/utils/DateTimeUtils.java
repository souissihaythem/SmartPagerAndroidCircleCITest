package net.smartpager.android.utils;

import android.annotation.SuppressLint;

import net.smartpager.android.consts.ArchiveMessages;
import net.smartpager.android.consts.RemoveFromArchive;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

// TODO - resolve this suppression
@SuppressWarnings("deprecation")
public class DateTimeUtils {

	public static final long MS_IN_SEC = 1000; 
	public static final long MS_IN_MIN = 60 * MS_IN_SEC; 
	public static final long MS_IN_HOUR = 60 * MS_IN_MIN; 
	public static final long MS_IN_DAY = 24 * MS_IN_HOUR; 
	public static final long MS_IN_WEEK = 7 * MS_IN_DAY; 
	
	// Formatter format reference
	// http://developer.android.com/reference/java/util/Formatter.html
	public static final String TIME_ZONE_ID = "GMT"; 
	public static final String DATE_TIMESTAMP_FORMAT = "yyyy-MM-dd"; 
	public static final String DATE_FORMAT = "%1$tm/%1$td"; 
	public static final String TIME_FORMAT = "%1$tl:%1$tM %1$Tp"; 
	public static final String SEPARATOR = ", "; 
	public static final String FULL_FORMAT = DATE_FORMAT + SEPARATOR + TIME_FORMAT; 
	
	public static String format(long milliseconds){
		Date date = new Date(milliseconds);		
		Date today = new Date();		
		String formatter = FULL_FORMAT;
		if ( 	date.getYear() 	== today.getYear()
			&& 	date.getMonth() == today.getMonth()
			&&	date.getDate() 	== today.getDate()	){
			formatter = TIME_FORMAT;
		}
		return String.format(formatter, date);
	}	

	// TODO - resolve this suppression
	@SuppressLint("DefaultLocale")
	public static String fullDateFormat(long milliseconds){
		return String.format(FULL_FORMAT, new Date(milliseconds));	
	}

	// TODO - resolve this suppression
	@SuppressLint("SimpleDateFormat")
	public static String today(String formater){
		Date today = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(formater);
		return sdf.format(today);
	}

	public static String today(){
		return today(DATE_TIMESTAMP_FORMAT);	
	}

	public static String dateFromTimestamp(String timestamp){
		try{			
			String date = timestamp.substring(0,10);
			return date;
		}
		catch (Exception e){
			return "";
		}
	}
	
	public static String progressToString(long milliseconds){
		long hours = milliseconds / MS_IN_HOUR;
		long minutes = ( milliseconds - hours * MS_IN_HOUR) / MS_IN_MIN;
		long seconds = ( milliseconds - hours * MS_IN_HOUR - minutes * MS_IN_MIN) / MS_IN_SEC;
		StringBuilder builder = new StringBuilder();
		if (hours > 0 ){
			builder.append(hours).append(":");
		}
		builder.append(String.format("%02d", minutes)).append(":");
		builder.append(String.format("%02d", seconds));
		return builder.toString();
	} 
	
	public static long getTimeInMillis(ArchiveMessages archive){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_ID));
        Date date = new Date();
		switch (archive) {			
			case hour:
//				calendar.roll(Calendar.HOUR, false);
                date.setDate(date.getHours() - 1);
			break;
			case day:
//				calendar.roll(Calendar.DAY_OF_MONTH, false);
                date.setDate(date.getDate() - 1);
			break;
			case week:
//				calendar.roll(Calendar.WEEK_OF_YEAR, false);
                date.setDate(date.getDate() - 7);
			break;
			default:
			break;
		}
        return date.getTime();
//		return calendar.getTimeInMillis();
	}

	public static long getTimeInMillis(RemoveFromArchive archive){
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(TIME_ZONE_ID));
        Date date = new Date();
		switch (archive) {
			case week:
//				calendar.roll(Calendar.WEEK_OF_YEAR, false);
                date.setDate(date.getDate() - 7);
				break;
			case month:
//				calendar.roll(Calendar.MONTH, false);
                date.setMonth(date.getMonth() - 1);
				break;
			default:
				break;
		}
		return date.getTime();
//		return calendar.getTimeInMillis();
	}
}
