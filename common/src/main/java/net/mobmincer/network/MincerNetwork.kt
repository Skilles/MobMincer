package net.mobmincer.network

import dev.architectury.networking.NetworkManager
import dev.architectury.utils.GameInstance
import io.netty.buffer.Unpooled
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.storage.loot.LootDataType
import net.minecraft.world.level.storage.loot.LootTable
import net.mobmincer.MobMincer
import net.mobmincer.core.attachment.AttachmentInstance
import net.mobmincer.core.attachment.Attachments
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.loot.LootLookup
import net.mobmincer.util.EncodingUtils.writeIdentifier
import java.util.*
import kotlin.math.min

object MincerNetwork {

    private val CHANGE_TARGET = ResourceLocation(MobMincer.MOD_ID, "ct")
    private val SEND_LOOT_DATA: ResourceLocation = ResourceLocation(MobMincer.MOD_ID, "sld")
    private val ASK_SYNC_LOOT: ResourceLocation = ResourceLocation(MobMincer.MOD_ID, "asl")
    private val SYNC_MINCER_DATA: ResourceLocation = ResourceLocation(MobMincer.MOD_ID, "smd")
    private val SYNC_MINCER_ATTACHMENT: ResourceLocation = ResourceLocation(MobMincer.MOD_ID, "sma")

    fun registerClientRecievers() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), CHANGE_TARGET) { buf, context ->
            val mincerId = buf.readInt()
            val newTargetId = buf.readInt()
            context.queue {
                val level = context.player.level()
                val mincer = level.getEntity(mincerId) as MobMincerEntity
                val newTarget = level.getEntity(newTargetId) as LivingEntity
                mincer.changeTarget(newTarget)
            }
        }

        NetworkManager.registerReceiver(NetworkManager.s2c(), SEND_LOOT_DATA) { buf, context ->
            val packetData = HashMap<ResourceLocation, CompoundTag>()
            val size = buf.readInt()
            for (i in 0 until size) {
                val identifier = buf.readResourceLocation()
                val receivedNbtData = buf.readNbt()
                receivedNbtData?.let {
                    packetData[identifier] = it
                }
            }
            context.queue {
                packetData.forEach { (identifier, nbt) ->
                    val deserializeResult = LootTable.CODEC.parse(NbtOps.INSTANCE, nbt)

                    if (deserializeResult.result().isPresent) {
                        val receivedLootTable = deserializeResult.result().get()
                        // Use the deserialized LootTable
                        LootLookup.set(identifier, receivedLootTable)
                    } else {
                        // Handle deserialization failure
                        MobMincer.logger.error("Failed to deserialize loot table $identifier")
                    }
                }
            }
        }

        NetworkManager.registerReceiver(NetworkManager.s2c(), SYNC_MINCER_DATA) { buf, context ->
            val mincerId = buf.readInt()
            val mincerData = buf.readNbt() ?: return@registerReceiver
            context.queue {
                val level = context.player.level()
                val mincer = level.getEntity(mincerId) as? MobMincerEntity
                mincer?.load(mincerData)
            }
        }

        NetworkManager.registerReceiver(NetworkManager.s2c(), SYNC_MINCER_ATTACHMENT) { buf, context ->
            val mincerId = buf.readInt()
            val attachment = buf.readEnum(Attachments::class.java)
            val attachmentData = buf.readNbt() ?: return@registerReceiver
            context.queue {
                val level = context.player.level()
                val mincer = level.getEntity(mincerId) as? MobMincerEntity
                mincer?.attachments?.loadAttachment(attachment, attachmentData)
            }
        }
    }

    fun registerServerRecievers() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), ASK_SYNC_LOOT) { _, context ->
            context.queue {
                sendLootToPlayers(GameInstance.getServer()!!, Collections.singletonList(context.player as ServerPlayer))
            }
        }
    }

    fun syncMincerData(mincerEntity: MobMincerEntity) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeInt(mincerEntity.id)
        val nbt = CompoundTag()
        mincerEntity.save(nbt)
        buf.writeNbt(nbt)
        val level = mincerEntity.level() as ServerLevel
        NetworkManager.sendToPlayers(level.players(), SYNC_MINCER_DATA, buf)
    }

    fun syncAttachmentData(mincerEntity: MobMincerEntity, attachment: Attachments) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeInt(mincerEntity.id)
        val nbt = CompoundTag()
        buf.writeEnum(attachment)
        mincerEntity.attachments.getAttachment<AttachmentInstance>(attachment)!!.serialize(nbt)
        buf.writeNbt(nbt)
        val level = mincerEntity.level() as ServerLevel
        NetworkManager.sendToPlayers(level.players(), SYNC_MINCER_ATTACHMENT, buf)
    }

    fun updateClientMincerTarget(mincerEntity: MobMincerEntity) {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeInt(mincerEntity.id)
        buf.writeInt(mincerEntity.target.id)
        val level = mincerEntity.level() as ServerLevel
        NetworkManager.sendToPlayers(level.players(), CHANGE_TARGET, buf)
    }

    fun sendLootToPlayers(server: MinecraftServer, players: List<ServerPlayer?>) {
        val lootManager = server.lootData
        val names: List<ResourceLocation> = lootManager.getKeys(LootDataType.TABLE).filter {
            it.path.contains("entities")
        }

        val size = 50
        var i = 0
        while (i < names.size) {
            val end = min(names.size.toDouble(), (i + size).toDouble()).toInt()
            val buf = FriendlyByteBuf(Unpooled.buffer())
            buf.writeInt(end - i)
            for (j in i until end) {
                val identifier = names[j]
                val table = lootManager.getLootTable(identifier)
                LootLookup.set(identifier, table)
                LootTable.CODEC.encodeStart(
                    NbtOps.INSTANCE,
                    table
                ).result().ifPresentOrElse({
                    buf.writeIdentifier(identifier)
                    buf.writeNbt(it)
                }, {
                    MobMincer.logger.error("Failed to serialize loot table $identifier")
                })
            }
            for (player in players) {
                NetworkManager.sendToPlayer(player, SEND_LOOT_DATA, FriendlyByteBuf(buf.duplicate()))
            }
            i += size
        }
    }

    fun askForLootData() {
        NetworkManager.sendToServer(ASK_SYNC_LOOT, FriendlyByteBuf(Unpooled.buffer()))
    }
}
