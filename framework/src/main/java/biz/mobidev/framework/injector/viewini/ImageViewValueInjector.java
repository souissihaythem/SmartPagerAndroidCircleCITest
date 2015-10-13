package biz.mobidev.framework.injector.viewini;

import java.lang.reflect.Field;

import android.view.View;
import android.widget.ImageView;
import biz.mobidev.framework.injector.anatation.ImageViewInjectAnatation;
import biz.mobidev.framework.utils.ContextView;


public class ImageViewValueInjector extends AbstractViewInjector<ImageViewInjectAnatation> {

	@Override
	public void doInjection(Field field, ContextView contextView, ImageViewInjectAnatation annotation) {
		 View view = initView(field, contextView, annotation.viewID());
		  ((ImageView)view).setImageResource(annotation.imageID());
		
	}

}
