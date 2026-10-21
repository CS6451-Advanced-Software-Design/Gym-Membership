package gym.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UseCaseDispatcherTest {

    private final List<String> calls = new ArrayList<>();
    private final UseCaseDispatcher dispatcher = new UseCaseDispatcher();
    private final UseCaseContext context = new UseCaseContext("PingUseCase", "ping", List.of());

    private UseCaseInterceptor recording(String name) {
        return new UseCaseInterceptor() {
            @Override
            public void onBefore(UseCaseContext context) {
                calls.add(name + ".before");
            }

            @Override
            public void onAfter(UseCaseContext context) {
                calls.add(name + ".after");
            }

            @Override
            public void onFailure(UseCaseContext context) {
                calls.add(name + ".failure");
            }
        };
    }

    @Test
    void firstInIsLastOut() {
        dispatcher.register(recording("logging"), 10);
        dispatcher.register(recording("security"), 0);

        dispatcher.onBefore(context);
        dispatcher.onAfter(context);

        assertEquals(List.of("security.before", "logging.before", "logging.after", "security.after"), calls);
    }

    @Test
    void failuresUnwindInReverseOrder() {
        dispatcher.register(recording("security"), 0);
        dispatcher.register(recording("logging"), 10);

        dispatcher.onFailure(context);

        assertEquals(List.of("logging.failure", "security.failure"), calls);
    }

    @Test
    void removedInterceptorsAreNoLongerCalled() {
        UseCaseInterceptor logging = recording("logging");
        dispatcher.register(logging, 0);
        dispatcher.remove(logging);

        dispatcher.onBefore(context);

        assertEquals(List.of(), calls);
    }
}
