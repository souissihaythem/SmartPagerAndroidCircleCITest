package biz.mobidev.framework.adapters.holderadapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

public interface IHolder {
	public IHolder createHolder(View view);
	public void setData(Object source);
	public void setListeners(Object... listeners);
	public ViewGroup getInflateView(Context context);
	public int getLayoutResorsID();
	
	public void preSetData(Bundle params);
	
}
