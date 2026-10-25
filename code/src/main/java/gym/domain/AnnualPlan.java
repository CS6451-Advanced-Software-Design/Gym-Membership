package gym.domain;

import java.time.LocalDate;

/**
 * Paid for the year upfront, so there is no Trial (BR-M2) and the membership ends after a year.
 */
public class AnnualPlan extends Plan {

    public AnnualPlan() {
        super(PlanType.ANNUAL);
    }

    @Override
    public boolean hasTrial() {
        return false;
    }

    @Override
    public LocalDate endDate(LocalDate start) {
        return start.plusYears(1);
    }

    @Override
    public String description() {
        return "Any time, paid for the year upfront, no trial. Cancelling within 6 months costs €50";
    }
}
