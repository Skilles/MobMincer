package net.mobmincer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.item.Item;
import net.mobmincer.common.config.MobMincerConfig;
import net.mobmincer.core.item.MobMincerItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * This mixin for {@link net.minecraft.world.item.Item} is used to modify the max durability of {@link MobMincerItem} post registration. If this was neoforge, we could just override this.
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
}
