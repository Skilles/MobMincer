package net.mobmincer.core.entity

import dev.architectury.extensions.network.EntitySpawnExtension
import dev.architectury.networking.NetworkManager
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.AnimationState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.AABB
import net.mobmincer.core.registry.MincerEntities.MOB_MINCER
import net.mobmincer.core.registry.MincerItems
import java.util.*

class MobMincerEntity(level: Level) : SmoothMotionEntity(MOB_MINCER.get(), level), EntitySpawnExtension {
    private var target: LivingEntity? = null

    private var targetUUID: UUID? = null

    private var currentMinceTick = 0

    private var durability = 0

    private val lootFactory by lazy { target?.createLootFactory(this) ?: error("Mob Mincer target is null") }

    val idleAnimationState = AnimationState()

    fun initialize(target: LivingEntity, durability: Int) {
        this.target = target
        this.durability = durability
        this.targetUUID = target.uuid
        this.setPos(target.x, target.y + target.bbHeight, target.z)
        this.customName = this.typeName
    }

    companion object {
        private const val MAX_MINCE_TICK = 100
    }

    override fun tick() {
        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(target?.isAlive == true, this.tickCount)
        }

        target?.let {
            if (it.isAlive) {
                if (!this.level().isClientSide) {
                    tickMince()
                }
                this.lerpTo(it.x, it.y + it.bbHeight, it.z, it.yHeadRot - 180, it.xRot, 1)
            } else {
                dropAsItem()
            }
        } ?: targetUUID?.let {
            val candidates = level().getEntities(
                this,
                AABB.ofSize(this.position(), 1.0, 1.0, 1.0)
            ) { entity -> entity.uuid.equals(it) }
            if (candidates.isEmpty()) {
                this.discard()
            } else {
                this.target = candidates[0] as LivingEntity
                this.tick()
            }
            return
        } ?: run {
            this.discard()
        }

        super.tick()
    }

    private fun tickMince() {
        if (++currentMinceTick >= MAX_MINCE_TICK) {
            if (dropTargetLoot()) {
                target?.let { it.hurt(damageSources().thorns(this), it.maxHealth * 0.1f) }
                if (--durability <= 0) {
                    dropAsItem()
                }
                (this.level() as ServerLevel).sendParticles(
                    ParticleTypes.HAPPY_VILLAGER, this.x, this.y + this.bbHeight, this.z,
                    3,
                    0.0,
                    0.1,
                    0.0,
                    1.0
                )
            } else {
                target?.let {
                    (this.level() as ServerLevel).sendParticles(
                        ParticleTypes.SMOKE, this.x, this.y + this.bbHeight, this.z,
                        10,
                        0.0,
                        0.1,
                        0.0,
                        0.01
                    )
                }
            }
            currentMinceTick = 0
        }
    }

    private fun dropTargetLoot(): Boolean {
        val loot = lootFactory()
        if (loot.isEmpty) {
            return false
        }
        loot[0].let {
            target?.spawnAtLocation(it)
        }
        return true
    }

    private fun dropAsItem() {
        if (this.isRemoved || level().isClientSide) {
            return
        }
        target?.removeTag("mob_mincer")
        if (durability > 0) {
            val itemStack = MincerItems.MOB_MINCER.get().defaultInstance
            itemStack.damageValue = itemStack.maxDamage - durability
            target?.spawnAtLocation(itemStack)
        }
        this.kill()
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (this.isInvulnerableTo(source)) {
            return false
        }
        if (source.`is`(DamageTypes.PLAYER_ATTACK)) {
            dropAsItem()
        }
        return true
    }

    override fun isPickable(): Boolean {
        return true
    }

    override fun skipAttackInteraction(entity: Entity): Boolean {
        if (entity is Player) {
            return this.hurt(damageSources().playerAttack(entity), 0.0f)
        }

        return false
    }

    override fun getAddEntityPacket(): Packet<ClientGamePacketListener> {
        return NetworkManager.createAddEntityPacket(this)
    }

    override fun defineSynchedData() {

    }

    override fun readAdditionalSaveData(compound: CompoundTag) = when {
        compound.hasUUID("Target") -> {
            this.targetUUID = compound.getUUID("Target")
        }

        else -> {
            this.targetUUID = null
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        target?.let { compound.putUUID("Target", it.uuid) }
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        buf.writeInt(target?.id ?: -1)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        val targetId = buf.readInt()
        if (targetId != -1) {
            this.target = level().getEntity(targetId) as LivingEntity?
        }
    }
}

private fun LivingEntity.createLootFactory(damager: Entity?): () -> ObjectArrayList<ItemStack> {
    val resourceLocation: ResourceLocation = this.lootTable
    val lootTable = level().server!!.lootData.getLootTable(resourceLocation)
    val builder = LootParams.Builder(level() as ServerLevel)
        .withParameter(LootContextParams.THIS_ENTITY, this)
        .withParameter(LootContextParams.ORIGIN, this.position())
        .withParameter(
            LootContextParams.DAMAGE_SOURCE,
            damager?.let { this.level().damageSources().thorns(it) } ?: this.level().damageSources().generic())
        .withOptionalParameter(LootContextParams.KILLER_ENTITY, damager)
        .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, damager)
    val lootParams = builder.create(LootContextParamSets.ENTITY)

    return { lootTable.getRandomItems(lootParams, this.lootTableSeed) }
}
