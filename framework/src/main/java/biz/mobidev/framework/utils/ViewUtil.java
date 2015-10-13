package biz.mobidev.framework.utils;



import biz.mobidev.framework.adapters.holderadapter.IHolder;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class ViewUtil {
	public static ViewGroup inflate(int resours, Context context, ViewGroup layout)
	{	
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater vi = (LayoutInflater) context.getSystemService(inflater);
		vi.inflate(resours, layout, true);
		return layout;
	}
	public static ViewGroup inflate(Context context, IHolder holder)
	{
		return inflate(holder.getLayoutResorsID(), context, holder.getInflateView(context));
	}
}
