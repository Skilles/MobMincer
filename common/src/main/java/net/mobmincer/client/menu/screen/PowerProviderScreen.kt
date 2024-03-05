package net.mobmincer.client.menu.screen

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.mobmincer.MobMincer
import net.mobmincer.client.menu.PowerProviderMenu

class PowerProviderScreen(menu: PowerProviderMenu, playerInventory: Inventory, title: Component) : AbstractContainerScreen<PowerProviderMenu>(
    menu,
    playerInventory,
    title
) {
    private var widthTooNarrow = false
    private var texture: ResourceLocation = ResourceLocation(MobMincer.MOD_ID, "textures/gui/power_provider.png")
    private val litProgressSprite: ResourceLocation = ResourceLocation("container/furnace/lit_progress")

    //private val burnProgressSprite: ResourceLocation = ResourceLocation("container/furnace/burn_progress")
    private val powerProgressSprite: ResourceLocation =
        ResourceLocation(MobMincer.MOD_ID, "power_progress")

    public override fun init() {
        super.init()
        // this.widthTooNarrow = this.width < 379
        // this.leftPos = if (this.widthTooNarrow) (this.width - this.imageWidth) / 2 else 97
        // this.titleLabelX = (this.imageWidth - font.width(this.title)) / 2
    }

    public override fun containerTick() {
        super.containerTick()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (this.widthTooNarrow) {
            this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        } else {
            super.render(guiGraphics, mouseX, mouseY, partialTick)
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderTooltip(guiGraphics: GuiGraphics, x: Int, y: Int) {
        super.renderTooltip(guiGraphics, x, y)

        val powerProgressTop = this.topPos + 25
        val powerProgressHeight = 36
        val powerProgressLeft = this.leftPos + 47
        val powerProgressWidth = 110
        if (x >= powerProgressLeft &&
            x <= powerProgressLeft + powerProgressWidth &&
            y >= powerProgressTop &&
            y <= powerProgressTop + powerProgressHeight
        ) {
            val energy = menu.energy
            val capacity = menu.capacity
            val tooltip = Component.literal("$energy / $capacity RF")
            guiGraphics.renderTooltip(minecraft!!.font, tooltip, x, y)
        }
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        var l: Int
        var k: Int
        val i = this.leftPos
        val j = this.topPos
        guiGraphics.blit(this.texture, i, j, 0, 0, this.imageWidth, this.imageHeight)
        if (menu.isBurning) {
            k = 14
            l = Mth.ceil(menu.burnProgress * 13.0f) + 1
            guiGraphics.blitSprite(this.litProgressSprite, 14, 14, 0, 14 - l, i + 19, j + 36 + 14 - l, 14, l)
        }
        k = 24
        l = Mth.ceil(menu.energyProgress * 112.0f)
        guiGraphics.blitSprite(this.powerProgressSprite, 112, 40, 2, 2, i + 48, j + 25, l, 36)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (this.widthTooNarrow) {
            return true
        }

        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun slotClicked(slot: Slot?, slotId: Int, mouseButton: Int, type: ClickType?) {
        super.slotClicked(slot, slotId, mouseButton, type)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun hasClickedOutside(mouseX: Double, mouseY: Double, guiLeft: Int, guiTop: Int, mouseButton: Int): Boolean {
        return mouseX < guiLeft.toDouble() || mouseY < guiTop.toDouble() || mouseX >= (guiLeft + this.imageWidth).toDouble() || mouseY >= (guiTop + this.imageHeight).toDouble()
    }
}
