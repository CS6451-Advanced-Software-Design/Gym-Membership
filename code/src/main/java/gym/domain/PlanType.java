package gym.domain;

/**
 * The four plans and their prices (BR-M1). The Annual price is for the whole year, paid upfront.
 */
public enum PlanType {
    MONTHLY(Money.euros(45)),
    ANNUAL(Money.euros(450)),
    STUDENT(Money.euros(30)),
    OFF_PEAK(Money.euros(30));

    private final Money price;

    PlanType(Money price) {
        this.price = price;
    }

    public Money price() {
        return price;
    }
}
