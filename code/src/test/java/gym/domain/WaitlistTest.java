package gym.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * BR-B1: the waitlist is first come, first served.
 */
class WaitlistTest {

    @Test
    void membersJoinAtTheEndOfTheLine() {
        Waitlist waitlist = Waitlist.empty().add("m1").add("m2");

        assertEquals(2, waitlist.size());
        assertEquals(1, waitlist.positionOf("m1"));
        assertEquals(2, waitlist.positionOf("m2"));
    }

    @Test
    void aMemberWhoIsNotWaitingHasPositionZero() {
        assertEquals(0, Waitlist.empty().positionOf("m1"));
    }

    @Test
    void theFirstInLineIsTheOneWhoWaitedLongest() {
        Waitlist waitlist = Waitlist.empty().add("m1").add("m2");

        assertEquals(Optional.of("m1"), waitlist.first());
    }

    @Test
    void removingTheFirstMovesEveryoneUp() {
        Waitlist waitlist = Waitlist.empty().add("m1").add("m2").add("m3").withoutFirst();

        assertEquals(Optional.of("m2"), waitlist.first());
        assertEquals(1, waitlist.positionOf("m2"));
        assertEquals(2, waitlist.positionOf("m3"));
        assertEquals(0, waitlist.positionOf("m1"));
    }

    @Test
    void anEmptyWaitlistHasNoFirstAndStaysEmpty() {
        assertEquals(Optional.empty(), Waitlist.empty().first());
        assertEquals(0, Waitlist.empty().withoutFirst().size());
    }

    @Test
    void aMemberCannotJoinTwice() {
        Waitlist waitlist = Waitlist.empty().add("m1");

        assertThrows(RuleViolationException.class, () -> waitlist.add("m1"));
    }

    @Test
    void aWaitlistNeverChanges() {
        Waitlist empty = Waitlist.empty();
        empty.add("m1");

        assertTrue(empty.memberIds().isEmpty());
    }
}