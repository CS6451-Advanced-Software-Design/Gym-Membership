package gym.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import gym.domain.Booking;
import gym.domain.BookingStatus;
import gym.domain.GymClass;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class BookingPersistenceIT {

    private static final LocalDateTime SPIN_STARTS = LocalDateTime.of(2026, 11, 6, 18, 0);

    @TempDir
    Path directory;

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void aClassKeepsItsWaitlistOrderAfterARestart() {
        PersistenceProperties properties = new PersistenceProperties(directory);
        GymClass spin = GymClass.schedule("c1", "Spin", SPIN_STARTS, 1, true)
                .joinWaitlist("m3")
                .joinWaitlist("m1")
                .joinWaitlist("m2");
        new JsonFileGymClassRepository(properties, mapper).save(spin);

        // A new repository has nothing in memory, so it has to read the file
        Optional<GymClass> reloaded = new JsonFileGymClassRepository(properties, mapper).findById("c1");

        assertEquals(Optional.of(spin), reloaded);
        assertEquals(List.of("m3", "m1", "m2"), reloaded.orElseThrow().waitlist().memberIds());
        assertEquals(Optional.of("m3"), reloaded.orElseThrow().waitlist().first());
    }

    @Test
    void aBookingKeepsItsStatusAfterARestart() {
        PersistenceProperties properties = new PersistenceProperties(directory);
        Booking cancelled = Booking.book("b1", "m1", "c1", SPIN_STARTS.minusDays(2)).cancel();
        new JsonFileBookingRepository(properties, mapper).save(cancelled);

        Optional<Booking> reloaded = new JsonFileBookingRepository(properties, mapper).findById("b1");

        assertEquals(Optional.of(cancelled), reloaded);
        assertEquals(BookingStatus.CANCELLED, reloaded.orElseThrow().status());
        assertFalse(reloaded.orElseThrow().isBooked());
    }
}