package gym.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Factory pattern: turns a stored {@link PlanType} into the {@link Plan} that knows its rules, so no other
 * class picks a plan class itself. A new plan (QA1) is registered here and nowhere else.
 */
public final class PlanFactory {

    private static final Map<PlanType, Supplier<Plan>> PLANS = new EnumMap<>(Map.of(
            PlanType.MONTHLY, MonthlyPlan::new,
            PlanType.ANNUAL, AnnualPlan::new,
            PlanType.STUDENT, StudentPlan::new,
            PlanType.OFF_PEAK, OffPeakPlan::new));

    private PlanFactory() {
    }

    /**
     * Pre: {@code type} is not null. Post: a plan whose {@link Plan#type()} is {@code type}.
     *
     * @throws IllegalArgumentException if no plan is registered for {@code type}
     */
    public static Plan create(PlanType type) {
        Supplier<Plan> plan = PLANS.get(type);
        if (plan == null) {
            throw new IllegalArgumentException("No plan is registered for " + type);
        }
        return plan.get();
    }

    /**
     * Every plan on offer, in the order of {@link PlanType}.
     */
    public static List<Plan> all() {
        return PLANS.keySet().stream().map(PlanFactory::create).toList();
    }
}
