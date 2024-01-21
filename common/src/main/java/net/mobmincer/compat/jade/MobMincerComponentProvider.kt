package net.mobmincer.compat.jade

import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.mobmincer.client.render.MobMincerEntityRenderer
import net.mobmincer.compat.jade.ComponentProviderUtils.appendTooltipData
import net.mobmincer.compat.jade.ComponentProviderUtils.getTooltipComponents
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.MMContent
import net.mobmincer.util.StringUtils
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
import kotlin.math.roundToInt

object MobMincerComponentProvider : IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    private fun getPowerBgHue(power: Float): Double {
        val color = StringUtils.getPercentageColour((power * 100).roundToInt())

        return when (color) {
            ChatFormatting.RED -> 0.0
            ChatFormatting.YELLOW -> 60.0
            ChatFormatting.GREEN -> 120.0
            else -> 180.0
        }
    }

    override fun appendTooltip(tooltip: ITooltip, accessor: EntityAccessor, config: IPluginConfig) {
        val serverData = accessor.serverData
        if (!serverData.isEmpty) {
            for ((i, component) in getTooltipComponents(serverData).withIndex()) {
                val contents = component.contents
                if (contents is TranslatableContents) {
                    val contentKey = contents.key
                    if (contentKey.endsWith("progress") || contentKey.endsWith("power") || contentKey.endsWith("fluid")) {
                        val progress = contents.args[0] as Float
                        val style = SimpleProgressStyle()
                        style.autoTextColor = false
                        style.textColor = Color.hex("#B2BEB5").toInt()
                        val bgHue = if (contentKey.endsWith("progress")) 158.0 else getPowerBgHue(progress)
                        val bgColor = Color.hsl(bgHue, 100.0, 32.0)
                        style.color = bgColor.toInt()
                        style.color2 = Color.hsl(bgColor.hue, bgColor.saturation, bgColor.lightness / 2).toInt()
                        val boxStyle = BoxStyle.GradientBorder.DEFAULT_VIEW_GROUP.clone()
                        boxStyle.roundCorner = true
                        boxStyle.borderWidth = 2f
                        val progressComponent = ProgressElement(
                            progress,
                            component,//.append(": ${(progress * 100).toInt()}%"),
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
        return MMContent.MOB_MINCER_ENTITY.id
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
