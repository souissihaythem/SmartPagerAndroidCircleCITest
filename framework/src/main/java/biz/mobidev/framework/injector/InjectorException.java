
package biz.mobidev.framework.injector;

/**
 * Notify about any injection problem
 * 
 * @author Prokofyev Roman
 * 
 */
public class InjectorException extends RuntimeException {

    private static final long serialVersionUID = -1L;

    public InjectorException(String message) {
        super(message);
    }

    public InjectorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InjectorException(Throwable cause) {
        super(cause);
    }
}
