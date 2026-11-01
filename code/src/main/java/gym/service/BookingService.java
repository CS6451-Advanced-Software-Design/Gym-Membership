package gym.service;

import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.GymClass;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.NotFoundException;
import gym.domain.RuleViolationException;
import gym.port.in.BookClassUseCase;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import gym.port.out.MembershipRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The Booking feature: gives members places in classes. What a booking can do lives in {@link Booking},
 * and what a class allows lives in {@link GymClass}. Whether a member may book at all is decided by their
 * {@link Membership}. This service loads them, asks them and saves the result.
 */
@Service
public class BookingService implements BookClassUseCase {

    private final GymClassRepository classes;
    private final BookingRepository bookings;
    private final MembershipRepository memberships;
    private final Clock clock;

    public BookingService(
            GymClassRepository classes,
            BookingRepository bookings,
            MembershipRepository memberships,
            Clock clock) {
        this.classes = classes;
        this.bookings = bookings;
        this.memberships = memberships;
        this.clock = clock;
    }

    @Override
    public BookingResult book(String memberId, String classId) {
        GymClass gymClass = classes.findById(classId).orElseThrow(() -> new NotFoundException("Class", classId));
        LocalDateTime now = LocalDateTime.now(clock);
        if (gymClass.hasStarted(now)) {
            throw new RuleViolationException("Class " + classId + " has already started");
        }
        Membership membership = currentMembership(memberId);
        if (!membership.canBook()) {
            throw new RuleViolationException("Membership " + membership.membershipId() + " can't book a class right now");
        }
        if (!membership.rules().allowsClassAt(gymClass.startTime())) {
            throw new RuleViolationException("Membership " + membership.membershipId() + " doesn't allow a class at this time");
        }
        if (holdsAPlace(memberId, classId)) {
            throw new RuleViolationException("Member " + memberId + " already has a place in class " + classId);
        }
        if (futureBookings(memberId, now) >= Booking.MAX_FUTURE_BOOKINGS) {
            throw new RuleViolationException("Member " + memberId + " already holds "
                    + Booking.MAX_FUTURE_BOOKINGS + " future bookings");
        }
        if (gymClass.isFull(placesTaken(classId))) {
            GymClass waiting = classes.save(gymClass.joinWaitlist(memberId));
            return BookingResult.waitlisted(waiting.waitlist().positionOf(memberId));
        }
        memberships.save(membership.recordBooking());
        return BookingResult.booked(bookings.save(Booking.book(newId("b-"), memberId, classId, now)));
    }

    /**
     * The member's membership that isn't cancelled, the same way {@link MemberService} finds it.
     */
    private Membership currentMembership(String memberId) {
        return memberships.findAll().stream()
                .filter(membership -> membership.memberId().equals(memberId) && membership.status() != MembershipStatus.CANCELLED)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Current membership for member", memberId));
    }

    private boolean holdsAPlace(String memberId, String classId) {
        return bookings.findAll().stream()
                .anyMatch(booking -> booking.isBooked() && booking.memberId().equals(memberId) && booking.classId().equals(classId));
    }

    /**
     * BR-B5: the places the member holds in classes that haven't started yet.
     */
    private long futureBookings(String memberId, LocalDateTime now) {
        return bookings.findAll().stream()
                .filter(booking -> booking.isBooked() && booking.memberId().equals(memberId))
                .filter(booking -> classes.findById(booking.classId()).filter(gymClass -> !gymClass.hasStarted(now)).isPresent())
                .count();
    }

    private int placesTaken(String classId) {
        return (int) bookings.findAll().stream()
                .filter(booking -> booking.isBooked() && booking.classId().equals(classId))
                .count();
    }

    private static String newId(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8);
    }
}
