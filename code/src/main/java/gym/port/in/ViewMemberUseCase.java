package gym.port.in;

import gym.domain.Member;

/**
 * Shows a member's profile.
 */
public interface ViewMemberUseCase {

    /**
     * @throws gym.domain.NotFoundException if there is no such member
     */
    Member member(String memberId);
}
