package gym.port.in;

import gym.domain.Booking;

/**
 * What the booking commands need to take an action back. No fee is refunded by any of these, because Billing
 * refuses credits (see {@link RecordChargeUseCase#record}).
 */
public interface UndoBookingUseCase {

    /**
     * Takes back a booking. It works like {@link CancelBookingUseCase#cancel}, so the late-cancellation fee still
     * applies and undo can't be used to avoid it. A Trial booking is also handed back, unless it is late, because
     * a late one still counts against the Trial's 2 bookings (BR-B3).
     *
     * <p>Pre: as for {@code cancel}. Post: the booking is CANCELLED.
     *
     * @return the cancelled booking
     * @throws gym.domain.NotFoundException if there is no such booking
     * @throws gym.domain.RuleViolationException if the booking isn't BOOKED or its class has started
     */
    Booking undoBooking(String bookingId);

    /**
     * Pre: the member is on the class's waitlist. Post: they aren't, and everyone behind them has moved up.
     *
     * @throws gym.domain.NotFoundException if there is no such class
     * @throws gym.domain.RuleViolationException if the member isn't waiting, e.g. they have since been promoted
     */
    void leaveWaitlist(String memberId, String classId);

    /**
     * Gives a member back a cancelled booking. It is the same booking, so nothing is charged again.
     *
     * <p>Pre: the booking is CANCELLED, its class hasn't started and still has a place, and the member could book
     * it. Post: the booking is BOOKED.
     *
     * @return the restored booking
     * @throws gym.domain.NotFoundException if there is no such booking
     * @throws gym.domain.RuleViolationException if any of those checks fail, e.g. the place went to the waitlist
     */
    Booking restoreBooking(String bookingId);
}
