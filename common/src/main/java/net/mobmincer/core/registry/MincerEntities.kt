package net.mobmincer.core.registry

import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.mobmincer.MobMincer.MOD_ID
import net.mobmincer.core.entity.MobMincerEntity
import java.util.function.Function

object MincerEntities {
    private val ENTITIES: DeferredRegister<EntityType<*>> = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE)
    val MOB_MINCER: RegistrySupplier<EntityType<MobMincerEntity>> = registerEntity(
        "mob_mincer",
        ::MobMincerEntity,
        MobCategory.MISC
    ) { builder: EntityType.Builder<MobMincerEntity> ->
        builder.noSummon().fireImmune().sized(
            0.5f,
            0.5f
        ).clientTrackingRange(10)
    }

    private fun <T : Entity> registerEntity(
        id: String,
        entityFactory: EntityType.EntityFactory<T>,
        category: MobCategory,
        properties: Function<EntityType.Builder<T>, EntityType.Builder<T>>
    ): RegistrySupplier<EntityType<T>> {
        return ENTITIES.register(id) { properties.apply(EntityType.Builder.of(entityFactory, category)).build(id) }
    }

    fun register() {
        ENTITIES.register()
    }
}
