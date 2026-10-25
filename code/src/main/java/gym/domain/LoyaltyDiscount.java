package gym.domain;

/**
 * 5% off for members of more than 24 months (BR-P2).
 */
public class LoyaltyDiscount implements DiscountPolicy {

    @Override
    public Money discountFor(BillingAccount account, Money amount) {
        return account.monthsAsMember() > 24 ? amount.percent(5) : Money.ZERO;
    }
}
