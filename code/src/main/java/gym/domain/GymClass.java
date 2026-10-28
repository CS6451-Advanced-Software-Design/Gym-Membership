package gym.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A class on the timetable, e.g. Spin on Friday at 18:00. It knows its capacity and who is waiting for a place
 * (BR-B1), but not who has booked: that is kept in the {@link Booking}s. A class never changes.
 * Each operation returns the new one.
 *
 * @param classId the class id
 * @param name what the class is, e.g. "Spin"
 * @param startTime when it starts
 * @param capacity how many places it has
 * @param premium whether it carries the premium surcharge (BR-B6)
 * @param waitlist the members waiting for a place
 */
public record GymClass(String classId, String name, LocalDateTime startTime, int capacity, boolean premium, Waitlist waitlist) {

    /**
     * BR-B3: cancelling closer to the start than this costs a late-cancellation fee.
     */
    public static final int LATE_CANCELLATION_HOURS = 2;

    public GymClass {
        Objects.requireNonNull(classId, "classId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(startTime, "startTime");
        if (capacity < 1) {
            throw new IllegalArgumentException("A class needs at least 1 place, not " + capacity);
        }
        waitlist = waitlist == null ? Waitlist.empty() : waitlist;
    }

    /**
     * A new class with nobody waiting.
     */
    public static GymClass schedule(String classId, String name, LocalDateTime startTime, int capacity, boolean premium) {
        return new GymClass(classId, name, startTime, capacity, premium, Waitlist.empty());
    }

    /**
     * Pre: {@code booked} is the number of active bookings for this class. Post: true when no place is left.
     */
    public boolean isFull(int booked) {
        return booked >= capacity;
    }

    public boolean hasStarted(LocalDateTime now) {
        return !now.isBefore(startTime);
    }

    /**
     * BR-B3: true when {@code now} is less than 2 hours before the start (or later).
     */
    public boolean isLateToCancel(LocalDateTime now) {
        return now.isAfter(startTime.minusHours(LATE_CANCELLATION_HOURS));
    }

    /**
     * Pre: the member is not already waiting. Post: they are last on the waitlist.
     *
     * @throws RuleViolationException if the member is already waiting
     */
    public GymClass joinWaitlist(String memberId) {
        return withWaitlist(waitlist.add(memberId));
    }

    /**
     * Pre: none. Post: the member who waited longest is off the waitlist. Their booking is made by the caller.
     */
    public GymClass promoteFromWaitlist() {
        return withWaitlist(waitlist.withoutFirst());
    }

    private GymClass withWaitlist(Waitlist next) {
        return new GymClass(classId, name, startTime, capacity, premium, next);
    }
}