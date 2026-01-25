package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.simibubi.create.AllKeys;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
//import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.tiestoettoet.create_train_parts.foundation.blockEntity.behaviour.scrollValue.BulkScrollOptionBehaviour;
import com.tiestoettoet.create_train_parts.foundation.gui.AllIcons;
import com.tiestoettoet.create_train_parts.foundation.utility.CreateTrainPartsLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;
import java.util.function.Function;

public class SlidingWindowBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    LerpedFloat animation;
    int bridgeTicks;
    boolean deferUpdate;
    Map<String, BlockState> neighborStates = new HashMap<>();

    public ScrollOptionBehaviour<SelectionMode> selectionMode;

    boolean connectedLeft;
    boolean connectedRight;

    ScrollValueBehaviour range;

    public int currentlySelectedRange;

    protected AssemblyException lastException;
    Object openObj = null;

    private boolean manuallyClosed = false;


    public SlidingWindowBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        animation = LerpedFloat.linear()
                .startWithValue(isOpen(getBlockState()) ? 1 : 0);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        lastException = AssemblyException.read(tag, registries);
        super.read(tag, registries, clientPacket);
        invalidateRenderBoundingBox();

        if (tag.contains("ForceOpen"))
            openObj = tag.getBoolean("ForceOpen");
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(selectionMode = new SlidingWindowOptionBehaviour<>(SelectionMode.class,
                CreateTrainPartsLang.translateDirect("sliding_window.mode"), this, new SlidingWindowModeSlot(), be -> ((SlidingWindowBlockEntity) be).collectSlidingWindowGroup()));

        selectionMode.onlyActiveWhen(this::isVisible);
//        range.between(1, max);
        selectionMode.requiresWrench();

        selectionMode.withCallback(settings -> {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> SlidingWindowRangeDisplay.display(this));
            BlockState blockState = getBlockState();
            SelectionMode selectedMode = SelectionMode.values()[settings];
            blockState = blockState.setValue(SlidingWindowBlock.MODE, selectedMode);
            level.setBlock(worldPosition, blockState, 3); // Update the block state in the world

            for (boolean side : Iterate.trueAndFalse) {
                if (!isConnected(side))
                    continue;
                // Additional logic for connected sides
            }
        });
    }

    public boolean isVisible() {
        return getBlockState().getValue(SlidingWindowBlock.VISIBLE);
    }

    public boolean isConnected(boolean leftSide) {
        return leftSide ? connectedLeft : connectedRight;
    }

    public SelectionMode getMode() {
        return selectionMode.get();
    }

    public int getRange() {
        return range.getValue();
    }

    public List<BlockPos> getIncludedBlockPositions(Direction forcedMovement, boolean visualize) {
        if (!(getBlockState().getBlock() instanceof SlidingWindowBlock))
            return Collections.emptyList();
        return getIncludedBlockPositionsLinear(forcedMovement, visualize);
    }

    public List<SlidingWindowBlockEntity> collectSlidingWindowGroup() {
        Queue<BlockPos> frontier = new LinkedList<>();
        List<SlidingWindowBlockEntity> collected = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(worldPosition);
        while (!frontier.isEmpty()) {
            BlockPos current = frontier.poll();
            if (visited.contains(current))
                continue;
            visited.add(current);
            BlockEntity blockEntity = level.getBlockEntity(current);
//            System.out.println("Visiting block at: " + current + ", BlockEntity: " + blockEntity);
            if (blockEntity instanceof SlidingWindowBlockEntity slidingWindow) {
                collected.add(slidingWindow);
                visited.add(current);
                slidingWindow.addAttachedSlidingWindows(frontier, visited);
            }
        }
//        System.out.println("SlidingWindow group size: " + collected.size());


        return collected;
    }

    // ...existing code...
    public boolean addAttachedSlidingWindows(Queue<BlockPos> frontier, Set<BlockPos> visited) {
        BlockState state = getBlockState();
        Direction.Axis axis = state.getValue(SlidingWindowBlock.FACING).getAxis();
        for (Direction direction : Iterate.directions) {
            BlockPos currentPos = worldPosition.relative(direction);
            if (visited.contains(currentPos))
                continue;
            if (!level.isLoaded(currentPos))
                continue;

            BlockState neighbourState = level.getBlockState(currentPos);
            if (
                    !AllBlocks.GLASS_SLIDING_WINDOW.has(neighbourState) &&
                            !AllBlocks.ANDESITE_SLIDING_WINDOW.has(neighbourState) &&
                            !AllBlocks.BRASS_SLIDING_WINDOW.has(neighbourState) &&
                            !AllBlocks.COPPER_SLIDING_WINDOW.has(neighbourState) &&
                            !AllBlocks.TRAIN_SLIDING_WINDOW.has(neighbourState)
            )
                continue;
            if (!SlidingWindowBlock.sameKind(state, neighbourState))
                continue;
            if (!visited.contains(currentPos))
                frontier.add(currentPos);
            if (neighbourState.getValue(SlidingWindowBlock.FACING).getAxis() != axis)
                continue;

            frontier.add(currentPos);
        }
        return true;
    }
