package gym.domain;

import java.util.Objects;

/**
 * A message for one member, e.g. "You have a place in Spin on Friday" after a waitlist promotion.
 *
 * @param memberId who it is for
 * @param subject a short title
 * @param message the full text
 */
public record Notification(String memberId, String subject, String message) {

    public Notification {
        Objects.requireNonNull(memberId, "memberId");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(message, "message");
    }
}
