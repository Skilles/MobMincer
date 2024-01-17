package net.mobmincer.client.menu

import net.mobmincer.core.registry.MincerRegistry

object Menus {

    val POWER_PROVIDER = MincerRegistry.register("power_provider", ::PowerProviderMenu)

    fun init() {
        // NO-OP
    }
}