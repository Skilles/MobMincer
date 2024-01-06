package net.mobmincer.mixin.loot;

import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.Connection;
import net.mobmincer.core.loot.LootLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Connection.class)
public class MixinClientConnection {

    @Inject(method = "channelInactive", at = @At("RETURN"))
    private void onChannelInactive(ChannelHandlerContext context, CallbackInfo ci) {
        LootLookup.INSTANCE.clear();
    }
}
