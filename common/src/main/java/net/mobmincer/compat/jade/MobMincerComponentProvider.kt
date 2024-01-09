package net.mobmincer.compat.jade

import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.mobmincer.client.render.MobMincerEntityRenderer
import net.mobmincer.compat.jade.ComponentProviderUtils.appendTooltipData
import net.mobmincer.compat.jade.ComponentProviderUtils.getTooltipComponents
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.MincerEntities.MOB_MINCER
import snownee.jade.api.EntityAccessor
import snownee.jade.api.IEntityComponentProvider
import snownee.jade.api.IServerDataProvider
import snownee.jade.api.ITooltip
import snownee.jade.api.config.IPluginConfig
import snownee.jade.api.ui.BoxStyle
import snownee.jade.api.ui.Color
import snownee.jade.api.ui.IElement
import snownee.jade.impl.ui.HealthElement
import snownee.jade.impl.ui.ProgressElement
import snownee.jade.impl.ui.SimpleProgressStyle
import snownee.jade.impl.ui.SpriteElement

object MobMincerComponentProvider : IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    override fun appendTooltip(tooltip: ITooltip, accessor: EntityAccessor, config: IPluginConfig) {
        val serverData = accessor.serverData
        if (!serverData.isEmpty) {
            for ((i, component) in getTooltipComponents(serverData).withIndex()) {
                val contents = component.contents
                if (contents is TranslatableContents) {
                    if (contents.key.endsWith("progress")) {
                        val progress = contents.args[0] as Float
                        val style = SimpleProgressStyle()
                        style.autoTextColor = false
                        style.textColor = Color.hex("#B2BEB5").toInt()
                        style.color = Color.hex("#00A36C").toInt()
                        style.color2 = Color.hex("#355E3B").toInt()
                        val boxStyle = BoxStyle.GradientBorder.DEFAULT_VIEW_GROUP.clone()
                        boxStyle.roundCorner = true
                        boxStyle.borderWidth = 2f
                        val progressComponent = ProgressElement(
                            progress,
                            Component.literal("${(progress * 100).toInt()}%"),
                            style,
                            boxStyle,
                            false
                        )
                        tooltip.add(i + 1, progressComponent)
                        continue
                    } else if (contents.key.endsWith("durability")) {
                        val (health, maxHealth) = contents.args
                        val healthComponent = HealthElement(maxHealth as Int / 2f, health as Int / 2f)
                        tooltip.add(i + 1, healthComponent)
                        continue
                    }
                }
                tooltip.add(i + 1, component)
            }
        }
    }

    override fun getUid(): ResourceLocation {
        return MOB_MINCER.id
    }

    override fun appendServerData(data: CompoundTag, accessor: EntityAccessor) {
        val entity = accessor.entity as MobMincerEntity
        entity.appendTooltipData(data)
    }

    override fun getIcon(accessor: EntityAccessor, config: IPluginConfig, currentIcon: IElement): IElement {
        val icon = SpriteElement(MobMincerEntityRenderer.TEXTURE_LOCATION, 16, 16)
        return icon
    }
}
