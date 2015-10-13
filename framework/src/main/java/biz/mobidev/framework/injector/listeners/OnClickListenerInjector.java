
package biz.mobidev.framework.injector.listeners;

import java.lang.reflect.Method;

import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.utils.ContextView;


import android.view.View;
import android.view.View.OnClickListener;



/**
 * Injects {@link View.OnClickListener#onClick(View)} method
 * 
 * @author Prokofyev Roman
 * 
 */
public class OnClickListenerInjector extends AbstractMethodInjector<ClickListenerInjectAnatation> {

    @Override
    public void doInjection(Object methodOwner, Method sourceMethod, ContextView contextView, ClickListenerInjectAnatation annotation) {
        Method targetMethod = getMethod(OnClickListener.class, "onClick", new Class<?>[] { View.class }, sourceMethod);
        OnClickListener onClickListener = createInvokationProxy(OnClickListener.class, methodOwner, sourceMethod, targetMethod);

        View view = getViewById(contextView.getRootView(), annotation.viewID());
        view.setOnClickListener(onClickListener);
    }

}
