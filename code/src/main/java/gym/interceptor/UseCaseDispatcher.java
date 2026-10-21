package gym.interceptor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dispatcher (POSA2): keeps the registered concrete interceptors and calls them back
 * when the framework reaches an interception point.
 *
 * <p>It offers the framework the same interface its interceptors offer it, so the framework
 * only ever talks to one {@link UseCaseInterceptor}. Interceptors run in priority order, lowest first,
 * on the way in, and in reverse order on the way out, so the first one in is the last one out.
 */
public class UseCaseDispatcher implements UseCaseInterceptor {

    private record Registration(UseCaseInterceptor interceptor, int priority) {
    }

    private final List<Registration> registrations = new CopyOnWriteArrayList<>();

    /**
     * @param priority lower runs earlier before the call, and later after it
     */
    public void register(UseCaseInterceptor interceptor, int priority) {
        registrations.add(new Registration(interceptor, priority));
        registrations.sort(Comparator.comparingInt(Registration::priority));
    }

    public void remove(UseCaseInterceptor interceptor) {
        registrations.removeIf(registration -> registration.interceptor() == interceptor);
    }

    @Override
    public void onBefore(UseCaseContext context) {
        inOrder().forEach(interceptor -> interceptor.onBefore(context));
    }

    @Override
    public void onAfter(UseCaseContext context) {
        inOrder().reversed().forEach(interceptor -> interceptor.onAfter(context));
    }

    @Override
    public void onFailure(UseCaseContext context) {
        inOrder().reversed().forEach(interceptor -> interceptor.onFailure(context));
    }

    /**
     * A snapshot, so an interceptor registered or removed mid-call doesn't affect that call.
     */
    private List<UseCaseInterceptor> inOrder() {
        List<UseCaseInterceptor> interceptors = new ArrayList<>();
        registrations.forEach(registration -> interceptors.add(registration.interceptor()));
        return interceptors;
    }
}
