package gym.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * One amount owed by or credited to a membership, e.g. a €5 no-show penalty or a €20 referral credit.
 * It waits for the next invoice, then records which invoice it went on.
 *
 * @param id the charge id
 * @param membershipId the membership it belongs to
 * @param type what it is for
 * @param amount positive for a fee, negative for a credit
 * @param date when it was incurred
 * @param invoiceId the invoice it went on, or {@code null} while it waits for the next one
 */
public record Charge(String id, String membershipId, ChargeType type, Money amount, LocalDate date, String invoiceId) {

    public Charge {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(membershipId, "membershipId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(date, "date");
        boolean rightSign = type.isCredit() ? amount.isNegative() : amount.isPositive();
        if (!rightSign) {
            throw new IllegalArgumentException("A " + type + " charge cannot be " + amount);
        }
    }

    /**
     * A new charge, not yet on an invoice.
     */
    public static Charge pending(String id, String membershipId, ChargeType type, Money amount, LocalDate date) {
        return new Charge(id, membershipId, type, amount, date, null);
    }

    public boolean isBilled() {
        return invoiceId != null;
    }

    public Charge billedOn(String invoiceId) {
        return new Charge(id, membershipId, type, amount, date, invoiceId);
    }
}
