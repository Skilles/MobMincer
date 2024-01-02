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
        (mincer.target as Mob).isNoAi = !enabled
    }
}
