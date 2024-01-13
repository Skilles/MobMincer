package net.mobmincer.api.inventory

import net.minecraft.world.Container

interface InventoryProvider {

    fun getInventory(): Container
}