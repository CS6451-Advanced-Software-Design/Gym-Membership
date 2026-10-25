package gym.domain;

/**
 * 10% off for the second and later members of a family (BR-P2).
 */
public class FamilyDiscount implements DiscountPolicy {

    @Override
    public Money discountFor(BillingAccount account, Money amount) {
        return account.familyMember() ? amount.percent(10) : Money.ZERO;
    }
}
