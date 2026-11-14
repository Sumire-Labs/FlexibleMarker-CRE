import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    // Make the project's plugin task types available to this applied Kotlin script.
    dependencies {
        classpath(files(rootProject.buildscript.configurations.getByName("classpath")))
    }
}

// Project-specific customizations. Applied after the template scripts.
apply(from = "gradle/scripts/verify-release.gradle.kts")
// With enable_shadow=true:
// tasks.named<ShadowJar>("shadowJar") {
//     relocate("org.example.library", "${providers.gradleProperty("root_package").get()}.internal.library")
// }
