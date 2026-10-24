package gym.port.out;

import gym.domain.Notification;

/**
 * Driven port for telling a member about something, e.g. a waitlist promotion, a welcome or a failed payment.
 * Services send and move on: how the message is delivered is the adapter's job.
 */
public interface NotificationSender {

    void send(Notification notification);
}
