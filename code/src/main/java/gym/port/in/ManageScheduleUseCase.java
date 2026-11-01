package gym.port.in;

import gym.domain.GymClass;
import java.time.LocalDateTime;
import java.util.List;

/**
 * UC10: Manage Class Schedule. Lets staff add classes and see what is coming up.
 */
public interface ManageScheduleUseCase {

    /**
     * Adds a class to the schedule.
     *
     * <p>Pre: {@code startTime} is in the future and {@code capacity} is at least 1. Post: the class is stored
     * with nobody booked and nobody waiting.
     *
     * @return the new class
     * @throws gym.domain.RuleViolationException if the start time has passed or the capacity is below 1
     */
    GymClass addClass(String name, LocalDateTime startTime, int capacity, boolean premium);

    /**
     * @return every class that hasn't started yet, soonest first
     */
    List<GymClass> schedule();
}
