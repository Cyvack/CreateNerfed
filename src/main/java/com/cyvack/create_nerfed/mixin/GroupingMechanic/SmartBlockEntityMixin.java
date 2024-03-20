package com.cyvack.create_nerfed.mixin.GroupingMechanic;

import com.cyvack.create_nerfed.CreateNerfed;
import com.cyvack.create_nerfed.backbone.BlockGrouping;
import com.cyvack.create_nerfed.backbone.GroupInformation;
import com.cyvack.create_nerfed.backbone.GroupPosition;
import com.cyvack.create_nerfed.backbone.ProcBlockInfoRegistry;
import com.cyvack.create_nerfed.mixinterfaces.BlockGroupGetter;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

import static com.cyvack.create_nerfed.CreateNerfed.GROUPLES;

@Mixin(SmartBlockEntity.class)
public class SmartBlockEntityMixin extends BlockEntity implements GroupInformation, BlockGroupGetter {

    /**
     * Initialized with the block entity. Is passed around to other groups to reduce the need for getBlockEntity calls
     */
    private GroupPosition groupPosition;

    @Shadow
    private boolean chunkUnloaded;
    @Unique
    BlockGrouping createNerfed$currentGroup;

    public SmartBlockEntityMixin(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void createGroupPos(BlockEntityType type, BlockPos pos, BlockState state, CallbackInfo ci) {
        groupPosition = new GroupPosition(pos, this);
    }

    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void visualizeGroup(CallbackInfo ci) {
        if (createNerfed$currentGroup != null) {
            createNerfed$currentGroup.visualize();
        }
    }

    @Inject(method = "initialize", at = @At("HEAD"), remap = false)
    private void addGroup(CallbackInfo ci) {
        if (!level.isClientSide && ProcBlockInfoRegistry.contains(getBlockType().builtInRegistryHolder().key().location())) {
            List<BlockGrouping> surrounding = GROUPLES.getSurroundingGroupsFrom(level, groupPosition);
            if (surrounding.isEmpty()) {
                GROUPLES.createGroup(groupPosition, getLevel()); //force floodfill just in case of any weird disconnects
            } else {
                if (surrounding.size() == 1) {
                    surrounding.get(0).insertOrUpdatePos(groupPosition, false);
                } else {
                    GROUPLES.mergeGroups(surrounding, getLevel(), false, groupPosition);
                }
            }
        }
    }

    @Inject(method = "setRemoved", at = @At("HEAD"), remap = false)
    private void removeFromGroup(CallbackInfo ci) {
        if (!level.isClientSide && !chunkUnloaded && createNerfed$currentGroup != null) {
            createNerfed$currentGroup.removePosition(groupPosition);
        }
    }

    @Override
    public Consumer<BlockGrouping> addCallback() {
        return (g) -> createNerfed$currentGroup = g;
    }

    @Override
    public Consumer<BlockGrouping> removeCallBack() {
        return (g) -> createNerfed$currentGroup = null;
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public Block getBlockType() {
        return getBlockState().getBlock();
    }

    @Override
    public GroupPosition getGroupPos() {
        return groupPosition;
    }

    @Override
    public BlockGrouping getGroup() {
        return createNerfed$currentGroup;
    }
}
