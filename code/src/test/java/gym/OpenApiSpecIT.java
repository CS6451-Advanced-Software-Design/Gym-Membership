package gym;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void foldersFollowTheControllersOrder() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.tags[0].name").value("Memberships"))
                .andExpect(jsonPath("$.tags[1].name").value("Billing"));
    }

    /**
     * Postman checks each example response against its schema, so a field that is {@code null} in a real
     * response must allow it.
     */
    @Test
    void nullableFieldsAllowNull() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.schemas.MembershipResponse.properties.endDate.type").value(hasItem("null")))
                .andExpect(jsonPath("$.components.schemas.MembershipResponse.properties.membershipId.type").value("string"));
    }

    /**
     * A problem's instance is the path that failed, such as {@code /memberships}, which isn't an absolute URI.
     */
    @Test
    void problemInstanceIsAUriReference() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.schemas.ProblemDetail.properties.instance.format").value("uri-reference"));
    }

    @Test
    void rootOpensTheSwaggerUi() throws Exception {
        mvc.perform(get("/")).andExpect(status().isFound()).andExpect(redirectedUrl("/swagger-ui.html"));
    }

    /**
     * Railway ends HTTPS at its proxy and forwards plain HTTP, so the redirect must follow X-Forwarded-Proto.
     */
    @Test
    void rootRedirectKeepsHttpsBehindAProxy() throws Exception {
        mvc.perform(get("/").header("X-Forwarded-Proto", "https").header("X-Forwarded-Host", "gym.example"))
                .andExpect(redirectedUrl("https://gym.example/swagger-ui.html"));
    }
}
