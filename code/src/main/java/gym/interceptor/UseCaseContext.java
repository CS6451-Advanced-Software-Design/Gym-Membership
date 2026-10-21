package gym.interceptor;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Context object (POSA2 Interceptor): the handle a concrete interceptor uses to query
 * one use-case call, and to pass its own data from one interception point to the next.
 *
 * <p>Reader set: the use case, operation, arguments, and after the call its result or failure and elapsed time.
 * Writer set: attributes, so an interceptor can store something in {@code onBefore} and read it back in
 * {@code onAfter}. The call itself can't be changed, so a faulty interceptor can't corrupt business data.
 */
public final class UseCaseContext {

    private final String useCase;
    private final String operation;
    private final List<Object> arguments;
    private final Map<String, Object> attributes = new HashMap<>();
    private Object result;
    private Throwable failure;
    private Duration elapsed;

    UseCaseContext(String useCase, String operation, List<Object> arguments) {
        this.useCase = useCase;
        this.operation = operation;
        this.arguments = arguments;
    }

    /**
     * @return the use-case interface, e.g. {@code BookClassUseCase}
     */
    public String useCase() {
        return useCase;
    }

    /**
     * @return the method called on the use case, e.g. {@code book}
     */
    public String operation() {
        return operation;
    }

    /**
     * @return the call's arguments, read only
     */
    public List<Object> arguments() {
        return arguments;
    }

    /**
     * @return what the use case returned, empty before the call, on failure or for a void method
     */
    public Optional<Object> result() {
        return Optional.ofNullable(result);
    }

    /**
     * @return what the use case threw, empty unless the call failed
     */
    public Optional<Throwable> failure() {
        return Optional.ofNullable(failure);
    }

    /**
     * @return how long the use case took, or {@link Duration#ZERO} before the call
     */
    public Duration elapsed() {
        return elapsed == null ? Duration.ZERO : elapsed;
    }

    public Optional<Object> attribute(String name) {
        return Optional.ofNullable(attributes.get(name));
    }

    public void attribute(String name, Object value) {
        attributes.put(name, value);
    }

    void succeeded(Object result, Duration elapsed) {
        this.result = result;
        this.elapsed = elapsed;
    }

    void failed(Throwable failure, Duration elapsed) {
        this.failure = failure;
        this.elapsed = elapsed;
    }
}
