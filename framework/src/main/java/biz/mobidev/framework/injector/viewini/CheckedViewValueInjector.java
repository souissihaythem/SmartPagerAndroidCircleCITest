package biz.mobidev.framework.injector.viewini;

import java.lang.reflect.Field;

import biz.mobidev.framework.injector.anatation.CheckedViewValueInjectAnatation;
import biz.mobidev.framework.utils.ContextView;

import android.view.View;


public class CheckedViewValueInjector extends AbstractViewInjector<CheckedViewValueInjectAnatation> {

	@Override
	public void doInjection(Field field, ContextView contextView, CheckedViewValueInjectAnatation annotation) {
		View view = initView(field, contextView, annotation.viewID());
		setValueToCheckView(view, annotation.checked());
		setTextToView(view, annotation.value());
	}

}
