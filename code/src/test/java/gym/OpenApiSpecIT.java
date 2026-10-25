package gym;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Keeps the committed OpenAPI spec, which the Postman collection is generated from, in step with the
 * Javadoc it is built from. After changing a controller or DTO, regenerate it with
 * {@code ./mvnw verify -Dskip.unit -Dit.test=OpenApiSpecIT -Dopenapi.write=true} and commit the result.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSpecIT {

    private static final Path SPEC = Path.of("postman/specs/openapi.yaml");

    @Autowired
    private MockMvc mvc;

    @Test
    void committedSpecMatchesTheCode() throws Exception {
        String generated = mvc.perform(get("/v3/api-docs.yaml")).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        if (Boolean.getBoolean("openapi.write")) {
            Files.createDirectories(SPEC.getParent());
            Files.writeString(SPEC, generated);
        }
        assertEquals(Files.readString(SPEC), generated,
                "postman/specs/openapi.yaml is out of date. Run ./mvnw verify -Dskip.unit -Dit.test=OpenApiSpecIT -Dopenapi.write=true and commit it");
    }
}
