package net.mobmincer.core.item

import net.minecraft.world.item.ItemStack

enum class MobMincerType {
    BASIC,
    POWERED,
    CREATIVE;


    companion object {
        fun ItemStack.getMincerType(): MobMincerType {
            require(this.item is MobMincerItem)
            val tag = this.getOrCreateTagElement("MobMincer")
            if (tag.contains("MincerType")) {
                return MobMincerType.valueOf(tag.getString("MincerType"))
            }

            return BASIC
        }

        fun ItemStack.setMincerType(type: MobMincerType) {
            require(this.item is MobMincerItem)
            val tag = this.getOrCreateTagElement("MobMincer")
            tag.putString("MincerType", type.name)
        }
    }
}