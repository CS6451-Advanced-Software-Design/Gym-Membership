package gym.adapter.persistence;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under {@code gym.persistence} in application.properties.
 *
 * @param directory the folder holding one JSON file per repository
 */
@ConfigurationProperties("gym.persistence")
public record PersistenceProperties(Path directory) {
}
