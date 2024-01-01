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
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
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
import net.mobmincer.core.attachment.AttachmentHolder
import net.mobmincer.core.attachment.Attachments
import net.mobmincer.core.config.MobMincerConfig
import net.mobmincer.core.loot.LootFactory
import net.mobmincer.core.loot.LootFactoryCache
import net.mobmincer.core.registry.MincerEntities.MOB_MINCER
import net.mobmincer.core.registry.MincerItems

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
    private val attachmentHolder = AttachmentHolder(this)

    var isErrored: Boolean
        get() = entityData.get(IS_ERRORED)
        set(value) {
            if (value == entityData.get(IS_ERRORED)) {
                return
            }
            entityData.set(IS_ERRORED, value)
        }

    val idleAnimationState = AnimationState()

    fun spawn(
        target: Mob,
        sourceStack: ItemStack,
        level: ServerLevel,
    ) {
        require(!level.isClientSide) { "Mob mincer entity must be spawned on the server" }
        require(sourceStack.`is`(MincerItems.MOB_MINCER.get())) { "Source stack must be a mob mincer item" }
        this.initSourceItem(sourceStack.copy())
        super.initialize(target)
        level.addFreshEntity(this)
        sourceStack.shrink(1)
    }

    private fun initSourceItem(sourceStack: ItemStack) {
        this.durability = sourceStack.maxDamage - sourceStack.damageValue
        this.itemEnchantments = EnchantmentHelper.getEnchantments(sourceStack)
        this.sourceStack = sourceStack
    }

    override fun onTargetBind() {
        if (!this.level().isClientSide) {
            val killedByPlayer = itemEnchantments.containsKey(Enchantments.SILK_TOUCH)
            this.lootFactory = LootFactoryCache.getLootFactory(target as Mob, killedByPlayer, itemEnchantments.getOrDefault(Enchantments.MOB_LOOTING, 0))
        }
    }

    companion object {
        private const val MAX_MINCE_TICK = 100

        private val IS_ERRORED = SynchedEntityData.defineId(MobMincerEntity::class.java, EntityDataSerializers.BOOLEAN)
    }

    override fun doTick() {
        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(target.isAlive, this.tickCount)
        }
        mainTick()
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
                val damage = MobMincerConfig.CONFIG.mobDamagePercent.get().toFloat() * target.maxHealth
                target.hurt(
                    damageSources().indirectMagic(this, this),
                    damage
                )
                attachmentHolder.onMince(damage)
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
        loot.filter { it.count > 0 }
            .randomOrNull()
            ?.let {
                if (!itemEnchantments.containsKey(Enchantments.MOB_LOOTING)) {
                    it.count = 1
                }
                this.spawnAtLocation(it)
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
        destroy()
    }

    override fun destroy(discard: Boolean) {
        target.removeTag("mob_mincer")
        attachmentHolder.onDeath()
        super.destroy(discard)
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

    override fun interact(player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown) {
            attachmentHolder.onInteract(player)
            return InteractionResult.sidedSuccess((this.level() as Level).isClientSide)
        }

        if (!player.mainHandItem.isEmpty && player.isShiftKeyDown) {
            // We are holding an item, so lets try to add it as an attachment
            if (attachmentHolder.tryAddAttachment(player.mainHandItem.item)) {
                player.mainHandItem.shrink(1)
            }
        }

        return InteractionResult.sidedSuccess((this.level() as Level).isClientSide)
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
        if (compound.contains("SourceStack") && compound.contains("Attachments")) {
            this.initSourceItem(ItemStack.of(compound.getCompound("SourceStack")))
            attachmentHolder.fromTag(compound.getList("Attachments", 10))
            attachmentHolder.onSpawn()
        } else {
            destroy(true)
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put("SourceStack", sourceStack.save(CompoundTag()))
        compound.put("Attachments", attachmentHolder.toTag())
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.saveAdditionalSpawnData(buf)
        buf.writeItem(sourceStack)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.loadAdditionalSpawnData(buf)
        this.initSourceItem(buf.readItem())
        attachmentHolder.onSpawn()
    }

    override fun containerChanged(container: Container) {
    }

    override fun openCustomInventoryScreen(player: Player) {
        if (!(level() as Level).isClientSide) {
        }
    }

    fun hasAttachment(attachment: Attachments): Boolean {
        return attachmentHolder.hasAttachment(attachment)
    }
}
