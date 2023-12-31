package net.mobmincer.fabric

import net.fabricmc.fabric.api.entity.FakePlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player

object FakePlayerImpl {

    @JvmStatic
    fun createImpl(level: ServerLevel): Player {
        return FakePlayer.get(level)
    }
}