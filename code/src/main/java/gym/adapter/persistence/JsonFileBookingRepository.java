package gym.adapter.persistence;

import gym.domain.Booking;
import gym.port.out.BookingRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

/**
 * Keeps bookings in bookings.json. All the reading and writing is done by {@link JsonFileRepository},
 * so this class only says which file to use and how to find a booking's id.
 */
@Repository
public class JsonFileBookingRepository extends JsonFileRepository<Booking, String> implements BookingRepository {

    // Spring passes in both of these. The properties tell us which folder the data lives in
    public JsonFileBookingRepository(PersistenceProperties properties, JsonMapper mapper) {
        super(properties.directory().resolve("bookings.json"), Booking.class, Booking::bookingId, mapper);
    }
}