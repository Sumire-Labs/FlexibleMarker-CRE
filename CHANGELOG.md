# Changelog

## [Unreleased]

## [3.1.0] - 2026-11-14

### Added

- English and Japanese labels for the up/down controls.
- Maven publishing for development (MCP) and runtime (SRG) artifacts.

### Changed

- Requires Cleanroom Loader 0.7.14-alpha or later.
- Moved the build, development runs, CI, and publishing to CleanroomDevEnv (`a7f3735`) and CleanroomGradle.
- Converted the Gradle scripts to Kotlin DSL and enabled configuration caching.
- Enabled Spotless formatting and import checks.
- Updated Gradle to 9.9.0 and `actions/setup-java` to v6.2.0.
- Release notes now use the tagged version's section in this changelog.

### Fixed

- Flexible marker up/down distances can return to zero through buttons and numeric inputs. Horizontal distances still have a one-block minimum.
- Markers with empty, zero-width, or inverted ranges no longer report a usable area to mining machines.
- Marker rendering clears the shared buffer's coordinate offset after each draw, including failed draws.
- Fresh markers keep their position-based bounds when saved or synchronized. Valid bounds at the world origin remain intact.
- Chunk markers can use the Nether's full build height, including above the roof.
- Legacy edit packets require the matching marker GUI to be open. Stale legacy containers stop accepting edits.
- Marker GUIs remain bound to their original world after dimension changes.
- Angle wrapping handles extreme and non-finite rotations without recursion.
- Range-update packets access the client world on the main thread.
- Nearby players see range changes even when the marker GUI is closed.
- Large chunk ranges no longer generate unbounded render vertices. Edits reject invalid sizes, heights, and out-of-world coordinates.
- Incomplete marker NBT restores safe defaults while preserving saved geometry.

## [3.0.1] - 2026-10-21

### Changed

- Updated the Mod icon.

## [3.0.0] - 2026-10-13

### Changed

- Ported Flexible Marker to Cleanroom Loader. Standard Forge is no longer supported.
- Replaced both marker GUIs with ModularUI.
- Ported the Scala renderer to Java.
- Migrated from Java 8 to Java 25.
- Updated MCP mappings to stable 39-1.12.

### Removed

- Removed the dependency on Scalar or Scala Legacy.

[Unreleased]: https://github.com/Sumire-Labs/FlexibleMarker-CRE/compare/3.1.0...HEAD
[3.1.0]: https://github.com/Sumire-Labs/FlexibleMarker-CRE/compare/3.0.1...3.1.0
[3.0.1]: https://github.com/Sumire-Labs/FlexibleMarker-CRE/compare/3.0.0...3.0.1
[3.0.0]: https://github.com/Sumire-Labs/FlexibleMarker-CRE/releases/tag/3.0.0
