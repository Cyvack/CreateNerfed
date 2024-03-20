package com.cyvack.create_nerfed.backbone;

import com.cyvack.create_nerfed.CreateNerfed;
import com.mojang.patchy.BlockedServers;
import com.simibubi.create.CreateClient;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class BlockGrouping {
    public static final GroupPosition DUMMY_POS_NEG = new GroupPosition(Integer.MIN_VALUE);
    public static final GroupPosition DUMMY_POS_POS = new GroupPosition(Integer.MAX_VALUE);

    private final Level level;

    private final Block groupingType;

    private GroupPosition rootGroupPos;

    private final Map<BlockPos, GroupPosition> heatMap;

    public boolean invalid = false;

    //caches so no new sets are constantly created
    private final Set<GroupPosition> removeCache = new ObjectOpenHashSet<>();

    private final Set<GroupPosition> floodFillCache = new ObjectOpenHashSet<>();
    private final LinkedList<GroupPosition> groupPositionsToCheck = new LinkedList<>();

    private final Set<BlockPos> totalRebuildCache = new HashSet<>();
    private final Queue<GroupInformation> totalRebuildQueue = new ArrayDeque<>();
    //

    public BlockGrouping(Level level, GroupPosition root) {
        this.level = level;
        heatMap = new HashMap<>();

        this.groupingType = root.posInfo.getBlockType();
        this.rootGroupPos = root;
    }

    /**
     * Used prior to splitting another group
     */
    public void forceInsertPositions(Collection<GroupPosition> positions) {
        if (invalid)
            return;

        for (GroupPosition pos : positions) {
            if (pos.posInfo.isValid())
                heatMap.put(pos.pos, pos);
        }
    }

    /**
     * Used to remove all positions when splitting this group
     */
    public void forceRemovePositions(Collection<GroupPosition> positions) {
        if (invalid)
            return;

        for (GroupPosition pos : positions) {
            heatMap.remove(pos.pos);
        }
    }

    /**
     * @param gp The group position to add to this block group
     */
    public boolean insertOrUpdatePos(GroupPosition gp, boolean forceUpdate) {
        return insertOrUpdatePos(gp, Integer.MAX_VALUE, forceUpdate);
    }

    /**
     * @param gp        The group position to add to this block group
     * @param heatValue The heat value that should be associated with this position. Usually {@link Integer#MAX_VALUE Integer Max Value}, unless setting root block, in which case it's 0
     * @return Whether this position was successfully added to the group
     */
    public boolean insertOrUpdatePos(GroupPosition gp, int heatValue, boolean forceUpdate) {
        if (!gp.posInfo.isValid() || gp.posInfo.getBlockType() != groupingType || invalid)
            return false;

        BlockPos immut = gp.pos;
        BlockPos.MutableBlockPos mut = immut.mutable();
        int lowestValue = heatValue;
        if (!heatMap.isEmpty()) {
            for (Direction dir : Direction.values()) {
                mut.setWithOffset(immut, dir);

                GroupPosition adjacentPos = heatMap.getOrDefault(mut, DUMMY_POS_POS);
                if (adjacentPos.getHeatValue() < lowestValue) {
                    lowestValue = adjacentPos.getHeatValue();
                }
            }
        }

        //No valid surrounding group positions were found, and we arnt setting the root block
        if (lowestValue == Integer.MAX_VALUE)
            return false;

        //set new heat value to the lowest value + 1, and add this to heatmap
        //This position is newly added, so trigger callback
        if (forceUpdate || !heatMap.containsKey(immut))
            gp.posInfo.addCallback().accept(this);

        gp.setHeatValue(lowestValue + 1);
        heatMap.put(immut, gp);

        return true;
    }

    /**
     * Called when a block position is removed. Usually done when a block entity is destroyed </b>
     * Not recommended to call when removing a large number of positions
     *
     * @param gp The group position to remove from this group
     */
    public void removePosition(GroupPosition gp) {
        int originalHeat = gp.getHeatValue();
        if (!heatMap.containsKey(gp.pos) || invalid)
            return;

        gp.clear();
        heatMap.remove(gp.pos);
        gp.posInfo.removeCallBack().accept(this);
        if (!heatMap.isEmpty()) {
            if (gp.equals(rootGroupPos)) { //get a new randomized root block
                rootGroupPos = heatMap.entrySet().stream().findAny().get().getValue();
                rebuild();
            }

            for (Direction dir : Direction.values()) {
                GroupPosition groupPos = heatMap.get(gp.pos.relative(dir));
                if (groupPos != null && groupPos.getHeatValue() >= originalHeat) {
                    if (floodFillSearch(groupPos, originalHeat)) {
                        BlockGrouping newGroup = CreateNerfed.GROUPLES.createGroup(groupPos, level);
                        newGroup.forceInsertPositions(floodFillCache);
                        newGroup.rebuild();

                        forceRemovePositions(floodFillCache);
                    }

                    groupPositionsToCheck.clear();
                    floodFillCache.clear();
                }
            }
            if (heatMap.isEmpty()) {
                clearAndMarkInvalid();
            }
        } else {
            clearAndMarkInvalid();
        }
    }

    /**
     * Completely clears the heatmap and marks this block grouping as invalid, preventing new positions from being added to this
     */
    public void clearAndMarkInvalid() {
        CreateNerfed.GROUPLES.removeBlockGroup(this, level);
        invalid = true;

        for (GroupPosition gp : getGroupPositions()) {
            gp.posInfo.removeCallBack().accept(this);
            gp.clear();
        }

        heatMap.clear();
        floodFillCache.clear();
        removeCache.clear();
        totalRebuildCache.clear();

        groupPositionsToCheck.clear();
    }

    //TODO: make this smarter
    private boolean floodFillSearch(GroupPosition toCheck, int originalRemovedHeat) {
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        groupPositionsToCheck.add(toCheck);
        floodFillCache.add(toCheck);
        while (!groupPositionsToCheck.isEmpty()) {
            GroupPosition checkPos = groupPositionsToCheck.poll();
            if (checkPos.getHeatValue() <= 2)
                return false;

            for (Direction dir : Direction.values()) {
                GroupPosition adjacentGP = heatMap.get(mut.setWithOffset(checkPos.pos, dir));
                if (adjacentGP != null) {
                    if (!floodFillCache.contains(adjacentGP)) {
                        floodFillCache.add(adjacentGP);
                        groupPositionsToCheck.add(adjacentGP);
                    }
                }
            }
        }

        return true; //we are not connected
    }

    /**
     * Called when rebuilding this block group. This method assumes that there are already entries within the heatmap. </b>
     *
     * @see BlockGrouping#totalRebuild()
     */
    public void rebuild() {
        resetMax();
        floodFillCache.clear(); //just in case

        /*
        * I don't like needing to do a floodfill to rebuild the heatmap here
        * So let's think of alternatives
        *   Currently, we reset all heat values to the max int value, and start from the root position of the block group. Then radiating out from there we figure out heat values
        *   Heat values are effectively how many steps it needs to take in order to get to the root
        *   However, each pos does not do a walk to the root, instead it takes the lowest value by itself and adds 1 to it, a good generalization
        *
        *   A change to the floodfill algorithm, if I decide to keep it, is to make it check for nearby heatmap positions that are the max integer value.
        *   If we do find any that are the max int value still, then we force that position to update.
        *   Even if that update fails for whatever reason (it shouldn't), there should be another check by a surrounding block to ensure it does get updated
        *
        *
        *   An alternative to the floodfill algorithm would be to have each position do a walk to the root position. This would be computationally expensive, with little upside.
        *   The walk method would have every position attempt to find the closest direction to the root, then continue to that position.
        *   I suppose you would be able to record what positions were walked, and update their heat values accordingly, to cut down on the needed walks
        *   if the walk never reaches the root, then it's not part of the group and should be invalidated along with every other position that was walked over
        *
        *   Another alternative would be to simply update the heatmap to use the distance (Not step distance) to the root. This would be incredibly inexpensive computationally.
        *   However, this generalization would break other systems that are currently in break, such as the splitting algorithm.
        *       (It does a floodfill and sees if it's connected by finding the lowest heat value that's 2 or below)
        *
        *
        *
        *
        */

        //execute floodfill and update heat values
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        insertOrUpdatePos(rootGroupPos, 0, true);

        groupPositionsToCheck.add(rootGroupPos);
        floodFillCache.add(rootGroupPos);
        while (!groupPositionsToCheck.isEmpty()) {
            GroupPosition checkGP = groupPositionsToCheck.poll();

            //gather surrounding positions
            for (Direction dir : Direction.values()) {
                GroupPosition adjacentGP = heatMap.get(mut.setWithOffset(checkGP.pos, dir));
                if (adjacentGP != null) {
                    insertOrUpdatePos(checkGP, true);

                    if (!floodFillCache.contains(adjacentGP)) {
                        floodFillCache.add(adjacentGP);
                        groupPositionsToCheck.add(adjacentGP);
                    }
                }
            }
        }

        floodFillCache.clear();
    }

    /**
     * Called when a total rebuild of this block group is desired. This will clear the heat map of all of its values, and gather all new, valid group positions.
     */
    ///TODO: add nearby group checking. If this total rebuild crashes into another group, merge this group with it
    public void totalRebuild() {
        clear();
        insertOrUpdatePos(rootGroupPos, 0, false);

        BlockGrouping groupToMerge = null;

        totalRebuildQueue.add(rootGroupPos.posInfo);
        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        while (!totalRebuildQueue.isEmpty()) {
            GroupInformation groupInfo = totalRebuildQueue.poll();
            BlockPos groupBlockPos = groupInfo.getGroupPos().pos;
            totalRebuildCache.add(groupBlockPos);

            insertOrUpdatePos(groupInfo.getGroupPos(), false);
            for (Direction dir : Direction.values()) {
                mut.setWithOffset(groupBlockPos, dir);

                if (!totalRebuildCache.contains(mut)) {
                    if (level.getBlockEntity(mut) instanceof GroupInformation gi && gi.getBlockType() == groupingType && gi.isValid()) {
                        totalRebuildQueue.add(gi);
                    }
                }
            }
        }

        totalRebuildCache.clear();
        if (groupToMerge != null) {
            clearAndMarkInvalid();
            groupToMerge.totalRebuild();
        }
    }

    private void resetMax() {
        for (GroupPosition gp : getGroupPositions()) {
            gp.setHeatValue(Integer.MAX_VALUE);
        }
    }

    private void resetMin() {
        for (GroupPosition gp : getGroupPositions()) {
            gp.setHeatValue(Integer.MIN_VALUE);
        }
    }

    public void clear() {
        heatMap.forEach(($, gp) -> gp.posInfo.removeCallBack().accept(this));
        heatMap.clear();
    }

    public boolean compareTypes(Block type) {
        return groupingType == type;
    }

    public boolean contains(BlockPos pos) {
        return heatMap.containsKey(pos);
    }

    public Map<BlockPos, GroupPosition> getHeatMap() {
        return heatMap;
    }

    public int getSize() {
        return heatMap.size();
    }

    public Set<GroupPosition> getGroupPositions() {
        Set<GroupPosition> positions = new HashSet<>();
        heatMap.forEach(($, gp) -> positions.add(gp));
        return positions;
    }

    public void visualize() {
        if (rootGroupPos != null) {
            long color = rootGroupPos.pos.asLong();

            for (Map.Entry<BlockPos, GroupPosition> set : heatMap.entrySet()) {
                BlockPos pos = set.getKey();
                int value = set.getValue().getHeatValue();

                CreateClient.OUTLINER.showAABB(pos, new AABB(pos).inflate(set.getValue().equals(rootGroupPos) ? 0.15f : 0)).colored((int) color);

                Vec3 start = pos.getCenter().add(0, 0.5f, 0);
                Vec3 end = start.add(0, value / 5f, 0);
                CreateClient.OUTLINER.showLine(start, start, end).colored((int) color);

            }
        }
    }
}
