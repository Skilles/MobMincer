package net.mobmincer.compat.jeirei

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.mobmincer.MobMincer

object MobMincerCategory {
    val TITLE: Component = Component.translatable("mobmincer.jei.category.title")

    val ID = ResourceLocation(MobMincer.MOD_ID, "mob_mincer")

    const val HEIGHT = 60
    const val WIDTH = 75
}
