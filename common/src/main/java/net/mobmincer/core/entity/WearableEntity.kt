package net.mobmincer.core.entity

import dev.architectury.extensions.network.EntitySpawnExtension
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
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
    }

    override fun tick() {
        if (!this::target.isInitialized) {
            rebindTarget()
            return
        }
        updatePosition()
        super.tick()
    }

    private fun rebindTarget() {
        val candidates = level().getEntities(
            this,
            AABB.ofSize(this.position(), 1.0, 1.0, 1.0)
        ) { entity -> entity.uuid.equals(targetUUID) }
        if (candidates.isEmpty() || candidates[0] !is Mob) {
            dispose(true)
        } else {
            this.target = candidates[0] as Mob
            this.tick()
        }
    }

    protected open fun dispose(discard: Boolean = false) {
        if (discard) {
            this.discard()
        } else {
            this.kill()
        }
    }

    protected fun updatePosition() {
        var x = target.x
        var z = target.z
        var y = target.y + target.bbHeight
        /*val yRot = mob.yHeadRot
        val isAnimal = mob is Animal

        val blendedRotation = (yRot * 0.3 + mob.yBodyRot * 0.7)
        val headRotationRadians = Math.toRadians(blendedRotation) + 1.5707963267948966
        val bodyRotationRadians = Math.toRadians(mob.yBodyRot.toDouble())
        val pitchRadians = Math.toRadians(mob.xRot.toDouble())

        // Adjust position based on head rotation
        val cosPitch = cos(pitchRadians)
        val sinPitch = sin(pitchRadians)
        val cosHeadRotation = cos(headRotationRadians)
        val sinHeadRotation = sin(headRotationRadians)
        val cosBodyRotation = cos(bodyRotationRadians)
        val sinBodyRotation = sin(bodyRotationRadians)

        val pitchAdjustmentMultiplier = leashOffset.y

         val leashOffset = if (isAnimal) mob.getLeashOffset(0f).scale(1.7) else Vec3.ZERO
        val xOffset = cosHeadRotation * leashOffset.z
        val zOffset = sinHeadRotation * leashOffset.z

        // Calculate forward/backward direction based on body rotation
        val forwardX = -sinBodyRotation * pitchAdjustmentMultiplier
        val forwardZ = cosBodyRotation * pitchAdjustmentMultiplier


        // Apply pitch adjustment
        x += xOffset + sinPitch * forwardX
        z += zOffset + sinPitch * forwardZ
        y += if (isAnimal) 0.15 else 0.0 + sinPitch * pitchAdjustmentMultiplier * 1.1*/

        this.lerpTo(x, y, z, target.yBodyRot - 180, target.xRot, 1)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        if (compound.hasUUID("Target") && compound.contains("SourceStack")) {
            this.targetUUID = compound.getUUID("Target")
        } else {
            dispose(true)
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        compound.putUUID("Target", target.uuid)
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        buf.writeInt(target.id)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        this.target = level().getEntity(buf.readInt()) as Mob
    }
}
