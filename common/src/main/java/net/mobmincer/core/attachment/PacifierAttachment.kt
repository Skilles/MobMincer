package net.mobmincer.core.attachment

import net.minecraft.world.entity.Mob
import net.mobmincer.core.entity.MobMincerEntity

class PacifierAttachment(type: MobMincerAttachment<*>, mincer: MobMincerEntity) : AttachmentInstance(type, mincer) {
    override fun onAttach() {
        setAi(false)
    }

    override fun onSpawn() {
        setAi(false)
    }

    override fun onDeath(reason: MobMincerEntity.DestroyReason): Boolean {
        setAi(true)

        return super.onDeath(reason)
    }

    private fun setAi(enabled: Boolean) {
        val target = mincer.target
        val ridingMob = mincer.target.vehicle
        if (ridingMob is Mob) {
            ridingMob.isNoAi = !enabled
        }
        target.passengersAndSelf.forEach {
            if (it is Mob) {
                it.isNoAi = !enabled
            }
        }

    }
}
