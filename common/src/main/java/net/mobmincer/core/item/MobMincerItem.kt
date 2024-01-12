package net.mobmincer.core.item

import net.minecraft.ChatFormatting
import net.minecraft.core.Direction
import net.minecraft.core.Position
import net.minecraft.core.dispenser.BlockSource
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Rarity
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DispenserBlock
import net.minecraft.world.phys.AABB
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.attachment.Attachments
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.item.MobMincerType.Companion.getMincerType
import net.mobmincer.core.item.MobMincerType.Companion.setMincerType
import net.mobmincer.core.registry.AttachmentRegistry
import net.mobmincer.energy.EnergyUtil
import net.mobmincer.energy.MMChargableItem
import kotlin.math.max

/**
 * A mob mincer item that can be placed on a mob. Over time, the mob will be "minced" and will drop loot until it dies.
 */
class MobMincerItem(properties: Properties) : Item(properties), MMChargableItem {

    override fun getDefaultInstance(): ItemStack {
        val instance = super.getDefaultInstance()
        instance.setMincerType(MobMincerType.BASIC)
        return instance
    }

    override fun verifyTagAfterLoad(tag: CompoundTag) {
        if (!tag.contains("MobMincer")) {
            tag.put("MobMincer", CompoundTag())
        }
    }

    override fun isFoil(stack: ItemStack): Boolean {
        return stack.isEnchanted || stack.getMincerType() == MobMincerType.CREATIVE
    }

    override fun getRarity(stack: ItemStack): Rarity {
        if (stack.isEnchanted) {
            return super.getRarity(stack)
        }

        return when (stack.getMincerType()) {
            MobMincerType.BASIC -> Rarity.COMMON
            MobMincerType.POWERED -> Rarity.UNCOMMON
            MobMincerType.CREATIVE -> Rarity.EPIC
        }
    }



    override fun interactLivingEntity(
        stack: ItemStack,
        player: Player,
        interactionTarget: LivingEntity,
        usedHand: InteractionHand
    ): InteractionResult {
        if (usedHand == InteractionHand.MAIN_HAND && !player.level().isClientSide) {
            if (MobMincerEntity.canAttach(interactionTarget, stack)) {
                MobMincerEntity.spawn(interactionTarget, stack, player.level() as ServerLevel)
                player.level().playSound(
                    null,
                    interactionTarget.blockPosition(),
                    SoundEvents.DONKEY_CHEST,
                    player.soundSource,
                    1.0f,
                    0.6f
                )
                return InteractionResult.SUCCESS
            }
            return InteractionResult.FAIL
        }
        return InteractionResult.PASS
    }

    override fun getEnchantmentValue(): Int {
        return 1
    }

    override fun getBarWidth(stack: ItemStack): Int {
        val maxDamage = MobMincerConfig.CONFIG.baseDurability.get()
        return Math.round(13.0f - stack.damageValue.toFloat() * 13.0f / maxDamage.toFloat())
    }

    override fun getBarColor(stack: ItemStack): Int {
        val maxDamage = MobMincerConfig.CONFIG.baseDurability.get()
        val f = max(0.0, ((maxDamage - stack.damageValue.toFloat()) / maxDamage).toDouble()).toFloat()
        return Mth.hsvToRgb(f / 3.0f, 1.0f, 1.0f)
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltipComponents: MutableList<Component>, isAdvanced: TooltipFlag) {
        if (!stack.hasTag()) {
            return
        }
        val stackTag = stack.tag!!
        if (stackTag.contains("MobMincer")) {
            val tag = stackTag.getCompound("MobMincer")
            if (tag.contains("Attachments")) {
                val attachments = tag.getList("Attachments", 10)
                if (attachments.isEmpty()) {
                    return
                }
                tooltipComponents.add(Component.literal("Attachments:"))
                for (i in 0 until attachments.size) {
                    val attachment = attachments.getCompound(i)
                    val type = Attachments.valueOf(attachment.getString("Type"))
                    AttachmentRegistry.get(type)?.name?.let { name ->
                        tooltipComponents.add(Component.literal("- ").append(name).withStyle(ChatFormatting.GRAY))
                    }
                }
            }
        }

        if (stack.getMincerType() == MobMincerType.POWERED) {
            val energy = EnergyUtil.getEnergyStorage(stack).energy
            val maxEnergy = getEnergyCapacity(stack)
            tooltipComponents.add(Component.literal("Energy: $energy/$maxEnergy").withStyle(ChatFormatting.GRAY))
        }
    }

    override fun getEnergyCapacity(stack: ItemStack): Long {
        return 1000
    }

    override fun getEnergyMaxInput(stack: ItemStack): Long {
        return 100
    }

    override fun getEnergyMaxOutput(stack: ItemStack): Long {
        return 0
    }

    companion object {
        val DISPENSE_BEHAVIOR = object : OptionalDispenseItemBehavior() {
            override fun execute(blockSource: BlockSource, item: ItemStack): ItemStack {
                val blockPos = blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING))
                val list = blockSource.level().getEntitiesOfClass(
                    LivingEntity::class.java,
                    AABB(blockPos)
                ) { mob: LivingEntity -> MobMincerEntity.canAttach(mob, item) }
                for (mob in list) {
                    MobMincerEntity.spawn(mob, item, blockSource.level())
                    this.isSuccess = true
                    return item
                }

                return dispenseItem(blockSource, item)
            }

            /**
             * Copied from [net.minecraft.core.dispenser.DefaultDispenseItemBehavior]
             */
            private fun dispenseItem(blockSource: BlockSource, item: ItemStack): ItemStack {
                val direction = blockSource.state().getValue(DispenserBlock.FACING)
                val position = DispenserBlock.getDispensePosition(blockSource)
                val itemStack = item.split(1)
                spawnItem(blockSource.level(), itemStack, direction, position)
                return item
            }

            private fun spawnItem(level: Level, stack: ItemStack, facing: Direction, position: Position) {
                val d = position.x()
                var e = position.y()
                val f = position.z()
                val speed = 6.0
                e -= if (facing.axis === Direction.Axis.Y) {
                    0.125
                } else {
                    0.15625
                }
                val itemEntity = ItemEntity(level, d, e, f, stack)
                val g = level.random.nextDouble() * 0.1 + 0.2
                itemEntity.setDeltaMovement(
                    level.random.triangle(facing.stepX.toDouble() * g, 0.0172275 * speed),
                    level.random.triangle(0.2, 0.0172275 * speed),
                    level.random.triangle(facing.stepZ.toDouble() * g, 0.0172275 * speed)
                )
                level.addFreshEntity(itemEntity)
                if (MobMincerConfig.CONFIG.allowDispensing.get()) {
                    itemEntity.addTag("mob_mincer:dispensed")
                }
            }
        }
    }
}
