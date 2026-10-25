package gym.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class DiscountPolicyTest {

    private static final Money FEE = Money.euros(45);

    private final DiscountPolicy best = new BestDiscount(List.of(new CorporateDiscount(), new FamilyDiscount(), new LoyaltyDiscount()));

    @Test
    void eachPolicyOnlyAppliesToMembersWhoQualify() {
        BillingAccount nobody = account(false, false, 1);

        assertEquals(Money.euros("6.75"), new CorporateDiscount().discountFor(account(true, false, 1), FEE));
        assertEquals(Money.euros("4.50"), new FamilyDiscount().discountFor(account(false, true, 1), FEE));
        assertEquals(Money.euros("2.25"), new LoyaltyDiscount().discountFor(account(false, false, 25), FEE));
        assertEquals(Money.ZERO, new CorporateDiscount().discountFor(nobody, FEE));
        assertEquals(Money.ZERO, new FamilyDiscount().discountFor(nobody, FEE));
        assertEquals(Money.ZERO, new LoyaltyDiscount().discountFor(nobody, FEE));
    }

    @Test
    void loyaltyNeedsMoreThan24Months() {
        assertEquals(Money.ZERO, new LoyaltyDiscount().discountFor(account(false, false, 24), FEE));
    }

    @Test
    void theBestDiscountWinsAndDiscountsDontStack() {
        // BR-P2: corporate (15%), family (10%) and loyalty (5%) all apply, but only the 15% is taken
        BillingAccount everything = account(true, true, 36);

        assertEquals(Money.euros("6.75"), best.discountFor(everything, FEE));
    }

    @Test
    void theBestDiscountIsTheLargestOneTheMemberQualifiesFor() {
        assertEquals(Money.euros("4.50"), best.discountFor(account(false, true, 36), FEE));
        assertEquals(Money.ZERO, best.discountFor(account(false, false, 1), FEE));
    }

    private static BillingAccount account(boolean corporate, boolean familyMember, long monthsAsMember) {
        return new BillingAccount("ms1", "m1", FEE, false, corporate, familyMember, monthsAsMember, false);
    }
}
