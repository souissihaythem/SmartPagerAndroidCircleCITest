package biz.mobidev.framework.viewdataprovider;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import biz.mobidev.framework.utils.ExString;
import biz.mobidev.framework.utils.Log;
import biz.mobidev.framework.validate.Validator;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewDataProvider {
	
	
	
	public static void  save(Bundle outState, View parent, Object data) {
		getData(parent, data);
		outState.putSerializable(data.getClass().getName(), (Serializable) data);
	}
	
	public static void  Restore(Bundle savedInstanceState, View parent, Object data) {
		data=savedInstanceState.getSerializable(data.getClass().getName());
		setData(parent, data);	
	}
	
	public static void setData(View parent, Object data) {
		if (ViewGroup.class.isInstance(parent)) {
			setDataToViewGroup((ViewGroup) parent, data);
		} else {
			setDataToView(parent, data);
		}
	}

	public static ArrayList<DataViewDependency> findMethodList(View parent, Object data, String patern,
			ArrayList<DataViewDependency> dependencies) {
		if (ViewGroup.class.isInstance(parent)) {
			ViewGroup parentGroup = (ViewGroup) parent;
			for (int i = 0; i != parentGroup.getChildCount(); i++) {
				dependencies = findMethodList(parentGroup.getChildAt(i), data, patern, dependencies);
			}
		} else {
			if (Validator.notNull(parent.getTag())) {
				String methodName = patern +  getMemberName(parent.getTag().toString());
				try {
					Method method = data.getClass().getMethod(methodName);
					dependencies.add(new DataViewDependency(parent, method));
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					Log.e("Method "+methodName + " not found");
				}
			}
		}
		return dependencies;
	}

	private static String getMemberName(String mamberName) {
		if(mamberName.toCharArray()[0]=='m'){
			mamberName = mamberName.substring(1);
		}else{
			mamberName = ExString.upFirstChar(mamberName);;
		}
		return mamberName;
	}

	public static ArrayList<DataViewDependency> findGetMethodList(View parent, Object data) {
		ArrayList<DataViewDependency> dependencies = new ArrayList<DataViewDependency>();
		dependencies = findMethodList(parent, data, "get", dependencies);
		return dependencies;
	}

	public static void getData(View parent, Object data) {
		if (ViewGroup.class.isInstance(parent)) {
			getDataFromViewGroup((ViewGroup) parent, data);
		} else {
			getDataFromView(parent, data);
		}
	}

	private static void setDataToView(View parent, Object data) {
		if (TextView.class.isInstance(parent)) {
			if (Validator.notNull(parent.getTag())) {
				String methodName = "get" + getMemberName(parent.getTag().toString());
				try {
					Method method = data.getClass().getMethod(methodName);
					Object result = method.invoke(data, new Object[] {});
					if (Validator.notNull(result)) {
						((TextView) parent).setText(result.toString());
					}
				} catch (SecurityException e) {
					Log.e("Method "+methodName + " have not publick access");
				} catch (NoSuchMethodException e) {
					Log.e("Method "+methodName + " not found");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					Log.e("Method "+methodName + " have not publick access");
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void getDataFromView(View parent, Object data) {
		if (TextView.class.isInstance(parent)) {
			if (Validator.notNull(parent.getTag())) {
				String fieldName = parent.getTag().toString();
				String methodName = "set" + ExString.upFirstChar(fieldName);
				try {
					Field field = data.getClass().getDeclaredField(fieldName);
					Class<?> fieldClass = field.getType();
					Object value = null;
					if(fieldClass.getSuperclass().equals(Number.class))
					{
						value = field.getType().getDeclaredMethod("valueOf", String.class)
								.invoke(field.getType(), ((TextView)parent).getText());
					}
					else{
						value = ((TextView)parent).getText()+"test";
					}
					Method method = data.getClass().getMethod(methodName, fieldClass);
					
					method.invoke(data, new Object[] {value});
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					Log.e("Field "+fieldName + " not found");
				} catch (NoSuchMethodException e) {
					Log.e("Method "+methodName + " not found");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void getDataFromViewGroup(ViewGroup parent, Object data) {
		int count = parent.getChildCount();
		for (int i = 0; i != count; i++) {
			getData(parent.getChildAt(i), data);
		}
	}
	
	private static void setDataToViewGroup(ViewGroup parent, Object data) {
		int count = parent.getChildCount();
		for (int i = 0; i != count; i++) {
			setData(parent.getChildAt(i), data);
		}
	}
}
