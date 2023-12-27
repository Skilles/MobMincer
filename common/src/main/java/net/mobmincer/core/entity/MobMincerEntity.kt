package net.mobmincer.core.entity

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.Level
import net.mobmincer.core.registry.MincerEntities.MOB_MINCER

class MobMincerEntity(level: Level) : Entity(MOB_MINCER.get(), level) {
    var target: LivingEntity? = null

    override fun tick() {
        super.tick()

        if (target != null) {
            this.moveTo(target!!.x, target!!.y + 1, target!!.z)
        }
    }


    override fun defineSynchedData() {
        entityData.define(TARGET_ACCESSOR, -1)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        val targetId = entityData.get(TARGET_ACCESSOR)
        if (targetId != -1) {
            this.target = level().getEntity(targetId) as LivingEntity?
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        entityData.set(TARGET_ACCESSOR, target?.id ?: -1)
    }

    companion object {
        private val TARGET_ACCESSOR: EntityDataAccessor<Int> = SynchedEntityData.defineId(MobMincerEntity::class.java, EntityDataSerializers.INT)
    }
}
