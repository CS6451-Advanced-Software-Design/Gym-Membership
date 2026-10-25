package gym.domain;

/**
 * 15% off for members who joined through their employer (BR-P2).
 */
public class CorporateDiscount implements DiscountPolicy {

    @Override
    public Money discountFor(BillingAccount account, Money amount) {
        return account.corporate() ? amount.percent(15) : Money.ZERO;
    }
}
