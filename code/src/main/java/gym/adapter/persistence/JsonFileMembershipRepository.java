package gym.adapter.persistence;

import gym.domain.Membership;
import gym.port.out.MembershipRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonFileMembershipRepository extends JsonFileRepository<Membership, String> implements MembershipRepository {

    public JsonFileMembershipRepository(PersistenceProperties properties, JsonMapper mapper) {
        super(properties.directory().resolve("memberships.json"), Membership.class, Membership::membershipId, mapper);
    }
}
