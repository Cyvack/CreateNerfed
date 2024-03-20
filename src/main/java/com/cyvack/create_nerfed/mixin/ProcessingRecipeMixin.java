package com.cyvack.create_nerfed.mixin;

import com.cyvack.create_nerfed.config.CNConfig;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProcessingRecipe.class)
public class ProcessingRecipeMixin {

    @Inject(method = "getProcessingDuration", at = @At("RETURN"), remap = false, cancellable = true)
    private void applyMult(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) (cir.getReturnValue() * CNConfig.server().processingSpeed.modifiers.globalProcessingDurationMult.get()));
    }

}
