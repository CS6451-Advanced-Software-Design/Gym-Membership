package gym.port.out;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for storing domain objects. Services depend on this interface,
 * never on how or where the objects are stored.
 *
 * @param <T> the domain type
 * @param <ID> the type of its identifier
 */
public interface Repository<T, ID> {

    /**
     * @return the object with this id, or empty if none is stored
     */
    Optional<T> findById(ID id);

    /**
     * @return every stored object, in the order they were first saved
     */
    List<T> findAll();

    /**
     * Inserts the object, or replaces the stored one with the same id.
     *
     * @return the saved object
     */
    T save(T entity);

    /**
     * Removes the object with this id. Does nothing if none is stored.
     */
    void deleteById(ID id);
}
