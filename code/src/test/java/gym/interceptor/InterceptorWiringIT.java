package gym.interceptor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.port.in.PingUseCase;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Checks Spring wraps use-case beans automatically, so controllers get the intercepted proxy.
 */
@SpringBootTest
class InterceptorWiringIT {

    @TestConfiguration
    static class Configuration {

        @Bean
        PingUseCase pingUseCase() {
            return new PingUseCase.Service();
        }
    }

    @Autowired
    PingUseCase ping;

    @Test
    void injectedUseCasesAreIntercepted() {
        assertTrue(Proxy.isProxyClass(ping.getClass()));
    }
}
