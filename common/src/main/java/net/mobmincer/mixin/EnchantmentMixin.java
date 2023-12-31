package net.mobmincer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.mobmincer.core.registry.MincerItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Constrains the Mob Mincer to certain enchantments in the anvil. See {@link EnchantmentHelperMixin} for the enchanting table version.
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @ModifyReturnValue(method = "canEnchant(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    private boolean canEnchant(boolean original, ItemStack stack) {
        if (!stack.is(MincerItems.INSTANCE.getMOB_MINCER().get())) {
            return original;
        }

        return this.equals(Enchantments.SILK_TOUCH) || this.equals(Enchantments.UNBREAKING) || this.equals(Enchantments.MOB_LOOTING);
    }
}
