package gym.adapter.persistence;

import gym.domain.CheckIn;
import gym.port.out.CheckInRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonFileCheckInRepository extends JsonFileRepository<CheckIn, String> implements CheckInRepository {

    public JsonFileCheckInRepository(PersistenceProperties properties, JsonMapper mapper) {
        super(properties.directory().resolve("check-ins.json"), CheckIn.class, CheckIn::checkInId, mapper);
    }
}
