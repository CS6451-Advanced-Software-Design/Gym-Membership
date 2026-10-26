package gym;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.RecordComponent;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.providers.JavadocProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * The title and overview of the OpenAPI spec. Everything else comes from the Javadoc: a controller's
 * class comment describes its tag, and each method and DTO describes its endpoint. The API is documented
 * in one place, next to the code. Example responses are real responses, kept in {@code openapi/examples/}.
 */
@Configuration
public class OpenApiConfiguration {

    private static final String PRODUCTION = "https://gym-membership-production-a725.up.railway.app";

    private static final Pattern LINE_BREAK = Pattern.compile("\\s*\\R\\s*");

    private static final String DESCRIPTION = """
            The REST API of a gym and fitness club: membership plans and billing, class bookings with \
            waitlists, check-in, freezes, penalties and referral credits.

            | Value | Format |
            | --- | --- |
            | Money | Euro, with two decimal places, e.g. `38.25` |
            | Month | `YYYY-MM`, e.g. `2026-11` |
            | Date | `YYYY-MM-DD`, e.g. `2026-11-01` |
            | Error | An [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem response. Its `detail` says what went wrong |""";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gym Membership")
                        .version("1.0")
                        .description(DESCRIPTION))
                .servers(List.of(
                        new Server().url(PRODUCTION).description("The live app on Railway, deployed from main"),
                        new Server().url("http://localhost:8080").description("The app started with ./mvnw spring-boot:run")));
    }

    /**
     * Describes each {@link Tag} with the class Javadoc of the controller that carries it,
     * which springdoc doesn't do by itself. The tags, and so the folders in Postman, follow the
     * controllers' {@code @Order}, lowest first, so they read in the order a member meets them.
     */
    @Bean
    public OpenApiCustomizer tagDescriptions(ApplicationContext context, JavadocProvider javadoc) {
        return openApi -> context.getBeansWithAnnotation(Tag.class).values().stream()
                .map(AopUtils::getTargetClass)
                .sorted(Comparator.<Class<?>>comparingInt(controller -> OrderUtils.getOrder(controller, Ordered.LOWEST_PRECEDENCE))
                        .thenComparing(Class::getSimpleName))
                .forEach(controller -> openApi.addTagsItem(new io.swagger.v3.oas.models.tags.Tag()
                        .name(controller.getAnnotation(Tag.class).name())
                        .description(javadoc.getClassJavadoc(controller))));
    }

    /**
     * Gives each response the real example in {@code openapi/examples/<operationId>-<status>.json},
     * e.g. {@code run-201.json}, instead of values made up from the schema.
     */
    @Bean
    public OpenApiCustomizer responseExamples() {
        return openApi -> openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation ->
                operation.getResponses().forEach((status, response) -> {
                    ClassPathResource example = new ClassPathResource("openapi/examples/" + operation.getOperationId() + "-" + status + ".json");
                    if (example.exists() && response.getContent() != null) {
                        Object value = read(example);
                        response.getContent().values().forEach(mediaType -> mediaType.setExample(value));
                    }
                })));
    }

    /**
     * Lets a DTO field marked {@link Nullable} be {@code null} in the spec as well, which springdoc
     * doesn't do by itself. Otherwise Postman flags every {@code null} in a real response as the wrong type.
     */
    @Bean
    public OpenApiCustomizer nullableFields() {
        return openApi -> {
            for (Map.Entry<String, Schema> entry : openApi.getComponents().getSchemas().entrySet()) {
                Class<?> dto = dto(entry.getKey());
                if (dto == null || !dto.isRecord()) {
                    continue;
                }
                Map<String, Schema> properties = entry.getValue().getProperties();
                for (RecordComponent field : dto.getRecordComponents()) {
                    if (field.getAnnotatedType().isAnnotationPresent(Nullable.class)) {
                        properties.get(field.getName()).addType("null");
                    }
                }
            }
        };
    }

    private static @Nullable Class<?> dto(String name) {
        try {
            return Class.forName("gym.dto." + name);
        } catch (ClassNotFoundException exception) {
            return null;
        }
    }

    /**
     * A problem's {@code type} and {@code instance} are URI references (RFC 9457), such as
     * {@code /memberships}, not the absolute URIs Spring's {@code ProblemDetail} schema asks for.
     */
    @Bean
    public OpenApiCustomizer problemReferences() {
        return openApi -> {
            Schema<?> problem = openApi.getComponents().getSchemas().get("ProblemDetail");
            if (problem != null) {
                problem.getProperties().get("type").setFormat("uri-reference");
                problem.getProperties().get("instance").setFormat("uri-reference");
            }
        };
    }

    private static Object read(ClassPathResource example) {
        try (InputStream input = example.getInputStream()) {
            return Json.mapper().readTree(input);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read " + example.getPath(), exception);
        }
    }

    /**
     * Joins the lines of every Javadoc-derived description. Javadoc is wrapped to fit the source, and its
     * HTML tags already mark paragraphs and lists, but viewers such as Postman show each source line break.
     */
    @Bean
    public OpenApiCustomizer joinJavadocLines() {
        return openApi -> {
            openApi.getTags().forEach(tag -> join(tag::getDescription, tag::setDescription));
            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation -> {
                join(operation::getDescription, operation::setDescription);
                if (operation.getParameters() != null) {
                    operation.getParameters().forEach(parameter -> join(parameter::getDescription, parameter::setDescription));
                }
                if (operation.getRequestBody() != null) {
                    join(operation.getRequestBody()::getDescription, operation.getRequestBody()::setDescription);
                }
                operation.getResponses().values().forEach(response -> join(response::getDescription, response::setDescription));
            }));
            for (Schema<?> schema : openApi.getComponents().getSchemas().values()) {
                join(schema::getDescription, schema::setDescription);
                if (schema.getProperties() != null) {
                    for (Schema<?> property : schema.getProperties().values()) {
                        join(property::getDescription, property::setDescription);
                    }
                }
            }
        };
    }

    private static void join(Supplier<String> description, Consumer<String> update) {
        if (description.get() != null) {
            update.accept(LINE_BREAK.matcher(description.get()).replaceAll(" ").strip());
        }
    }
}
