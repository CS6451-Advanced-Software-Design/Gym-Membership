package gym.domain;

/**
 * The final state: every operation is refused.
 */
public class CancelledState extends MembershipState {

    @Override
    public MembershipStatus status() {
        return MembershipStatus.CANCELLED;
    }
}
