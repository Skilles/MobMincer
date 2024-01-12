package net.mobmincer.fabric.mixin.enchant;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.mobmincer.compat.mixin.MixinUtils;
import net.mobmincer.mixin.enchant.EnchantmentMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Constrains the Mob Mincer to certain enchantments in the enchanting table. See {@link EnchantmentMixin} for the anvil version.
 */
@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @ModifyExpressionValue(method = "getAvailableEnchantmentResults(ILnet/minecraft/world/item/ItemStack;Z)Ljava/util/List;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentCategory;canEnchant(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean canEnchant(boolean original, @Local Item item, @Local Enchantment enchantment) {
        return MixinUtils.canEnchant(original, item, enchantment);
    }
}
