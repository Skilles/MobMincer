package net.mobmincer.core.registry

import dev.architectury.registry.client.level.entity.EntityRendererRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.Level
import net.mobmincer.MobMincer.MOD_ID
import net.mobmincer.core.entity.MobMincerEntity
import net.mobmincer.core.entity.MobMincerEntityRenderer
import java.util.function.Function

object MincerEntities {
    private val ENTITIES: DeferredRegister<EntityType<*>> = DeferredRegister.create(MOD_ID, Registries.ENTITY_TYPE)
    val MOB_MINCER: RegistrySupplier<EntityType<MobMincerEntity>> = registerEntity("mob_mincer", { _: EntityType<MobMincerEntity>, level: Level -> MobMincerEntity(level) }, MobCategory.MISC, { builder: EntityType.Builder<MobMincerEntity> -> builder.noSummon().fireImmune() })

    private fun <T : Entity> registerEntity(id: String, entityFactory: EntityType.EntityFactory<T>, category: MobCategory, properties: Function<EntityType.Builder<T>, EntityType.Builder<T>>): RegistrySupplier<EntityType<T>> {
        return ENTITIES.register(id) { properties.apply(EntityType.Builder.of(entityFactory, category)).build(id) }
    }

    fun register() {
        ENTITIES.register()

        EntityRendererRegistry.register(MOB_MINCER, MobMincerEntityRenderer.Provider())
    }
}
