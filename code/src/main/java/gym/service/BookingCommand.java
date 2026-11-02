package gym.service;

/**
 * The Command pattern: one booking action held as an object, so it can be run now and taken back later.
 * Each command remembers what it needs for its own undo, which is why {@link BookingCommandService} only has to
 * keep the commands and never needs to know what any of them did.
 *
 * @param <R> what running the command returns
 */
interface BookingCommand<R> {

    /**
     * Pre: none, the rules are checked by the use case it calls. Post: the action has happened.
     *
     * @return the result of the action
     */
    R execute();

    /**
     * Pre: {@link #execute} has succeeded. Post: the action is taken back.
     *
     * @throws gym.domain.RuleViolationException if it is too late to take it back
     */
    void undo();
}
