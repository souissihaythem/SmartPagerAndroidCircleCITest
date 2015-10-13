package biz.mobidev.library.lazybitmap;

import java.io.File;

import net.smartpager.android.utils.FileUtils;
import android.content.Context;

public class FileCache {
    
    
    public FileCache(Context context){

    }
    
    public static File getFile(String url){
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String fileName = URLEncoder.encode(url);
        File f = new File(FileUtils.getCacheDir(), filename);
        return f;       
    }
    
    public static File getLocalFile(String url){
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        //Another possible solution (thanks to grantland)
        //String fileName = URLEncoder.encode(url);
        File result = new File(FileUtils.getCacheDir(), filename);
        if(!result.exists()){
        	result = new File(url);
        }
        return result;       
    }
    
    public void clear(){
        File[] files=FileUtils.getCacheDir().listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }

}