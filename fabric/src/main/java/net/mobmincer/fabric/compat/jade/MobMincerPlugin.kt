package net.mobmincer.fabric.compat.jade

import net.mobmincer.compat.jade.MobMincerComponentProvider
import net.mobmincer.core.entity.MobMincerEntity
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin

class MobMincerPlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        registration.registerEntityDataProvider(
            MobMincerComponentProvider,
            MobMincerEntity::class.java
        )
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerEntityComponent(
            MobMincerComponentProvider,
            MobMincerEntity::class.java
        )
    }
}
