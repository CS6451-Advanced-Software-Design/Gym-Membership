package gym.service;

import gym.domain.BookingResult;
import gym.port.in.BookClassUseCase;
import gym.port.in.UndoBookingUseCase;

/**
 * Books a class. Undoing it gives up the place, or leaves the waitlist if the class was full.
 */
class BookClassCommand implements BookingCommand<BookingResult> {

    private final BookClassUseCase booking;
    private final UndoBookingUseCase undoing;
    private final String memberId;
    private final String classId;
    private BookingResult result;

    BookClassCommand(BookClassUseCase booking, UndoBookingUseCase undoing, String memberId, String classId) {
        this.booking = booking;
        this.undoing = undoing;
        this.memberId = memberId;
        this.classId = classId;
    }

    @Override
    public BookingResult execute() {
        result = booking.book(memberId, classId);
        return result;
    }

    @Override
    public void undo() {
        if (result.isWaitlisted()) {
            undoing.leaveWaitlist(memberId, classId);
        } else {
            undoing.undoBooking(result.booking().bookingId());
        }
    }
}
