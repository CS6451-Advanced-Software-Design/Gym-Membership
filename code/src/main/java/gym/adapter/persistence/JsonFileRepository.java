package gym.adapter.persistence;

import gym.port.out.Repository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Driven adapter that keeps one type of object in a single JSON file, as a list.
 * The file is read on first use and rewritten on every change.
 *
 * <p>Each feature extends this for its own repository:
 *
 * <pre>{@code
 * @Repository
 * public class JsonFileMemberRepository extends JsonFileRepository<Member, String> implements MemberRepository {
 *
 *     public JsonFileMemberRepository(PersistenceProperties properties, JsonMapper mapper) {
 *         super(properties.directory().resolve("members.json"), Member.class, Member::id, mapper);
 *     }
 * }
 * }</pre>
 */
public class JsonFileRepository<T, ID> implements Repository<T, ID> {

    private final Path file;
    private final JavaType listType;
    private final Function<T, ID> identity;
    private final JsonMapper mapper;
    private final Map<ID, T> entities = new LinkedHashMap<>();
    private boolean loaded;

    /**
     * @param file the JSON file, created on the first save if it doesn't exist
     * @param type the class stored in the file
     * @param identity reads the id of an object, e.g. {@code Member::id}
     * @param mapper the application's JSON mapper
     */
    public JsonFileRepository(Path file, Class<T> type, Function<T, ID> identity, JsonMapper mapper) {
        this.file = file;
        this.listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        this.identity = identity;
        this.mapper = mapper;
    }

    @Override
    public synchronized Optional<T> findById(ID id) {
        load();
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public synchronized List<T> findAll() {
        load();
        return List.copyOf(entities.values());
    }

    @Override
    public synchronized T save(T entity) {
        load();
        entities.put(identity.apply(entity), entity);
        write();
        return entity;
    }

    @Override
    public synchronized void deleteById(ID id) {
        load();
        if (entities.remove(id) != null) {
            write();
        }
    }

    private void load() {
        if (loaded) {
            return;
        }
        if (Files.exists(file)) {
            List<T> stored = mapper.readValue(file.toFile(), listType);
            stored.forEach(entity -> entities.put(identity.apply(entity), entity));
        }
        loaded = true;
    }

    /**
     * Writes to a temporary file first and then moves it into place,
     * so a crash mid-write never leaves a half-written file behind.
     */
    private void write() {
        try {
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temporary = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), entities.values());
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not write " + file, exception);
        }
    }
}
