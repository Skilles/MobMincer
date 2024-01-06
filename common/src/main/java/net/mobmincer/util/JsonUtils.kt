package net.mobmincer.util

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

object JsonUtils {
    /**
     * Writes a [ResourceLocation] to a [FriendlyByteBuf] by stripping the namespace for vanilla identifiers to save space.
     */
    fun writeIdentifier(buf: FriendlyByteBuf, identifier: ResourceLocation) {
        if (identifier.namespace == "minecraft") {
            buf.writeUtf(identifier.path)
        } else {
            buf.writeUtf(identifier.toString())
        }
    }
}