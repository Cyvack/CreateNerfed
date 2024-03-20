package com.cyvack.create_nerfed;

import com.cyvack.create_nerfed.backbone.GroupingHandler;
import com.cyvack.create_nerfed.backbone.ProcBlockInfoRegistry;
import com.cyvack.create_nerfed.config.CNConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod(CreateNerfed.ID)
public class CreateNerfed {
    public static final String ID = "create_nerfed";

    public static GroupingHandler GROUPLES = new GroupingHandler();

    public CreateNerfed() {
        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        MinecraftForge.EVENT_BUS.register(this);

        ProcBlockInfoRegistry.register();
        CNConfig.register(modLoadingContext);
    }
}
