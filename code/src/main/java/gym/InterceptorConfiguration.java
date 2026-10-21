package gym;

import gym.adapter.logging.LoggingInterceptor;
import gym.interceptor.UseCaseDispatcher;
import gym.interceptor.UseCaseInterception;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application side of the Interceptor pattern (POSA2 step 7): creates the concrete
 * interceptors and registers them with the dispatcher. A new out-of-band service is one more line here.
 */
@Configuration
public class InterceptorConfiguration {

    /**
     * Static, because a post-processor has to exist before the beans it wraps.
     */
    @Bean
    public static UseCaseInterception useCaseInterception(ObjectProvider<UseCaseDispatcher> dispatcher) {
        return new UseCaseInterception(dispatcher);
    }

    @Bean
    public UseCaseDispatcher useCaseDispatcher() {
        UseCaseDispatcher dispatcher = new UseCaseDispatcher();
        dispatcher.register(new LoggingInterceptor(), 0);
        return dispatcher;
    }
}
