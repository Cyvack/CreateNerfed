package com.cyvack.create_nerfed.mixin;

import com.cyvack.create_nerfed.CreateNerfedHelpers;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.cyvack.create_nerfed.config.CNConfig.server;

@Mixin(MechanicalMixerBlockEntity.class)
public class MechanicalMixerBlockEntityMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    private int applyMult(int value, int min, int max) {
        return (int) CreateNerfedHelpers.getModifiedSpeed((MechanicalMixerBlockEntity) (Object) this, Mth.clamp(value, min, max), true);
    }
}
