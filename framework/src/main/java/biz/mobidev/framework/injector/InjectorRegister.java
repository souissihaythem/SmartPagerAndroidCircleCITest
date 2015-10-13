package biz.mobidev.framework.injector;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import biz.mobidev.framework.injector.anatation.CheckedViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.CheckedViewValueInjectAnatation;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ImageViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.OnItemClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.OnItemSelectedListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewFontInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectBackgraundAnatation;
import biz.mobidev.framework.injector.anatation.ViewValueInjectAnatation;
import biz.mobidev.framework.injector.listeners.AbstractMethodInjector;
import biz.mobidev.framework.injector.listeners.OnClickListenerInjector;
import biz.mobidev.framework.injector.listeners.OnItemClickListenerInjector;
import biz.mobidev.framework.injector.listeners.OnItemSelectedListenerInjector;
import biz.mobidev.framework.injector.viewini.AbstractViewInjector;
import biz.mobidev.framework.injector.viewini.CheckedViewInjector;
import biz.mobidev.framework.injector.viewini.CheckedViewValueInjector;
import biz.mobidev.framework.injector.viewini.ImageViewValueInjector;
import biz.mobidev.framework.injector.viewini.ViewBackgraundValueInjector;
import biz.mobidev.framework.injector.viewini.ViewFontInjector;
import biz.mobidev.framework.injector.viewini.ViewInjector;
import biz.mobidev.framework.injector.viewini.ViewValueInjector;

@SuppressWarnings("unchecked")
public class InjectorRegister {

	private static final Map<Class<? extends Annotation>, AbstractViewInjector<?>> REGISTER_FIELD;
	private static final Map<Class<? extends Annotation>, AbstractMethodInjector<?>> REGISTER_METHOD;
	
    static {
       
    	REGISTER_FIELD 	= new HashMap<Class<? extends Annotation>, AbstractViewInjector<?>>();
    	REGISTER_METHOD = new HashMap<Class<? extends Annotation>, AbstractMethodInjector<?>>();

    	REGISTER_FIELD.put( ViewInjectAnatation.class, new ViewInjector());
    	REGISTER_FIELD.put( ViewValueInjectAnatation.class, new ViewValueInjector());
    	REGISTER_FIELD.put(CheckedViewValueInjectAnatation.class, new CheckedViewValueInjector());
    	REGISTER_FIELD.put(CheckedViewInjectAnatation.class, new CheckedViewInjector());
    	REGISTER_FIELD.put(ViewFontInjectAnatation.class, new ViewFontInjector());
    	REGISTER_FIELD.put(ImageViewInjectAnatation.class, new ImageViewValueInjector());
    	REGISTER_FIELD.put(ViewInjectBackgraundAnatation.class, new ViewBackgraundValueInjector());
    	
    	REGISTER_METHOD.put(ClickListenerInjectAnatation.class, new OnClickListenerInjector());
    	REGISTER_METHOD.put(OnItemSelectedListenerInjectAnatation.class, new OnItemSelectedListenerInjector());
    	REGISTER_METHOD.put(OnItemClickListenerInjectAnatation.class, new OnItemClickListenerInjector());
    	
    }
    
    public static boolean containsField(Class<? extends Annotation> annotationClass) {
        return REGISTER_FIELD.containsKey(annotationClass);
    }

    public static boolean containsMethod(Class<? extends Annotation> annotationClass) {
        return REGISTER_METHOD.containsKey(annotationClass);
    }

    
	public static AbstractViewInjector<Annotation> getFieldInjector(Class<? extends Annotation> annotationClass) {
        return (AbstractViewInjector<Annotation>) REGISTER_FIELD.get(annotationClass);
    }
    
    public static AbstractMethodInjector<Annotation> getMethodInjector(Class<? extends Annotation> annotationClass) {
        return (AbstractMethodInjector<Annotation>) REGISTER_METHOD.get(annotationClass);
    }
  
}
