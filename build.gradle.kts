
import groovy.lang.Closure
import io.github.pacifistmc.forgix.plugin.ForgixMergeExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    java
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.4-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.+" apply false
    id("io.github.pacifistmc.forgix") version "1.2.+"
    id("io.github.detekt.gradle.compiler-plugin") version "1.+"
    id("me.shedaniel.unified-publishing") version "0.1.+"
}

apply(plugin = "me.shedaniel.unified-publishing")

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
    detektPlugins("io.cole.matthew.detekt.operator:detekt-operator:0.0.1")
    detektPlugins("com.github.Faire:faire-detekt-rules:v0.1.1")
}

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
}

forgix {
    group = rootProject.property("maven_group").toString()
    mergedJarName = "${rootProject.property("archives_base_name")}-${rootProject.property("mod_version")}-${rootProject.property("minecraft_version")}.jar"
    outputDir = "build/libs/merged-${rootProject.property("minecraft_version")}"

    @Suppress("UNCHECKED_CAST")
    custom(
        closureOf<ForgixMergeExtension.CustomContainer> {
            projectName = "neoforge"
        } as Closure<ForgixMergeExtension.CustomContainer>
    )
}

detekt {
    config.setFrom(files("detekt.yml"))
    buildUponDefaultConfig = true
}

subprojects {
    apply {
        plugin("dev.architectury.loom")
    }

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

    loom.silentMojangMappingsLicense()

    dependencies {
        "minecraft"("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
        // Use mojmap with ParchmentMC
        "mappings"(
            loom.layered {
                officialMojangMappings()
                parchment(
                    "org.parchmentmc.data:parchment-${
                        rootProject.property(
                            "minecraft_version"
                        )
                    }:${rootProject.property("parchment_version")}@zip"
                )
            }
        )
        implementation(
            annotationProcessor(
                "io.github.llamalad7:mixinextras-common:${rootProject.property("mixin_extras_version")}"
            )!!
        )
    }

    repositories {
        maven {
            name = "Fuzs Mod Resources"
            url = uri("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
        }
        maven {
            name = "TerraformersMC"
            url = uri("https://maven.terraformersmc.com/")
        }
        maven {
            name = "Jared's maven"
            url = uri("https://maven.blamejared.com/")
        }
        exclusiveContent {
            forRepository {
                maven {
                    url = uri("https://www.cursemaven.com")
                }
            }
            filter {
                includeGroup("curse.maven")
            }
        }
    }
}

allprojects {
    apply {
        plugin("java")
        plugin("kotlin")
        plugin("architectury-plugin")
    }

    base.archivesName.set(rootProject.property("archives_base_name").toString())
    version = rootProject.property("mod_version").toString()
    group = rootProject.property("maven_group").toString()

    repositories {
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }
    }

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib")
    }

    tasks.withType(JavaCompile::class.java) {
        options.release.set(17)
        options.encoding = "UTF-8"
        options.isFork = true
    }

    kotlin.target.compilations.all {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs.plus("-opt-in=kotlin.ExperimentalStdlibApi")
    }

    java {
        withSourcesJar()
    }
}

unifiedPublishing {
    project {
        gameVersions = rootProject.property("enabled_versions").toString().split(",")
        gameLoaders = rootProject.property("enabled_platforms").toString().split(",")
        version = rootProject.property("mod_version").toString()
        displayName = "Mob Mincer v${version.get()}"
        changelog = File("changelogs/${rootProject.property("mod_version")}.md").readText()
        //debugMode = true

        mainPublication.set(File("${forgix.outputDir}/${forgix.mergedJarName}"))

        curseforge {
            token = System.getenv("CF_TOKEN")
            id = rootProject.property("curseforge_id").toString()
        }

        modrinth {
            token = System.getenv("MODRINTH_TOKEN")
            id = rootProject.property("modrinth_id").toString()
        }

        relations {
            depends {
                curseforge = "architectury-api"
                modrinth = "architectury-api"
            }
        }
    }
}

val buildAll = tasks.register("buildAll") {
    dependsOn(tasks.clean, tasks.build, tasks.mergeJars)
}

tasks.publishUnified {
    dependsOn(buildAll)
}
