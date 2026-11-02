package gym.port.in;

import gym.domain.Booking;
import gym.domain.BookingResult;

/**
 * Booking and cancelling with undo. This is the way in for the API: each action is kept, so the member can take
 * it back with {@link #undoLast}.
 */
public interface BookingCommandsUseCase {

    /**
     * Books a class, as {@link BookClassUseCase#book}, and keeps the action for undo.
     *
     * @return the booking, or the member's place on the waitlist
     */
    BookingResult book(String memberId, String classId);

    /**
     * Cancels a booking, as {@link CancelBookingUseCase#cancel}, and keeps the action for undo.
     *
     * @return the cancelled booking
     */
    Booking cancel(String bookingId);

    /**
     * Takes back the member's most recent booking or cancellation. Calling it again takes back the one before.
     *
     * <p>Pre: the member has an action that hasn't been undone. Post: the action is taken back and forgotten.
     * If it is refused, nothing changes and the action is kept.
     *
     * @throws gym.domain.RuleViolationException if there is nothing to undo, or {@link UndoBookingUseCase} refuses
     */
    void undoLast(String memberId);
}
