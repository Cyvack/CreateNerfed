package com.cyvack.create_nerfed.mixin.kineticNetwork;

import com.cyvack.create_nerfed.mixinterfaces.GroupAmountGetterBE;
import com.cyvack.create_nerfed.mixinterfaces.GroupAmountGetterNetwork;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KineticBlockEntity.class)
public abstract class KineticBlockEntityMixin implements GroupAmountGetterBE {

    @Shadow
    public abstract KineticNetwork getOrCreateNetwork();

    @Shadow public abstract boolean hasNetwork();

    //Added to allow syncing between server and client
    NonNullSupplier<Integer> countSupplier;
    @Unique
    int countInNetwork;

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void getCountSetter(BlockEntityType typeIn, BlockPos pos, BlockState state, CallbackInfo ci) {
        countSupplier = () -> {
            if (hasNetwork()) {
                KineticBlockEntity be = (KineticBlockEntity) (Object) this;
                return ((GroupAmountGetterNetwork) getOrCreateNetwork()).getKineticCount(be.getBlockState().getBlock());
            } else {
                return 0;
            }
        };
    }

    @Inject(method = "write", at = @At("TAIL"), remap = false)
    private void writeCurrentCount(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        compound.putInt("countInNetwork", countSupplier.get());
    }

    @Inject(method = "read", at = @At("TAIL"), remap = false)
    private void readCurrentCount(CompoundTag compound, boolean clientPacket, CallbackInfo ci) {
        countInNetwork = compound.getInt("countInNetwork");
    }

    @Override
    public int getKineticCount() {
        return countSupplier.get();
    }
}
