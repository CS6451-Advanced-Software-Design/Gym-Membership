package gym.port.out;

import gym.domain.Member;
import java.util.Optional;

public interface MemberRepository extends Repository<Member, String> {

    /**
     * @return the member with this email (case-insensitive), or empty if none is stored.
     * Used to recognise someone who left and joins again (UC1).
     */
    default Optional<Member> findByEmail(String email) {
        return findAll().stream().filter(member -> member.email().equalsIgnoreCase(email)).findFirst();
    }
}
