package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.BookingStatus;
import gym.domain.GymClass;
import gym.domain.Membership;
import gym.domain.MonthlyPlan;
import gym.domain.NotFoundException;
import gym.domain.RuleViolationException;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import gym.service.BillingServiceTest.InMemoryMembershipRepository;
import gym.support.InMemoryRepository;
import gym.support.TestClock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class BookingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
    private static final LocalDateTime SPIN_STARTS = LocalDateTime.of(2026, 11, 6, 18, 0);

    static class InMemoryGymClassRepository extends InMemoryRepository<GymClass, String> implements GymClassRepository {
        InMemoryGymClassRepository() {
            super(GymClass::classId);
        }
    }

    static class InMemoryBookingRepository extends InMemoryRepository<Booking, String> implements BookingRepository {
        InMemoryBookingRepository() {
            super(Booking::bookingId);
        }
    }

    private final InMemoryGymClassRepository classes = new InMemoryGymClassRepository();
    private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    private final InMemoryMembershipRepository memberships = new InMemoryMembershipRepository();
    private final TestClock clock = new TestClock(TODAY);

    private final BookingService service = new BookingService(classes, bookings, memberships, clock);

    @Test
    void bookingAClassStoresAPlaceForTheMember() {
        spinClass(10);
        activeMembership("m1");

        BookingResult result = service.book("m1", "c1");

        assertFalse(result.isWaitlisted());
        Booking booking = result.booking();
        assertEquals("m1", booking.memberId());
        assertEquals("c1", booking.classId());
        assertEquals(BookingStatus.BOOKED, booking.status());
        assertEquals(TODAY.atTime(9, 0), booking.bookedAt());
        assertTrue(bookings.findById(booking.bookingId()).isPresent());
    }

    @Test
    void bookingAnUnknownClassIsRefused() {
        activeMembership("m1");

        assertThrows(NotFoundException.class, () -> service.book("m1", "nobody"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aClassThatHasStartedCannotBeBooked() {
        spinClass(10);
        activeMembership("m1");
        clock.advanceDays(5);

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aMemberWithoutACurrentMembershipCannotBook() {
        spinClass(10);

        assertThrows(NotFoundException.class, () -> service.book("m1", "c1"));
    }

    @Test
    void aFrozenMembershipCannotBook() {
        spinClass(10);
        memberships.save(activeMembership("m1").freeze(1, TODAY));

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aMemberCannotBookTheSameClassTwice() {
        spinClass(10);
        activeMembership("m1");
        service.book("m1", "c1");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertEquals(1, bookings.findAll().size());
    }

    @Test
    void aMemberCanRebookAfterCancelling() {
        spinClass(10);
        activeMembership("m1");
        bookings.save(service.book("m1", "c1").booking().cancel());

        Booking again = service.book("m1", "c1").booking();

        assertEquals(BookingStatus.BOOKED, again.status());
    }

    @Test
    void aFullClassPutsTheMemberOnTheWaitlist() {
        // BR-B1: one place, so the second member has to wait
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        service.book("m1", "c1");

        BookingResult result = service.book("m2", "c1");

        assertTrue(result.isWaitlisted());
        assertEquals(1, result.waitlistPosition());
        assertEquals(1, bookings.findAll().size());
        assertEquals(List.of("m2"), classes.findById("c1").orElseThrow().waitlist().memberIds());
    }

    @Test
    void theWaitlistIsFirstComeFirstServed() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        activeMembership("m3");
        service.book("m1", "c1");

        service.book("m3", "c1");
        BookingResult second = service.book("m2", "c1");

        assertEquals(2, second.waitlistPosition());
        assertEquals(List.of("m3", "m2"), classes.findById("c1").orElseThrow().waitlist().memberIds());
    }

    @Test
    void aMemberCannotJoinTheWaitlistTwice() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        service.book("m1", "c1");
        service.book("m2", "c1");

        assertThrows(RuleViolationException.class, () -> service.book("m2", "c1"));
    }

    @Test
    void aCancelledBookingFreesItsPlace() {
        spinClass(1);
        activeMembership("m1");
        activeMembership("m2");
        bookings.save(service.book("m1", "c1").booking().cancel());

        BookingResult result = service.book("m2", "c1");

        assertFalse(result.isWaitlisted());
    }

    @Test
    void aTrialMemberCanBookOnlyTwoClasses() {
        // BR-M2: the third booking is refused, and nothing is stored for it
        aClass("c1", 10);
        aClass("c2", 10);
        aClass("c3", 10);
        trialMembership("m1");

        service.book("m1", "c1");
        service.book("m1", "c2");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c3"));
        assertEquals(2, bookings.findAll().size());
        assertEquals(2, memberships.findById("ms-m1").orElseThrow().trialBookingsUsed());
    }

    @Test
    void aCancelledBookingStillCountsAgainstTheTrial() {
        // BR-B3: cancelling doesn't give a Trial member the booking back
        aClass("c1", 10);
        aClass("c2", 10);
        aClass("c3", 10);
        trialMembership("m1");
        bookings.save(service.book("m1", "c1").booking().cancel());
        service.book("m1", "c2");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c3"));
    }

    @Test
    void joiningTheWaitlistDoesNotUseATrialBooking() {
        // BR-M2 counts bookings, and a member on the waitlist doesn't hold one yet
        aClass("c1", 1);
        activeMembership("m1");
        trialMembership("m2");
        service.book("m1", "c1");

        service.book("m2", "c1");

        assertEquals(0, memberships.findById("ms-m2").orElseThrow().trialBookingsUsed());
    }

    @Test
    void anActiveMemberHasNoTrialLimit() {
        aClass("c1", 10);
        aClass("c2", 10);
        aClass("c3", 10);
        activeMembership("m1");

        service.book("m1", "c1");
        service.book("m1", "c2");
        service.book("m1", "c3");

        assertEquals(3, bookings.findAll().size());
    }

    private void aClass(String classId, int capacity) {
        classes.save(GymClass.schedule(classId, "Spin", SPIN_STARTS, capacity, false));
    }

    private Membership trialMembership(String memberId) {
        return memberships.save(Membership.signUp("ms-" + memberId, memberId, new MonthlyPlan(), TODAY));
    }

    private void spinClass(int capacity) {
        classes.save(GymClass.schedule("c1", "Spin", SPIN_STARTS, capacity, false));
    }

    private Membership activeMembership(String memberId) {
        return memberships.save(Membership.signUp("ms-" + memberId, memberId, new MonthlyPlan(), TODAY).endTrial(TODAY.plusDays(7)));
    }
}
