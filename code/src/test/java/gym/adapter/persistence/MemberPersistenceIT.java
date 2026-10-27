package gym.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gym.domain.CheckIn;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class MemberPersistenceIT {

    @TempDir
    Path directory;

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void checkInsSurviveARestart() {
        PersistenceProperties properties = new PersistenceProperties(directory);
        CheckIn checkIn = new CheckIn("ci1", "m1", "ms1", LocalDateTime.of(2026, 11, 2, 9, 30));
        new JsonFileCheckInRepository(properties, mapper).save(checkIn);

        assertEquals(Optional.of(checkIn), new JsonFileCheckInRepository(properties, mapper).findById("ci1"));
    }
}
