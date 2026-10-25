package gym.adapter.logging;

/**
 * Where a use-case call is, as logged in the {@code outcome} field.
 */
enum Outcome {
    CALLED("called"),
    DONE("done"),
    FAILED("failed");

    private final String value;

    Outcome(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }
}
