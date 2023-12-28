import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("java")
    kotlin("jvm") version "2.0.0-Beta2"
    kotlin("plugin.serialization") version "2.0.0-Beta2"
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.4-SNAPSHOT" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.github.pacifistmc.forgix") version "1.2.6"
}

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
}

forgix {
    group = rootProject.property("maven_group").toString()
    mergedJarName = "${rootProject.property("archives_base_name")}-${rootProject.property("mod_version")}-${rootProject.property("minecraft_version")}.jar"
    outputDir = "build/libs/merged-${rootProject.property("minecraft_version")}"
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

    loom.silentMojangMappingsLicense()

    dependencies {
        "minecraft"("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
        // Use mojmap with ParchmentMC
        "mappings"(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${rootProject.property("minecraft_version")}:${rootProject.property("parchment_version")}@zip")
        })
    }

    repositories {
        maven {
            name = "Sonar Maven"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    base.archivesName.set(rootProject.property("archives_base_name").toString())
    //base.archivesBaseName = rootProject.property("archives_base_name").toString()
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
    }

    kotlin.target.compilations.all {
        kotlinOptions.jvmTarget = "17"
        kotlinOptions.freeCompilerArgs.plus("-opt-in=kotlin.ExperimentalStdlibApi")
    }

    java {
        withSourcesJar()
    }
}
