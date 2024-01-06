package net.mobmincer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.mobmincer.compat.mixin.MixinUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = {LivingEntity.class, Monster.class})
public abstract class LivingEntityMixin {

    @ModifyReturnValue(method = "shouldDropLoot()Z", at = @At(value = "RETURN"))
    private boolean shouldDropLoot(boolean original) {
        return MixinUtils.INSTANCE.canDropLoot(original, (LivingEntity) (Object) this);
    }
}
