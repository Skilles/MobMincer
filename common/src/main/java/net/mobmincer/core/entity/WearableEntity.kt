package net.mobmincer.core.entity

import dev.architectury.extensions.network.EntitySpawnExtension
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.Animal
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import java.util.*

abstract class WearableEntity(entityType: EntityType<*>, level: Level) :
    SmoothMotionEntity(
        entityType,
        level
    ),
    EntitySpawnExtension {

    lateinit var target: LivingEntity
        private set

    private lateinit var targetUUID: UUID

    fun initialize(target: LivingEntity) {
        this.target = target
        this.targetUUID = target.uuid
        this.setPos(target.x, target.y + target.bbHeight, target.z)
        onTargetBind()
    }

    final override fun tick() {
        if (!this::targetUUID.isInitialized) {
            super.tick()
            return
        }
        if (!this::target.isInitialized) {
            rebindTarget()
            return
        }
        updatePosition()
        doTick()
        super.tick()
    }

    protected open fun doTick() {
        // NO-OP
    }

    protected open fun onTargetBind() {
        // NO-OP
    }

    private fun rebindTarget() {
        val candidates = level().getEntities(
            this,
            AABB.ofSize(this.position(), 1.25, 1.5, 1.25)
        ) { entity -> entity.uuid.equals(targetUUID) }
        if (candidates.isEmpty() || candidates[0] !is LivingEntity) {
            destroy(true)
        } else {
            this.target = candidates[0] as LivingEntity
            onTargetBind()
            this.tick()
        }
    }

    protected open fun destroy(discard: Boolean = false) {
        if (discard) {
            this.discard()
        } else {
            this.kill()
        }
    }

    protected fun updatePosition() {
        val pos = target.getPassengerRidingPosition(this)
        val isHumanoid = target !is Animal
        val yRot = if (isHumanoid) target.yHeadRot else target.yBodyRot

        this.setPos(pos.x, pos.y, pos.z)
        this.yRot = yRot - 180
        if (isHumanoid) {
            this.xRot = target.lerpTargetXRot()
        }
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        if (compound.hasUUID("Target") && compound.contains("SourceStack")) {
            this.targetUUID = compound.getUUID("Target")
        } else {
            destroy(true)
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        compound.putUUID("Target", target.uuid)
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        buf.writeInt(target.id)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        this.target = level().getEntity(buf.readInt()) as LivingEntity
    }
}
