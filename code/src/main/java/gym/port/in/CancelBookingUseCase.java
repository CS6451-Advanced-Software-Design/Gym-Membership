package gym.port.in;

import gym.domain.Booking;

/**
 * UC3: Cancel Booking. Gives up a member's place in a class.
 */
public interface CancelBookingUseCase {

    /**
     * Cancels the booking, which frees its place in the class. Less than 2 hours before the class this costs
     * a late-cancellation fee, unless the member is on a Trial (BR-B3).
     *
     * <p>Pre: the booking exists, is BOOKED, and its class hasn't started. Post: the booking is CANCELLED
     * and no longer holds a place.
     *
     * @return the cancelled booking
     * @throws gym.domain.NotFoundException if there is no such booking
     * @throws gym.domain.RuleViolationException if the booking isn't BOOKED or its class has started
     */
    Booking cancel(String bookingId);
}
