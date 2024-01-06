package net.mobmincer.mixin.enchant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.Item;
import net.mobmincer.core.registry.MincerItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Allows the mob mincer to be enchanted with DIGGER and WEAPON enchantments. See {@link EnchantmentMixin} and {@link net.mobmincer.compat.mixin.MixinUtils} for further constraints.
 */
@Mixin(targets = {"net.minecraft.world.item.enchantment.EnchantmentCategory$6", "net.minecraft.world.item.enchantment.EnchantmentCategory$7"})
public class EnchantmentCategoryMixin {

    @ModifyReturnValue(method = "canEnchant(Lnet/minecraft/world/item/Item;)Z", at = @At(value = "RETURN"))
    private boolean canEnchant(boolean original, Item item) {
        return item.equals(MincerItems.INSTANCE.getMOB_MINCER().get()) || original;
    }
}
