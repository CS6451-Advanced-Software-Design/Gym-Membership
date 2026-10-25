package gym.dto;

import gym.domain.Membership;
import gym.domain.Plan;
import gym.domain.PlanType;
import java.math.BigDecimal;

/**
 * A plan on offer.
 *
 * @param plan the plan to ask for when signing up
 * @param price in euro, per month, or per year when <code>paidYearly</code>
 * @param paidYearly <code>true</code> if the year is paid upfront
 * @param trialDays how long the trial lasts, 0 if there is none
 * @param description who the plan is for and when it can be used
 */
public record PlanResponse(PlanType plan, BigDecimal price, boolean paidYearly, int trialDays, String description) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(plan.type(), plan.price().amount(), !plan.hasTrial(),
                plan.hasTrial() ? Membership.TRIAL_DAYS : 0, plan.description());
    }
}
