package net.mobmincer.core.entity

import dev.architectury.extensions.network.EntitySpawnExtension
import dev.architectury.networking.NetworkManager
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.world.entity.AnimationState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.mobmincer.core.loot.LootFactory
import net.mobmincer.core.registry.MincerEntities.MOB_MINCER
import net.mobmincer.core.registry.MincerItems
import java.util.*

class MobMincerEntity(level: Level) : SmoothMotionEntity(MOB_MINCER.get(), level), EntitySpawnExtension {
    lateinit var target: Mob
        private set

    private lateinit var targetUUID: UUID

    var currentMinceTick = 0

    var maxMinceTick = MAX_MINCE_TICK

    var durability = 0
    var maxDurability = 0
    private var unbreakingLevel = 0

    private lateinit var lootFactory: LootFactory

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
        durability: Int,
        maxDurability: Int,
        lootFactory: LootFactory,
        enchantments: Map<Enchantment, Int>
    ) {
        this.target = target
        this.durability = durability
        this.maxDurability = maxDurability
        this.targetUUID = target.uuid
        this.setPos(target.x, target.y + target.bbHeight, target.z)
        this.lootFactory = lootFactory
        this.unbreakingLevel = enchantments.getOrDefault(Enchantments.UNBREAKING, 0)
    }

    companion object {
        private const val MAX_MINCE_TICK = 100

        private val IS_ERRORED = SynchedEntityData.defineId(MobMincerEntity::class.java, EntityDataSerializers.BOOLEAN)
    }

    override fun tick() {
        if (!this::target.isInitialized) {
            rebindTarget(targetUUID)
            return
        }

        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(target.isAlive, this.tickCount)
        }

        mainTick(target)

        super.tick()
    }

    private fun rebindTarget(it: UUID) {
        val candidates = level().getEntities(
            this,
            AABB.ofSize(this.position(), 1.0, 1.0, 1.0)
        ) { entity -> entity.uuid.equals(it) }
        if (candidates.isEmpty() || candidates[0] !is Mob) {
            this.discard()
        } else {
            this.target = candidates[0] as Mob
            this.tick()
        }
    }

    private fun mainTick(mob: Mob) {
        val isClient = this.level().isClientSide
        if (mob.isAlive) {
            if (!isClient) {
                tickMince(this.level() as ServerLevel)
            }

            var x = mob.x
            var z = mob.z
            var y = mob.y + mob.bbHeight
            /*val yRot = mob.yHeadRot
            val isAnimal = mob is Animal

            val blendedRotation = (yRot * 0.3 + mob.yBodyRot * 0.7)
            val headRotationRadians = Math.toRadians(blendedRotation) + 1.5707963267948966
            val bodyRotationRadians = Math.toRadians(mob.yBodyRot.toDouble())
            val pitchRadians = Math.toRadians(mob.xRot.toDouble())

            // Adjust position based on head rotation
            val cosPitch = cos(pitchRadians)
            val sinPitch = sin(pitchRadians)
            val cosHeadRotation = cos(headRotationRadians)
            val sinHeadRotation = sin(headRotationRadians)
            val cosBodyRotation = cos(bodyRotationRadians)
            val sinBodyRotation = sin(bodyRotationRadians)

            val pitchAdjustmentMultiplier = leashOffset.y

             val leashOffset = if (isAnimal) mob.getLeashOffset(0f).scale(1.7) else Vec3.ZERO
            val xOffset = cosHeadRotation * leashOffset.z
            val zOffset = sinHeadRotation * leashOffset.z

            // Calculate forward/backward direction based on body rotation
            val forwardX = -sinBodyRotation * pitchAdjustmentMultiplier
            val forwardZ = cosBodyRotation * pitchAdjustmentMultiplier


            // Apply pitch adjustment
            x += xOffset + sinPitch * forwardX
            z += zOffset + sinPitch * forwardZ
            y += if (isAnimal) 0.15 else 0.0 + sinPitch * pitchAdjustmentMultiplier * 1.1*/


            this.lerpTo(x, y, z, mob.yBodyRot - 180, mob.xRot, 1)
        } else if (!isClient) {
            dropAsItem()
        }
    }

    private fun tickMince(level: ServerLevel) {
        if (++currentMinceTick >= MAX_MINCE_TICK) {
            if (dropTargetLoot()) {
                isErrored = false
                target.hurt(damageSources().thorns(this), target.maxHealth * 0.1f)
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
        if (this.random.nextInt(6) < unbreakingLevel) {
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
        val lootItem = loot[0]
        if (lootItem.count > 1) {
            lootItem.count = 1
        }
        target.spawnAtLocation(lootItem)
        return true
    }

    private fun dropAsItem() {
        if (this.isRemoved || level().isClientSide) {
            return
        }
        target.removeTag("mob_mincer")
        if (durability > 0) {
            val itemStack = MincerItems.MOB_MINCER.get().defaultInstance
            itemStack.damageValue = itemStack.maxDamage - durability
            target.spawnAtLocation(itemStack)
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
        this.entityData.define(IS_ERRORED, false)
    }

    override fun readAdditionalSaveData(compound: CompoundTag) {
        if (compound.hasUUID("Target")) {
            this.targetUUID = compound.getUUID("Target")
        }
    }

    override fun addAdditionalSaveData(compound: CompoundTag) {
        compound.putUUID("Target", target.uuid)
    }

    override fun saveAdditionalSpawnData(buf: FriendlyByteBuf) {
        buf.writeInt(target.id)
    }

    override fun loadAdditionalSpawnData(buf: FriendlyByteBuf) {
        val targetId = buf.readInt()
        this.target = level().getEntity(targetId) as Mob
    }
}


