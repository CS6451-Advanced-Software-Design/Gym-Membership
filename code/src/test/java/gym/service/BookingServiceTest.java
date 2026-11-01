package gym.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.Booking;
import gym.domain.BookingStatus;
import gym.domain.GymClass;
import gym.domain.NotFoundException;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
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
    private final TestClock clock = new TestClock(TODAY);

    private final BookingService service = new BookingService(classes, bookings, clock);

    @Test
    void bookingAClassStoresAPlaceForTheMember() {
        classes.save(GymClass.schedule("c1", "Spin", SPIN_STARTS, 10, false));

        Booking booking = service.book("m1", "c1");

        assertEquals("m1", booking.memberId());
        assertEquals("c1", booking.classId());
        assertEquals(BookingStatus.BOOKED, booking.status());
        assertEquals(TODAY.atTime(9, 0), booking.bookedAt());
        assertTrue(bookings.findById(booking.bookingId()).isPresent());
    }

    @Test
    void bookingAnUnknownClassIsRefused() {
        assertThrows(NotFoundException.class, () -> service.book("m1", "nobody"));
        assertTrue(bookings.findAll().isEmpty());
    }
}