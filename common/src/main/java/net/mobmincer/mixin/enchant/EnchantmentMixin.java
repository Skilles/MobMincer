package net.mobmincer.mixin.enchant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.mobmincer.core.registry.MincerItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Constrains the Mob Mincer to certain enchantments in the anvil. See {@link EnchantmentHelperMixin} for the enchanting table version.
 * Also allows silk touch and looting to be compatible, since the original check is really for silk touch and fortune.
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

    @ModifyReturnValue(method = "isCompatibleWith(Lnet/minecraft/world/item/enchantment/Enchantment;)Z", at = @At(value = "RETURN"))
    private boolean isCompatibleWith(boolean original, Enchantment other) {
        // Allow silk touch and looting to be compatible (hopefully this has no unintended side effects)
        if ((this.equals(Enchantments.SILK_TOUCH) && other.equals(Enchantments.MOB_LOOTING)) || (this.equals(Enchantments.MOB_LOOTING) && other.equals(Enchantments.SILK_TOUCH))) {
            return true;
        }
        return original;
    }
}
