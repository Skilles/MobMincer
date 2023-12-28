package net.mobmincer.core.entity

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level

abstract class SmoothMotionEntity(entityType: EntityType<*>, level: Level) : Entity(entityType, level) {
    private var lerpSteps = 0
    private var lerpX = 0.0
    private var lerpY = 0.0
    private var lerpZ = 0.0
    private var lerpYRot = 0.0
    private var lerpXRot = 0.0

    override fun lerpTargetX(): Double {
        return if (this.lerpSteps > 0) this.lerpX else this.x
    }

    override fun lerpTargetY(): Double {
        return if (this.lerpSteps > 0) this.lerpY else this.y
    }

    override fun lerpTargetZ(): Double {
        return if (this.lerpSteps > 0) this.lerpZ else this.z
    }

    override fun lerpTargetXRot(): Float {
        return if (this.lerpSteps > 0) lerpXRot.toFloat() else this.xRot
    }

    override fun lerpTargetYRot(): Float {
        return if (this.lerpSteps > 0) lerpYRot.toFloat() else this.yRot
    }

    override fun lerpTo(x: Double, y: Double, z: Double, yRot: Float, xRot: Float, steps: Int) {
        this.lerpX = x
        this.lerpY = y
        this.lerpZ = z
        this.lerpYRot = yRot.toDouble()
        this.lerpXRot = xRot.toDouble()
        this.lerpSteps = steps
    }

    override fun tick() {
        super.tick()
        this.tickLerp()
    }

    private fun tickLerp() {
        if (this.lerpSteps <= 0) {
            return
        }
        this.lerpPositionAndRotationStep(
            this.lerpSteps,
            this.lerpX,
            this.lerpY,
            this.lerpZ,
            this.lerpYRot,
            this.lerpXRot
        )
        --this.lerpSteps
    }
}
