package gym.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

/**
 * The callback mechanism in the concrete framework (POSA2 step 6).
 *
 * <p>Every bean that implements a use-case interface from {@code gym.port.in} is wrapped in a
 * dynamic proxy. Controllers inject the interface, so they get the proxy, and every call passes
 * through the dispatcher's interception points. Services don't know they are intercepted.
 */
public class UseCaseInterception implements BeanPostProcessor {

    private static final String USE_CASE_PACKAGE = "gym.port.in";

    private final ObjectProvider<UseCaseDispatcher> dispatcher;

    /**
     * Takes a provider rather than the dispatcher itself, so creating this post-processor early
     * doesn't force the dispatcher to be created before the other post-processors are ready.
     */
    public UseCaseInterception(ObjectProvider<UseCaseDispatcher> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) {
        Class<?>[] useCases = Arrays.stream(ClassUtils.getAllInterfaces(bean))
                .filter(type -> type.getPackageName().equals(USE_CASE_PACKAGE))
                .toArray(Class<?>[]::new);
        if (useCases.length == 0) {
            return bean;
        }
        return Proxy.newProxyInstance(bean.getClass().getClassLoader(), useCases,
                (proxy, method, arguments) -> invoke(bean, method, arguments));
    }

    private Object invoke(Object target, Method method, Object[] arguments) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(target, arguments);
        }
        UseCaseDispatcher dispatcher = this.dispatcher.getObject();
        List<Object> values = arguments == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(Arrays.asList(arguments)));
        UseCaseContext context = new UseCaseContext(method.getDeclaringClass().getSimpleName(), method.getName(), values);

        dispatcher.onBefore(context);
        long start = System.nanoTime();
        try {
            Object result = method.invoke(target, arguments);
            context.succeeded(result, Duration.ofNanos(System.nanoTime() - start));
            dispatcher.onAfter(context);
            return result;
        } catch (InvocationTargetException exception) {
            Throwable failure = exception.getCause();
            context.failed(failure, Duration.ofNanos(System.nanoTime() - start));
            dispatcher.onFailure(context);
            throw failure;
        }
    }
}
