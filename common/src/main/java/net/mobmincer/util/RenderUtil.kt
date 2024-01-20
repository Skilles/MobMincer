package net.mobmincer.util

import net.minecraft.world.entity.AnimationState


object RenderUtil {

    fun AnimationState.setToTime(desiredTimeInSeconds: Float) {
        // Convert desired time to ticks (20 ticks = 1 second)
        val desiredTimeInTicks = (desiredTimeInSeconds * 20).toInt()

        // Start the animation
        this.start(0) // Start at tick 0

        // Calculate the age in ticks to advance the animation
        // Assuming 'speed' is a known factor you want to apply
        val speed = 1.0f // Example speed
        val ageInTicks = desiredTimeInTicks / speed

        // Update the animation to the desired time
        this.updateTime(ageInTicks, speed)
    }

}