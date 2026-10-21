package gym.adapter.logging;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.RuleViolationException;
import gym.interceptor.UseCaseDispatcher;
import gym.interceptor.UseCaseInterception;
import gym.port.in.PingUseCase;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
