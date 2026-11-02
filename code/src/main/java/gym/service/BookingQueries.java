package gym.service;

import gym.domain.Booking;
import gym.domain.BookingStatus;
import gym.domain.GymClass;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.NoShows;
import gym.domain.NotFoundException;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import gym.port.out.MembershipRepository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * The questions {@link BookingService} and {@link AttendanceService} both ask about stored bookings. It only reads,
 * so the rules about what happens next stay in the services.
 */
class BookingQueries {

    private final GymClassRepository classes;
    private final BookingRepository bookings;
    private final MembershipRepository memberships;

    BookingQueries(GymClassRepository classes, BookingRepository bookings, MembershipRepository memberships) {
        this.classes = classes;
        this.bookings = bookings;
        this.memberships = memberships;
    }

    Booking findBooking(String bookingId) {
        return bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking", bookingId));
    }

    GymClass findClass(String classId) {
        return classes.findById(classId).orElseThrow(() -> new NotFoundException("Class", classId));
    }

    GymClass classOf(Booking booking) {
        return findClass(booking.classId());
    }

    /**
     * BR-B4: worked out from the member's no-shows each time, so there is no suspension to store or clear.
     */
    boolean isSuspended(String memberId, LocalDateTime now) {
        return new NoShows(bookings.findAll().stream()
                .filter(booking -> booking.status() == BookingStatus.NO_SHOW && booking.memberId().equals(memberId))
                .map(booking -> classOf(booking).startTime())
                .toList())
                .suspendBookingAt(now);
    }

    /**
     * The member's membership that isn't cancelled, the same way {@link MemberService} finds it.
     */
    Membership currentMembership(String memberId) {
        return findCurrentMembership(memberId)
                .orElseThrow(() -> new NotFoundException("Current membership for member", memberId));
    }

    Optional<Membership> findCurrentMembership(String memberId) {
        return memberships.findAll().stream()
                .filter(membership -> membership.memberId().equals(memberId) && membership.status() != MembershipStatus.CANCELLED)
                .findFirst();
    }

    boolean holdsAPlace(String memberId, String classId) {
        return bookings.findAll().stream()
                .anyMatch(booking -> booking.isBooked() && booking.memberId().equals(memberId) && booking.classId().equals(classId));
    }

    /**
     * BR-B5: the places the member holds in classes that haven't started yet.
     */
    long futureBookings(String memberId, LocalDateTime now) {
        return bookings.findAll().stream()
                .filter(booking -> booking.isBooked() && booking.memberId().equals(memberId))
                .filter(booking -> classes.findById(booking.classId()).filter(gymClass -> !gymClass.hasStarted(now)).isPresent())
                .count();
    }

    int placesTaken(String classId) {
        return (int) bookings.findAll().stream()
                .filter(booking -> booking.isBooked() && booking.classId().equals(classId))
                .count();
    }
}
