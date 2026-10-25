package gym.adapter.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import gym.domain.RuleViolationException;
import gym.interceptor.UseCaseDispatcher;
import gym.interceptor.UseCaseInterception;
import gym.port.in.PingUseCase;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/**
 * Proves the interceptor works by logging as an out-of-band service (Week 7 lecture, Interceptor Part 2):
 * the use case itself contains no logging.
 */
@ExtendWith(OutputCaptureExtension.class)
class LoggingInterceptorTest {

    private PingUseCase ping;

    @BeforeEach
    void setUp() {
        UseCaseDispatcher dispatcher = new UseCaseDispatcher();
        dispatcher.register(new LoggingInterceptor(), 0);
        UseCaseInterception interception = new UseCaseInterception(
                new StaticListableBeanFactory(Map.of("dispatcher", dispatcher)).getBeanProvider(UseCaseDispatcher.class));
        ping = (PingUseCase) interception.postProcessAfterInitialization(new PingUseCase.Service(), "ping");
    }

    @Test
    void logsTheCallAndItsOutcome(CapturedOutput output) {
        ping.ping("Ann");

        assertTrue(output.getOut().contains("call   PingUseCase.ping(Ann)"));
        assertTrue(output.getOut().matches("(?s).*done   PingUseCase\\.ping in \\d+ ms.*"));
    }

    @Test
    void logsTheFailureReason(CapturedOutput output) {
        assertThrows(RuleViolationException.class, () -> ping.ping("frozen"));

        assertTrue(output.getOut().matches("(?s).*failed PingUseCase\\.ping in \\d+ ms: Membership is frozen.*"));
    }

    @Test
    void attachesFieldsForJsonLogs() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingInterceptor.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            assertThrows(RuleViolationException.class, () -> ping.ping("frozen"));
        } finally {
            logger.detachAppender(appender);
        }

        Map<String, Object> failure = fields(appender.list.getLast());
        assertEquals("PingUseCase", failure.get("useCase"));
        assertEquals("ping", failure.get("operation"));
        assertEquals("failed", failure.get("outcome"));
        assertEquals("Membership is frozen", failure.get("reason"));
        assertTrue(failure.get("durationMs") instanceof Long);
    }

    private static Map<String, Object> fields(ILoggingEvent event) {
        return event.getKeyValuePairs().stream().collect(Collectors.toMap(pair -> pair.key, pair -> pair.value));
    }
}
