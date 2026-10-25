package gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gym.domain.Invoice;
import gym.port.in.RunMonthlyBillingUseCase;
import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BillingJobTest {

    @Test
    void billsTheMonthThatHasJustStarted() {
        List<YearMonth> periods = new ArrayList<>();
        RunMonthlyBillingUseCase billing = period -> {
            periods.add(period);
            return List.<Invoice>of();
        };
        Clock clock = Clock.fixed(Instant.parse("2026-11-01T06:00:00Z"), ZoneId.of("Europe/Dublin"));

        new BillingJob(billing, clock).run();

        assertEquals(List.of(YearMonth.of(2026, 11)), periods);
    }
}
