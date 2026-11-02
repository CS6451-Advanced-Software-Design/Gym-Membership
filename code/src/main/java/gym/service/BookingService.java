package gym.service;

import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.ChargeType;
import gym.domain.GymClass;
import gym.domain.Membership;
import gym.domain.Money;
import gym.domain.NoShows;
import gym.domain.PlanType;
import gym.domain.RuleViolationException;
import gym.port.in.BookClassUseCase;
import gym.port.in.CancelBookingUseCase;
import gym.port.in.RecordChargeUseCase;
import gym.port.in.UndoBookingUseCase;
import gym.port.out.BookingRepository;
import gym.port.out.GymClassRepository;
import gym.port.out.MembershipRepository;
import gym.port.out.NotificationSender;
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
public class BookingService
        implements BookClassUseCase, CancelBookingUseCase, UndoBookingUseCase {

    /**
     * BR-B6: what a premium class costs on top of the plan, unless the member is on Annual.
     */
    private static final Money PREMIUM_SURCHARGE = Money.euros(8);

    /**
     * BR-B3: what cancelling less than 2 hours before a class costs.
     */
    private static final Money LATE_CANCELLATION_FEE = Money.euros(5);

    private final GymClassRepository classes;
    private final BookingRepository bookings;
    private final MembershipRepository memberships;
    private final RecordChargeUseCase charges;
    private final NotificationSender notifications;
    private final Clock clock;
    private final BookingQueries queries;

    public BookingService(
            GymClassRepository classes,
            BookingRepository bookings,
            MembershipRepository memberships,
            RecordChargeUseCase charges,
            NotificationSender notifications,
            Clock clock) {
        this.classes = classes;
        this.bookings = bookings;
        this.memberships = memberships;
        this.charges = charges;
        this.notifications = notifications;
        this.clock = clock;
        this.queries = new BookingQueries(classes, bookings, memberships);
    }

    /**
     * Synchronized because counting the places and then saving is two steps. Without it, two members could both
     * see the last place free. Each single repository call is locked, but not the two together.
     */
    @Override
    public synchronized BookingResult book(String memberId, String classId) {
        GymClass gymClass = queries.findClass(classId);
        LocalDateTime now = LocalDateTime.now(clock);
        if (gymClass.hasStarted(now)) {
            throw new RuleViolationException("Class " + classId + " has already started");
        }
        Membership membership = queries.currentMembership(memberId);
        if (!membership.canBook()) {
            throw new RuleViolationException("Membership " + membership.membershipId() + " can't book a class right now");
        }
        if (!membership.rules().allowsClassAt(gymClass.startTime())) {
            throw new RuleViolationException("Membership " + membership.membershipId() + " doesn't allow a class at this time");
        }
        if (queries.isSuspended(memberId, now)) {
            throw new RuleViolationException("Member " + memberId + " can't book: "
                    + NoShows.LIMIT + " no-shows in " + NoShows.WINDOW_DAYS + " days suspend booking for "
                    + NoShows.SUSPENSION_DAYS + " days");
        }
        if (queries.holdsAPlace(memberId, classId)) {
            throw new RuleViolationException("Member " + memberId + " already has a place in class " + classId);
        }
        if (queries.futureBookings(memberId, now) >= Booking.MAX_FUTURE_BOOKINGS) {
            throw new RuleViolationException("Member " + memberId + " already holds "
                    + Booking.MAX_FUTURE_BOOKINGS + " future bookings");
        }
        if (gymClass.isFull(queries.placesTaken(classId))) {
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
        return bookings.save(Booking.book(newBookingId(), membership.memberId(), gymClass.classId(), now));
    }

    /**
     * Synchronized for the same reason as {@link #book}: both change who holds the places in a class.
     */
    @Override
    public synchronized Booking cancel(String bookingId) {
        Booking booking = queries.findBooking(bookingId);
        GymClass gymClass = queries.classOf(booking);
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
    public synchronized Booking undoBooking(String bookingId) {
        boolean late = queries.classOf(queries.findBooking(bookingId)).isLateToCancel(LocalDateTime.now(clock));
        Booking cancelled = cancel(bookingId);
        if (!late) {
            queries.findCurrentMembership(cancelled.memberId())
                    .ifPresent(membership -> memberships.save(membership.undoBooking()));
        }
        return cancelled;
    }

    @Override
    public synchronized void leaveWaitlist(String memberId, String classId) {
        GymClass gymClass = queries.findClass(classId);
        classes.save(gymClass.leaveWaitlist(memberId));
    }

    @Override
    public synchronized Booking restoreBooking(String bookingId) {
        Booking booking = queries.findBooking(bookingId);
        GymClass gymClass = queries.classOf(booking);
        LocalDateTime now = LocalDateTime.now(clock);
        if (gymClass.hasStarted(now)) {
            throw new RuleViolationException("Class " + gymClass.classId() + " has already started");
        }
        if (gymClass.isFull(queries.placesTaken(gymClass.classId()))) {
            throw new RuleViolationException("The place in class " + gymClass.classId() + " has been taken");
        }
        if (queries.holdsAPlace(booking.memberId(), gymClass.classId())) {
            throw new RuleViolationException("Member " + booking.memberId() + " already has a place in class " + gymClass.classId());
        }
        // Checked as if this booking were handed back, as it still counts towards a Trial's 2 bookings
        boolean canBook = queries.findCurrentMembership(booking.memberId())
                .filter(membership -> canStillBook(membership.undoBooking(), gymClass, now))
                .isPresent();
        if (!canBook) {
            throw new RuleViolationException("Member " + booking.memberId() + " can't book class " + gymClass.classId() + " right now");
        }
        return bookings.save(booking.restore());
    }

    /**
     * BR-B2: the place goes to the first member still able to take it, and they are told. Anyone who can no
     * longer book is taken off the waitlist, so they don't hold up the members behind them.
     */
    private void promoteFromWaitlist(GymClass gymClass, LocalDateTime now) {
        GymClass remaining = gymClass;
        while (remaining.waitlist().size() > 0) {
            String memberId = remaining.waitlist().first().orElseThrow();
            remaining = remaining.promoteFromWaitlist();
            Optional<Membership> next = queries.findCurrentMembership(memberId)
                    .filter(membership -> canStillBook(membership, gymClass, now));
            if (next.isPresent()) {
                classes.save(remaining);
                takePlace(next.get(), gymClass, now);
                // Sent last, so a member is only told once the place is really theirs
                notifications.send(gymClass.promotionNotice(memberId));
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
                && !queries.isSuspended(membership.memberId(), now)
                && queries.futureBookings(membership.memberId(), now) < Booking.MAX_FUTURE_BOOKINGS;
    }

    /**
     * BR-B3. A member who has since left the gym has no membership to charge, so they pay nothing.
     */
    private void chargeLateCancellationFee(String memberId) {
        queries.findCurrentMembership(memberId)
                .filter(Membership::paysLateCancellationFee)
                .ifPresent(membership -> charges.record(
                        membership.membershipId(), ChargeType.LATE_CANCELLATION, LATE_CANCELLATION_FEE));
    }

    private static String newBookingId() {
        return "b-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
