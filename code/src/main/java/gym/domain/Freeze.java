package gym.domain;

import java.time.LocalDate;
import java.util.Objects;

/**
 * One freeze of a membership (BR-M3).
 *
 * @param startDate the day it was frozen
 * @param months how long it is frozen for, 1 to 3
 */
public record Freeze(LocalDate startDate, int months) {

    public Freeze {
        Objects.requireNonNull(startDate, "startDate");
    }

    public LocalDate endDate() {
        return startDate.plusMonths(months);
    }
}
