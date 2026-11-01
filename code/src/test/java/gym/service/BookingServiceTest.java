package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.Booking;
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
        spinClass();
        activeMembership("m1");

        Booking booking = service.book("m1", "c1");

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
        spinClass();
        activeMembership("m1");
        clock.advanceDays(5);

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aMemberWithoutACurrentMembershipCannotBook() {
        spinClass();

        assertThrows(NotFoundException.class, () -> service.book("m1", "c1"));
    }

    @Test
    void aFrozenMembershipCannotBook() {
        spinClass();
        memberships.save(activeMembership("m1").freeze(1, TODAY));

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertTrue(bookings.findAll().isEmpty());
    }

    @Test
    void aMemberCannotBookTheSameClassTwice() {
        spinClass();
        activeMembership("m1");
        service.book("m1", "c1");

        assertThrows(RuleViolationException.class, () -> service.book("m1", "c1"));
        assertEquals(1, bookings.findAll().size());
    }

    @Test
    void aMemberCanRebookAfterCancelling() {
        spinClass();
        activeMembership("m1");
        bookings.save(service.book("m1", "c1").cancel());

        Booking again = service.book("m1", "c1");

        assertEquals(BookingStatus.BOOKED, again.status());
    }

    private void spinClass() {
        classes.save(GymClass.schedule("c1", "Spin", SPIN_STARTS, 10, false));
    }

    private Membership activeMembership(String memberId) {
        return memberships.save(Membership.signUp("ms-" + memberId, memberId, new MonthlyPlan(), TODAY).endTrial(TODAY.plusDays(7)));
    }
}
