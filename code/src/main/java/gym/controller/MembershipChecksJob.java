package gym.controller;

import gym.port.in.RunDailyChecksUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The Clock actor's driving adapter for UC13: runs the daily membership checks every morning, through the
 * same use case Postman reaches through {@link MembershipController}.
 */
@Component
public class MembershipChecksJob {

    private final RunDailyChecksUseCase checks;

    public MembershipChecksJob(RunDailyChecksUseCase checks) {
        this.checks = checks;
    }

    /**
     * Ends trials and freezes that are over, lapses unpaid memberships and cancels ones lapsed for 60 days.
     * Running it twice on one day changes nothing the second time.
     */
    @Scheduled(cron = "${gym.membership.checks-schedule}", zone = "${gym.billing.zone}")
    public void run() {
        checks.runDailyChecks();
    }
}
