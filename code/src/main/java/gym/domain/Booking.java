package gym.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * One member's place in one class. A booking never changes. Each operation returns the new one,
 * the same way {@link Membership} does.
 *
 * @param bookingId the booking id
 * @param memberId the member who holds the place
 * @param classId the class it is for
 * @param status where it is: booked, cancelled, attended or a no-show
 * @param bookedAt when the member booked, or when they were promoted from the waitlist
 */
public record Booking(String bookingId, String memberId, String classId, BookingStatus status, LocalDateTime bookedAt) {

    public Booking {
        Objects.requireNonNull(bookingId, "bookingId");
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(classId, "classId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(bookedAt, "bookedAt");
    }

    /**
     * A new booking, holding a place.
     */
    public static Booking book(String bookingId, String memberId, String classId, LocalDateTime at) {
        return new Booking(bookingId, memberId, classId, BookingStatus.BOOKED, at);
    }

    /**
     * True while the booking holds a place in the class.
     */
    public boolean isBooked() {
        return status == BookingStatus.BOOKED;
    }

    /**
     * Pre: the booking is BOOKED. Post: it is CANCELLED and no longer holds a place.
     *
     * @throws RuleViolationException if it was already cancelled, attended or a no-show
     */
    public Booking cancel() {
        return moveTo(BookingStatus.CANCELLED, "cancelled");
    }

    /**
     * Pre: the booking is BOOKED. Post: it is ATTENDED.
     *
     * @throws RuleViolationException if it is not BOOKED
     */
    public Booking attend() {
        return moveTo(BookingStatus.ATTENDED, "marked as attended");
    }

    /**
     * Pre: the booking is BOOKED. Post: it is NO_SHOW (BR-B4).
     *
     * @throws RuleViolationException if it is not BOOKED
     */
    public Booking noShow() {
        return moveTo(BookingStatus.NO_SHOW, "marked as a no-show");
    }

    private Booking moveTo(BookingStatus next, String action) {
        if (!isBooked()) {
            throw new RuleViolationException("Booking " + bookingId + " is " + status + ", so it can't be " + action);
        }
        return new Booking(bookingId, memberId, classId, next, bookedAt);
    }
}