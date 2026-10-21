package gym.domain;

/**
 * Thrown when a request is well formed but breaks a business rule,
 * e.g. booking a class while the membership is frozen.
 */
public class RuleViolationException extends RuntimeException {

    public RuleViolationException(String message) {
        super(message);
    }
}
