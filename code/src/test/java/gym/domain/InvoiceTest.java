package gym.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 1);
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    @Test
    void totalIsThePlanFeeMinusTheDiscountPlusPenaltiesMinusCredits() {
        // BR-P1: €45 - €6.75 + €5 + €8 - €20 = €31.25
        Invoice invoice = Invoice.issue("i1", "m1", NOVEMBER, List.of(
                charge(ChargeType.PLAN_FEE, "45"),
                charge(ChargeType.NO_SHOW, "5"),
                charge(ChargeType.PREMIUM_SURCHARGE, "8"),
                charge(ChargeType.REFERRAL_CREDIT, "-20")), Money.euros("6.75"));

        assertEquals(Money.euros("31.25"), invoice.total());
        assertEquals(InvoiceStatus.UNPAID, invoice.status());
        assertTrue(invoice.charges().stream().allMatch(charge -> "i1".equals(charge.invoiceId())));
    }

    @Test
    void anInvoiceWithNothingDueIsAlreadyPaid() {
        Invoice invoice = Invoice.issue("i1", "m1", NOVEMBER, List.of(
                charge(ChargeType.NO_SHOW, "5"),
                charge(ChargeType.REFERRAL_CREDIT, "-5")), Money.ZERO);

        assertEquals(InvoiceStatus.PAID, invoice.status());
    }

    @Test
    void refusesCreditsLargerThanWhatIsOwed() {
        List<Charge> charges = List.of(charge(ChargeType.NO_SHOW, "5"), charge(ChargeType.REFERRAL_CREDIT, "-20"));

        assertThrows(IllegalArgumentException.class, () -> Invoice.issue("i1", "m1", NOVEMBER, charges, Money.ZERO));
    }

    @Test
    void aChargeMustHaveTheRightSign() {
        assertThrows(IllegalArgumentException.class, () -> charge(ChargeType.NO_SHOW, "-5"));
        assertThrows(IllegalArgumentException.class, () -> charge(ChargeType.REFERRAL_CREDIT, "20"));
    }

    private static Charge charge(ChargeType type, String amount) {
        return Charge.pending(type + amount, "m1", type, Money.euros(amount), TODAY);
    }
}
