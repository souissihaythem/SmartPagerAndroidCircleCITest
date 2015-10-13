
package biz.mobidev.framework.injector;

import java.lang.annotation.Annotation;

import android.view.View;


/**
 * Injects field or method using data from annotation
 * 
 * @author Prokofyev Roman
 * 
 * @param <T> type of corresponding annotation
 */
public abstract class AbstractInjector<T extends Annotation> {

    protected View getViewById(View viewHolder, int viewId) {
        View view = viewHolder.findViewById(viewId);
        if (view == null) {
            String errorPattern = "There is no view with id 0x%s in view %s";
            throw new InjectorException(String.format(errorPattern, Integer.toHexString(viewId), viewHolder));
        }
        return view;
    }

}
