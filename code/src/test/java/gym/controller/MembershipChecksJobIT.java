package gym.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;

/**
 * Checks the clock is really wired: Spring schedules the daily membership checks for 05:00 every day.
 */
@SpringBootTest
class MembershipChecksJobIT {

    @Autowired
    ScheduledTaskHolder scheduler;

    @Test
    void theChecksRunEveryMorning() {
        List<String> schedules = scheduler.getScheduledTasks().stream()
                .map(ScheduledTask::getTask)
                .filter(task -> task.toString().contains(MembershipChecksJob.class.getName() + ".run"))
                .map(task -> ((CronTask) task).getExpression())
                .toList();

        assertEquals(List.of("0 0 5 * * *"), schedules);
    }
}
