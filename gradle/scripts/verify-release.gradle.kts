import groovy.json.JsonSlurper
import java.util.jar.JarFile
import net.minecraftforge.renamer.gradle.RenameJar

val runtimeJar = tasks.named<RenameJar>("reobfJar").flatMap { it.output }
val expectedVersion = project.version.toString()
val verifyReleaseJar = tasks.register("verifyReleaseJar") {
    group = "verification"
    description = "Checks Flexible Marker's runtime classes, icon, dependencies, and license notices."
    inputs.file(runtimeJar)
    inputs.property("modVersion", expectedVersion)
    doLast {
        val archive = inputs.files.singleFile
        JarFile(archive).use { jar ->
            fun read(name: String): ByteArray {
                val entry = jar.getJarEntry(name) ?: throw GradleException("Missing release entry: $name")
                return jar.getInputStream(entry).use { it.readBytes() }
            }
            val metadataText = read("mcmod.info").toString(Charsets.UTF_8)
            val metadata = (JsonSlurper().parseText(metadataText) as List<*>).single() as Map<*, *>
            if (metadata["modid"] != "flexiblemarker" || metadata["version"] != inputs.properties["modVersion"] ||
                metadataText.contains("@{") || metadataText.contains("\${version}")) {
                throw GradleException("Incorrect or unresolved release metadata")
            }
            if (jar.manifest.mainAttributes.getValue("ModType") != "CRL") {
                throw GradleException("Release must target Cleanroom")
            }
            read("com/kotori316/marker/render/Box.class")
            read("com/sumirelabs/flexiblemarker/gui/mui/MarkerGuiFactory.class")
            if ("modularui" !in (metadata["dependencies"] as List<*>)) {
                throw GradleException("ModularUI must be declared as a dependency")
            }
            if (metadata["logoFile"] != "logo.png") throw GradleException("Missing Mod icon metadata")
            read("logo.png")
            read("META-INF/LICENSE.md")
            read("META-INF/licenses/CleanroomDevEnv-MIT.txt")
            val entries = jar.entries().asSequence().toList()
            if (entries.any { it.name.startsWith("scala/") || it.name.endsWith(".scala") || it.name.endsWith(".tasty") }) {
                throw GradleException("Scala runtime or sources unexpectedly bundled in the distribution")
            }
            entries.filter { it.name.endsWith(".class") }.forEach { entry ->
                val data = read(entry.name).toString(Charsets.ISO_8859_1)
                if (data.contains("scala/") || data.contains("@{") || data.contains("\${version}")) {
                    throw GradleException("Unexpected Scala dependency or token in ${entry.name}")
                }
            }
        }
        logger.lifecycle("Verified Scala-free Cleanroom JAR: {}", archive.name)
    }
}

tasks.named("check") { dependsOn(verifyReleaseJar) }
