package gym.interceptor;

/**
 * Interceptor (POSA2): the callback interface for the use-case interception group.
 * One hook method per interception point. A concrete interceptor overrides only the hooks it needs.
 *
 * <pre>
 *   Idle --call--> [onBefore] --> Running --returns--> [onAfter]   --> Idle
 *                                         \--throws--> [onFailure] --> Idle
 * </pre>
 */
public interface UseCaseInterceptor {

    /**
     * Called before the use case runs. Throwing here stops the call.
     */
    default void onBefore(UseCaseContext context) {
    }

    /**
     * Called after the use case returns. {@link UseCaseContext#result()} holds what it returned.
     */
    default void onAfter(UseCaseContext context) {
    }

    /**
     * Called after the use case throws. {@link UseCaseContext#failure()} holds the exception,
     * which is rethrown to the caller once every interceptor has seen it.
     */
    default void onFailure(UseCaseContext context) {
    }
}
