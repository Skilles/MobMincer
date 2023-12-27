package net.mobmincer

import net.mobmincer.core.registry.MincerEntities
import net.mobmincer.core.registry.MincerItems
import net.mobmincer.core.registry.MincerTabs
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MobMincer {
    const val MOD_ID: String = "mobmincer"

    val logger: Logger = LoggerFactory.getLogger("Mob Mincer")


    @JvmStatic
    fun init() {
        MincerTabs.register()
        MincerItems.register()
        MincerEntities.register()
    }
}
