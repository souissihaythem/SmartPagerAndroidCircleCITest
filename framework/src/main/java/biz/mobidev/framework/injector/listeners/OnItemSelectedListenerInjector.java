package biz.mobidev.framework.injector.listeners;

import java.lang.reflect.Method;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import biz.mobidev.framework.injector.anatation.OnItemSelectedListenerInjectAnatation;
import biz.mobidev.framework.utils.ContextView;

@SuppressWarnings("unchecked")
public class OnItemSelectedListenerInjector extends AbstractMethodInjector<OnItemSelectedListenerInjectAnatation> {

	@Override
	public void doInjection(Object methodOwner, Method sourceMethod, ContextView contextView,
			OnItemSelectedListenerInjectAnatation annotation) {		 
		
        Method targetMethod = getMethod(OnItemSelectedListener.class, "onItemSelected", new Class<?>[] { AdapterView.class, View.class, int.class, long.class }, sourceMethod);
        OnItemSelectedListener onItemSelectedListener = createInvokationProxy(OnItemSelectedListener.class, methodOwner, sourceMethod, targetMethod);
        
		AdapterView<Adapter> adapterView= (AdapterView<Adapter>) getViewById(contextView.getRootView(), annotation.viewID());
        adapterView.setOnItemSelectedListener(onItemSelectedListener);
	}

}
