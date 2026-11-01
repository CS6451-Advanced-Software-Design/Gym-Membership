package gym.domain;

/**
 * What came of a booking request: the member either got a place or joined the waitlist (BR-B1).
 *
 * @param booking the booking, or null if the member joined the waitlist
 * @param waitlistPosition their place in the line, counting from 1, or 0 if they got a place
 */
public record BookingResult(Booking booking, int waitlistPosition) {

    public static BookingResult booked(Booking booking) {
        return new BookingResult(booking, 0);
    }

    public static BookingResult waitlisted(int position) {
        return new BookingResult(null, position);
    }

    /**
     * True when the class was full and the member is waiting for a place.
     */
    public boolean isWaitlisted() {
        return booking == null;
    }
}
