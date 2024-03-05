package net.mobmincer.core.block

import net.minecraft.core.BlockPos
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.getFuel
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.isFuel
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.mobmincer.api.blockentity.EnergyMachineBlockEntity
import net.mobmincer.api.inventory.InventoryAccess
import net.mobmincer.api.inventory.MachineInventory
import net.mobmincer.client.menu.PowerProviderMenu
import net.mobmincer.common.config.MobMincerConfig
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.registry.MMContent
import net.mobmincer.energy.EnergyUtil.getEnergyStorage
import net.mobmincer.energy.EnergyUtil.usesEnergy
import net.mobmincer.energy.MMEnergyStorage
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

class MincerPowerProviderBlockEntity(pos: BlockPos, blockState: BlockState) :
    EnergyMachineBlockEntity(
        MMContent.POWER_PROVIDER.blockEntity.get(),
        pos,
        blockState
    ),
    BlockEntityTicker<MincerPowerProviderBlockEntity> {

    val inventory = MachineInventory(
        2,
        "PowerProviderBlockEntity",
        64,
        this
    ) { slotID, stack, face, direction, blockEntity ->
        when { // TODO move this
            direction != InventoryAccess.AccessDirection.INSERT -> true

            when (slotID) {
                FUEL_SLOT -> isFuel(stack)
                CHARGE_SLOT -> stack.usesEnergy()
                else -> false
            } -> true

            else -> false
        }
    }

    var burnTime = 0
    var maxBurnTime = 0
    var lastPulseTicks = 0

    override fun tick(level: Level, blockPos: BlockPos, blockState: BlockState, blockEntity: MincerPowerProviderBlockEntity) {
        if (level.isClientSide) return

        if (blockEntity.burnTime > 0) {
            blockEntity.burnTime--
            blockEntity.energyStorage.insert(MobMincerConfig.CONFIG.powerProviderBurnRate.get().toLong())
        } else {
            val fuelStack = inventory.getItem(FUEL_SLOT)
            if (isFuel(fuelStack)) {
                blockEntity.burnTime = getBurnDuration(fuelStack)
                blockEntity.maxBurnTime = burnTime
                fuelStack.shrink(1)
            }
        }
        discharge(CHARGE_SLOT)
        chargeNearbyMincers(level, blockPos, blockEntity)
    }

    fun getBurnDuration(fuel: ItemStack): Int {
        if (fuel.isEmpty) {
            return 0
        } else {
            val item = fuel.item
            return getFuel().getOrDefault(item, 0) / 16
        }
    }

    override fun getInventory(): Container = inventory

    override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
        return PowerProviderMenu(i, inventory, this, data, ContainerLevelAccess.create(level!!, blockPos))
    }

    private val data = object : ContainerData {
        override fun get(index: Int): Int = when (index) {
            0 -> burnTime
            1 -> maxBurnTime
            2 -> energyStorage.energy.toInt()
            3 -> energyStorage.energyCapacity.toInt()
            else -> 0
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> burnTime = value
                1 -> throw IllegalArgumentException("Cannot set max burn time")
                2 -> energyStorage.energy = value.toLong()
                3 -> throw IllegalArgumentException("Cannot set energy capacity")
            }
        }

        override fun getCount(): Int = 4
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        burnTime = tag.getInt("BurnTime")
        maxBurnTime = tag.getInt("MaxBurnTime")
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putInt("BurnTime", burnTime)
        tag.putInt("MaxBurnTime", maxBurnTime)
    }

    companion object {
        fun chargeNearbyMincers(
            level: Level,
            pos: BlockPos,
            blockEntity: MincerPowerProviderBlockEntity
        ) {
            require(level is ServerLevel)
            if (blockEntity.energyStorage.isEmpty || --blockEntity.lastPulseTicks > 0) return
            blockEntity.lastPulseTicks = MobMincerConfig.CONFIG.powerProviderPulseRate.get()

            val nearbyMincers: Queue<Pair<MMEnergyStorage, Vec3>> = LinkedList(
                level.getEntitiesOfClass(
                    MobMincerEntity::class.java,
                    AABB(pos).inflate(5.0)
                ).map { it.getEnergyStorage() to it.position() }.filter { (storage, _) -> storage.energy < storage.energyCapacity }
            )

            if (nearbyMincers.isEmpty()) return

            var totalExtract = min(blockEntity.energyStorage.getEnergyMaxOutput(null), blockEntity.energyStorage.energy)
            val extractPerMincer = totalExtract / nearbyMincers.size
            val powerProviderPos = blockEntity.blockPos.center
            var extractCount = 20

            while (nearbyMincers.isNotEmpty() && totalExtract > 0 && extractCount > 0) {
                val (energyStorage, position) = nearbyMincers.poll()
                val extractAmount = blockEntity.energyStorage.extract(extractPerMincer)
                if (extractAmount > 0) {
                    val insertAmount = energyStorage.insert(extractAmount)
                    totalExtract -= insertAmount
                    val leftoverExtract = extractAmount - insertAmount
                    if (leftoverExtract > 0) {
                        blockEntity.energyStorage.insert(leftoverExtract)
                    } else if (totalExtract > 0) {
                        nearbyMincers.add(energyStorage to position)
                    }
                    if (MobMincerConfig.CONFIG.enablePowerParticles.get()) {
                        drawLineOfParticles(level, powerProviderPos, position, ParticleTypes.ELECTRIC_SPARK, 4)
                    }
                }
                extractCount--
            }
        }

        private fun drawLineOfParticles(level: ServerLevel, start: Vec3, end: Vec3, particle: ParticleOptions, countMultiplier: Int = 2) {
            val difference = end.subtract(start)

            val dx = difference.x
            val dy = difference.y
            val dz = difference.z

            val isArcUpward = end.y > start.y
            val arcHeight = abs(dy) / 2
            val particleCount = difference.length().toInt() * countMultiplier
            for (i in 0..particleCount) {
                val fraction = i.toDouble() / particleCount

                val posX = start.x + fraction * dx
                val posY = if (isArcUpward) {
                    start.y + fraction * dy + arcHeight * sin(Math.PI * fraction)
                } else {
                    start.y + fraction * dy - arcHeight * sin(Math.PI * fraction)
                }
                val posZ = start.z + fraction * dz

                level.sendParticles(particle, posX, posY, posZ, 1, 0.0, 0.0, 0.0, 0.0)
            }
        }

        const val CHARGE_SLOT = 0
        const val FUEL_SLOT = 1
    }
}
