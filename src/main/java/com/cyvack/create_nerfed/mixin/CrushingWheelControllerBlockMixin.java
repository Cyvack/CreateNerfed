package com.cyvack.create_nerfed.mixin;

import com.cyvack.create_nerfed.CreateNerfed;
import com.cyvack.create_nerfed.CreateNerfedHelpers;
import com.cyvack.create_nerfed.config.CNConfig;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlock;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(CrushingWheelControllerBlock.class)
public class CrushingWheelControllerBlockMixin {

    @Inject(method = "lambda$updateSpeed$1", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(F)F"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void applyMult(BlockState state, LevelAccessor world, BlockPos pos, CrushingWheelControllerBlockEntity be, CallbackInfo ci, Direction[] var4, int var5, int var6, Direction d, BlockState neighbour, BlockEntity adjBE, CrushingWheelBlockEntity cwbe) {
        be.crushingspeed = (float) CreateNerfedHelpers.getModifiedSpeed(cwbe, Math.abs(cwbe.getSpeed() / 50f), false);
    }
}
