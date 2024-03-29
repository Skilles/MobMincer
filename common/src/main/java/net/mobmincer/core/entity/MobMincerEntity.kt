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
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.attachment.AttachmentHolder
import net.mobmincer.core.attachment.Attachments
import net.mobmincer.core.attachment.StorageAttachment
import net.mobmincer.core.loot.LootFactory
import net.mobmincer.core.loot.LootLookup
import net.mobmincer.core.registry.MMContent
import net.mobmincer.network.MincerNetwork
import net.mobmincer.util.MathUtils
import java.util.*
import kotlin.math.roundToInt

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

    var isErrored: Boolean
        get() = entityData.get(IS_ERRORED)
        set(value) {
            if (value == entityData.get(IS_ERRORED)) {
                return
            }
            entityData.set(IS_ERRORED, value)
        }

    val idleAnimationState = AnimationState()

    private fun initSourceItem(sourceStack: ItemStack) {
        this.itemEnchantments = EnchantmentHelper.getEnchantments(sourceStack)
        this.sourceStack = sourceStack
        this.maxMinceTick = MathUtils.getCalculatedMaxMinceTick(itemEnchantments.getOrDefault(Enchantments.SOUL_SPEED, 0))
        this.hasMending = itemEnchantments.containsKey(Enchantments.MENDING)
    }

    private fun initialize(target: LivingEntity, sourceStack: ItemStack, level: ServerLevel) {
        initSourceItem(sourceStack.copy())
        super.initialize(target)
        sourceStack.shrink(1)
        level.addFreshEntity(this)
        attachments.onAttach()
    }

    override fun onTargetBind() {
        this.target.addTag(ROOT_TAG)
        if (!this.level().isClientSide) {
            val killedByPlayer = itemEnchantments.containsKey(Enchantments.SILK_TOUCH)
            this.lootFactory = LootFactory.create(target, killedByPlayer, itemEnchantments.getOrDefault(Enchantments.MOB_LOOTING, 0))
        }
        attachments.onSpawn()
    }

    fun changeTarget(target: LivingEntity) {
        this.target.removeTag(ROOT_TAG)
        super.initialize(target)
        if (!this.level().isClientSide) {
            MincerNetwork.updateClientMincerTarget(this)
        }
    }

    companion object {
        private val IS_ERRORED = SynchedEntityData.defineId(MobMincerEntity::class.java, EntityDataSerializers.BOOLEAN)

        const val ROOT_TAG = "mobmincer"
        const val TAG_SKIP_LOOT = "${ROOT_TAG}:skip_loot"

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
            return !target.isBaby && target.isAlive && target.passengers.isEmpty() && LootLookup.hasLoot(target, EnchantmentHelper.hasSilkTouch(sourceStack)) && !target.tags.contains(ROOT_TAG)
        }
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
                if (hasMending) {
                    mendMincer()
                }
                dropAsItem(DestroyReason.TARGET_KILLED)
            }
        }
    }

    private fun mendMincer() {
        val repairAmount = damageDealt * MobMincerConfig.CONFIG.mendingRepairMultiplier.get()
        if (repairAmount > 0) {
            sourceStack.hurt(-repairAmount.roundToInt(), random, null)
        }
    }

    private fun tickMince(level: ServerLevel) {
        if (++currentMinceTick >= maxMinceTick) {
            if (dropTargetLoot()) {
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
            attachments.getAttachment(Attachments.STORAGE)?.let { attachment ->
                attachment as StorageAttachment
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
        target.removeTag(ROOT_TAG)
        super.remove(reason)
    }

    override fun onClientRemoval() {
        target.removeTag(ROOT_TAG)
    }

    override fun interact(player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown) {
            attachments.onInteract(player)
            return InteractionResult.sidedSuccess((this.level() as Level).isClientSide)
        }

        if (!player.mainHandItem.isEmpty && player.isShiftKeyDown) {
            // We are holding an item, so lets try to add it as an attachment
            if (attachments.tryAddAttachment(player.mainHandItem.item)) {
                player.mainHandItem.shrink(1)
                return InteractionResult.sidedSuccess((this.level() as Level).isClientSide)
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
        if (compound.contains("SourceStack") && compound.contains("Attachments") && compound.contains("CurrentMinceTick")) {
            currentMinceTick = compound.getInt("CurrentMinceTick")
            this.initSourceItem(ItemStack.of(compound.getCompound("SourceStack")))
            attachments.fromEntityTag(compound.getList("Attachments", 10))
        } else {
            destroy(true)
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        super.addAdditionalSaveData(compound)
        compound.put("SourceStack", sourceStack.save(CompoundTag()))
        compound.put("Attachments", attachments.toEntityTag())
        compound.putInt("CurrentMinceTick", currentMinceTick)
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.saveAdditionalSpawnData(buf)
        buf.writeItem(sourceStack)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        super.loadAdditionalSpawnData(buf)
        this.initSourceItem(buf.readItem())
        attachments.onAttach()
        this.target.addTag(ROOT_TAG)
    }
}
