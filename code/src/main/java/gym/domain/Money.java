package gym.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An amount in euro, always kept to 2 decimal places. Every price, fee, discount and credit uses it,
 * so no feature does its own rounding.
 *
 * <p>Amounts can be negative, e.g. a referral credit on an invoice.
 * Results are rounded half up to the cent.
 *
 * @param amount the amount in euro
 */
public record Money(BigDecimal amount) implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private static final int CENTS = 2;

    public Money {
        Objects.requireNonNull(amount, "amount");
        amount = amount.setScale(CENTS, RoundingMode.HALF_UP);
    }

    /**
     * @param amount e.g. {@code "45"} or {@code "7.50"}
     */
    public static Money euros(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money euros(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money times(long factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)));
    }

    /**
     * @param factor e.g. {@code 0.85} for 15% off, or {@code 12/30} to pro-rate 12 days of a 30-day month
     */
    public Money times(BigDecimal factor) {
        return new Money(amount.multiply(factor));
    }

    /**
     * @param percent e.g. {@code 15} for a 15% discount
     * @return that percentage of this amount, e.g. €6.75 for 15% of €45.00
     */
    public Money percent(int percent) {
        return new Money(amount.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), CENTS, RoundingMode.HALF_UP));
    }

    /**
     * Pro-rates a monthly amount, e.g. a member joining with 12 days left of a 30-day month pays 12/30.
     *
     * @param days the days to charge for
     * @param daysInPeriod the days in the whole period
     */
    public Money proRata(int days, int daysInPeriod) {
        if (daysInPeriod <= 0 || days < 0 || days > daysInPeriod) {
            throw new IllegalArgumentException("Cannot pro-rate " + days + " of " + daysInPeriod + " days");
        }
        return new Money(amount.multiply(BigDecimal.valueOf(days)).divide(BigDecimal.valueOf(daysInPeriod), CENTS, RoundingMode.HALF_UP));
    }

    public Money negate() {
        return new Money(amount.negate());
    }

    /**
     * The smaller of the two, e.g. to cap a referral credit at what is left of the yearly limit.
     */
    public Money min(Money other) {
        return compareTo(other) <= 0 ? this : other;
    }

    public Money max(Money other) {
        return compareTo(other) >= 0 ? this : other;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isGreaterThan(Money other) {
        return compareTo(other) > 0;
    }

    @Override
    public int compareTo(Money other) {
        return amount.compareTo(other.amount);
    }

    @Override
    public String toString() {
        return (isNegative() ? "-€" : "€") + amount.abs().toPlainString();
    }
}
