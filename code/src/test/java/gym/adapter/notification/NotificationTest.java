package gym.adapter.notification;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gym.domain.Notification;
import gym.port.out.NotificationSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

/**
 * A notification sent through the port reaches its listener over Spring's event bus.
 */
@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class NotificationTest {

    @Autowired
    private NotificationSender sender;

    @Test
    void aSentNotificationIsDelivered(CapturedOutput output) {
        sender.send(new Notification("m-1", "Waitlist", "You have a place in Spin on Friday"));

        assertTrue(output.getOut().contains("notify member m-1: Waitlist - You have a place in Spin on Friday"));
    }
}
