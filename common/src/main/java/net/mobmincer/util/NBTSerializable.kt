package net.mobmincer.util

import net.minecraft.nbt.Tag

interface NBTSerializable {
    fun serialize(): Tag? = null

    fun deserialize(tag: Tag?) {}
}