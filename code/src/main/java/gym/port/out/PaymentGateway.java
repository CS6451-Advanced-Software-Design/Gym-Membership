package gym.port.out;

import gym.domain.Money;

/**
 * Driven port to the external system that takes card payments (the Payment Gateway actor).
 * Used for the first charge at sign-up, the monthly billing run and final bills.
 */
public interface PaymentGateway {

    /**
     * Charges a member's card on file.
     *
     * <p>Pre: {@code amount} is positive. Post: the member is charged exactly once, or not at all.
     *
     * @param memberId the member to charge
     * @param amount what to charge
     * @return {@code true} if the payment went through, {@code false} if it was declined
     * @throws IllegalArgumentException if {@code amount} is zero or negative
     */
    boolean charge(String memberId, Money amount);
}
