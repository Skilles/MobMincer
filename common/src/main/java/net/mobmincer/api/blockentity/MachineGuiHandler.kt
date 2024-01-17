package net.mobmincer.api.blockentity

import dev.architectury.registry.menu.MenuRegistry
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level


fun interface MachineGuiHandler {
    fun open(player: Player, pos: BlockPos, level: Level): Boolean

    companion object {
        val SIMPLE = MachineGuiHandler { player, pos, level ->
            val blockEntity = level.getBlockEntity(pos)
            if (blockEntity is MenuProvider && !level.isClientSide) {
                MenuRegistry.openMenu(player as ServerPlayer, blockEntity)
                return@MachineGuiHandler true
            }

            return@MachineGuiHandler false
        }
    }
}