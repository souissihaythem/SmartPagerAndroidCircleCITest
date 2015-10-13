package net.smartpager.android.view;


import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

public class ScrollingWebView extends WebView {
	
	public interface ScrollListener{
		public void onScrollChanged(float percent,int position,int max);
	}
	
	ScrollListener onScrollListener;
	
	public void setOnScrollListener(ScrollListener onScrollListener) {
		this.onScrollListener = onScrollListener;
	}
	
	public ScrollingWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		float contentHeight = getContentHeight() * getScale();
		int curScroll = t + getMeasuredHeight();
		if (onScrollListener!=null){
			onScrollListener.onScrollChanged(curScroll/contentHeight, curScroll, Math.round(contentHeight));
		}
	}
}
