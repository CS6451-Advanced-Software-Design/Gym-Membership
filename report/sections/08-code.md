# Code {#code}

<!--
Everyone. One captioned snippet per pattern with the test that proves it, a table mapping each use case to the classes
that implement it, Hexagonal shown by ArchitectureTest, and the version control visualisations (commit graph,
contributors chart). Each pattern's owner writes its part.
Evidence: the Patterns table in the root README.md.
-->

## Use Cases and Their Classes

*To be written.*

## Architectural Patterns

*MVC, Hexagonal and Interceptor, each with a snippet and its test.*

```java
@ArchTest
static final ArchRule layersOnlyDependInwards = layeredArchitecture()
        .consideringOnlyDependenciesInLayers()
        .withOptionalLayers(true)
        // ... one layer per package
        .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
        .whereLayer("Inbound port").mayOnlyBeAccessedByLayers("Controller", "Service")
        .whereLayer("Outbound port").mayOnlyBeAccessedByLayers("Service", "Adapter")
        .whereLayer("Service").mayNotBeAccessedByAnyLayer()
        .whereLayer("Adapter").mayNotBeAccessedByAnyLayer();

@ArchTest
static final ArchRule domainOnlyDependsOnTheJdk = classes()
        .that().resideInAPackage("gym.domain..")
        .should().onlyDependOnClassesThat()
        .resideInAnyPackage("gym.domain..", "java.lang..", "java.util..", "java.time..", "java.math..");
```

```java
@Bean
public UseCaseDispatcher useCaseDispatcher() {
    UseCaseDispatcher dispatcher = new UseCaseDispatcher();
    dispatcher.register(new LoggingInterceptor(), 0);
    return dispatcher;
}
```

## Design Patterns

*Strategy (Chirag), State and Factory (Dhruv), Observer (Bharat), each with a snippet and its test.*

## Automated Tests

*To be written.*

## Version Control

*Commit graph and contributors chart.*
