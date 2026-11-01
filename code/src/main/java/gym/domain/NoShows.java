package gym.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A member's no-shows, and what they mean for booking (BR-B4): 3 in 30 days suspend booking for 7 days.
 * Each no-show is dated by when its class started, because a {@link Booking} has no date of its own for one.
 *
 * @param classStarts when each missed class started
 */
public record NoShows(List<LocalDateTime> classStarts) {

    public static final int LIMIT = 3;
    public static final int WINDOW_DAYS = 30;
    public static final int SUSPENSION_DAYS = 7;

    public NoShows {
        classStarts = classStarts.stream().sorted().toList();
    }

    /**
     * Pre: none. Post: true while a run of 3 no-shows inside 30 days is less than 7 days old at {@code now}.
     */
    public boolean suspendBookingAt(LocalDateTime now) {
        for (int last = LIMIT - 1; last < classStarts.size(); last++) {
            LocalDateTime third = classStarts.get(last);
            LocalDateTime first = classStarts.get(last - LIMIT + 1);
            if (!third.isAfter(first.plusDays(WINDOW_DAYS)) && now.isBefore(third.plusDays(SUSPENSION_DAYS))) {
                return true;
            }
        }
        return false;
    }
}
