package gym.adapter.logging;

import gym.interceptor.UseCaseContext;
import gym.interceptor.UseCaseInterceptor;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

/**
 * Concrete interceptor (POSA2): logging as an out-of-band service. It logs every use-case call,
 * its outcome and how long it took, without a single log statement in a controller or service.
 *
 * <p>Each line also carries the use case, operation, outcome and duration as key-value pairs. The readable
 * console format leaves them out, and JSON logs (as on Railway) turn them into fields that can be filtered on.
 */
public class LoggingInterceptor implements UseCaseInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public void onBefore(UseCaseContext context) {
        String arguments = context.arguments().stream().map(String::valueOf).collect(Collectors.joining(", "));
        describe(LOGGER.atInfo(), context, Outcome.CALLED)
                .log("call   {}.{}({})", context.useCase(), context.operation(), arguments);
    }

    @Override
    public void onAfter(UseCaseContext context) {
        describe(LOGGER.atInfo(), context, Outcome.DONE)
                .addKeyValue("durationMs", context.elapsed().toMillis())
                .log("done   {}.{} in {} ms", context.useCase(), context.operation(), context.elapsed().toMillis());
    }

    @Override
    public void onFailure(UseCaseContext context) {
        String reason = context.failure().map(Throwable::getMessage).orElse("");
        describe(LOGGER.atWarn(), context, Outcome.FAILED)
                .addKeyValue("durationMs", context.elapsed().toMillis())
                .addKeyValue("reason", reason)
                .log("failed {}.{} in {} ms: {}", context.useCase(), context.operation(), context.elapsed().toMillis(),
                        reason);
    }

    private static LoggingEventBuilder describe(LoggingEventBuilder event, UseCaseContext context, Outcome outcome) {
        return event
                .addKeyValue("useCase", context.useCase())
                .addKeyValue("operation", context.operation())
                .addKeyValue("outcome", outcome.value());
    }
}
