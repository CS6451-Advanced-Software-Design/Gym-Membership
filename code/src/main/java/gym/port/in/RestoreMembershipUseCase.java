package gym.port.in;

import gym.domain.Membership;

/**
 * UC14 Pay Arrears, from the membership's side: paying what is owed restores a Lapsed membership.
 */
public interface RestoreMembershipUseCase {

    /**
     * Pays every unpaid invoice in one payment and, for a Lapsed membership, makes it Active again (BR-M4).
     *
     * @throws gym.domain.NotFoundException if there is no such membership
     * @throws gym.domain.RuleViolationException if it is Frozen or Cancelled, owes nothing, or the card is declined
     */
    Membership payArrears(String membershipId);
}