// ...existing code...

    @Override
    public void tick() {
        if (deferUpdate && !level.isClientSide()) {
            deferUpdate = false;
            BlockState blockState = getBlockState();
            blockState.handleNeighborChanged(level, worldPosition, Blocks.AIR, worldPosition, false);
        }
        super.tick();
        BlockState block = getBlockState();
        SlidingWindowBlock slidingWindowBlock = (SlidingWindowBlock) block.getBlock();
        boolean open = openObj instanceof Boolean ? (Boolean) openObj : isOpen(getBlockState());
        if (open != isOpen(getBlockState())) {
            slidingWindowBlock.toggle(block, level, worldPosition, null, open);
        }
        boolean wasSettled = animation.settled();
        animation.chase(open ? 1 : 0, .15f, LerpedFloat.Chaser.LINEAR);
        animation.tickChaser();

        if (level.isClientSide()) {
            if (bridgeTicks < 2 && open)
                bridgeTicks++;
            else if (bridgeTicks > 0 && !open && isVisible(getBlockState()))
                bridgeTicks--;
            return;
        }
//        System.out.println("Open: " + open+ ", !wasSettled: " + !wasSettled + ", settled: " + animation.settled() + ", !isVisible: " + !isVisible(getBlockState()) + ", bridgeTicks: " + bridgeTicks);
        if (!open && !wasSettled && animation.settled() && !isVisible(getBlockState()))
            showBlockModel();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    protected boolean isVisible(BlockState state) {

        return state.getOptionalValue(SlidingWindowBlock.VISIBLE)
                .orElse(true);
    }

    protected boolean shouldRenderSpecial(BlockState state) {
        return !isVisible(state) || bridgeTicks != 0;
    }

    protected void showBlockModel() {
        level.setBlock(worldPosition, getBlockState().setValue(SlidingWindowBlock.VISIBLE, true), 3);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, .5f, 1);
    }

    public void setMode(SelectionMode mode) {
        if (mode != null) {
            selectionMode.setValue(mode.ordinal());
        }
    }



    public boolean isManuallyClosed() {
        return manuallyClosed;
    }

    public void setManuallyClosed(boolean manuallyClosed) {
        this.manuallyClosed = manuallyClosed;
    }

    public enum SelectionMode implements INamedIconOptions, StringRepresentable {
        UP(AllIcons.I_SLIDING_WINDOW_UP),
        RIGHT(AllIcons.I_SLIDING_WINDOW_RIGHT),
        DOWN(AllIcons.I_SLIDING_WINDOW_DOWN),
        LEFT(AllIcons.I_SLIDING_WINDOW_LEFT)

        ;

        private final String translationKey;
        private final AllIcons icon;

        SelectionMode(AllIcons icon) {
            this.icon = icon;
            this.translationKey = "window.mode." + Lang.asId(name());
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static boolean isOpen(BlockState state) {
        return state.getOptionalValue(SlidingWindowBlock.OPEN)
                .orElse(false);
    }

    private List<BlockPos> getIncludedBlockPositionsLinear(Direction forcedMovement, boolean visualize) {
        List<BlockPos> positions = new ArrayList<>();
        BlockState state = getBlockState();
        SlidingWindowBlock block = (SlidingWindowBlock) state.getBlock();
        Direction.Axis axis = state.getValue(SlidingWindowBlock.FACING).getAxis();
        Direction facing = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        int chassisRange = visualize ? currentlySelectedRange : getRange();

        for (int offset : new int[]{1, -1}) {
            if (offset == -1)
                facing = facing.getOpposite();
            boolean sticky = true;
            for (int i = 1; i <= chassisRange; i++) {
                BlockPos current = worldPosition.relative(facing, i);
                BlockState currentState = level.getBlockState(current);

                // Ignore replaceable Blocks and Air-like
                if (!BlockMovementChecks.isMovementNecessary(currentState, level, current))
                    break;
                if (BlockMovementChecks.isBrittle(currentState))
                    break;

                positions.add(current);

                if (BlockMovementChecks.isNotSupportive(currentState, facing))
                    break;
            }
        }

        return positions;
    }

    public class SlidingWindowOptionBehaviour<E extends Enum<E> & INamedIconOptions> extends BulkScrollOptionBehaviour<E> {
        Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter;
        private E[] options;

        public SlidingWindowOptionBehaviour(Class<E> enum_, Component label, SmartBlockEntity be, ValueBoxTransform slot, Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
            super(enum_, label, be, slot, groupGetter);
            this.groupGetter = groupGetter;
        }

        @Override
        public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
//            System.out.println("setValueSettings called, ctrlDown=" + ctrlDown);
            if (!ctrlDown) {
                super.setValueSettings(player, valueSetting, ctrlDown);
                return;
            }
            if (!valueSetting.equals(getValueSettings()))
                playFeedbackSound(this);
            for (SmartBlockEntity be : getBulk()) {
//                System.out.println("Setting value for " + be.getBlockPos() + " to " + valueSetting.value() + " in SlidingWindowBlockEntity");
                if (be instanceof SlidingWindowBlockEntity cbe && cbe.selectionMode != null)
                    cbe.selectionMode.setValue(valueSetting.value());
            }
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public void newSettingHovered(ValueSettings valueSetting) {
            if (!level.isClientSide)
                return;
            if (!AllKeys.ctrlDown())
                currentlySelectedRange = valueSetting.value() + 1;
            else
                for (SmartBlockEntity be : getBulk())
                    if (be instanceof SlidingWindowBlockEntity cbe)
                        cbe.currentlySelectedRange = valueSetting.value() + 1;
            SlidingWindowRangeDisplay.display(SlidingWindowBlockEntity.this);
        }

        public List<? extends SmartBlockEntity> getBulk() {
            return groupGetter.apply(blockEntity);
        }

    }
}
