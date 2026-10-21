package gym;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Enforces the MVC + Hexagonal dependency rule from report/architecture/README.md.
 * Every dependency points inwards, towards the domain. A violation fails the build.
 */
@AnalyzeClasses(packages = "gym", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule layersOnlyDependInwards = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .withOptionalLayers(true)
            .layer("Controller").definedBy("gym.controller..")
            .layer("Dto").definedBy("gym.dto..")
            .layer("Inbound port").definedBy("gym.port.in..")
            .layer("Outbound port").definedBy("gym.port.out..")
            .layer("Service").definedBy("gym.service..")
            .layer("Domain").definedBy("gym.domain..")
            .layer("Adapter").definedBy("gym.adapter..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Dto").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Inbound port").mayOnlyBeAccessedByLayers("Controller", "Service")
            .whereLayer("Outbound port").mayOnlyBeAccessedByLayers("Service", "Adapter")
            .whereLayer("Service").mayNotBeAccessedByAnyLayer()
            .whereLayer("Adapter").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule domainOnlyDependsOnTheJdk = classes()
            .that().resideInAPackage("gym.domain..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("gym.domain..", "java.lang..", "java.util..", "java.time..", "java.math..");

    @ArchTest
    static final ArchRule coreIgnoresWebAndStorage = noClasses()
            .that().resideInAnyPackage("gym.service..", "gym.port..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework.web..",
                    "org.springframework.http..",
                    "jakarta.servlet..",
                    "tools.jackson..",
                    "java.io..",
                    "java.nio.file..")
            .allowEmptyShould(true);
}
