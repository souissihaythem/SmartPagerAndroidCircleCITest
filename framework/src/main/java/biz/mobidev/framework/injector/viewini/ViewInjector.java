package biz.mobidev.framework.injector.viewini;

import java.lang.reflect.Field;

import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.utils.ContextView;


public class ViewInjector extends AbstractViewInjector<ViewInjectAnatation> {

	@Override
	public void doInjection(Field field, ContextView contextView, ViewInjectAnatation annotation) {
		
		initView(field, contextView, annotation.viewID());
	}
	
	

}
