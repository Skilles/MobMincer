plugins {
    id("com.github.johnrengelman.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    /*val modId = rootProject.property("mod_id").toString()
    val generatedResources = file("src/main/generated")
    runs {
        register("data") {
            data()
            programArgs("--all", "--mod", modId)
            programArgs("--output", generatedResources.absolutePath)
        }
    }*/
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentNeoForge: Configuration by configurations.getting

configurations {
    common
    shadowCommon
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentNeoForge.extendsFrom(common)
}

repositories {
    // KFF
    maven {
        name = "Kotlin for Forge"
        setUrl("https://thedarkcolour.github.io/KotlinForForge/")
    }
    maven {
        name = "NeoForged"
        setUrl("https://maven.neoforged.net/releases/")
    }
}

dependencies {
    neoForge("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-neoforge:${rootProject.property("architectury_version")}")

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionNeoForge")) { isTransitive = false }

    // Kotlin For Forge
    implementation("thedarkcolour:kotlinforforge-neoforge:${rootProject.property("kotlin_for_forge_version")}")

    modImplementation("curse.maven:jade-324717:${rootProject.property("jade_version_neoforge")}")
    modRuntimeOnly(
        "me.shedaniel:RoughlyEnoughItems-neoforge:${rootProject.property("rei_version")}"
    )
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-neoforge:${rootProject.property("rei_version")}")
}

tasks.processResources {
    inputs.property("group", rootProject.property("maven_group"))
    inputs.property("version", project.version)

    filesMatching("META-INF/mods.toml") {
        expand(
            mapOf(
                "group" to rootProject.property("maven_group"),
                "version" to project.version,

                "minecraft_version" to rootProject.property("minecraft_version"),
                "architectury_version" to rootProject.property("architectury_version"),
                "kotlin_for_forge_version" to rootProject.property("kotlin_for_forge_version")
            )
        )
    }
}

tasks.shadowJar {
    exclude("fabric.mod.json")
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    injectAccessWidener.set(true)

    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set(null as String?)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.sourcesJar {
    val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}

components.getByName("java") {
    this as AdhocComponentWithVariants
    this.withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
        skip()
    }
}
