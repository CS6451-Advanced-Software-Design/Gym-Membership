package gym.service;

import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.ChargeType;
import gym.domain.GymClass;
import gym.domain.Membership;
import gym.domain.MembershipStatus;
import gym.domain.Money;
import gym.domain.NotFoundException;
import gym.domain.PlanType;
import gym.domain.RuleViolationException;
import gym.port.in.BookClassUseCase;
import gym.port.in.CancelBookingUseCase;
import gym.port.in.RecordAttendanceUseCase;
import gym.port.in.RecordChargeUseCase;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import gym.port.out.MembershipRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * The Booking feature: gives members places in classes. What a booking can do lives in {@link Booking},
 * and what a class allows lives in {@link GymClass}. Whether a member may book at all is decided by their
 * {@link Membership}. This service loads them, asks them and saves the result.
 */
@Service
public class BookingService implements BookClassUseCase, CancelBookingUseCase, RecordAttendanceUseCase {

    /**
     * BR-B6: what a premium class costs on top of the plan, unless the member is on Annual.
     */
    private static final Money PREMIUM_SURCHARGE = Money.euros(8);

    /**
     * BR-B3: what cancelling less than 2 hours before a class costs.
     */
    private static final Money LATE_CANCELLATION_FEE = Money.euros(5);

    /**
     * BR-B4: what missing a booked class costs.
     */
    private static final Money NO_SHOW_FEE = Money.euros(5);

    private final GymClassRepository classes;
    private final BookingRepository bookings;
    private final MembershipRepository memberships;
    private final RecordChargeUseCase charges;
    private final Clock clock;

    public BookingService(
            GymClassRepository classes,
            BookingRepository bookings,
            MembershipRepository memberships,
            RecordChargeUseCase charges,
            Clock clock) {
        this.classes = classes;
        this.bookings = bookings;
        this.memberships = memberships;
        this.charges = charges;
        this.clock = clock;
    }

    /**
     * Synchronized because counting the places and then saving is two steps. Without it, two members could both
     * see the last place free. Each single repository call is locked, but not the two together.
     */
    @Override
    public synchronized BookingResult book(String memberId, String classId) {
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
        return BookingResult.booked(takePlace(membership, gymClass, now));
    }

    /**
     * Gives the member a place, whether they booked it or were promoted to it.
     */
    private Booking takePlace(Membership membership, GymClass gymClass, LocalDateTime now) {
        // Charged first: this is the step that can still fail, and there are no transactions to undo the saves below
        if (gymClass.premium() && membership.plan() != PlanType.ANNUAL) {
            charges.record(membership.membershipId(), ChargeType.PREMIUM_SURCHARGE, PREMIUM_SURCHARGE);
        }
        memberships.save(membership.recordBooking());
        return bookings.save(Booking.book(newId("b-"), membership.memberId(), gymClass.classId(), now));
    }

    /**
     * Synchronized for the same reason as {@link #book}: both change who holds the places in a class.
     */
    @Override
    public synchronized Booking cancel(String bookingId) {
        Booking booking = findBooking(bookingId);
        GymClass gymClass = classOf(booking);
        LocalDateTime now = LocalDateTime.now(clock);
        if (gymClass.hasStarted(now)) {
            throw new RuleViolationException("Class " + gymClass.classId() + " has already started");
        }
        // Cancelled first, so a booking that can't be cancelled is refused before any fee is charged
        Booking cancelled = booking.cancel();
        if (gymClass.isLateToCancel(now)) {
            chargeLateCancellationFee(booking.memberId());
        }
        Booking saved = bookings.save(cancelled);
        promoteFromWaitlist(gymClass, now);
        return saved;
    }

    @Override
    public synchronized Booking markAttended(String bookingId) {
        return bookings.save(startedBooking(bookingId).attend());
    }

    @Override
    public synchronized Booking markNoShow(String bookingId) {
        // Marked first, so a booking that can't be a no-show is refused before any fee is charged
        Booking noShow = startedBooking(bookingId).noShow();
        findCurrentMembership(noShow.memberId())
                .ifPresent(membership -> charges.record(membership.membershipId(), ChargeType.NO_SHOW, NO_SHOW_FEE));
        return bookings.save(noShow);
    }

    /**
     * Attendance can only be known once the class has started.
     */
    private Booking startedBooking(String bookingId) {
        Booking booking = findBooking(bookingId);
        if (!classOf(booking).hasStarted(LocalDateTime.now(clock))) {
            throw new RuleViolationException("Class " + booking.classId() + " hasn't started yet");
        }
        return booking;
    }

    private Booking findBooking(String bookingId) {
        return bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking", bookingId));
    }

    private GymClass classOf(Booking booking) {
        return classes.findById(booking.classId()).orElseThrow(() -> new NotFoundException("Class", booking.classId()));
    }

    /**
     * BR-B2: the place goes to the first member still able to take it. Anyone who can no longer book is taken
     * off the waitlist, so they don't hold up the members behind them.
     */
    private void promoteFromWaitlist(GymClass gymClass, LocalDateTime now) {
        GymClass remaining = gymClass;
        while (remaining.waitlist().size() > 0) {
            String memberId = remaining.waitlist().first().orElseThrow();
            remaining = remaining.promoteFromWaitlist();
            Optional<Membership> next = findCurrentMembership(memberId)
                    .filter(membership -> canStillBook(membership, gymClass, now));
            if (next.isPresent()) {
                classes.save(remaining);
                takePlace(next.get(), gymClass, now);
                return;
            }
        }
        classes.save(remaining);
    }

    /**
     * The checks that may have changed since the member joined the waitlist.
     */
    private boolean canStillBook(Membership membership, GymClass gymClass, LocalDateTime now) {
        return membership.canBook()
                && membership.rules().allowsClassAt(gymClass.startTime())
                && futureBookings(membership.memberId(), now) < Booking.MAX_FUTURE_BOOKINGS;
    }

    /**
     * BR-B3. A member who has since left the gym has no membership to charge, so they pay nothing.
     */
    private void chargeLateCancellationFee(String memberId) {
        findCurrentMembership(memberId)
                .filter(Membership::paysLateCancellationFee)
                .ifPresent(membership -> charges.record(
                        membership.membershipId(), ChargeType.LATE_CANCELLATION, LATE_CANCELLATION_FEE));
    }

    /**
     * The member's membership that isn't cancelled, the same way {@link MemberService} finds it.
     */
    private Membership currentMembership(String memberId) {
        return findCurrentMembership(memberId)
                .orElseThrow(() -> new NotFoundException("Current membership for member", memberId));
    }

    private Optional<Membership> findCurrentMembership(String memberId) {
        return memberships.findAll().stream()
                .filter(membership -> membership.memberId().equals(memberId) && membership.status() != MembershipStatus.CANCELLED)
                .findFirst();
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
