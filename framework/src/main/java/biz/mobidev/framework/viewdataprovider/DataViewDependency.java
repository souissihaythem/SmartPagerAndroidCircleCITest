package biz.mobidev.framework.viewdataprovider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.view.View;
import android.widget.TextView;

public class DataViewDependency {
	TextView view;
	Method method;
	public DataViewDependency(View view, Method method)
	{
		this.view = (TextView) view;
		this.method = method;
	}
	
	public void fillView(Object sours)
	{
		try {
			Object value = method.invoke(sours, new Object[] {});
			view.setText(value.toString());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
	}
	
	
	
}
