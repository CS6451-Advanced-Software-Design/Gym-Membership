package gym.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The class timetable rules: capacity (BR-B1), the waitlist and the late-cancellation window (BR-B3).
 */
class GymClassTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 11, 6, 18, 0);

    private static GymClass spin() {
        return GymClass.schedule("c1", "Spin", START, 2, false);
    }

    @Test
    void aClassIsFullOnceEveryPlaceIsBooked() {
        assertFalse(spin().isFull(1));
        assertTrue(spin().isFull(2));
    }

    @Test
    void aClassNeedsAtLeastOnePlace() {
        assertThrows(IllegalArgumentException.class, () -> GymClass.schedule("c2", "Yoga", START, 0, false));
    }

    @Test
    void aNewClassHasNobodyWaiting() {
        assertEquals(0, spin().waitlist().size());
    }

    @Test
    void membersJoiningAFullClassWaitInOrder() {
        GymClass full = spin().joinWaitlist("m1").joinWaitlist("m2");

        assertEquals(1, full.waitlist().positionOf("m1"));
        assertEquals(2, full.waitlist().positionOf("m2"));
    }

    @Test
    void promotingTakesTheFirstOffTheWaitlist() {
        GymClass promoted = spin().joinWaitlist("m1").joinWaitlist("m2").promoteFromWaitlist();

        assertEquals(Optional.of("m2"), promoted.waitlist().first());
    }

    @Test
    void cancellingLessThanTwoHoursBeforeIsLate() {
        assertTrue(spin().isLateToCancel(START.minusMinutes(119)));
        assertFalse(spin().isLateToCancel(START.minusMinutes(121)));
    }

    @Test
    void aClassHasStartedFromItsStartTime() {
        assertFalse(spin().hasStarted(START.minusMinutes(1)));
        assertTrue(spin().hasStarted(START));
    }

    @Test
    void aClassNeverChanges() {
        GymClass original = spin();
        original.joinWaitlist("m1");

        assertEquals(0, original.waitlist().size());
    }
}