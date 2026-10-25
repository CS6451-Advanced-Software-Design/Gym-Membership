package gym.controller;

import gym.port.in.RunMonthlyBillingUseCase;
import java.time.Clock;
import java.time.YearMonth;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The Clock actor's driving adapter for UC7: starts the monthly billing run on the first of every month,
 * through the same use case Postman reaches through {@link BillingController}.
 */
@Component
public class BillingJob {

    private final RunMonthlyBillingUseCase billing;
    private final Clock clock;

    public BillingJob(RunMonthlyBillingUseCase billing, Clock clock) {
        this.billing = billing;
        this.clock = clock;
    }

    /**
     * Bills the month that has just started. A run already done that month bills nobody twice.
     */
    @Scheduled(cron = "${gym.billing.schedule}", zone = "${gym.billing.zone}")
    public void run() {
        billing.run(YearMonth.now(clock));
    }
}
