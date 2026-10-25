package gym.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * What a plan allows and how long it runs (BR-M1). Each plan type is a subclass, made by {@link PlanFactory},
 * so a new plan is a new subclass and one line in the factory, with no change to Membership (QA1).
 */
public abstract class Plan {

    private final PlanType type;

    protected Plan(PlanType type) {
        this.type = type;
    }

    public PlanType type() {
        return type;
    }

    /**
     * Per month, or per year for a plan paid upfront.
     */
    public Money price() {
        return type.price();
    }

    /**
     * BR-M2: plans paid monthly start with a 7-day Trial.
     */
    public boolean hasTrial() {
        return true;
    }

    /**
     * When a membership on this plan ends, or {@code null} if it rolls on month by month.
     */
    public LocalDate endDate(LocalDate start) {
        return null;
    }

    public boolean allowsCheckIn(LocalDateTime at) {
        return true;
    }

    /**
     * BR-B5: a class can be booked only if the plan lets the member in when it starts.
     */
    public boolean allowsClassAt(LocalDateTime start) {
        return allowsCheckIn(start);
    }

    /**
     * Pre: none. Post: returns only if the member may join this plan.
     *
     * @throws RuleViolationException if they may not, e.g. a Student plan without a student ID
     */
    public void checkEligible(Member member) {
    }

    /**
     * Who the plan is for and when it can be used, in a sentence.
     */
    public abstract String description();
}
