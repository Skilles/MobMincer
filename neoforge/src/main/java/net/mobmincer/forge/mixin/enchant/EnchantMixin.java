package net.mobmincer.forge.mixin.enchant;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.mobmincer.compat.mixin.MixinUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Enchantment.class)
public class EnchantMixin {

    @ModifyReturnValue(method = "canEnchant(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "RETURN"))
    private boolean canEnchant(boolean original, ItemStack stack) {
        return MixinUtils.canEnchant(original, stack.getItem(), (Enchantment) (Object) this);
    }
}
