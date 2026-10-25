package gym.domain;

import java.util.List;

/**
 * Discounts don't stack: asks every policy and applies only the largest (BR-P2).
 */
public class BestDiscount implements DiscountPolicy {

    private final List<DiscountPolicy> policies;

    public BestDiscount(List<DiscountPolicy> policies) {
        this.policies = List.copyOf(policies);
    }

    @Override
    public Money discountFor(BillingAccount account, Money amount) {
        Money best = Money.ZERO;
        for (DiscountPolicy policy : policies) {
            best = best.max(policy.discountFor(account, amount));
        }
        return best;
    }
}
