package gym.domain;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The bill for one membership and one month, grouping the charges billed on it (BR-P1):
 * the plan fee, minus the best discount, plus penalties, minus referral credits.
 *
 * @param id the invoice id
 * @param membershipId the membership it bills
 * @param period the month it bills
 * @param charges the charges on it
 * @param discount taken off the plan fee
 * @param total what the member pays, never negative
 * @param status whether the payment went through
 */
public record Invoice(
        String id,
        String membershipId,
        YearMonth period,
        List<Charge> charges,
        Money discount,
        Money total,
        InvoiceStatus status) {

    public Invoice {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(membershipId, "membershipId");
        Objects.requireNonNull(period, "period");
        Objects.requireNonNull(discount, "discount");
        Objects.requireNonNull(total, "total");
        Objects.requireNonNull(status, "status");
        charges = List.copyOf(charges);
    }

    /**
     * Pre: the charges' credits don't exceed what is owed. Post: an unpaid invoice, or a paid one if nothing is due.
     */
    public static Invoice issue(String id, String membershipId, YearMonth period, List<Charge> charges, Money discount) {
        List<Charge> billed = new ArrayList<>();
        Money total = discount.negate();
        for (Charge charge : charges) {
            billed.add(charge.billedOn(id));
            total = total.plus(charge.amount());
        }
        if (total.isNegative()) {
            throw new IllegalArgumentException("Invoice " + id + " would total " + total);
        }
        InvoiceStatus status = total.isZero() ? InvoiceStatus.PAID : InvoiceStatus.UNPAID;
        return new Invoice(id, membershipId, period, billed, discount, total, status);
    }

    public Invoice settled(boolean paid) {
        return new Invoice(id, membershipId, period, charges, discount, total, paid ? InvoiceStatus.PAID : InvoiceStatus.UNPAID);
    }
}
