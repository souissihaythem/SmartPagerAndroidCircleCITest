
package biz.mobidev.framework.injector.listeners;

import java.lang.reflect.Method;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import biz.mobidev.framework.injector.anatation.OnItemClickListenerInjectAnatation;
import biz.mobidev.framework.utils.ContextView;



/**
 * Injects {@link View.OnClickListener#onClick(View)} method
 * 
 * @author Prokofyev Roman
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class OnItemClickListenerInjector extends AbstractMethodInjector<OnItemClickListenerInjectAnatation> {

    @Override
    public void doInjection(Object methodOwner, Method sourceMethod, ContextView contextView, OnItemClickListenerInjectAnatation annotation) {
    															
        Method targetMethod = getMethod(OnItemClickListener.class, "onItemClick", new Class<?>[] { AdapterView.class, View.class, int.class, long.class }, sourceMethod);
        OnItemClickListener onItemClickListener = createInvokationProxy(OnItemClickListener.class, methodOwner, sourceMethod, targetMethod);

		AdapterView adapterView= (AdapterView<Adapter>)getViewById(contextView.getRootView(), annotation.viewID());
        adapterView.setOnItemClickListener(onItemClickListener);
    }

}
