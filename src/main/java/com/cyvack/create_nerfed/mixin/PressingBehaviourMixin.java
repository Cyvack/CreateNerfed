package com.cyvack.create_nerfed.mixin;

import com.cyvack.create_nerfed.CreateNerfed;
import com.cyvack.create_nerfed.CreateNerfedHelpers;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.kinetics.press.PressingBehaviour;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static com.cyvack.create_nerfed.config.CNConfig.server;

@Mixin(PressingBehaviour.class)
public class PressingBehaviourMixin extends BeltProcessingBehaviour {

    public PressingBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Redirect(method = "getRunningTickSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float applyMult(float value, float min, float max) {
        return (float) CreateNerfedHelpers.getModifiedSpeed(blockEntity, Mth.lerp(value, min, max), false);
    }
}
