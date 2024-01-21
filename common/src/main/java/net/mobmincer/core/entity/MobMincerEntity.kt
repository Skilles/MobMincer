package net.mobmincer.core.entity

import dev.architectury.networking.NetworkManager
import net.minecraft.core.particles.ParticleOptions
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
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.AnimationState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.mobmincer.MobMincer
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.attachment.AttachmentHolder
import net.mobmincer.core.attachment.Attachments
import net.mobmincer.core.attachment.StorageAttachment
import net.mobmincer.core.item.MobMincerType
import net.mobmincer.core.item.MobMincerType.Companion.getMincerType
import net.mobmincer.core.loot.LootFactory
import net.mobmincer.core.loot.LootLookup
import net.mobmincer.core.registry.MMContent
import net.mobmincer.energy.EnergyUtil.getEnergyStorage
import net.mobmincer.network.MincerNetwork
import net.mobmincer.util.MathUtils
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class MobMincerEntity(type: EntityType<*>, level: Level) :
    WearableEntity(
        type,
        level
    ) {

    var currentMinceTick = 0
    var maxMinceTick: Int = 1000

    var damageDealt: Float = 0.0f

    private lateinit var lootFactory: LootFactory // not initialized on client
    private lateinit var itemEnchantments: Map<Enchantment, Int>
    private var hasMending: Boolean = false
    lateinit var sourceStack: ItemStack
    val attachments = AttachmentHolder(this)
    lateinit var mincerType: MobMincerType

    var isErrored: Boolean
        get() = entityData.get(IS_ERRORED)
        set(value) {
            if (value == entityData.get(IS_ERRORED)) {
                return
            }
            entityData.set(IS_ERRORED, value)
        }

    // val idleAnimationState = AnimationState()
    val fillTankAnimationState = AnimationState()

    val canMince: Boolean
        get() = when (this.mincerType) {
            MobMincerType.CREATIVE -> true
            MobMincerType.POWERED -> (sourceStack.getEnergyStorage()?.energy ?: 0) >= MobMincerConfig.CONFIG.poweredMinceCost.get()
            else -> sourceStack.damageValue <= sourceStack.maxDamage
        }

    private fun initSourceItem(sourceStack: ItemStack) {
        this.sourceStack = sourceStack
        this.itemEnchantments = EnchantmentHelper.getEnchantments(sourceStack)
        this.maxMinceTick = MathUtils.getCalculatedMaxMinceTick(itemEnchantments.getOrDefault(Enchantments.SOUL_SPEED, 0))
        this.hasMending = itemEnchantments.containsKey(Enchantments.MENDING)
        this.mincerType = sourceStack.getMincerType()
    }

    private fun initialize(target: LivingEntity, sourceStack: ItemStack, level: ServerLevel) {
        initSourceItem(sourceStack.copy())
        super.initialize(target)
        sourceStack.shrink(1)
        readAttachmentsFromTag(sourceStack.getOrCreateTagElement(ROOT_TAG))
        level.addFreshEntity(this)
        attachments.onAttach()
    }

    override fun onTargetBind() {
        this.target.addTag(TARGET_TAG)
        if (!this.level().isClientSide) {
            val killedByPlayer = itemEnchantments.containsKey(Enchantments.SILK_TOUCH)
            this.lootFactory = LootFactory.create(target, killedByPlayer, itemEnchantments.getOrDefault(Enchantments.MOB_LOOTING, 0))
        }
        attachments.onSpawn()
    }

    fun changeTarget(target: LivingEntity) {
        this.target.removeTag(TARGET_TAG)
        super.initialize(target)
        if (!this.level().isClientSide) {
            MincerNetwork.updateClientMincerTarget(this)
        }
    }

    companion object {
        private val IS_ERRORED = SynchedEntityData.defineId(MobMincerEntity::class.java, EntityDataSerializers.BOOLEAN)

        // Used to know when an entity has a mob mincer attached to it
        const val TARGET_TAG = "mobmincer"

        // Used to know when an entity has already been minced
        const val TAG_SKIP_LOOT = "${TARGET_TAG}:skip_loot"

        const val ROOT_TAG = "MobMincer"
        const val ATTACHMENTS_TAG = "Attachments"
        const val ATTACHMENTS_TYPE_TAG = "Type"
        const val ATTACHMENTS_DATA_TAG = "Data"

        fun spawn(
            target: LivingEntity,
            sourceStack: ItemStack,
            level: Level,
        ): MobMincerEntity? {
            require(!level.isClientSide) { "Mob mincer entity must be spawned on the server" }
            require(sourceStack.`is`(MMContent.MOB_MINCER_ITEM.get())) { "Source stack must be a mob mincer item" }
            val entity = MMContent.MOB_MINCER_ENTITY.get().create(level) ?: return null
            entity.initialize(target, sourceStack, level as ServerLevel)
            return entity
        }

        fun canAttach(target: LivingEntity, sourceStack: ItemStack): Boolean {
            return !target.isBaby &&
                    target.isAlive &&
                    target.passengers.isEmpty() &&
                    LootLookup.hasLoot(target, EnchantmentHelper.hasSilkTouch(sourceStack)) &&
                    !target.tags.contains(TARGET_TAG) &&
                    MobMincerConfig.testEntityFilter(target.type.`arch$registryName`())
        }
    }

    override fun doTick() {
        val level = this.level()
        if (level is ServerLevel) {
            serverTick(level)
        } else {
            // idleAnimationState.animateWhen(target.isAlive, this.tickCount)
            fillTankAnimationState.animateWhen(attachments.hasAttachment(Attachments.TANK), this.tickCount)
        }
    }

    private fun serverTick(level: ServerLevel) {
        if (target.isAlive) {
            tickMince(level)
        } else {
            if (hasMending) {
                mendMincer()
            }
            dropAsItem(DestroyReason.TARGET_KILLED)
        }
    }

    private fun mendMincer() {
        val repairAmount = damageDealt * MobMincerConfig.CONFIG.mendingRepairMultiplier.get()
        MobMincer.logger.info("Repairing mincer $repairAmount") // TODO: Remove
        if (repairAmount > 0) {
            if (mincerType == MobMincerType.POWERED) {
                sourceStack.getEnergyStorage()?.insert(repairAmount.roundToLong() * (getMincePowerCost() / 2))
            } else if (mincerType == MobMincerType.BASIC) {
                sourceStack.hurt(-repairAmount.roundToInt(), random, null)
            }
        }
    }

    private fun tickMince(level: ServerLevel) {
        if (++currentMinceTick >= maxMinceTick) {
            if (canMince && dropTargetLoot()) {
                isErrored = false
                val damage = MobMincerConfig.CONFIG.mobDamagePercent.get().toFloat() * target.maxHealth
                if (target.hurt(
                        damageSources().indirectMagic(this, this),
                        damage
                    )
                ) {
                    damageDealt += damage
                }
                attachments.onMince(damage)
                spawnParticles(level, ParticleTypes.HAPPY_VILLAGER, 3, 1.0)
                takeDurabilityDamage()
            } else {
                isErrored = true
                spawnParticles(level, ParticleTypes.SMOKE, 10, 0.01)
            }
            currentMinceTick = 0
        }
    }

    private fun spawnParticles(level: ServerLevel, type: ParticleOptions, count: Int = 1, speed: Double = 1.0) {
        level.sendParticles(
            type, this.x, this.y + this.bbHeight, this.z,
            count,
            0.0,
            0.1,
            0.0,
            speed
        )
    }

    private fun getMincePowerCost(): Long {
        val baseCost = MobMincerConfig.CONFIG.poweredMinceCost.get()
        val unbreaking = itemEnchantments.getOrDefault(Enchantments.UNBREAKING, 0)
        return if (unbreaking > 0) {
            min(
                baseCost.toDouble(),
                baseCost * (baseCost * 1.5 / unbreaking)
            ).roundToLong()
        } else {
            baseCost.toLong()
        }
    }

    private fun takeDurabilityDamage() {
        if (this.mincerType == MobMincerType.CREATIVE) {
            return
        }

        if (this.mincerType == MobMincerType.POWERED) {
            sourceStack.getEnergyStorage()?.extract(getMincePowerCost())
            return
        }

        // Chance to ignore durability: (1 / bound) * level
        if (this.random.nextInt(MobMincerConfig.CONFIG.unbreakingBound.get()) < itemEnchantments.getOrDefault(Enchantments.UNBREAKING, 0)) {
            return
        }
        if (++sourceStack.damageValue > sourceStack.maxDamage) {
            dropAsItem(DestroyReason.BROKEN)
        }
    }

    private fun dropTargetLoot(): Boolean {
        val loot = generateLoot() ?: return false
        loot.ifPresent {
            if (!itemEnchantments.containsKey(Enchantments.MOB_LOOTING)) {
                it.count = 1
            }
            attachments.getAttachment<StorageAttachment>(Attachments.STORAGE)?.let { attachment ->
                if (attachment.inventory.canAddItem(it)) {
                    attachment.inventory.addItem(it)
                } else {
                    this.spawnAtLocation(it)
                }
            } ?: this.spawnAtLocation(it)
            target.addTag(TAG_SKIP_LOOT)
        }
        return true
    }

    private fun generateLoot(): Optional<ItemStack>? {
        if (random.nextDouble() > MobMincerConfig.CONFIG.dropChance.get()) {
            return null
        }
        val loot = lootFactory.generateLoot()
        if (loot.isEmpty) {
            return null
        }
        return Optional.ofNullable(
            loot
                .filter { it.count > 0 }
                .randomOrNull()
                ?.also {
                    if (!itemEnchantments.containsKey(Enchantments.MOB_LOOTING)) {
                        it.count = 1
                    }
                }
        )
    }

    private fun dropAsItem(reason: DestroyReason) {
        if (this.isRemoved || level().isClientSide) {
            return
        }

        if (sourceStack.damageValue > sourceStack.maxDamage) {
            (level() as ServerLevel).playSound(
                null,
                this.blockPosition(),
                SoundEvents.ITEM_BREAK,
                SoundSource.NEUTRAL,
                1.0f,
                1.0f
            )
            destroy(DestroyReason.BROKEN)
            return
        }
        destroy(reason)
    }

    enum class DestroyReason {
        DISCARD,
        TARGET_KILLED,
        UNEQUIPPED,
        BROKEN
    }

    private fun destroy(reason: DestroyReason = DestroyReason.DISCARD) {
        if (!attachments.onDeath(reason)) {
            if (reason != DestroyReason.BROKEN) {
                spawnAtLocation(sourceStack)
            }
            this.destroy(reason == DestroyReason.DISCARD)
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (this.isInvulnerableTo(source)) {
            return false
        }
        if (source.`is`(DamageTypes.PLAYER_ATTACK)) {
            dropAsItem(DestroyReason.UNEQUIPPED)
        }
        return true
    }

    override fun remove(reason: RemovalReason) {
        target.removeTag(TARGET_TAG)
        super.remove(reason)
    }

    override fun onClientRemoval() {
        target.removeTag(TARGET_TAG)
    }

    override fun interact(player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown || player.mainHandItem.isEmpty) {
            attachments.onInteract(player)
            return InteractionResult.sidedSuccess(this.level().isClientSide)
        }

        if (!player.mainHandItem.isEmpty && player.isShiftKeyDown) {
            // We are holding an item, so lets try to add it as an attachment
            if (attachments.tryAddAttachment(player.mainHandItem.item)) {
                player.mainHandItem.shrink(1)
                return InteractionResult.sidedSuccess(this.level().isClientSide)
            }
        }

        return InteractionResult.FAIL
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
        if (!compound.contains("SourceStack")) {
            destroy(true)
            return
        }

        this.initSourceItem(ItemStack.of(compound.getCompound("SourceStack")))

        if (compound.contains("DamageDealt")) {
            damageDealt = compound.getFloat("DamageDealt")
        }

        if (compound.contains("CurrentMinceTick")) {
            currentMinceTick = compound.getInt("CurrentMinceTick")
        }

        if (compound.contains(ATTACHMENTS_TAG)) {
            attachments.fromTag(compound.getList(ATTACHMENTS_TAG, 10))
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put("SourceStack", sourceStack.save(CompoundTag()))
        compound.putInt("CurrentMinceTick", currentMinceTick)
        compound.putFloat("DamageDealt", damageDealt)
        compound.put(ATTACHMENTS_TAG, attachments.toTag())
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.saveAdditionalSpawnData(buf)
        buf.writeItem(sourceStack)
        val compoundTag = CompoundTag()
        compoundTag.put(ATTACHMENTS_TAG, attachments.toTag())
        buf.writeNbt(compoundTag)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.loadAdditionalSpawnData(buf)
        this.initSourceItem(buf.readItem())
        buf.readNbt()?.let(::readAttachmentsFromTag)
        attachments.onAttach()
        this.target.addTag(TARGET_TAG)
    }

    private fun readAttachmentsFromTag(tag: CompoundTag) {
        if (tag.contains(ATTACHMENTS_TAG)) {
            attachments.fromTag(tag.getList(ATTACHMENTS_TAG, 10))
        }
    }
}
