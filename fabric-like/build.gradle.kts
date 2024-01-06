architectury {
    common(rootProject.property("enabled_platforms").toString().split(","))
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    modApi("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury-fabric:${rootProject.property("architectury_version")}")

    compileOnly(project(":common", "namedElements")) {
        isTransitive = false
    }

    // Forge Config Screens
    // modLocalRuntime("fuzs.forgeconfigscreens:forgeconfigscreens-fabric:${rootProject.property("forge_config_screen_version")}")
    // Mod Menu
    modLocalRuntime("com.terraformersmc:modmenu:${rootProject.property("mod_menu_version")}")

    modImplementation("curse.maven:jade-324717:${rootProject.property("jade_version_fabric")}")

    modImplementation(
        "fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${rootProject.property("forge_config_version")}"
    )
}
