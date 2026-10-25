package gym.dto;

import gym.domain.Charge;
import gym.domain.ChargeType;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One line of an invoice: a fee, a penalty or a credit.
 *
 * @param type what the charge is for
 * @param amount in euro. Fees and penalties are positive, referral credits negative
 * @param date when it was incurred
 */
public record ChargeResponse(ChargeType type, BigDecimal amount, LocalDate date) {

    public static ChargeResponse from(Charge charge) {
        return new ChargeResponse(charge.type(), charge.amount().amount(), charge.date());
    }
}
