package gym.service;

import gym.domain.Booking;
import gym.domain.ChargeType;
import gym.domain.Money;
import gym.domain.RuleViolationException;
import gym.port.in.RecordAttendanceUseCase;
import gym.port.in.RecordChargeUseCase;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import gym.port.out.MembershipRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * UC8: what an instructor records once a class has started. It is kept apart from {@link BookingService} because
 * these bookings can no longer be booked, cancelled or undone, which also means the two never change the same one.
 */
@Service
public class AttendanceService implements RecordAttendanceUseCase {

    /**
     * BR-B4: what missing a booked class costs.
     */
    private static final Money NO_SHOW_FEE = Money.euros(5);

    private final BookingRepository bookings;
    private final RecordChargeUseCase charges;
    private final Clock clock;
    private final BookingQueries queries;

    public AttendanceService(
            GymClassRepository classes,
            BookingRepository bookings,
            MembershipRepository memberships,
            RecordChargeUseCase charges,
            Clock clock) {
        this.bookings = bookings;
        this.charges = charges;
        this.clock = clock;
        this.queries = new BookingQueries(classes, bookings, memberships);
    }

    @Override
    public synchronized Booking markAttended(String bookingId) {
        return bookings.save(startedBooking(bookingId).attend());
    }

    @Override
    public synchronized Booking markNoShow(String bookingId) {
        // Marked first, so a booking that can't be a no-show is refused before any fee is charged
        Booking noShow = startedBooking(bookingId).noShow();
        queries.findCurrentMembership(noShow.memberId())
                .ifPresent(membership -> charges.record(membership.membershipId(), ChargeType.NO_SHOW, NO_SHOW_FEE));
        return bookings.save(noShow);
    }

    /**
     * Attendance can only be known once the class has started.
     */
    private Booking startedBooking(String bookingId) {
        Booking booking = queries.findBooking(bookingId);
        if (!queries.classOf(booking).hasStarted(LocalDateTime.now(clock))) {
            throw new RuleViolationException("Class " + booking.classId() + " hasn't started yet");
        }
        return booking;
    }
}
