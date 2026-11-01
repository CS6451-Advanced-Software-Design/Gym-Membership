package gym.adapter.persistence;

import gym.domain.GymClass;
import gym.port.out.GymClassRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonFileGymClassRepository extends JsonFileRepository<GymClass, String> implements GymClassRepository {

    public JsonFileGymClassRepository(PersistenceProperties properties, JsonMapper mapper) {
        super(properties.directory().resolve("classes.json"), GymClass.class, GymClass::classId, mapper);
    }
}