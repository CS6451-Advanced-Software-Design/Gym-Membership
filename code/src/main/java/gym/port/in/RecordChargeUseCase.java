package gym.port.in;

import gym.domain.Charge;
import gym.domain.ChargeType;
import gym.domain.Money;

/**
 * Adds charges and credits to a membership's next invoice, for the features that cause them.
 */
public interface RecordChargeUseCase {

    /**
     * Records a fee or penalty, e.g. a late cancellation (BR-B3), a no-show (BR-B4) or a premium surcharge (BR-B6).
     *
     * @throws gym.domain.RuleViolationException if {@code type} is a credit or {@code amount} isn't positive
     */
    Charge record(String membershipId, ChargeType type, Money amount);

    /**
     * Credits the referrer's membership €20 for a successful referral, capped at €60 per calendar year (BR-P3).
     *
     * @return the credit given, {@link Money#ZERO} once the cap is reached
     */
    Money creditReferral(String membershipId);
}
