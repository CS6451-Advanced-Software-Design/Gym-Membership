package gym.service;

import gym.domain.Booking;
import gym.domain.GymClass;
import gym.domain.NotFoundException;
import gym.port.in.BookClassUseCase;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The Booking feature: gives members places in classes. What a booking can do lives in {@link Booking},
 * and what a class allows lives in {@link GymClass}. This service loads them, asks them and saves the result.
 */
@Service
public class BookingService implements BookClassUseCase {

    private final GymClassRepository classes;
    private final BookingRepository bookings;
    private final Clock clock;

    public BookingService(GymClassRepository classes, BookingRepository bookings, Clock clock) {
        this.classes = classes;
        this.bookings = bookings;
        this.clock = clock;
    }

    @Override
    public Booking book(String memberId, String classId) {
        GymClass gymClass = classes.findById(classId).orElseThrow(() -> new NotFoundException("Class", classId));
        return bookings.save(Booking.book(newId("b-"), memberId, gymClass.classId(), LocalDateTime.now(clock)));
    }

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }
}