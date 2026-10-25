package gym.domain;

/**
 * What a {@link Charge} is for (BR-P1). Every kind is a debit except {@link #REFERRAL_CREDIT}.
 */
public enum ChargeType {
    PLAN_FEE,
    PREMIUM_SURCHARGE,
    LATE_CANCELLATION,
    NO_SHOW,
    EARLY_EXIT,
    REFERRAL_CREDIT;

    public boolean isCredit() {
        return this == REFERRAL_CREDIT;
    }
}
