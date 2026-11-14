# Flexible Marker CRE (Cleanroom Edition)

<img src="src/main/resources/logo.png" alt="Flexible Marker icon" align="left" width="80" height="80">

This mod requires Cleanroom Loader.

A Cleanroom port of [Flexible Marker](https://www.curseforge.com/minecraft/mc-mods/flexiblemarker).

<br clear="all">

## Description

### Flexible Marker

A marker that lets you configure a machine's working area by setting the distances upward, downward, left, right, and forward independently. You can adjust the area through its GUI after placement, without having to place multiple markers far apart.

Useful for defining areas around structures or limiting mining and terrain clearing to specific heights.

### Chunk Marker

A marker that lets you configure a machine's working area in chunks. The horizontal area aligns with chunk boundaries, and you can adjust the minimum and maximum Y coordinates through its GUI.

Useful for chunk-aligned mining and clearing larger areas.

## Requirements

- Cleanroom Loader 0.7.14-alpha or later
- ModularUI 3.1.6 or later

## Changes

- Rewrote the GUIs using ModularUI.
- Rewrote the Scala code in Java, removing the dependency on a Scala runtime provider.
- Migrated to Java 25 and Cleanroom Loader.

## Development

The build uses [CleanroomDevEnv](https://github.com/Sumire-Labs/CleanroomDevEnv)
at `a7f3735ce867457d19b8bb0de4a47c3849ed84f7`, with Kotlin DSL, CleanroomGradle and Java 25.

- `./gradlew spotlessApply` applies the template's import order and Eclipse Java formatting.
  Formatting settings are in `gradle/spotless/`.
- `./gradlew clean build` runs tests, formatting checks, and both distribution JAR checks.
- `./gradlew runClient` and `./gradlew runServer` launch the development environment.
- `./gradlew runClient -Pmarker_standalone=true` omits the optional BuildCraft runtime integration.
- `./gradlew runClient -Pextra_jvm_args=-Dflexiblemarker.smokeTest=true` runs the opt-in
  GUI/integration smoke test in a new disposable world and exits. Check the log for
  `Flexible Marker smoke passed`; a normal game shutdown alone does not indicate success.

The distributable SRG JAR is `build/libs/flexiblemarker-<version>.jar`.
The `-mcp.jar` is for development; it must not be installed in a normal game.
Git tags determine the version on the `1.12.2` release branch.
Release publishing uses the template's workflow and can be enabled with
`curseforge_project_id`, `modrinth_project_id`, or the Maven properties in `gradle.properties`.
Existing `CURSEFORGE_PROJECT_ID` / `MODRINTH_PROJECT_ID` repository variables are also supported.
See [.github/RENOVATE.md](.github/RENOVATE.md) for optional Renovate setup.
Project dependencies are configured in `gradle/scripts/dependencies.gradle.kts`.
The build enables Gradle's configuration cache; run the same command twice to check cache reuse.
Releases use the matching version section from `CHANGELOG.md` for their release notes.

## License

All Rights Reserved

## Credits

- [Kotori316](https://github.com/Kotori316) (Original author)
  - Creator of the original mod.
  - Original project: [Flexible Marker](https://github.com/Kotori316/FlexibleMarker) — All Rights Reserved.
  - Granted special permission to port this mod to Cleanroom and distribute the modified version.

- CleanroomMC
  - CleanroomModTemplate
  - CleanroomVersioning
  - TokenEnvoy

- Sumire Labs
  - [CleanroomDevEnv](https://github.com/Sumire-Labs/CleanroomDevEnv), the current build template.
  - Template MIT notice: [LICENSES/CleanroomDevEnv-MIT.txt](LICENSES/CleanroomDevEnv-MIT.txt).

This project is an unofficial fork, not an official release by the original author.

This fork retains the original project's All Rights Reserved status. The modified version is published and distributed with permission from the original author. This permission does not grant third parties unrestricted rights to reuse or redistribute the project.
