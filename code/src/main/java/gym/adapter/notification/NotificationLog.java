package gym.adapter.notification;

import gym.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Delivers notifications by writing them to the log, standing in for email or push messages in Part 1.
 */
@Component
public class NotificationLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationLog.class);

    @EventListener
    public void deliver(Notification notification) {
        LOGGER.info("notify member {}: {} - {}", notification.memberId(), notification.subject(), notification.message());
    }
}
