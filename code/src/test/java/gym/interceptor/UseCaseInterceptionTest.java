package gym.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.RuleViolationException;
import gym.port.in.PingUseCase;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

class UseCaseInterceptionTest {

    private final List<UseCaseContext> before = new ArrayList<>();
    private final List<UseCaseContext> after = new ArrayList<>();
    private final List<UseCaseContext> failures = new ArrayList<>();
    private final UseCaseDispatcher dispatcher = new UseCaseDispatcher();
    private PingUseCase ping;

    @BeforeEach
    void setUp() {
        dispatcher.register(new UseCaseInterceptor() {
            @Override
            public void onBefore(UseCaseContext context) {
                before.add(context);
            }

            @Override
            public void onAfter(UseCaseContext context) {
                after.add(context);
            }

            @Override
            public void onFailure(UseCaseContext context) {
                failures.add(context);
            }
        }, 0);
        StaticListableBeanFactory beans = new StaticListableBeanFactory(Map.of("dispatcher", dispatcher));
        UseCaseInterception interception = new UseCaseInterception(beans.getBeanProvider(UseCaseDispatcher.class));
        ping = (PingUseCase) interception.postProcessAfterInitialization(new PingUseCase.Service(), "ping");
    }

    @Test
    void onlyUseCasesAreWrapped() {
        StaticListableBeanFactory beans = new StaticListableBeanFactory(Map.of("dispatcher", dispatcher));
        UseCaseInterception interception = new UseCaseInterception(beans.getBeanProvider(UseCaseDispatcher.class));
        Object other = new Object();

        assertTrue(Proxy.isProxyClass(ping.getClass()));
        assertSame(other, interception.postProcessAfterInitialization(other, "other"));
    }

    @Test
    void aSuccessfulCallPassesBeforeAndAfter() {
        assertEquals("pong Ann", ping.ping("Ann"));

        UseCaseContext context = before.getFirst();
        assertSame(context, after.getFirst());
        assertEquals("PingUseCase", context.useCase());
        assertEquals("ping", context.operation());
        assertEquals(List.of("Ann"), context.arguments());
        assertEquals("pong Ann", context.result().orElseThrow());
        assertTrue(failures.isEmpty());
    }

    @Test
    void aFailedCallPassesOnFailureAndRethrowsTheOriginalException() {
        RuleViolationException thrown = assertThrows(RuleViolationException.class, () -> ping.ping("frozen"));

        UseCaseContext context = failures.getFirst();
        assertSame(thrown, context.failure().orElseThrow());
        assertFalse(context.result().isPresent());
        assertTrue(after.isEmpty());
    }

    @Test
    void anInterceptorCanStopTheCallBeforeItRuns() {
        dispatcher.register(new UseCaseInterceptor() {
            @Override
            public void onBefore(UseCaseContext context) {
                throw new RuleViolationException("Blocked");
            }
        }, 1);

        assertThrows(RuleViolationException.class, () -> ping.ping("Ann"));
        assertTrue(after.isEmpty());
    }

    @Test
    void contextAttributesCarryDataFromBeforeToAfter() {
        dispatcher.register(new UseCaseInterceptor() {
            @Override
            public void onBefore(UseCaseContext context) {
                context.attribute("ticket", 42);
            }
        }, 1);

        ping.ping("Ann");

        assertEquals(42, after.getFirst().attribute("ticket").orElseThrow());
    }
}
