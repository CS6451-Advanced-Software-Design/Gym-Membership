package gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gym.domain.Membership;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MembershipChecksJobTest {

    @Test
    void runsTheDailyChecksOnEachTick() {
        AtomicInteger runs = new AtomicInteger();

        new MembershipChecksJob(() -> {
            runs.incrementAndGet();
            return List.<Membership>of();
        }).run();

        assertEquals(1, runs.get());
    }
}
