package gym.domain;

/**
 * Thrown when a use case refers to an object that doesn't exist, e.g. an unknown member id.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String type, Object id) {
        super(type + " " + id + " not found");
    }
}
