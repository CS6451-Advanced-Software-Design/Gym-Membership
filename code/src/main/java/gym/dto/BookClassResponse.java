package gym.dto;

import gym.domain.BookingResult;
import org.jspecify.annotations.Nullable;

/**
 * What came of a booking request: a place in the class, or a place on its waitlist.
 *
 * @param memberId the member who asked
 * @param classId the class they asked for
 * @param bookingId the new booking, or <code>null</code> if the class was full and they joined the waitlist
 * @param waitlistPosition their place on the waitlist, counting from 1, or 0 if they got a place
 */
public record BookClassResponse(String memberId, String classId, @Nullable String bookingId, int waitlistPosition) {

    public static BookClassResponse from(String memberId, String classId, BookingResult result) {
        String bookingId = result.isWaitlisted() ? null : result.booking().bookingId();
        return new BookClassResponse(memberId, classId, bookingId, result.waitlistPosition());
    }
}
