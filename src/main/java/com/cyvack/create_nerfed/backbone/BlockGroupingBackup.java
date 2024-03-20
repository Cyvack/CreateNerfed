package com.cyvack.create_nerfed.backbone;

import com.simibubi.create.CreateClient;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public class BlockGroupingBackup {
    private final Level level;

    private final Block groupingType;

    private BlockPos rootBlockPos;

    private final Object2IntMap<BlockPos> heatMap;

    //caches so no new sets are constantly created
    private final Set<BlockPos> removeCache = new ObjectOpenHashSet<>();

    private final Set<BlockPos> floodFillCache = new ObjectOpenHashSet<>();
    private final Queue<BlockPos> blockPosToCheck = new ArrayDeque<>();
    //

    public BlockGroupingBackup(Level level, Block groupingType, BlockPos root) {
        this.level = level;
        this.groupingType = groupingType;
        heatMap = new Object2IntOpenHashMap<>();

        this.rootBlockPos = root;
        heatMap.put(root, 0);
    }

    public void rebuild(boolean rootBlockremoved) {
        //If the current heatmap is empty, remove us from the global map
        if (heatMap.isEmpty()) {
            //TODO: remove from global map
            return;
        }

        //Find new root pos and set that as the origin of this group
        //Currently finds a random position, this might be changed to find the closest position to the previous root pos
        if (rootBlockremoved) {
            rootBlockPos = heatMap.keySet().stream().findAny().get();
        }

        //Clear all positions in the heatmap so we can rebuild
        heatMap.clear();
        insertOrUpdatePos(rootBlockPos, 0);
        floodFill(rootBlockPos, (bp) -> {
            if (bp.equals(rootBlockPos)) {
                insertOrUpdatePos(bp, 0);
            } else {
                insertOrUpdatePos(bp);
            }
        });
    }

    /**
     * @param startingPos The position where this floodfill will start
     * @param callback    Callback triggered when the block position is valid
     */
    public void floodFill(BlockPos startingPos, Consumer<BlockPos> callback) {
        blockPosToCheck.add(startingPos);
        while (!blockPosToCheck.isEmpty()) {
            BlockPos poll = blockPosToCheck.poll();
            floodFillCache.add(poll);

            //Trigger callback
            callback.accept(poll);

            //Gather all surrounding block positions
            BlockPos.MutableBlockPos mut = poll.mutable();
            for (Direction dir : Direction.values()) {
                mut.setWithOffset(poll, dir);
                if (floodFillCache.contains(mut))
                    continue;

                //If the relative state is not the same block, skip it
                BlockPos relative = mut.immutable();
                if (!compareTypes(level.getBlockState(relative).getBlock())) {
                    floodFillCache.add(relative);
                    continue;
                }

                blockPosToCheck.add(relative);
            }
        }

        floodFillCache.clear();
    }

    //Used to add new positions before rebuild
    public void insertSetOfPositionsSilently(Set<BlockPos> positions) {
        for (BlockPos pos : positions) {
            heatMap.put(pos, -1);
        }
    }

    public void insertPosSilently(BlockPos pos) {
        heatMap.put(pos, -1);
    }
    //

    public void insertSetOfPositions(Set<BlockPos> positions) {
        positions.forEach(this::insertOrUpdatePos);
    }

    public void insertOrUpdatePos(BlockPos pos) {
        insertOrUpdatePos(pos, null);
    }

    public void insertOrUpdatePos(BlockPos pos, @Nullable Integer forcedHeatValue) {
        if (forcedHeatValue == null) forcedHeatValue = Integer.MAX_VALUE;

        //Make the pos mutable, so we arnt creating numerous instances
        BlockPos.MutableBlockPos mutPos = pos.mutable();

        int newHeatMapValue = heatMap.getOrDefault(mutPos, forcedHeatValue.intValue());
        for (Direction dir : Direction.values() /*TODO: add interface for the block type that allows us to dictate which directions are valid*/) {
            mutPos.setWithOffset(pos, dir);
            int relativeValue = heatMap.getOrDefault(mutPos, Integer.MAX_VALUE);
            if (relativeValue < newHeatMapValue) {
                newHeatMapValue = relativeValue + 1;
            }
        }

        //No valid blocks were found nearby, so this block can not be updated nor added to this group
        if (newHeatMapValue == -1)
            return;

        heatMap.put(pos, newHeatMapValue);
    }

    public void removePosition(BlockPos pos) {
        int currentHeat = heatMap.getOrDefault(pos, -1);
        if (currentHeat == -1) //We can't remove something that doesn't exist already
            return;

        BlockPos.MutableBlockPos mutable = pos.mutable();
        for (Direction dir : Direction.values()) {
            mutable.setWithOffset(pos, dir);

            if (heatMap.getOrDefault(mutable, -1) > currentHeat)
                removeCache.add(mutable.immutable());
        }

        //Remove the heatmap value before we do the searching, so we arnt checking its position
        heatMap.removeInt(pos);
        //rebuild if the removed block is the root block
        if (rootBlockPos.equals(pos)) {
            rootBlockPos = null;
            rebuild(true);
        }

        for (BlockPos searchingPos : removeCache) {
            if (!progressiveCheck(searchingPos, currentHeat)) { //if we are not able to connect back to the main group
                //Floodfill and populate this set
                Set<BlockPos> newGroup = new ObjectOpenHashSet<>();
                floodFill(searchingPos, newGroup::add);

                //remove all entries from this heatmap that share the ones in newGroup
                for (BlockPos blockPos : newGroup) {
                    heatMap.removeInt(blockPos);
                }

                //Create the new group
                BlockGroupingBackup splitGroup = new BlockGroupingBackup(level, groupingType, searchingPos);
                splitGroup.insertSetOfPositions(newGroup);
            }
        }

        removeCache.clear();
    }

    private boolean progressiveCheck(BlockPos rootPos, int refrenceHeat /*refrence value. If we reach this value, or one under it, that means we are still connected to the group*/) {
        BlockPos.MutableBlockPos mutPos = rootPos.mutable();

        boolean hasValidPosition = true;
        groupSearch:
        while (hasValidPosition) {
            int currentHeat = heatMap.getInt(mutPos);
            for (Direction dir : Direction.values()) {
                mutPos.setWithOffset(rootPos, dir); //Set the pos to the next block position

                //We are assuming these positions are still within the same heatmap, so check it
                int adjacentHeat = heatMap.getOrDefault(mutPos, -1);
                if (adjacentHeat == -1) //position is not within the heatmap, so skip it
                    continue;

                if (adjacentHeat < currentHeat) {
                    //we are still connected
                    if (adjacentHeat <= refrenceHeat)
                        return true;

                    rootPos = mutPos.immutable();
                    continue groupSearch;
                }
            }

            //No valid adjacent positions were found, meaning we are not connected
            hasValidPosition = false;
        }

        return false;
    }

    public Set<BlockPos> getPositions() {
        return heatMap.keySet();
    }

    public boolean contains(BlockPos pos) {
        return heatMap.containsKey(pos);
    }

    public int size() {
        return heatMap.size();
    }

    public void clear() {
        heatMap.clear();
        rootBlockPos = null;
    }

    public boolean compareTypes(Block block) {
        return groupingType == block;
    }

    public boolean compareTypes(BlockEntity be) {
        return be.getBlockState().getBlock() == groupingType;
    }

    public Block getGroupingType() {
        return groupingType;
    }

    public void visualize() {
        if (rootBlockPos != null) {
            long color = rootBlockPos.asLong();

            for (Object2IntMap.Entry<BlockPos> posIntSet : heatMap.object2IntEntrySet()) {
                BlockPos pos = posIntSet.getKey();
                int value = posIntSet.getIntValue();

                CreateClient.OUTLINER.showAABB(pos, new AABB(pos)).colored((int) color);

                Vec3 start = pos.getCenter().add(0, 0.5f, 0);
                Vec3 end = start.add(0, value / 5f, 0);
                CreateClient.OUTLINER.showLine(start, start, end).colored((int) color);
            }
        }
    }
}
