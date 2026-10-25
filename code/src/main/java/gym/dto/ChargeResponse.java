package gym.dto;

import gym.domain.Charge;
import gym.domain.ChargeType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ChargeResponse(ChargeType type, BigDecimal amount, LocalDate date) {

    public static ChargeResponse from(Charge charge) {
        return new ChargeResponse(charge.type(), charge.amount().amount(), charge.date());
    }
}
