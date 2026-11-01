package gym.port.out;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.Member;
import gym.support.InMemoryRepository;
import org.junit.jupiter.api.Test;

class MemberRepositoryTest {

    static class InMemoryMemberRepository extends InMemoryRepository<Member, String> implements MemberRepository {
        InMemoryMemberRepository() {
            super(Member::memberId);
        }
    }

    private final MemberRepository members = new InMemoryMemberRepository();

    @Test
    void findsByEmailCaseInsensitively() {
        Member member = new Member("m-1", "Ann Byrne", "ann@example.com", null, null, null);
        members.save(member);

        assertEquals(member, members.findByEmail("ANN@EXAMPLE.COM").orElseThrow());
    }

    @Test
    void findsNothingForAnUnknownEmail() {
        assertTrue(members.findByEmail("nobody@example.com").isEmpty());
    }
}
