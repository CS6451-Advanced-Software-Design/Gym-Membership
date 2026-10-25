package gym;

import java.time.Clock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class GymApplication {

    public static void main(String[] arguments) {
        SpringApplication.run(GymApplication.class, arguments);
    }

    /**
     * Services read the time from this clock instead of calling now() directly,
     * so tests can fix the date for billing runs, freezes and trial expiry.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
