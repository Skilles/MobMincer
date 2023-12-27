package net.mobmincer.forge;

import dev.architectury.platform.forge.EventBuses;
import net.mobmincer.MobMincer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MobMincer.MOD_ID)
public class MobMincerForge
{
    public MobMincerForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(MobMincer.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        MobMincer.init();
    }
}
