package gym.domain;

/**
 * Strategy for one kind of discount on the plan fee (BR-P2). {@link BestDiscount} picks among them,
 * so a new discount is a new class rather than another branch in the billing code.
 */
public interface DiscountPolicy {

    /**
     * Pre: {@code amount} is zero or positive. Post: {@code 0 <= result <= amount}.
     *
     * @return how much comes off {@code amount}, or {@link Money#ZERO} if the member doesn't qualify
     */
    Money discountFor(BillingAccount account, Money amount);
}
