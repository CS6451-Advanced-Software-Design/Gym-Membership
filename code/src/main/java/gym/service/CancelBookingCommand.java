package gym.service;

import gym.domain.Booking;
import gym.port.in.CancelBookingUseCase;
import gym.port.in.UndoBookingUseCase;

/**
 * Cancels a booking. Undoing it gives the member the same place back, if nobody has taken it.
 */
class CancelBookingCommand implements BookingCommand<Booking> {

    private final CancelBookingUseCase cancelling;
    private final UndoBookingUseCase undoing;
    private final String bookingId;

    CancelBookingCommand(CancelBookingUseCase cancelling, UndoBookingUseCase undoing, String bookingId) {
        this.cancelling = cancelling;
        this.undoing = undoing;
        this.bookingId = bookingId;
    }

    @Override
    public Booking execute() {
        return cancelling.cancel(bookingId);
    }

    @Override
    public void undo() {
        undoing.restoreBooking(bookingId);
    }
}
