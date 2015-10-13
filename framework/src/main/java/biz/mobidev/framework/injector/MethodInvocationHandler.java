
package biz.mobidev.framework.injector;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Invocation handler for injected methods.
 * 
 * @author Prokofyev Roman
 * 
 */
public class MethodInvocationHandler implements InvocationHandler {

    private Object mWorkObject;

    private Method mTargetMethod;

    private Method mSourceMethod;

    /**
     * Constructs invokation handler. 
     * 
     * @param workObject Object object that contains fields or methods that should be injected
     * @param sourceMethod Method method to be called
     * @param targetMethod Method method of listener
     */
    public MethodInvocationHandler(Object workObject, Method sourceMethod, Method targetMethod) {
        this.mWorkObject = workObject;
        this.mTargetMethod = targetMethod;
        this.mSourceMethod = sourceMethod;
    }


    
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals(mTargetMethod.getName())) {
            mSourceMethod.setAccessible(true);
            return mSourceMethod.invoke(mWorkObject, args);
        }
        return null;
	}

}