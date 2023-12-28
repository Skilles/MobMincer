package net.mobmincer.fabriclike;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.mobmincer.MobMincer;

public class MobMincerFabricLike
{
    public static void init() {
        MobMincer.init();
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        MobMincer.initClient();
    }
}
