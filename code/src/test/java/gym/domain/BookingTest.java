package gym.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * A booking holds a place until it is cancelled, attended or marked as a no-show.
 */
class BookingTest {

    private static final LocalDateTime AT = LocalDateTime.of(2026, 11, 2, 9, 0);

    private static Booking booked() {
        return Booking.book("b1", "m1", "c1", AT);
    }

    @Test
    void aNewBookingHoldsAPlace() {
        assertEquals(BookingStatus.BOOKED, booked().status());
        assertTrue(booked().isBooked());
    }

    @Test
    void cancellingFreesThePlace() {
        Booking cancelled = booked().cancel();

        assertEquals(BookingStatus.CANCELLED, cancelled.status());
        assertFalse(cancelled.isBooked());
    }

    @Test
    void aBookingCanBeMarkedAttendedOrNoShow() {
        assertEquals(BookingStatus.ATTENDED, booked().attend().status());
        assertEquals(BookingStatus.NO_SHOW, booked().noShow().status());
    }

    @Test
    void aBookingThatIsNotBookedAnymoreRefusesEveryChange() {
        Booking cancelled = booked().cancel();

        assertThrows(RuleViolationException.class, cancelled::cancel);
        assertThrows(RuleViolationException.class, cancelled::attend);
        assertThrows(RuleViolationException.class, cancelled::noShow);
    }

    @Test
    void aBookingNeverChanges() {
        Booking original = booked();
        original.cancel();

        assertEquals(BookingStatus.BOOKED, original.status());
    }
}