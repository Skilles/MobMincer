package net.mobmincer.neoforge

import com.mojang.authlib.GameProfile
import net.minecraft.client.resources.language.I18n
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.common.util.FakePlayer
import java.util.*

object FakePlayerImpl {

    @JvmStatic
    fun createImpl(level: ServerLevel): Player {
        return FakePlayer(
            level,
            GameProfile(
                UUID.nameUUIDFromBytes("fakeplayer.mobmincer".toByteArray()),
                I18n.get("fakeplayer.mobmincer")
            )
        )
    }
}
