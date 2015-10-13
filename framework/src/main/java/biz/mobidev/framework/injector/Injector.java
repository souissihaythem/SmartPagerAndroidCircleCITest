package biz.mobidev.framework.injector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import biz.mobidev.framework.injector.anatation.CheckedViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.CheckedViewValueInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewValueInjectAnatation;
import biz.mobidev.framework.injector.listeners.AbstractMethodInjector;
import biz.mobidev.framework.injector.viewini.AbstractViewInjector;
import biz.mobidev.framework.utils.ContextView;
import biz.mobidev.framework.validate.Validator;

@SuppressWarnings("unused")
public class Injector {

	public static void doInjection(Activity activity) {
		ContextView contextView = new ContextView(activity);
		doInjection(contextView);
	}

	public static void doInjection(Dialog dialog) {
		ContextView contextView = new ContextView(dialog);
		doInjection(contextView);
	}

	public static void doInjection(ContextView contextView) {
		doInjectFilds(contextView);
		doInjectMethods(contextView);
	}

	private static void doInjectFilds(ContextView contextView) {
		Class<?> windowClass = contextView.getTargetObject().getClass();
		do {
			Field[] declaredFields = windowClass.getDeclaredFields();
			for (Field field : declaredFields) {
				/*
				 * if (field.getAnnotations().length > 0) { if
				 * (field.isAnnotationPresent(ViewValueInjectAnatation.class)) { initViewWithValue(activity, field);
				 * continue; } if(field.isAnnotationPresent(ViewInjectAnatation.class)) { initView(activity, field);
				 * continue; } if(field.isAnnotationPresent(CheckedViewInjectAnatation.class)) { initCheckView(activity,
				 * field); continue; } if(field.isAnnotationPresent(CheckedViewValueInjectAnatation.class)) {
				 * initCheckViewWithValue(activity, field); continue; } }
				 */
				Annotation[] annotations = field.getAnnotations();
				for (Annotation annotation : annotations) {
					Class<? extends Annotation> annotationClass = annotation.annotationType();
					if (InjectorRegister.containsField(annotationClass)) {
						AbstractViewInjector<Annotation> injector = InjectorRegister.getFieldInjector(annotationClass);
						injector.doInjection(field, contextView, annotation);
					}
				}
			}
			windowClass = windowClass.getSuperclass();
		} while (windowClass != Activity.class && windowClass != Dialog.class && windowClass != FragmentActivity.class);

	}

	private static void doInjectMethods(ContextView contextView) {
		Method[] methods = contextView.getTargetObject().getClass().getMethods();
		for (Method method : methods) {
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				Class<? extends Annotation> annotationClass = annotation.annotationType();
				if (InjectorRegister.containsMethod(annotationClass)) {
					AbstractMethodInjector<Annotation> injector = InjectorRegister.getMethodInjector(annotationClass);
					injector.doInjection(contextView.getTargetObject(), method, contextView, annotation);
				}
			}
		}
	}

	private static void initViewWithValue(Activity activity, Field field) {
		TextView view = (TextView) initView(activity, field, field.getAnnotation(ViewValueInjectAnatation.class)
				.viewID());
		if (Validator.notNull(view)) {
			String value = field.getAnnotation(ViewValueInjectAnatation.class).value();
			view.setText(value);
		}
	}

	private static CompoundButton initCheckView(Activity activity, Field field, int id, boolean checked) {
		CompoundButton view = (CompoundButton) initView(activity, field, id);
		if (Validator.notNull(view)) {
			view.setChecked(checked);
		}
		return view;
	}

	private static CompoundButton initCheckView(Activity activity, Field field) {
		return initCheckView(activity, field, field.getAnnotation(CheckedViewInjectAnatation.class).viewID(), field
				.getAnnotation(CheckedViewInjectAnatation.class).checked());
	}

	private static View initCheckViewWithValue(Activity activity, Field field) {
		CompoundButton view = initCheckView(activity, field, field.getAnnotation(CheckedViewValueInjectAnatation.class)
				.viewID(), field.getAnnotation(CheckedViewValueInjectAnatation.class).checked());
		if (Validator.notNull(view)) {
			view.setText(field.getAnnotation(CheckedViewValueInjectAnatation.class).value());
		}
		return view;
	}

	private static View initView(Activity activity, Field field) {
		return initView(activity, field, field.getAnnotation(ViewInjectAnatation.class).viewID());
	}

	private static View initView(Activity activity, Field field, int id) {
		View view = null;
		try {
			view = activity.findViewById(id);
			field.setAccessible(true);
			field.set(activity, view);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return view;
	}
}
