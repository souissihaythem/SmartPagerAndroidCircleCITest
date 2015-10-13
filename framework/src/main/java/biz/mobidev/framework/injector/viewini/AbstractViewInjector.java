package biz.mobidev.framework.injector.viewini;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import android.graphics.Typeface;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import biz.mobidev.framework.injector.AbstractInjector;
import biz.mobidev.framework.utils.ContextView;
import biz.mobidev.framework.utils.Log;


public abstract class AbstractViewInjector<A extends Annotation> extends AbstractInjector<A> {

	 public abstract void doInjection(Field field, ContextView contextView, A annotation);
	 
	 public View initView(Field field, ContextView contextView, int id){
			View view = null;
			try {
				view = contextView.getRootView().findViewById(id);
				view.setVisibility(View.VISIBLE);
				field.setAccessible(true);
				field.set(contextView.getTargetObject(), view);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			return view;
	 }
	 
	 public void setTextToView(View view, String value)
	 {
			if(TextView.class.isInstance(view)){
				((TextView)view).setText(value);
			}
			else
			{
				Log.e("Class "+view.getClass().getName()+" is not instantce of TextView");
			}
	 }
	 
	 public void setValueToCheckView(View view, boolean check){
			if(CompoundButton.class.isInstance(view)){
				((CompoundButton)view).setChecked(check);			
				}
			else
			{
				Log.e("Class "+view.getClass().getName()+" is not instantce of CompoundButton");
			}
	 }
	 
	 public void setFontToView(TextView textView, String fontName){
			Typeface font = Typeface.createFromAsset(textView.getContext().getAssets(), fontName);  
			textView.setTypeface(font);
	 }
}
