package gym.port.in;

import gym.domain.Booking;

/**
 * UC8: Record Attendance. Says whether a member came to a class they had booked.
 */
public interface RecordAttendanceUseCase {

    /**
     * Records that the member came to the class.
     *
     * <p>Pre: the booking exists, is BOOKED, and its class has started. Post: the booking is ATTENDED.
     *
     * @return the updated booking
     * @throws gym.domain.NotFoundException if there is no such booking
     * @throws gym.domain.RuleViolationException if the booking isn't BOOKED or its class hasn't started
     */
    Booking markAttended(String bookingId);

    /**
     * Records that the member didn't come, which costs a no-show fee (BR-B4).
     *
     * <p>Pre: the booking exists, is BOOKED, and its class has started. Post: the booking is NO_SHOW and the
     * fee is charged to the member's current membership, if they still have one.
     *
     * @return the updated booking
     * @throws gym.domain.NotFoundException if there is no such booking
     * @throws gym.domain.RuleViolationException if the booking isn't BOOKED or its class hasn't started
     */
    Booking markNoShow(String bookingId);
}
