package com.cyvack.create_nerfed.mixin;

import com.cyvack.create_nerfed.CreateNerfedHelpers;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.cyvack.create_nerfed.config.CNConfig.server;

@Mixin(SpoutBlockEntity.class)
public class SpoutBlockEntityMixin {


    @Shadow @Final public static int FILLING_TIME;

    @Shadow public int processingTicks;

    @Inject(method = "lambda$tick$0", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/spout/SpoutBlockEntity;notifyUpdate()V"), remap = false)
    private void applyMult(FluidStack currentFluidInTank, BlockSpoutingBehaviour behaviour, CallbackInfo ci) {
        processingTicks = getNewTicks();
    }

    @Inject(method = "whenItemHeld", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/spout/SpoutBlockEntity;notifyUpdate()V", ordinal = 0), remap = false)
    private void applyMult2(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler, CallbackInfoReturnable<BeltProcessingBehaviour.ProcessingResult> cir) {
        processingTicks = getNewTicks();
    }

    private int getNewTicks() {
        return (int) CreateNerfedHelpers.getModifiedSpeed((SpoutBlockEntity) (Object) this, FILLING_TIME, true);
    }
}
