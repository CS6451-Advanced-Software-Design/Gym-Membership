package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.AnnualPlan;
import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.BookingStatus;
import gym.domain.GymClass;
import gym.domain.Membership;
import gym.domain.MonthlyPlan;
import gym.domain.NotFoundException;
import gym.domain.RuleViolationException;
import gym.service.BillingServiceTest.InMemoryMembershipRepository;
import gym.service.BookingServiceTest.InMemoryBookingRepository;
import gym.service.BookingServiceTest.InMemoryGymClassRepository;
import gym.service.BookingServiceTest.RecordedCharges;
import gym.support.TestClock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * The Command pattern: booking and cancelling are kept per member and can be taken back, newest first.
 */
class BookingCommandServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
    private static final LocalDateTime SPIN_STARTS = LocalDateTime.of(2026, 11, 6, 18, 0);

    private final InMemoryGymClassRepository classes = new InMemoryGymClassRepository();
    private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    private final InMemoryMembershipRepository memberships = new InMemoryMembershipRepository();
    private final RecordedCharges charges = new RecordedCharges();
    private final TestClock clock = new TestClock(TODAY);

    private final BookingService receiver = new BookingService(classes, bookings, memberships, charges, clock);
    private final BookingCommandService commands = new BookingCommandService(receiver, receiver, receiver);

    @Test
    void undoingABookingGivesUpThePlace() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = commands.book("m1", "c1").booking();

        commands.undoLast("m1");

        assertEquals(BookingStatus.CANCELLED, statusOf(booking));
    }

    @Test
    void undoingABookingPromotesTheFirstOnTheWaitlist() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        commands.book("m1", "c1");
        commands.book("m2", "c1");

        commands.undoLast("m1");

        assertTrue(bookings.findAll().stream().anyMatch(booking -> booking.isBooked() && booking.memberId().equals("m2")));
    }

    @Test
    void undoingABookingHandsBackATrialBooking() {
        spinClass(10);
        trialMembership("m1");
        commands.book("m1", "c1");

        commands.undoLast("m1");

        assertEquals(0, membershipOf("m1").trialBookingsUsed());
    }

    @Test
    void aLateUndoStillCountsAgainstTheTrial() {
        // BR-B3: otherwise undo would be a way round the rule
        lateClass();
        trialMembership("m1");
        commands.book("m1", "c1");

        commands.undoLast("m1");

        assertEquals(1, membershipOf("m1").trialBookingsUsed());
    }

    @Test
    void aLateUndoStillPaysTheLateFee() {
        lateClass();
        activeMembership("m1");
        commands.book("m1", "c1");

        commands.undoLast("m1");

        assertEquals(1, charges.size());
    }

    @Test
    void undoingAWaitlistPlaceLeavesTheWaitlist() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        activeMembership("m3");
        commands.book("m1", "c1");
        commands.book("m2", "c1");
        BookingResult third = commands.book("m3", "c1");
        assertEquals(2, third.waitlistPosition());

        commands.undoLast("m2");

        GymClass spin = classes.findById("c1").orElseThrow();
        assertFalse(spin.waitlist().contains("m2"));
        assertEquals(1, spin.waitlist().positionOf("m3"));
    }

    @Test
    void aMemberPromotedSinceCannotUndoTheirWaitlistPlace() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        Booking booking = commands.book("m1", "c1").booking();
        commands.book("m2", "c1");
        commands.cancel(booking.bookingId());

        assertThrows(RuleViolationException.class, () -> commands.undoLast("m2"));
    }

    @Test
    void undoingACancelGivesThePlaceBackWithoutChargingAgain() {
        premiumClass();
        trialMembership("m1");
        Booking booking = commands.book("m1", "c1").booking();
        commands.cancel(booking.bookingId());

        commands.undoLast("m1");

        assertEquals(BookingStatus.BOOKED, statusOf(booking));
        assertEquals(1, charges.size());
    }

    @Test
    void aTrialThatUsedItsBookingsCanStillUndoACancel() {
        spinClass(10);
        classes.save(GymClass.schedule("c2", "Yoga", SPIN_STARTS, 10, false));
        trialMembership("m1");
        Booking booking = commands.book("m1", "c1").booking();
        commands.book("m1", "c2");
        commands.undoLast("m1");
        commands.book("m1", "c2");
        commands.cancel(booking.bookingId());

        commands.undoLast("m1");

        assertEquals(BookingStatus.BOOKED, statusOf(booking));
        assertEquals(2, membershipOf("m1").trialBookingsUsed());
    }

    @Test
    void aCancelCannotBeUndoneOnceThePlaceWentToTheWaitlist() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        Booking booking = commands.book("m1", "c1").booking();
        commands.book("m2", "c1");
        commands.cancel(booking.bookingId());

        assertThrows(RuleViolationException.class, () -> commands.undoLast("m1"));
        assertEquals(BookingStatus.CANCELLED, statusOf(booking));
    }

    @Test
    void aCancelCannotBeUndoneOnceTheClassHasStarted() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = commands.book("m1", "c1").booking();
        commands.cancel(booking.bookingId());
        clock.advanceDays(5);

        assertThrows(RuleViolationException.class, () -> commands.undoLast("m1"));
    }

    @Test
    void aCancelCannotBeUndoneOnceTheMemberIsFrozen() {
        spinClass(10);
        Membership membership = activeMembership("m1");
        Booking booking = commands.book("m1", "c1").booking();
        commands.cancel(booking.bookingId());
        memberships.save(membership.freeze(1, TODAY));

        assertThrows(RuleViolationException.class, () -> commands.undoLast("m1"));
    }

    @Test
    void aRefusedUndoIsKeptAndCanBeTriedAgain() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        Booking first = commands.book("m1", "c1").booking();
        commands.cancel(first.bookingId());
        commands.book("m2", "c1");
        assertThrows(RuleViolationException.class, () -> commands.undoLast("m1"));

        commands.undoLast("m2");
        commands.undoLast("m1");

        assertEquals(BookingStatus.BOOKED, statusOf(first));
    }

    @Test
    void undoGoesBackOneActionAtATime() {
        spinClass(10);
        activeMembership("m1");
        Booking booking = commands.book("m1", "c1").booking();
        commands.cancel(booking.bookingId());

        commands.undoLast("m1");
        assertEquals(BookingStatus.BOOKED, statusOf(booking));

        commands.undoLast("m1");
        assertEquals(BookingStatus.CANCELLED, statusOf(booking));
        assertThrows(RuleViolationException.class, () -> commands.undoLast("m1"));
    }

    @Test
    void oneMembersUndoLeavesAnothersActionsAlone() {
        spinClass(10);
        activeMembership("m1");
        activeMembership("m2");
        Booking booking = commands.book("m1", "c1").booking();

        assertThrows(RuleViolationException.class, () -> commands.undoLast("m2"));
        assertEquals(BookingStatus.BOOKED, statusOf(booking));
    }

    @Test
    void aRefusedBookingLeavesNothingToUndo() {
        spinClass(10);

        assertThrows(NotFoundException.class, () -> commands.book("m1", "c1"));
        assertThrows(RuleViolationException.class, () -> commands.undoLast("m1"));
    }

    @Test
    void aRestoredBookingIsRefusedIfTheMemberHoldsAPlaceAgain() {
        // Only reachable by calling the receiver directly, as the history undoes the newest action first
        spinClass(10);
        activeMembership("m1");
        Booking first = commands.book("m1", "c1").booking();
        commands.cancel(first.bookingId());
        commands.book("m1", "c1");

        assertThrows(RuleViolationException.class, () -> receiver.restoreBooking(first.bookingId()));
    }

    private BookingStatus statusOf(Booking booking) {
        return bookings.findById(booking.bookingId()).orElseThrow().status();
    }

    private Membership membershipOf(String memberId) {
        return memberships.findAll().stream().filter(membership -> membership.memberId().equals(memberId)).findFirst().orElseThrow();
    }

    private void spinClass(int capacity) {
        classes.save(GymClass.schedule("c1", "Spin", SPIN_STARTS, capacity, false));
    }

    private void premiumClass() {
        classes.save(GymClass.schedule("c1", "Reformer", SPIN_STARTS, 10, true));
    }

    private void lateClass() {
        classes.save(GymClass.schedule("c1", "Spin", TODAY.atTime(10, 0), 10, false));
    }

    private Membership activeMembership(String memberId) {
        return memberships.save(Membership.signUp("ms-" + memberId, memberId, new AnnualPlan(), TODAY));
    }

    private void trialMembership(String memberId) {
        memberships.save(Membership.signUp("ms-" + memberId, memberId, new MonthlyPlan(), TODAY));
    }
}
