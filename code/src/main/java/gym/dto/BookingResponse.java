package gym.dto;

import gym.domain.Booking;
import gym.domain.BookingStatus;
import java.time.LocalDateTime;

/**
 * A member's place in a class.
 *
 * @param bookingId the booking id
 * @param memberId the member who holds the place
 * @param classId the class it is for
 * @param status where it is: booked, cancelled, attended or a no-show
 * @param bookedAt when the member booked, or when they were promoted from the waitlist
 */
public record BookingResponse(String bookingId, String memberId, String classId, BookingStatus status, LocalDateTime bookedAt) {

    public static BookingResponse from(Booking booking) {
        return new BookingResponse(booking.bookingId(), booking.memberId(), booking.classId(), booking.status(), booking.bookedAt());
    }
}
