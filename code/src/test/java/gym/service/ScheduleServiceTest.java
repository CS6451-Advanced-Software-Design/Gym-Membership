package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.Booking;
import gym.domain.GymClass;
import gym.domain.RuleViolationException;
import gym.port.in.ManageScheduleUseCase.ScheduledClass;
import gym.service.BookingServiceTest.InMemoryBookingRepository;
import gym.service.BookingServiceTest.InMemoryGymClassRepository;
import gym.support.TestClock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * UC10: staff add classes and see what is coming up.
 */
class ScheduleServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 2);
    private static final LocalDateTime SPIN_STARTS = LocalDateTime.of(2026, 11, 6, 18, 0);

    private final InMemoryGymClassRepository classes = new InMemoryGymClassRepository();
    private final InMemoryBookingRepository bookings = new InMemoryBookingRepository();
    private final TestClock clock = new TestClock(TODAY);
    private final ScheduleService service = new ScheduleService(classes, bookings, clock);

    @Test
    void addingAClassStoresItWithNobodyWaiting() {
        GymClass added = service.addClass("Spin", SPIN_STARTS, 12, true);

        assertEquals("Spin", added.name());
        assertEquals(SPIN_STARTS, added.startTime());
        assertEquals(12, added.capacity());
        assertTrue(added.premium());
        assertEquals(0, added.waitlist().size());
        assertEquals(Optional.of(added), classes.findById(added.classId()));
    }

    @Test
    void aClassCannotBeAddedInThePast() {
        assertThrows(RuleViolationException.class, () -> service.addClass("Spin", TODAY.minusDays(1).atTime(18, 0), 12, false));
        assertTrue(classes.findAll().isEmpty());
    }

    @Test
    void aClassNeedsAtLeastOnePlace() {
        assertThrows(RuleViolationException.class, () -> service.addClass("Spin", SPIN_STARTS, 0, false));
        assertTrue(classes.findAll().isEmpty());
    }

    @Test
    void theScheduleListsUpcomingClassesSoonestFirst() {
        GymClass later = service.addClass("Yoga", SPIN_STARTS.plusDays(2), 10, false);
        GymClass sooner = service.addClass("Spin", SPIN_STARTS, 10, false);

        assertEquals(List.of(sooner, later), scheduledClasses());
    }

    @Test
    void theScheduleLeavesOutClassesThatHaveStarted() {
        service.addClass("Spin", SPIN_STARTS, 10, false);
        GymClass later = service.addClass("Yoga", SPIN_STARTS.plusDays(2), 10, false);
        clock.advanceDays(5);

        assertEquals(List.of(later), scheduledClasses());
    }

    @Test
    void placesLeftCountOnlyTheBookingsThatHoldAPlaceInThatClass() {
        GymClass spin = service.addClass("Spin", SPIN_STARTS, 3, false);
        GymClass yoga = service.addClass("Yoga", SPIN_STARTS.plusDays(1), 3, false);
        bookings.save(Booking.book("b1", "m1", spin.classId(), SPIN_STARTS.minusDays(1)));
        bookings.save(Booking.book("b2", "m2", spin.classId(), SPIN_STARTS.minusDays(1)).cancel());
        bookings.save(Booking.book("b3", "m3", yoga.classId(), SPIN_STARTS.minusDays(1)));
        bookings.save(Booking.book("b4", "m4", yoga.classId(), SPIN_STARTS.minusDays(1)));

        assertEquals(List.of(2, 1), service.schedule().stream().map(ScheduledClass::placesLeft).toList());
    }

    private List<GymClass> scheduledClasses() {
        return service.schedule().stream().map(ScheduledClass::gymClass).toList();
    }
}
