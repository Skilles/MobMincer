package net.mobmincer.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.Item;
import net.mobmincer.core.config.MobMincerConfig;
import net.mobmincer.core.item.MobMincerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * This mixin for {@link Item} is used to modify the max durability of {@link MobMincerItem} post registration.
 * It also modifies the bar width and color to use the getter function for durability instead of the field.
 */
@Mixin(Item.class)
public abstract class ItemMixin {

    @ModifyReturnValue(method = "getMaxDamage()I", at = @At("RETURN"))
    public int getMaxDamage(int original) {
        if ((Object) this instanceof MobMincerItem) {
            return MobMincerConfig.Companion.getCONFIG().getBaseDurability().get();
        }
        return original;
    }

    @ModifyExpressionValue(method = "getBarWidth(Lnet/minecraft/world/item/ItemStack;)I", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Item;maxDamage:I"))
    private int getBarWidth(int original) {
        return this.getMaxDamage();
    }

    @Shadow
    public abstract int getMaxDamage();

    @ModifyExpressionValue(method = "getBarColor(Lnet/minecraft/world/item/ItemStack;)I", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Item;maxDamage:I"))
    private int getBarColor(int original) {
        return this.getMaxDamage();
    }
}
