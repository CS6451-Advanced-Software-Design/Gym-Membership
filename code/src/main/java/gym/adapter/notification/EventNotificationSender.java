package gym.adapter.notification;

import gym.domain.Notification;
import gym.port.out.NotificationSender;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Driven adapter that puts each notification on Spring's in-process event bus (our message bus, see
 * report/tech-stack.md). Listeners such as {@link NotificationLog} deliver it, so adding a new channel
 * means adding a listener, not changing this class or any service.
 */
@Component
public class EventNotificationSender implements NotificationSender {

    private final ApplicationEventPublisher events;

    public EventNotificationSender(ApplicationEventPublisher events) {
        this.events = events;
    }

    @Override
    public void send(Notification notification) {
        events.publishEvent(notification);
    }
}
