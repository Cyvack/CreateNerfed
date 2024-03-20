package com.cyvack.create_nerfed.mixin.kineticNetwork;

import com.cyvack.create_nerfed.mixinterfaces.GroupAmountGetterNetwork;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KineticNetwork.class)
public class KineticNetworkMixin implements GroupAmountGetterNetwork {

    private final Object2IntMap<Block> countMap = new Object2IntOpenHashMap<>();

    @Inject(method = "add", at = @At("TAIL"), remap = false)
    private void updateMapAdd(KineticBlockEntity be, CallbackInfo ci) {
        increment(be);
    }

    @Inject(method = "addSilently", at = @At("TAIL"), remap = false)
    private void updateMapAddSilent(KineticBlockEntity be, float lastCapacity, float lastStress, CallbackInfo ci) {
        increment(be);
    }

    @Inject(method = "remove", at = @At("TAIL"), remap = false)
    private void updateMapRemove(KineticBlockEntity be, CallbackInfo ci) {
        decrement(be);
    }


    private void increment(KineticBlockEntity be) {
        countMap.computeInt(be.getBlockState().getBlock(), (clazz, i) -> (i != null ? (i + 1) : 1));
    }

    private void decrement(KineticBlockEntity be) {
        countMap.computeInt(be.getBlockState().getBlock(), (clazz, i) -> (i != null ? (i - 1) : 0));
    }

    @Override
    public int getKineticCount(Block block) {
        return countMap.getOrDefault(block, 0);
    }
}
