package gym.adapter.persistence;

import gym.domain.Member;
import gym.port.out.MemberRepository;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class JsonFileMemberRepository extends JsonFileRepository<Member, String> implements MemberRepository {

    public JsonFileMemberRepository(PersistenceProperties properties, JsonMapper mapper) {
        super(properties.directory().resolve("members.json"), Member.class, Member::memberId, mapper);
    }
}
