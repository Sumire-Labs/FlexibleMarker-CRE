## Unreleased

- Used the build height rather than terrain height so chunk markers work above the Nether roof.

- Updated CleanroomDevEnv to a7f3735, migrating the build scripts to Kotlin DSL and enabling configuration caching.
- Updated release-note generation to use the tagged version's CHANGELOG section.

- Required a matching open marker GUI for legacy edit packets and closed stale legacy containers.
- Kept GUI access bound to its original world across dimension changes.
- Replaced recursive angle wrapping to handle extreme rotations safely.

- Fixed range updates accessing the client world from the network thread.
- Synchronized range changes to nearby players even when their marker GUI is closed.
- Bounded range-rendering work for large chunk areas and rejected invalid or out-of-world ranges.
- Restored safe defaults for incomplete marker NBT and translated the vertical direction labels.

## 3.1.0

- Migrated the build, development runs, CI, and publishing to CleanroomDevEnv and CleanroomGradle.
- Raised the minimum Cleanroom version to 0.7.14-alpha.
- Enabled the template's Spotless formatting and import checks.

## 3.0.1

- Update Mod icon

## 3.0.0

- Ported the mod to Cleanroom Loader. Standard Forge is no longer supported.
- Replaced both marker GUIs with CleanroomMC's ModularUI.
- Ported the Scala rendering code to Java, removing the mod's dependency on Scalar or Scala Legacy.
- Migrated from Java 8 to Java 25.
- Updated MCP mappings to stable 39-1.12.
