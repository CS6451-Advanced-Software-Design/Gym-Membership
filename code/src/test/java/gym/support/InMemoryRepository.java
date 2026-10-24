package gym.support;

import gym.port.out.Repository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * In-memory fake of the {@link Repository} port for service unit tests: no files, no Spring.
 *
 * <pre>{@code
 * class InMemoryMembershipRepository extends InMemoryRepository<Membership, String> implements MembershipRepository {
 *     InMemoryMembershipRepository() {
 *         super(Membership::id);
 *     }
 * }
 * }</pre>
 */
public class InMemoryRepository<T, ID> implements Repository<T, ID> {

    private final Function<T, ID> identity;
    private final Map<ID, T> entities = new LinkedHashMap<>();

    public InMemoryRepository(Function<T, ID> identity) {
        this.identity = identity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public List<T> findAll() {
        return List.copyOf(entities.values());
    }

    @Override
    public T save(T entity) {
        entities.put(identity.apply(entity), entity);
        return entity;
    }

    @Override
    public void deleteById(ID id) {
        entities.remove(id);
    }
}
