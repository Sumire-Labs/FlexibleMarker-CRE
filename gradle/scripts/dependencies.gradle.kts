import com.cleanroommc.gradle.api.ext.CleanroomExtension
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import javax.inject.Inject

val cleanroom = extensions.getByType<CleanroomExtension>()

fun nonTransitiveMod(notation: String): ModuleDependency =
    (cleanroom.deobf(notation) as ModuleDependency).apply { isTransitive = false }

repositories {
    maven("https://cursemaven.com") {
        name = "CurseMaven"
        content { includeGroup("curse.maven") }
    }
    maven("https://curse.cleanroommc.com") {
        name = "CleanroomCurseMaven"
        content { includeGroup("curse.maven") }
    }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content { includeGroup("maven.modrinth") }
    }
    maven("https://mod-buildcraft.com/maven") {
        name = "BuildCraft"
        content { includeGroup("com.mod-buildcraft") }
    }
    exclusiveContent {
        forRepository { maven("https://maven.cleanroommc.com") { name = "CleanroomHEI" } }
        filter { includeGroup("mezz") }
    }
    if (providers.gradleProperty("cg.repos.enableLocal").orElse("false").get().toBoolean()) mavenLocal()
}

dependencies {
    add("implementation", cleanroom.deobf("mezz:jei:4.35.0@jar"))
    add("implementation", nonTransitiveMod("com.cleanroommc:modularui:3.1.6"))
    add("compileOnly", nonTransitiveMod("curse.maven:inventory-bogosorter-632327:7412409"))
    add("compileOnly", nonTransitiveMod("curse.maven:neverenoughanimation-1062347:6533650"))
    // Keep both machine integrations optional, with a standalone development variant.
    val buildcraftScope = if (providers.gradleProperty("marker_standalone").orElse("false").get().toBoolean()) {
        "compileOnly"
    } else "implementation"
    add(buildcraftScope, cleanroom.deobf("com.mod-buildcraft:buildcraft-main:7.99.24.7"))
    add("compileOnly", nonTransitiveMod("curse.maven:additional-enchanted-miner-282837:3851282"))
    providers.gradleProperty("quarryplus_dev_jar").orNull?.let { path ->
        add("runtimeOnly", cleanroom.deobf(files(path)))
        add("runtimeOnly", "org.scala-lang:scala-library:3.9.0")
        add("runtimeOnly", nonTransitiveMod("curse.maven:scalar-1012168:8800375"))
    }
}

val sourceSets = extensions.getByType<SourceSetContainer>()
val main = sourceSets.getByName("main")
sourceSets.named("test") {
    compileClasspath += main.compileClasspath
    runtimeClasspath += main.runtimeClasspath + main.compileClasspath
}

// The old signed QuarryPlus dependency retains invalid signatures after deobfuscation.
// Only JUnit uses this unsigned copy; the original dependency and runtime jar are untouched.
abstract class PrepareQuarryTestApi : DefaultTask() {
    @get:Classpath
    abstract val apiJars: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @get:Inject
    abstract val archives: ArchiveOperations

    @get:Inject
    abstract val fileSystem: FileSystemOperations

    @TaskAction
    fun extract() {
        fileSystem.sync {
            from(apiJars.files.map { archives.zipTree(it) })
            exclude("META-INF/**")
            into(destination)
        }
    }
}

if (providers.gradleProperty("enable_junit_testing").get().toBoolean()) {
    val quarryTestApi = main.compileClasspath.filter {
        it.name.contains("additional-enchanted-miner-282837") && it.name.endsWith(".jar")
    }
    val prepareQuarryTestApi = tasks.register<PrepareQuarryTestApi>("prepareQuarryTestApi") {
        apiJars.from(quarryTestApi)
        destination.set(layout.buildDirectory.dir("test-integration-api/quarryplus"))
    }
    tasks.named<Test>("test") {
        dependsOn(prepareQuarryTestApi)
        classpath = classpath.minus(quarryTestApi).plus(files(prepareQuarryTestApi.flatMap { it.destination }))
    }
}
