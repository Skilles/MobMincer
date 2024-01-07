package net.mobmincer.util

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation

object EncodingUtils {
    /**
     * Writes a [ResourceLocation] to a [FriendlyByteBuf] by stripping the namespace for vanilla identifiers to save space.
     */
    fun FriendlyByteBuf.writeIdentifier(identifier: ResourceLocation) {
        if (identifier.namespace == "minecraft") {
            this.writeUtf(identifier.path)
        } else {
            this.writeUtf(identifier.toString())
        }
    }

    fun <T : Tag> CompoundTag.getOrCreateTag(key: String, tagClass: Class<T>): T {
        return (
                this.get(key) ?: tagClass.getDeclaredConstructor().newInstance().also {
                    this.put(key, it)
                }
                ) as? T ?: error("Unable to create tag of type $tagClass")
    }
}
