package gym.service;

import gym.domain.Booking;
import gym.domain.BookingResult;
import gym.domain.RuleViolationException;
import gym.port.in.BookClassUseCase;
import gym.port.in.BookingCommandsUseCase;
import gym.port.in.CancelBookingUseCase;
import gym.port.in.UndoBookingUseCase;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * The invoker of the Command pattern: runs each {@link BookingCommand} and keeps it for undo. Every member has
 * their own history, so one member can't undo another's action. The history is kept in memory, so a restart
 * clears it. The bookings themselves are safe in their repository.
 */
@Service
public class BookingCommandService implements BookingCommandsUseCase {

    private final BookClassUseCase booking;
    private final CancelBookingUseCase cancelling;
    private final UndoBookingUseCase undoing;
    private final Map<String, Deque<BookingCommand<?>>> histories = new HashMap<>();

    public BookingCommandService(BookClassUseCase booking, CancelBookingUseCase cancelling, UndoBookingUseCase undoing) {
        this.booking = booking;
        this.cancelling = cancelling;
        this.undoing = undoing;
    }

    @Override
    public synchronized BookingResult book(String memberId, String classId) {
        BookClassCommand command = new BookClassCommand(booking, undoing, memberId, classId);
        BookingResult result = command.execute();
        remember(memberId, command);
        return result;
    }

    @Override
    public synchronized Booking cancel(String bookingId) {
        CancelBookingCommand command = new CancelBookingCommand(cancelling, undoing, bookingId);
        Booking cancelled = command.execute();
        remember(cancelled.memberId(), command);
        return cancelled;
    }

    /**
     * The command is only dropped once its undo has worked, so a refused undo can be tried again, e.g. if the
     * place comes free.
     */
    @Override
    public synchronized void undoLast(String memberId) {
        Deque<BookingCommand<?>> history = histories.getOrDefault(memberId, new ArrayDeque<>());
        if (history.isEmpty()) {
            throw new RuleViolationException("Member " + memberId + " has nothing to undo");
        }
        history.peek().undo();
        history.pop();
    }

    private void remember(String memberId, BookingCommand<?> command) {
        histories.computeIfAbsent(memberId, member -> new ArrayDeque<>()).push(command);
    }
}
