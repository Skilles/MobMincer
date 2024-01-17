package net.mobmincer.client.menu.screen

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.mobmincer.client.menu.PowerProviderMenu

class PowerProviderScreen(menu: PowerProviderMenu, playerInventory: Inventory, title: Component) : AbstractContainerScreen<PowerProviderMenu>(
    menu,
    playerInventory,
    title
) {
    private var widthTooNarrow = false
    private var texture: ResourceLocation = ResourceLocation("textures/gui/container/furnace.png")
    private val litProgressSprite: ResourceLocation = ResourceLocation("container/furnace/lit_progress")
    private val burnProgressSprite: ResourceLocation = ResourceLocation("container/furnace/burn_progress")

    public override fun init() {
        super.init()
        this.widthTooNarrow = this.width < 379
        this.leftPos = if (this.widthTooNarrow) (this.width - this.imageWidth) / 2 else 97
        this.titleLabelX = (this.imageWidth - font.width(this.title)) / 2
    }

    public override fun containerTick() {
        super.containerTick()
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        this.renderTooltip(guiGraphics, mouseX, mouseY)
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        var l: Int
        var k: Int
        val i = this.leftPos
        val j = this.topPos
        guiGraphics.blit(this.texture, i, j, 0, 0, this.imageWidth, this.imageHeight)
        if (menu.isActive) {
            k = 14
            l = Mth.ceil(menu.energyProgress * 13.0f) + 1
            guiGraphics.blitSprite(this.litProgressSprite, 14, 14, 0, 14 - l, i + 56, j + 36 + 14 - l, 14, l)
        }
        k = 24
        l = Mth.ceil(menu.burnProgress * 24.0f)
        guiGraphics.blitSprite(this.burnProgressSprite, 24, 16, 0, 0, i + 79, j + 34, l, 16)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
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
