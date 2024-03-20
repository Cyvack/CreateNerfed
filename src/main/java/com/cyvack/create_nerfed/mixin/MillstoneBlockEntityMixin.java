package com.cyvack.create_nerfed.mixin;

import com.cyvack.create_nerfed.CreateNerfed;
import com.cyvack.create_nerfed.CreateNerfedHelpers;
import com.cyvack.create_nerfed.config.CNConfig;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MillstoneBlockEntity.class)
public class MillstoneBlockEntityMixin {

    @Redirect(method = "getProcessingSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private int applyMult(int value, int min, int max) {
        return (int) CreateNerfedHelpers.getModifiedSpeed((MillstoneBlockEntity) (Object) this, Mth.clamp(value, min, max), false);
    }
}
