package biz.mobidev.framework.injector.viewini;

import java.lang.reflect.Field;


import android.view.View;
import android.widget.TextView;
import biz.mobidev.framework.injector.anatation.ViewFontInjectAnatation;
import biz.mobidev.framework.utils.ContextView;

public class ViewFontInjector extends AbstractViewInjector<ViewFontInjectAnatation> {

	@Override
	public void doInjection(Field field, ContextView contextView, ViewFontInjectAnatation annotation) {
		View view = initView(field, contextView, annotation.viewID());
		if(view instanceof TextView){
			setFontToView((TextView) view, annotation.fontName());
		}
	}

}
