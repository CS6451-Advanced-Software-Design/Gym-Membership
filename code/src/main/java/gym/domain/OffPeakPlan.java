package gym.domain;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * BR-M1: check-in only from 10:00 to 16:00 on weekdays.
 */
public class OffPeakPlan extends Plan {

    private static final LocalTime OPENS = LocalTime.of(10, 0);
    private static final LocalTime CLOSES = LocalTime.of(16, 0);

    public OffPeakPlan() {
        super(PlanType.OFF_PEAK);
    }

    @Override
    public boolean allowsCheckIn(LocalDateTime at) {
        DayOfWeek day = at.getDayOfWeek();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        LocalTime time = at.toLocalTime();
        return weekday && !time.isBefore(OPENS) && time.isBefore(CLOSES);
    }

    @Override
    public String description() {
        return "Weekdays 10:00 to 16:00 only, billed monthly";
    }
}
