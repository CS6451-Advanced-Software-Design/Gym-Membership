package gym.adapter.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

class JsonFileRepositoryTest {

    record Item(String id, String name) {
    }

    @TempDir
    Path directory;

    private final JsonMapper mapper = JsonMapper.builder().build();
    private Path file;
    private JsonFileRepository<Item, String> repository;

    @BeforeEach
    void setUp() {
        file = directory.resolve("data").resolve("items.json");
        repository = new JsonFileRepository<>(file, Item.class, Item::id, mapper);
    }

    @Test
    void startsEmptyWhenTheFileDoesNotExist() {
        assertEquals(List.of(), repository.findAll());
        assertEquals(Optional.empty(), repository.findById("1"));
        assertFalse(Files.exists(file));
    }

    @Test
    void savedObjectsSurviveARestart() {
        repository.save(new Item("1", "Yoga"));
        repository.save(new Item("2", "Spin"));

        JsonFileRepository<Item, String> restarted = new JsonFileRepository<>(file, Item.class, Item::id, mapper);

        assertEquals(List.of(new Item("1", "Yoga"), new Item("2", "Spin")), restarted.findAll());
    }

    @Test
    void savingAnExistingIdReplacesIt() {
        repository.save(new Item("1", "Yoga"));
        repository.save(new Item("1", "Hot yoga"));

        assertEquals(List.of(new Item("1", "Hot yoga")), repository.findAll());
    }

    @Test
    void deleteRemovesTheObjectFromTheFile() {
        repository.save(new Item("1", "Yoga"));
        repository.deleteById("1");

        JsonFileRepository<Item, String> restarted = new JsonFileRepository<>(file, Item.class, Item::id, mapper);

        assertTrue(restarted.findAll().isEmpty());
    }
}
