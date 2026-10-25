package gym.port.in;

import gym.domain.Invoice;
import gym.domain.Membership;

/**
 * UC6 Cancel Membership.
 */
public interface CancelMembershipUseCase {

    /**
     * The cancelled membership and its final bill.
     */
    record Cancellation(Membership membership, Invoice finalInvoice) {
    }

    /**
     * Cancels the membership and issues the final bill, with the €50 early-exit fee for an Annual plan
     * cancelled within 6 months (BR-M5).
     *
     * @throws gym.domain.NotFoundException if there is no such membership
     * @throws gym.domain.RuleViolationException if it is already cancelled
     */
    Cancellation cancel(String membershipId);
}
