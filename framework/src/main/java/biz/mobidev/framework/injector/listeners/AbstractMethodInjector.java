
package biz.mobidev.framework.injector.listeners;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import android.view.View;
import biz.mobidev.framework.injector.AbstractInjector;
import biz.mobidev.framework.injector.InjectorException;
import biz.mobidev.framework.injector.MethodInvocationHandler;
import biz.mobidev.framework.utils.ContextView;



/**
 * Injects event handler 
 * 
 * @author Prokofyev Roman
 * 
 * @param <A> type of corresponding annotation
 */
public abstract class AbstractMethodInjector<A extends Annotation> extends AbstractInjector<A> {

    /**
     * Injects event handler
     * @param methodOwner Objects object that contain method
     * @param injectionContext InjectionContext object to be used for retrieving information about injection context
     * @param sourceMethod method to be invoked as event handler
     * @param annotation T annotation fir providing data for injection
     */
    public abstract void doInjection(Object methodOwner,  Method sourceMethod,  ContextView contextView, A annotation);

    protected void checkIsViewAssignable(Class<? extends View> expectedClass, Class<? extends View> actualClass) {
        if (!expectedClass.isAssignableFrom(actualClass)) {
            String errorPattern = "Injecting is allowable only for view with type %s, but not for %s";
            throw new InjectorException(String.format(errorPattern, expectedClass.getName(), actualClass.getName()));
        }
    }

    protected void checkMethodSignature(Method sourceMethod, Method targetMethod) {
        Class<?>[] sourceMethodArgTypes = sourceMethod.getParameterTypes();
        Class<?>[] targetMethodArgTypes = targetMethod.getParameterTypes();

        if (!Arrays.equals(sourceMethodArgTypes, targetMethodArgTypes)) {
            throw new InjectorException(String.format("Method has incorrect parameters: %s. Expected: %s",
                    Arrays.toString(targetMethodArgTypes), Arrays.toString(sourceMethodArgTypes)));
        }
        if (!sourceMethod.getReturnType().equals(targetMethod.getReturnType())) {
            throw new InjectorException(String.format("Method has incorrect return type: %s. Expected: %s",
                    targetMethod.getReturnType(), sourceMethod.getReturnType()));
        }
    }

    protected Method getMethod(Class<?> methodOwner, String methodName, Class<?>[] argsTypes, Method sourceMethod) {
        String errorPattern = "Error getting method named '%s' from class %s";
        try {
            Method method = methodOwner.getMethod(methodName, argsTypes);
            checkMethodSignature(method, sourceMethod);
            return method;
        }
        catch (SecurityException e) {
            throw new IllegalArgumentException(String.format(errorPattern, methodName, methodOwner.getName()), e);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(String.format(errorPattern, methodName, methodOwner.getName()), e);
        }
    }

    @SuppressWarnings("unchecked")
	protected <H> H createInvokationProxy(Class<H> proxyClass, Object methodOwner, Method sourceMethod, Method targetMethod) {
        MethodInvocationHandler methodInvocationHandler = new MethodInvocationHandler(methodOwner, sourceMethod, targetMethod);
        ClassLoader classLoader = proxyClass.getClassLoader();
        return (H) Proxy.newProxyInstance(classLoader, new Class[] { proxyClass }, methodInvocationHandler);
    }
}
