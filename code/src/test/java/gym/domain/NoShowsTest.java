package gym.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class NoShowsTest {

    private static final LocalDateTime FIRST = LocalDateTime.of(2026, 10, 1, 18, 0);

    @Test
    void twoNoShowsDoNotSuspendBooking() {
        NoShows noShows = new NoShows(List.of(FIRST, FIRST.plusDays(1)));

        assertFalse(noShows.suspendBookingAt(FIRST.plusDays(2)));
    }

    @Test
    void threeNoShowsInThirtyDaysSuspendBooking() {
        NoShows noShows = new NoShows(List.of(FIRST, FIRST.plusDays(10), FIRST.plusDays(20)));

        assertTrue(noShows.suspendBookingAt(FIRST.plusDays(21)));
    }

    @Test
    void theThirtyDaysIncludeTheLastDay() {
        NoShows noShows = new NoShows(List.of(FIRST, FIRST.plusDays(10), FIRST.plusDays(30)));

        assertTrue(noShows.suspendBookingAt(FIRST.plusDays(31)));
    }

    @Test
    void threeNoShowsSpreadOverMoreThanThirtyDaysDoNotSuspendBooking() {
        NoShows noShows = new NoShows(List.of(FIRST, FIRST.plusDays(10), FIRST.plusDays(31)));

        assertFalse(noShows.suspendBookingAt(FIRST.plusDays(32)));
    }

    @Test
    void theSuspensionEndsSevenDaysAfterTheThirdNoShow() {
        LocalDateTime third = FIRST.plusDays(20);
        NoShows noShows = new NoShows(List.of(FIRST, FIRST.plusDays(10), third));

        assertTrue(noShows.suspendBookingAt(third.plusDays(7).minusMinutes(1)));
        assertFalse(noShows.suspendBookingAt(third.plusDays(7)));
    }

    @Test
    void theOrderTheyAreGivenInDoesNotMatter() {
        NoShows noShows = new NoShows(List.of(FIRST.plusDays(20), FIRST, FIRST.plusDays(10)));

        assertTrue(noShows.suspendBookingAt(FIRST.plusDays(21)));
    }

    @Test
    void aLaterRunOfThreeCounts() {
        // The first three are too spread out, but the last three fit inside 30 days
        NoShows noShows = new NoShows(List.of(FIRST, FIRST.plusDays(40), FIRST.plusDays(50), FIRST.plusDays(60)));

        assertTrue(noShows.suspendBookingAt(FIRST.plusDays(61)));
    }
}
