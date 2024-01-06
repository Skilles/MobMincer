package net.mobmincer.mixin.loot;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.mobmincer.network.MincerNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;

@Mixin(PlayerList.class)
public class MixinPlayerManager {
    @Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
    private void onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
        MincerNetwork.INSTANCE.sendLootToPlayers(((PlayerList) (Object) this).getServer(), Collections.singletonList(player));
    }

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void onDataPacksReloaded(CallbackInfo info) {
        MincerNetwork.INSTANCE.sendLootToPlayers(((PlayerList) (Object) this).getServer(), ((PlayerList) (Object) this).getPlayers());
    }
}
