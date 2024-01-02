package net.mobmincer.network

import dev.architectury.networking.NetworkManager
import io.netty.buffer.Unpooled
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Mob
import net.mobmincer.MobMincer
import net.mobmincer.core.entity.MobMincerEntity

object MincerNetwork {

    private val CHANGE_TARGET_PACKET_ID = ResourceLocation(MobMincer.MOD_ID, "change_target")

    fun registerClientRecievers() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), CHANGE_TARGET_PACKET_ID) { buf, context ->
            val mincerId = buf.readInt()
            val newTargetId = buf.readInt()
            val level = context.player.level()
            val mincer = level.getEntity(mincerId) as MobMincerEntity
            val newTarget = level.getEntity(newTargetId) as Mob
            mincer.changeTarget(newTarget)
        }
    }

    fun updateClientMincerTarget(mincerEntity: MobMincerEntity) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeInt(mincerEntity.id)
        buf.writeInt(mincerEntity.target.id)
        val level = mincerEntity.level() as ServerLevel
        NetworkManager.sendToPlayers(level.players(), CHANGE_TARGET_PACKET_ID, buf)
    }
}