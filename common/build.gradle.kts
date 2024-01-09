architectury {
    common(rootProject.property("enabled_platforms").toString().split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/mobmincer.accesswidener"))
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury:${rootProject.property("architectury_version")}")

    api("fuzs.forgeconfigapiport:forgeconfigapiport-common:${rootProject.property("forge_config_version")}")

    modCompileOnly("curse.maven:jade-324717:${rootProject.property("jade_version_fabric")}")
    /*modCompileOnlyApi(
        "mezz.jei:jei-${rootProject.property("minecraft_version")}-common-api:${rootProject.property("jei_version")}"
    )*/
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:${rootProject.property("rei_version")}")
}
