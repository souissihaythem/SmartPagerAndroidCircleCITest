package biz.mobidev.framework.injector.viewini;

import java.lang.reflect.Field;

import android.view.View;
import biz.mobidev.framework.injector.anatation.ViewInjectBackgraundAnatation;
import biz.mobidev.framework.utils.ContextView;

public class ViewBackgraundValueInjector extends AbstractViewInjector<ViewInjectBackgraundAnatation> {

	@Override
	public void doInjection(Field field, ContextView contextView, ViewInjectBackgraundAnatation annotation) {
		View view = initView(field, contextView, annotation.viewID());
		view.setBackgroundResource(annotation.imageID());

	}

}
