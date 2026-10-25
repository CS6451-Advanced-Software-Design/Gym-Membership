package gym.port.in;

import gym.domain.Membership;

/**
 * UC5 Freeze / Unfreeze Membership.
 */
public interface FreezeMembershipUseCase {

    /**
     * Freezes an Active membership for 1 to 3 months, at most once in 12 months (BR-M3). It isn't billed while
     * frozen, and its end date moves forward by the same number of months.
     *
     * @throws gym.domain.NotFoundException if there is no such membership
     * @throws gym.domain.RuleViolationException if it isn't Active or the freeze breaks BR-M3
     */
    Membership freeze(String membershipId, int months);

    /**
     * Ends a freeze early. The daily checks end it when the months are up.
     *
     * @throws gym.domain.NotFoundException if there is no such membership
     * @throws gym.domain.RuleViolationException if it isn't Frozen
     */
    Membership unfreeze(String membershipId);
}
