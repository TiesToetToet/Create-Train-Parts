package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllKeys;
import com.simibubi.create.AllSpecialTextures;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.*;

public class SlidingWindowRangeDisplay {

    private static final int DISPLAY_TIME = 200;
    private static GroupEntry lastHoveredGroup = null;

    private static class Entry {
        SlidingWindowBlockEntity be;
        int timer;

        public Entry(SlidingWindowBlockEntity be) {
            this.be = be;
            timer = DISPLAY_TIME;
            Outliner.getInstance().showCluster(getOutlineKey(), createSelection(be))
                    .colored(0xFFFFFF)
                    .disableLineNormals()
                    .lineWidth(1 / 16f)
                    .withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED);
        }

        protected Object getOutlineKey() {
            return Pair.of(be.getBlockPos(), 1);
        }

        protected Set<BlockPos> createSelection(SlidingWindowBlockEntity slidingWindow) {
            Set<BlockPos> positions = new HashSet<>();
            List<BlockPos> includedBlockPositions = slidingWindow.getIncludedBlockPositions(null, true);
            if (includedBlockPositions == null)
                return Collections.emptySet();
            positions.addAll(includedBlockPositions);
            return positions;
        }

    }

    private static class GroupEntry extends Entry {

        List<SlidingWindowBlockEntity> includedBEs;

        public GroupEntry(SlidingWindowBlockEntity be) {
            super(be);
        }

        @Override
        protected Object getOutlineKey() {
            return this;
        }

        @Override
        protected Set<BlockPos> createSelection(SlidingWindowBlockEntity slidingWindow) {
            Set<BlockPos> list = new HashSet<>();
            includedBEs = be.collectSlidingWindowGroup();
            if (includedBEs == null)
                return list;
            for (SlidingWindowBlockEntity slidingWindowBlockEntity : includedBEs)
                list.addAll(super.createSelection(slidingWindowBlockEntity));
            return list;
        }

    }

    static Map<BlockPos, Entry> entries = new HashMap<>();
    static List<GroupEntry> groupEntries = new ArrayList<>();

    public static void tick() {
        Player player = Minecraft.getInstance().player;
        Level world = Minecraft.getInstance().level;
        boolean hasWrench = AllItems.WRENCH.isIn(player.getMainHandItem());

        for (Iterator<BlockPos> iterator = entries.keySet()
                .iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            Entry entry = entries.get(pos);
            if (tickEntry(entry, hasWrench))
                iterator.remove();
            Outliner.getInstance().keep(entry.getOutlineKey());
        }

        for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext(); ) {
            GroupEntry group = iterator.next();
            if (tickEntry(group, hasWrench)) {
                iterator.remove();
                if (group == lastHoveredGroup)
                    lastHoveredGroup = null;
            }
            Outliner.getInstance().keep(group.getOutlineKey());
        }

        if (!hasWrench)
            return;

        HitResult over = Minecraft.getInstance().hitResult;
        if (!(over instanceof BlockHitResult ray))
            return;
        BlockPos pos = ray.getBlockPos();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved())
            return;
        if (!(blockEntity instanceof SlidingWindowBlockEntity slidingWindowBlockEntity))
            return;

        boolean ctrl = AllKeys.ctrlDown();

        if (ctrl) {
            GroupEntry existingGroupForPos = getExistingGroupForPos(pos);
            if (existingGroupForPos != null) {
                for (SlidingWindowBlockEntity included : existingGroupForPos.includedBEs) {
//                    System.out.println("Removing: " + included.getBlockPos());
                    entries.remove(included.getBlockPos());
                }
                existingGroupForPos.timer = DISPLAY_TIME;
                return;
            }
        }

//        System.out.println("Ctrl down: " + ctrl + ", pos: " + pos + ", entries: " + entries.toString() + ", groupEntries: " + groupEntries.toString());


//        System.out.println("Entries: " + entries.toString());

        if (!entries.containsKey(pos) || ctrl) {
//            System.out.println("Displaying sliding window at pos: " + pos + ", ctrl: " + ctrl+ ", entries: " + entries.toString() + ", groupEntries: " + groupEntries.toString());
            display(slidingWindowBlockEntity);
        }
        else {
            if (!ctrl)
                entries.get(pos).timer = DISPLAY_TIME;
        }
    }

    private static boolean tickEntry(Entry entry, boolean hasWrench) {
        SlidingWindowBlockEntity slidingWindowBlockEntity = entry.be;
        Level beWorld = slidingWindowBlockEntity.getLevel();
        Level world = Minecraft.getInstance().level;

        if (slidingWindowBlockEntity.isRemoved() || beWorld == null || beWorld != world
                || !world.isLoaded(slidingWindowBlockEntity.getBlockPos())) {
            return true;
        }

        if (!hasWrench && entry.timer > 20) {
            entry.timer = 20;
            return false;
        }

        entry.timer--;
        if (entry.timer == 0)
            return true;
        return false;
    }

    public static void display(SlidingWindowBlockEntity slidingWindow) {

        if (AllKeys.ctrlDown()) {
            GroupEntry hoveredGroup = new GroupEntry(slidingWindow);

            for (SlidingWindowBlockEntity included : hoveredGroup.includedBEs) {
//                System.out.println("Removing: " + included.getBlockPos());
                Outliner.getInstance().remove(Pair.of(included.getBlockPos(), 1));
            }

            groupEntries.forEach(entry -> Outliner.getInstance().remove(entry.getOutlineKey()));
            groupEntries.clear();
            entries.clear();
            groupEntries.add(hoveredGroup);
//            System.out.println("Displaying group for sliding window at pos: " + slidingWindow.getBlockPos() + ", entries: " + entries.toString() + ", groupEntries: " + groupEntries.toString());
            for (GroupEntry groupEntry : groupEntries) {
//                System.out.println("GroupEntry:");
                for (SlidingWindowBlockEntity object : groupEntry.includedBEs) {
//                    System.out.println(" - BlockPos: " + object.getBlockPos());
                }
            }
            return;
        }

        BlockPos pos = slidingWindow.getBlockPos();
        GroupEntry entry = getExistingGroupForPos(pos);
        if (entry != null)
            Outliner.getInstance().remove(entry.getOutlineKey());

        groupEntries.clear();
        entries.clear();
        entries.put(pos, new Entry(slidingWindow));

    }

    private static GroupEntry getExistingGroupForPos(BlockPos pos) {
        for (GroupEntry groupEntry : groupEntries)
            for (SlidingWindowBlockEntity slidingWindow : groupEntry.includedBEs)
                if (pos.equals(slidingWindow.getBlockPos()))
                    return groupEntry;
        return null;
    }
}
