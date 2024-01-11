plugins {
    id("com.github.johnrengelman.shadow")
}

repositories {
    maven {
        url = uri("https://maven.quiltmc.org/repository/release/")
    }
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val generatedResources = file("../common/src/main/generated")

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    val modId = rootProject.property("mod_id").toString()
    runs {
        register("data") {
            server()
            name = "Data Generation"

            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=$generatedResources")
            vmArg("-Dfabric-api.datagen.modid=$modId")

            runDir("build/datagen")
        }
    }
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentFabric: Configuration by configurations.getting

configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentFabric.extendsFrom(common)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-fabric:${rootProject.property("architectury_version")}")

    common(project(":common", "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(":common", "transformProductionFabric")) {
        isTransitive = false
    }

    modLocalRuntime("com.terraformersmc:modmenu:${rootProject.property("mod_menu_version")}")

    modImplementation("curse.maven:jade-324717:${rootProject.property("jade_version_fabric")}")

    // Fabric Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:${rootProject.property("fabric_kotlin_version")}")

    modApi(
        "fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${rootProject.property("forge_config_version")}"
    )
    /*modLocalRuntime(
        "mezz.jei:jei-${rootProject.property("minecraft_version")}-fabric:${rootProject.property("jei_version")}"
    )*/
    modRuntimeOnly(
        "me.shedaniel:RoughlyEnoughItems-fabric:${rootProject.property("rei_version")}"
    )
}

tasks.processResources {
    inputs.property("group", rootProject.property("maven_group"))
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "group" to rootProject.property("maven_group"),
                "version" to project.version,

                "mod_id" to rootProject.property("mod_id"),
                "minecraft_version" to rootProject.property("minecraft_version"),
                "architectury_version" to rootProject.property("architectury_version"),
                "fabric_kotlin_version" to rootProject.property("fabric_kotlin_version")
            )
        )
    }
}

tasks.shadowJar {
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
