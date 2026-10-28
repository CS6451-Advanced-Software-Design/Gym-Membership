package gym.domain;


/**
 * Where a {@link Booking} is: it starts BOOKED and then ends in one of the other three.
 */

public enum BookingStatus {
    BOOKED,
    CANCELLED,
    ATTENDED,
    NO_SHOW   
}
