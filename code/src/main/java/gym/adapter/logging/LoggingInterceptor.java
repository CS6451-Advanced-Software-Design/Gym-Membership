package gym.adapter.logging;

import gym.interceptor.UseCaseContext;
import gym.interceptor.UseCaseInterceptor;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete interceptor (POSA2): logging as an out-of-band service. It logs every use-case call,
 * its outcome and how long it took, without a single log statement in a controller or service.
 */
public class LoggingInterceptor implements UseCaseInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public void onBefore(UseCaseContext context) {
        LOGGER.info("call   {}.{}({})", context.useCase(), context.operation(),
                context.arguments().stream().map(String::valueOf).collect(Collectors.joining(", ")));
    }

    @Override
    public void onAfter(UseCaseContext context) {
        LOGGER.info("done   {}.{} in {} ms", context.useCase(), context.operation(), context.elapsed().toMillis());
    }

    @Override
    public void onFailure(UseCaseContext context) {
        LOGGER.warn("failed {}.{} in {} ms: {}", context.useCase(), context.operation(), context.elapsed().toMillis(),
                context.failure().map(Throwable::getMessage).orElse(""));
    }
}
