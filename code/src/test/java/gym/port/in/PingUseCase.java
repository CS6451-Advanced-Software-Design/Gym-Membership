package gym.port.in;

import gym.domain.RuleViolationException;

/**
 * A stand-in use case for the interceptor tests, since no feature use cases exist yet.
 */
public interface PingUseCase {

    String ping(String name);

    /**
     * Implementation that answers "pong name", and breaks a rule for the name "frozen".
     */
    class Service implements PingUseCase {

        @Override
        public String ping(String name) {
            if (name.equals("frozen")) {
                throw new RuleViolationException("Membership is frozen");
            }
            return "pong " + name;
        }
    }
}
