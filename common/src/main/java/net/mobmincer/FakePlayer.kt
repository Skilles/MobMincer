package net.mobmincer

import dev.architectury.injectables.annotations.ExpectPlatform
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.phys.Vec3
import java.lang.ref.WeakReference

object FakePlayer {

    private var instance: Player? = null

    fun create(level: ServerLevel): WeakReference<Player> {
        return WeakReference(instance ?: createImpl(level))
    }

    fun create(level: ServerLevel, position: Vec3): WeakReference<Player> {
        instance ?: createImpl(level)
        instance?.setPos(position)

        return WeakReference(instance)
    }

    fun unload(world: LevelAccessor) {
        if (instance?.level() == world) {
            instance = null
        }
    }

    @JvmStatic
    @ExpectPlatform
    fun createImpl(level: ServerLevel): Player {
        throw AssertionError()
    }
}
