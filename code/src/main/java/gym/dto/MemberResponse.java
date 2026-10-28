package gym.dto;

import gym.domain.Member;
import org.jspecify.annotations.Nullable;

/**
 * A member's profile.
 *
 * @param memberId the member id
 * @param name their full name
 * @param email where notifications go
 * @param studentId their student ID for the Student plan, or null
 * @param employer their employer for the Corporate discount, or null
 * @param familyId shared by members of one family for the Family discount, or null
 */
public record MemberResponse(
        String memberId,
        String name,
        String email,
        @Nullable String studentId,
        @Nullable String employer,
        @Nullable String familyId) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.memberId(), member.name(), member.email(),
                member.studentId(), member.employer(), member.familyId());
    }
}
