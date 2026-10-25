package gym.adapter.persistence;

import gym.domain.Charge;
import gym.port.out.ChargeRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonFileChargeRepository extends JsonFileRepository<Charge, String> implements ChargeRepository {

    public JsonFileChargeRepository(PersistenceProperties properties, JsonMapper mapper) {
        super(properties.directory().resolve("charges.json"), Charge.class, Charge::id, mapper);
    }
}
