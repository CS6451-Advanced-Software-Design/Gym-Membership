package gym.port.in;

import gym.domain.Invoice;
import java.time.LocalDate;

/**
 * Calculate Charges, included by Sign Up (UC1) and Cancel Membership (UC6).
 */
public interface CalculateChargesUseCase {

    /**
     * Bills and charges the first payment at sign-up: the plan fee pro-rated by the days left in the month
     * (BR-P4), or the whole year for an Annual plan, minus the best discount (BR-P2).
     *
     * @throws gym.domain.NotFoundException if the membership has no billing account
     */
    Invoice billFirstMonth(String membershipId, LocalDate joined);

    /**
     * Bills and charges everything still owed when a membership is cancelled, plus the €50 early-exit fee
     * for an Annual plan cancelled within its first 6 months (BR-M5).
     *
     * @throws gym.domain.NotFoundException if the membership has no billing account
     */
    Invoice billFinal(String membershipId);
}
