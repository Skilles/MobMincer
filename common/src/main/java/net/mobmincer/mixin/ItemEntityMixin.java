package net.mobmincer.mixin;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.mobmincer.core.entity.MobMincerEntity;
import net.mobmincer.core.registry.MincerItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        var item = getItem();
        if (item.is(MincerItems.INSTANCE.getMOB_MINCER().get())) {
            // Collide and attach to an entity if shot from a dispenser
            var thisEntity = (ItemEntity) (Object) (this);
            if (!thisEntity.onGround() && thisEntity.getTags().contains("mob_mincer:dispensed")) {
                AABB aABB = thisEntity.getBoundingBox().inflate(0.25, 0.25, 0.25);
                List<Entity> list = thisEntity.level().getEntities(thisEntity, aABB, entity -> entity instanceof LivingEntity mob && !entity.isRemoved() && MobMincerEntity.Companion.canAttach(mob, item));
                Entity touchedEntity = null;
                for (Entity entity : list) {
                    touchedEntity = entity;
                    break;
                }
                if (touchedEntity != null) {
                    var mob = (LivingEntity) touchedEntity;
                    if (!thisEntity.level().isClientSide) {
                        MobMincerEntity.Companion.spawn(mob, item, thisEntity.level());
                        thisEntity.level().playSound(null, thisEntity.blockPosition(), SoundEvents.DONKEY_CHEST, SoundSource.NEUTRAL, 1.0F, 0.5F);
                    }
                    thisEntity.remove(Entity.RemovalReason.DISCARDED);
                } else if (thisEntity.tickCount > 100) {
                    // Remove the dispensed tag after 5 seconds to prevent transporting the item after it was dispensed
                    thisEntity.removeTag("mob_mincer:dispensed");
                }
            }
        }
    }

    @Shadow
    public abstract ItemStack getItem();
}
