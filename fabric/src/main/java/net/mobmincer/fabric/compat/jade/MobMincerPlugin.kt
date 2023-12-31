package net.mobmincer.fabric.compat.jade

import net.mobmincer.core.entity.MobMincerEntity
import snownee.jade.api.IWailaClientRegistration
import snownee.jade.api.IWailaCommonRegistration
import snownee.jade.api.IWailaPlugin

class MobMincerPlugin : IWailaPlugin {
    override fun register(registration: IWailaCommonRegistration) {
        registration.registerEntityDataProvider(
            net.mobmincer.compat.jade.MobMincerComponentProvider,
            MobMincerEntity::class.java
        )
    }

    override fun registerClient(registration: IWailaClientRegistration) {
        registration.registerEntityComponent(
            net.mobmincer.compat.jade.MobMincerComponentProvider,
            MobMincerEntity::class.java
        )
    }
}
