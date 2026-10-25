package gym.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * The Factory pattern: every plan type gets the plan class that knows its rules.
 */
class PlanFactoryTest {

    private static final LocalDate JOINED = LocalDate.of(2026, 11, 2);

    @Test
    void createsThePlanClassForEachType() {
        assertInstanceOf(MonthlyPlan.class, PlanFactory.create(PlanType.MONTHLY));
        assertInstanceOf(AnnualPlan.class, PlanFactory.create(PlanType.ANNUAL));
        assertInstanceOf(StudentPlan.class, PlanFactory.create(PlanType.STUDENT));
        assertInstanceOf(OffPeakPlan.class, PlanFactory.create(PlanType.OFF_PEAK));
    }

    @Test
    void offersEveryPlanType() {
        assertEquals(Arrays.asList(PlanType.values()), PlanFactory.all().stream().map(Plan::type).toList());
    }

    @Test
    void plansKeepThePricesOfBrM1() {
        assertEquals(Money.euros(45), PlanFactory.create(PlanType.MONTHLY).price());
        assertEquals(Money.euros(450), PlanFactory.create(PlanType.ANNUAL).price());
    }

    @Test
    void onlyTheAnnualPlanSkipsTheTrialAndHasAnEndDate() {
        Plan annual = PlanFactory.create(PlanType.ANNUAL);
        Plan monthly = PlanFactory.create(PlanType.MONTHLY);

        assertFalse(annual.hasTrial());
        assertEquals(JOINED.plusYears(1), annual.endDate(JOINED));
        assertTrue(monthly.hasTrial());
        assertNull(monthly.endDate(JOINED));
    }

    @Test
    void theStudentPlanNeedsAnEightDigitStudentId() {
        Plan student = PlanFactory.create(PlanType.STUDENT);

        assertDoesNotThrow(() -> student.checkEligible(member("24123456")));
        assertThrows(RuleViolationException.class, () -> student.checkEligible(member(null)));
        assertThrows(RuleViolationException.class, () -> student.checkEligible(member("abc")));
        assertDoesNotThrow(() -> PlanFactory.create(PlanType.MONTHLY).checkEligible(member(null)));
    }

    @Test
    void offPeakAllowsWeekdaysFromTenToFour() {
        Plan offPeak = PlanFactory.create(PlanType.OFF_PEAK);

        assertTrue(offPeak.allowsCheckIn(LocalDateTime.of(2026, 11, 2, 10, 0)));
        assertTrue(offPeak.allowsCheckIn(LocalDateTime.of(2026, 11, 2, 15, 59)));
        assertFalse(offPeak.allowsCheckIn(LocalDateTime.of(2026, 11, 2, 16, 0)));
        assertFalse(offPeak.allowsCheckIn(LocalDateTime.of(2026, 11, 2, 9, 59)));
        // Saturday
        assertFalse(offPeak.allowsCheckIn(LocalDateTime.of(2026, 11, 7, 11, 0)));
        assertFalse(offPeak.allowsClassAt(LocalDateTime.of(2026, 11, 2, 18, 30)));
        assertTrue(PlanFactory.create(PlanType.MONTHLY).allowsClassAt(LocalDateTime.of(2026, 11, 7, 18, 30)));
    }

    private static Member member(String studentId) {
        return new Member("m1", "Cara", "cara@example.com", studentId, null, null);
    }
}
