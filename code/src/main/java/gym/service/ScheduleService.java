package gym.service;

import gym.domain.GymClass;
import gym.domain.RuleViolationException;
import gym.port.in.ManageScheduleUseCase;
import gym.port.out.GymClassRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * UC10: the timetable staff work with. It is kept apart from {@link BookingService} because adding and listing
 * classes has nothing to do with who holds a place in them.
 */
@Service
public class ScheduleService implements ManageScheduleUseCase {

    private final GymClassRepository classes;
    private final Clock clock;

    public ScheduleService(GymClassRepository classes, Clock clock) {
        this.classes = classes;
        this.clock = clock;
    }

    /**
     * Capacity is checked here as well as in {@link GymClass}, so a bad request is a refused rule rather than a crash.
     */
    @Override
    public GymClass addClass(String name, LocalDateTime startTime, int capacity, boolean premium) {
        if (!startTime.isAfter(LocalDateTime.now(clock))) {
            throw new RuleViolationException("A class can't be added at " + startTime + ", which has passed");
        }
        if (capacity < 1) {
            throw new RuleViolationException("A class needs at least 1 place, not " + capacity);
        }
        String classId = "c-" + UUID.randomUUID().toString().substring(0, 8);
        return classes.save(GymClass.schedule(classId, name, startTime, capacity, premium));
    }

    @Override
    public List<GymClass> schedule() {
        LocalDateTime now = LocalDateTime.now(clock);
        return classes.findAll().stream()
                .filter(gymClass -> !gymClass.hasStarted(now))
                .sorted(Comparator.comparing(GymClass::startTime))
                .toList();
    }
}
