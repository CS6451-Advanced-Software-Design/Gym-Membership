package gym.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void keepsTwoDecimalPlacesSoEqualAmountsAreEqual() {
        assertEquals(Money.euros("45.00"), Money.euros(45));
        assertEquals(new BigDecimal("45.00"), Money.euros(45).amount());
    }

    @Test
    void roundsHalfUpToTheCent() {
        assertEquals(Money.euros("0.13"), Money.euros("0.125"));
    }

    @Test
    void addsSubtractsAndMultiplies() {
        Money plan = Money.euros(45);

        assertEquals(Money.euros(50), plan.plus(Money.euros(5)));
        assertEquals(Money.euros(25), plan.minus(Money.euros(20)));
        assertEquals(Money.euros(450), plan.times(10));
        assertEquals(Money.euros("38.25"), plan.times(new BigDecimal("0.85")));
    }

    @Test
    void takesAPercentage() {
        // BR-P2: a 15% corporate discount on the €45 monthly plan
        assertEquals(Money.euros("6.75"), Money.euros(45).percent(15));
    }

    @Test
    void proRatesTheDaysLeftInAMonth() {
        // BR-P4: joining with 12 days left of a 30-day month
        assertEquals(Money.euros(18), Money.euros(45).proRata(12, 30));
        assertEquals(Money.euros("14.52"), Money.euros(45).proRata(10, 31));
    }

    @Test
    void refusesToProRateMoreDaysThanThePeriodHas() {
        assertThrows(IllegalArgumentException.class, () -> Money.euros(45).proRata(31, 30));
        assertThrows(IllegalArgumentException.class, () -> Money.euros(45).proRata(1, 0));
    }

    @Test
    void canGoNegativeForCredits() {
        Money credit = Money.euros(20).negate();

        assertTrue(credit.isNegative());
        assertEquals(Money.euros(25), Money.euros(45).plus(credit));
        assertEquals("-€20.00", credit.toString());
    }

    @Test
    void comparesAmounts() {
        // BR-P3: a referral credit is capped by what is left of the €60 yearly limit
        assertEquals(Money.euros(10), Money.euros(20).min(Money.euros(10)));
        assertEquals(Money.euros(20), Money.euros(20).max(Money.euros(10)));
        assertTrue(Money.euros(8).isGreaterThan(Money.euros(5)));
        assertFalse(Money.ZERO.isPositive());
        assertTrue(Money.ZERO.isZero());
    }

    @Test
    void printsInEuro() {
        assertEquals("€45.00", Money.euros(45).toString());
    }
}
