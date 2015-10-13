package biz.mobidev.framework.injector.viewini;

import java.lang.reflect.Field;

import biz.mobidev.framework.injector.anatation.ViewValueInjectAnatation;
import biz.mobidev.framework.utils.ContextView;

import android.view.View;


public class ViewValueInjector extends AbstractViewInjector<ViewValueInjectAnatation> {

	@Override
	public void doInjection(Field field, ContextView contextView, ViewValueInjectAnatation annotation) {
		 View view = initView(field, contextView, annotation.viewID());
		 setTextToView(view, annotation.value());
	}

}
