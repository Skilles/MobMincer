package net.mobmincer.api.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level


fun interface MachineGuiHandler {
    fun open(player: Player, pos: BlockPos, level: Level)
}