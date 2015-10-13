package net.smartpager.android.utils.log;


import android.util.Log;

import java.io.File;
import java.util.Locale;

public class ELog {
	public static void m(int id,String format, Object...values){
		String messages[] = String.format(format, values).split("\n");
		for (int i = 0; i < messages.length; i++) {
			v(id, messages[i]);
		}
	}

	public static void v(int id,String format, Object...values){
		v(id,String.format(format, values));
	}
	private static void v(int id, String message) {
		Log.v("ELOG", String.format(Locale.getDefault(),"##%d %s",id,message));
	}
	
	
	
	public static void pt(int id,File path){
		pt(id,path,"");
	}
	
	private static void pt(int id,File path, String tab){
		if (path.isFile()){
			v(id,"%s %s ",tab,path.getName());
		}else{
			v(id,"%s[%s]",tab,path.getName());
			File [] files = path.listFiles();
			for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
				pt(id,files[fileIndex],tab+"\t");
			}
		}
	}

}