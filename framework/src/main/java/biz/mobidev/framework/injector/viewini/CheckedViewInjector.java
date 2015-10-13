package biz.mobidev.framework.injector.viewini;

import java.lang.reflect.Field;

import android.view.View;
import biz.mobidev.framework.injector.anatation.CheckedViewInjectAnatation;
import biz.mobidev.framework.utils.ContextView;


public class CheckedViewInjector extends AbstractViewInjector<CheckedViewInjectAnatation> {

	@Override
	public void doInjection(Field field, ContextView contextView, CheckedViewInjectAnatation annotation) {
		View view = initView(field, contextView, annotation.viewID());
		setValueToCheckView(view, annotation.checked());
	}

}
