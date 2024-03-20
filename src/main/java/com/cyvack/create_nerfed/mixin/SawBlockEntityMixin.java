package com.cyvack.create_nerfed.mixin;

import com.cyvack.create_nerfed.CreateNerfedHelpers;
import com.cyvack.create_nerfed.config.CNConfig;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SawBlockEntity.class)
public class SawBlockEntityMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private float applyMult(float value, float min, float max) {
        return (float) CreateNerfedHelpers.getModifiedSpeed((SawBlockEntity) (Object) this, Mth.clamp(value, min, max), false);
    }
}
