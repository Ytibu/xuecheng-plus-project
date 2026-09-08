# Repository Guidelines

This repository is a Spring Cloud / Spring Boot microservices project. The shared parent POM is `xuecheng-plus-parent/pom.xml`; there is no root aggregator POM, so Maven commands run per module.

## Project Structure & Module Organization

- `xuecheng-plus-parent/` — dependency and plugin management (Java 8, Spring Boot 2.3.7, Spring Cloud Hoxton.SR9, Spring Cloud Alibaba 2.2.6).
- `xuecheng-plus-base/` — shared models, exceptions, validation, and configuration under `com.xuecheng.base`.
- `xuecheng-plus-content/`, `xuecheng-plus-media/`, `xuecheng-plus-system/` — domain modules, each split into `-api` (controllers and startup), `-model` (DTOs and POs), and `-service` (business logic, mappers, tests).
- `xuecheng-plus-gateway/` — Spring Cloud Gateway entry point (Java 17).
- `xuecheng-plus-generator/` — MyBatis-Plus code-generation templates.
- Sources live in `src/main/java/com/xuecheng/<domain>/...`; tests live in `src/test/java/...`.

## Build, Test, and Development Commands

Run commands from the relevant module directory. No Maven wrapper is committed.

- `mvn clean install` — compile, test, and install the current module.
- `mvn test` — run JUnit tests.
- `mvn spring-boot:run` — start `xuecheng-plus-content-api`, `xuecheng-plus-system-api`, `xuecheng-plus-media-api`, or `xuecheng-plus-gateway`.

Startup depends on Nacos at the address in each module's `bootstrap.yml`; keep `spring.profiles.active` aligned with the configured `dev` namespace and group.

## Coding Style & Naming Conventions

- Follow the existing `com.xuecheng.<domain>` package layout.
- Place controllers in `api` or `controller`, service interfaces in `service`, implementations in `service/impl`, DTOs in `model/dto`, and persistence objects in `model/po`.
- Use Lombok where surrounding code already uses it; prefer MapStruct for DTO/PO mapping when it avoids manual assignment.
- No formatter or linter is enforced. Match the existing style, use 4-space indentation, and avoid unrelated edits.

## Testing Guidelines

- Use JUnit 5 through `spring-boot-starter-test`; context-dependent tests use `@SpringBootTest`.
- Name test classes `<Subject>Tests.java` or `<Subject>Test.java` and keep them in the matching `src/test/java` package.
- Run `mvn test`; integration tests may require Nacos and MySQL access.

## Commit & Pull Request Guidelines

- Use short imperative Chinese commit messages that match history, such as `新增系统管理模块...`, `修复课程查询...`, or `功能：实现课程管理功能...`.
- Pull requests should explain the change, link related issues when available, and include screenshots or test output for behavior changes.

## Security & Configuration Tips

- Do not commit real credentials. `bootstrap.yml` only points to Nacos; keep environment-specific database, MinIO, and XXL-Job settings in external Nacos configuration.
- `target/`, `logs/`, and IDE files are already ignored by `.gitignore`.
