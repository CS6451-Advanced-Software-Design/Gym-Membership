package gym.domain;

public class MonthlyPlan extends Plan {

    public MonthlyPlan() {
        super(PlanType.MONTHLY);
    }

    @Override
    public String description() {
        return "Any time, billed monthly";
    }
}
