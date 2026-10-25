package gym.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * A clock a test can move forward, e.g. to the day a trial ends.
 */
public class TestClock extends Clock {

    private Instant now;

    public TestClock(LocalDate today) {
        set(today);
    }

    /**
     * Pre: none. Post: it is 09:00 UTC on {@code today}.
     */
    public void set(LocalDate today) {
        now = today.atTime(9, 0).toInstant(ZoneOffset.UTC);
    }

    public void advanceDays(long days) {
        now = now.plusSeconds(days * 24 * 60 * 60);
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now;
    }
}
