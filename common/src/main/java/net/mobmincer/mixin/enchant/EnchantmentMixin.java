package net.mobmincer.mixin.enchant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.mobmincer.compat.mixin.MixinUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Constrains the Mob Mincer to certain enchantments in the anvil.
 * Also allows silk touch and looting to be compatible, since the original check is really for silk touch and fortune.
 */
@Mixin(Enchantment.class)
public class EnchantmentMixin {

    @ModifyReturnValue(method = "canEnchant(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    private boolean canEnchant(boolean original, ItemStack stack) {
        return MixinUtils.INSTANCE.canEnchant(original, stack.getItem(), (Enchantment) (Object) this);
    }

    @ModifyReturnValue(method = "isCompatibleWith(Lnet/minecraft/world/item/enchantment/Enchantment;)Z", at = @At(value = "RETURN"))
    private boolean isCompatibleWith(boolean original, Enchantment other) {
        // Allow silk touch and looting to be compatible (hopefully this has no unintended side effects)
        return (this.equals(Enchantments.SILK_TOUCH) && other.equals(Enchantments.MOB_LOOTING)) || (this.equals(Enchantments.MOB_LOOTING) && other.equals(Enchantments.SILK_TOUCH)) || original;
    }
}
