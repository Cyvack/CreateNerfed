package com.cyvack.create_nerfed;

import com.cyvack.create_nerfed.backbone.GroupPosition;
import com.cyvack.create_nerfed.backbone.GroupingHandler;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CNEvents {

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!event.getLevel().isClientSide())
            CreateNerfed.GROUPLES.unloadLevel((Level) event.getLevel());
    }
}
