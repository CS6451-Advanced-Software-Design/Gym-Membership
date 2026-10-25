package gym.domain;

import java.time.LocalDate;

/**
 * BR-M3: not billed, and no booking or check-in until the freeze is over.
 */
public class FrozenState extends MembershipState {

    @Override
    public MembershipStatus status() {
        return MembershipStatus.FROZEN;
    }

    @Override
    public Membership unfreeze(Membership membership) {
        return membership.moveTo(MembershipStatus.ACTIVE);
    }

    @Override
    public Membership cancel(Membership membership) {
        return membership.moveTo(MembershipStatus.CANCELLED);
    }

    /**
     * A membership frozen without a recorded freeze stays frozen until it is unfrozen by hand.
     */
    @Override
    public Membership checkOn(Membership membership, LocalDate today) {
        Freeze freeze = membership.currentFreeze();
        return freeze == null || today.isBefore(freeze.endDate()) ? membership : unfreeze(membership);
    }
}
