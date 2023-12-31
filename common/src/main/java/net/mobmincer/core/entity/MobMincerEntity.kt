package net.mobmincer.core.entity

import dev.architectury.networking.NetworkManager
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.Container
import net.minecraft.world.ContainerListener
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.AnimationState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.HasCustomInventoryScreen
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.mobmincer.core.config.MobMincerConfig
import net.mobmincer.core.loot.LootFactory
import net.mobmincer.core.loot.LootFactoryCache
import net.mobmincer.core.registry.MincerEntities.MOB_MINCER

class MobMincerEntity(level: Level) :
    WearableEntity(
        MOB_MINCER.get(),
        level
    ),
    ContainerListener,
    HasCustomInventoryScreen {

    var currentMinceTick = 0

    var maxMinceTick: Int = MobMincerConfig.CONFIG.maxMinceTick.get()

    var durability = 0

    private lateinit var lootFactory: LootFactory
    private lateinit var itemEnchantments: Map<Enchantment, Int>
    lateinit var sourceStack: ItemStack

    var isErrored: Boolean
        get() = entityData.get(IS_ERRORED)
        set(value) {
            if (value == entityData.get(IS_ERRORED)) {
                return
            }
            entityData.set(IS_ERRORED, value)
        }

    val idleAnimationState = AnimationState()

    fun initialize(
        target: Mob,
        sourceStack: ItemStack,
    ) {
        super.initialize(target)
        this.setPos(target.x, target.y + target.bbHeight, target.z)

        this.initSourceItem(sourceStack)
    }

    private fun initSourceItem(sourceStack: ItemStack) {
        this.durability = sourceStack.maxDamage - sourceStack.damageValue
        this.itemEnchantments = EnchantmentHelper.getEnchantments(sourceStack)
        this.sourceStack = sourceStack
        val killedByPlayer = itemEnchantments.containsKey(Enchantments.SILK_TOUCH)
        this.lootFactory = LootFactoryCache.getLootFactory(target as Mob, killedByPlayer)
    }

    companion object {
        private const val MAX_MINCE_TICK = 100

        private val IS_ERRORED = SynchedEntityData.defineId(MobMincerEntity::class.java, EntityDataSerializers.BOOLEAN)
    }

    override fun tick() {
        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(target.isAlive, this.tickCount)
        }
        mainTick()
        super.tick()
    }

    private fun mainTick() {
        val isClient = this.level().isClientSide

        if (!isClient) {
            if (target.isAlive) {
                tickMince(this.level() as ServerLevel)
            } else {
                dropAsItem()
            }
        }
    }

    private fun tickMince(level: ServerLevel) {
        if (++currentMinceTick >= MAX_MINCE_TICK) {
            if (dropTargetLoot()) {
                isErrored = false
                target.hurt(
                    damageSources().thorns(this),
                    target.maxHealth * MobMincerConfig.CONFIG.mobDamagePercent.get().toFloat()
                )
                takeDurabilityDamage()
                level.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER, this.x, this.y + this.bbHeight, this.z,
                    3,
                    0.0,
                    0.1,
                    0.0,
                    1.0
                )
            } else {
                isErrored = true
                level.sendParticles(
                    ParticleTypes.SMOKE, this.x, this.y + this.bbHeight, this.z,
                    10,
                    0.0,
                    0.1,
                    0.0,
                    0.01
                )
            }
            currentMinceTick = 0
        }
    }

    private fun takeDurabilityDamage() {
        // Chance to ignore durability: (1 / bound) * level
        if (this.random.nextInt(MobMincerConfig.CONFIG.unbreakingBound.get()) < itemEnchantments.getOrDefault(Enchantments.UNBREAKING, 0)) {
            return
        }
        if (--durability <= 0) {
            dropAsItem()
        }
    }

    private fun dropTargetLoot(): Boolean {
        val loot = lootFactory.generateLoot()
        if (loot.isEmpty) {
            return false
        }
        val lootItem = loot.firstOrNull { it.count > 0 }
        lootItem?.let {
            it.count = 1 * (1 + itemEnchantments.getOrDefault(Enchantments.MOB_LOOTING, 0))
            target.spawnAtLocation(it)
        }
        return true
    }

    private fun dropAsItem() {
        if (this.isRemoved || level().isClientSide) {
            return
        }
        if (durability > 0) {
            val itemStack = sourceStack
            itemStack.damageValue = itemStack.maxDamage - durability
            this.spawnAtLocation(itemStack)
        } else {
            (level() as ServerLevel).playSound(
                null,
                this.blockPosition(),
                SoundEvents.ITEM_BREAK,
                SoundSource.NEUTRAL,
                1.0f,
                1.0f
            )
        }
        dispose()
    }

    override fun dispose(discard: Boolean) {
        target.removeTag("mob_mincer")
        super.dispose(discard)
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
        this.entityData.define(IS_ERRORED, false)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        super.readAdditionalSaveData(compound)
        if (compound.contains("SourceStack")) {
            this.initSourceItem(ItemStack.of(compound.getCompound("SourceStack")))
        } else {
            dispose(true)
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put("SourceStack", sourceStack.save(CompoundTag()))
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.saveAdditionalSpawnData(buf)
        buf.writeItem(sourceStack)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.loadAdditionalSpawnData(buf)
        this.initSourceItem(buf.readItem())
    }

    override fun containerChanged(container: Container) {
    }

    override fun openCustomInventoryScreen(player: Player) {
        if (!(level() as Level).isClientSide) {
        }
    }
}
