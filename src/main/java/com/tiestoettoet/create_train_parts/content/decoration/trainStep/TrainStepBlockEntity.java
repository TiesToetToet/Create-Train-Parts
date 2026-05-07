package com.tiestoettoet.create_train_parts.content.decoration.trainStep;

import com.simibubi.create.AllKeys;
import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
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

public class TrainStepBlockEntity extends SmartBlockEntity {
    LerpedFloat animation;
    int bridgeTicks;
    boolean deferUpdate;
    Map<String, BlockState> neighborStates = new HashMap<>();
    TrainStepType trainStepType;
    public ScrollOptionBehaviour<SlideMode> slideMode;
    public int currentlySelectedRange;
    ScrollValueBehaviour range;
    protected AssemblyException lastException;
    Object openObj = null;

    public TrainStepBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        animation = LerpedFloat.linear()
                .startWithValue(isOpen(getBlockState()) ? 1 : 0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(slideMode = new TrainStepOptionBehaviour<>(SlideMode.class,
                CreateTrainPartsLang.translateDirect("train_step.mode"),
                this,
                new TrainStepModeSlot(),
                be -> ((TrainStepBlockEntity) be).collectTrainStepGroup()));

        slideMode.onlyActiveWhen(this::isVisible);
        slideMode.requiresWrench();

        slideMode.withCallback(settings -> {
            CatnipServices.PLATFORM.executeOnClientOnly(() -> () -> TrainStepRangeDisplay.display(this));

        });
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        lastException = AssemblyException.read(tag, registries);
        super.read(tag, registries, clientPacket);
        invalidateRenderBoundingBox();

        if (tag.contains("ForceOpen"))
            openObj = tag.getBoolean("ForceOpen");
    }

    public SlideMode getMode() {
        return slideMode.get();
    }

    public int getRange() {
        return range.getValue();
    }

    public List<BlockPos> getIncludedBlockPositions(Direction forcedMovement, boolean visualize) {
        if (!(getBlockState().getBlock() instanceof TrainStepBlock))
            return Collections.emptyList();
        return getIncludedBlockPositionsLinear(forcedMovement, visualize);
    }

    public List<TrainStepBlockEntity> collectTrainStepGroup() {
        Queue<BlockPos> frontier = new LinkedList<>();
        List<TrainStepBlockEntity> collected = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(worldPosition);
        while (!frontier.isEmpty()) {
            BlockPos current = frontier.poll();
            if (visited.contains(current))
                continue;
            visited.add(current);
            BlockEntity blockEntity = level.getBlockEntity(current);
            // System.out.println("Visiting block at: " + current + ", BlockEntity: " +
            // blockEntity);
            if (blockEntity instanceof TrainStepBlockEntity trainStep) {
                collected.add(trainStep);
                visited.add(current);
                trainStep.addAttachedTrainSteps(frontier, visited);
            }
        }

        return collected;
    }

    public void setSlideMode(SlideMode mode) {
        if (slideMode != null) {
            slideMode.setValue(mode.ordinal());
        }
    }

    // ...existing code...
    public boolean addAttachedTrainSteps(Queue<BlockPos> frontier, Set<BlockPos> visited) {
        BlockState state = getBlockState();
        Direction.Axis axis = state.getValue(TrainStepBlock.FACING).getAxis();
        for (Direction direction : Iterate.directions) {
            BlockPos currentPos = worldPosition.relative(direction);
            if (visited.contains(currentPos))
                continue;
            if (!level.isLoaded(currentPos))
                continue;

            BlockState neighbourState = level.getBlockState(currentPos);
            if (

            !AllBlocks.TRAIN_STEP_ANDESITE.has(neighbourState) &&
                    !AllBlocks.TRAIN_STEP_BRASS.has(neighbourState) &&
                    !AllBlocks.TRAIN_STEP_COPPER.has(neighbourState) &&
                    !AllBlocks.TRAIN_STEP_TRAIN.has(neighbourState))
                continue;
            if (!TrainStepBlock.sameKind(state, neighbourState))
                continue;
            if (!visited.contains(currentPos))
                frontier.add(currentPos);
            if (neighbourState.getValue(TrainStepBlock.FACING).getAxis() != axis)
                continue;

            frontier.add(currentPos);
        }
        return true;
    }

    public boolean isVisible() {
        return getBlockState().getValue(TrainStepBlock.VISIBLE);
    }

    @Override
    public void tick() {
        if (deferUpdate && !level.isClientSide()) {
            deferUpdate = false;
            BlockState blockState = getBlockState();
            blockState.handleNeighborChanged(level, worldPosition, Blocks.AIR, worldPosition, false);
        }
        super.tick();
        BlockState block = getBlockState();
        TrainStepBlock trainStepBlock = (TrainStepBlock) block.getBlock();
        boolean open = openObj instanceof Boolean ? (Boolean) openObj : isOpen(getBlockState());
        if (open != isOpen(getBlockState())) {
            trainStepBlock.toggle(block, level, worldPosition, null, null, open);
        }
        boolean wasSettled = animation.settled();
        // System.out.println("TrainStepBlockEn/**/tity ticking: " + worldPosition + ",
        // open: " + open + ", wasSettled: " + wasSettled);
        animation.chase(open ? 1 : 0, .15f, LerpedFloat.Chaser.LINEAR);
        animation.tickChaser();

        if (level.isClientSide()) {
            if (bridgeTicks < 2 && open)
                bridgeTicks++;
            else if (bridgeTicks > 0 && !open && isVisible(getBlockState()))
                bridgeTicks--;
            return;
        }

        if (!open && !wasSettled && animation.settled() && !isVisible(getBlockState()))
            showBlockModel();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    protected boolean isVisible(BlockState state) {

        return state.getOptionalValue(TrainStepBlock.VISIBLE)
                .orElse(true);
    }

    public void setNeighborState(BlockState state) {
        if (level == null)
            return; // Ensure the level is not null

        Direction facing = state.getValue(TrainStepBlock.FACING); // Get the block's facing direction
        BlockPos leftPos = worldPosition.relative(facing.getCounterClockWise()); // Calculate left neighbor position
        BlockPos rightPos = worldPosition.relative(facing.getClockWise()); // Calculate right neighbor position

        BlockState leftState = level.getBlockState(leftPos); // Get the left neighbor's state
        BlockState rightState = level.getBlockState(rightPos); // Get the right neighbor's state

        neighborStates.put("left", leftState); // Store the left state
        neighborStates.put("right", rightState); // Store the right state
    }

    public Map<String, BlockState> getNeighborStates() {
        return neighborStates; // Return the map of neighbor states
    }

    public void setTrainStepType(TrainStepType trainStepType) {
        this.trainStepType = trainStepType;
    }

    public TrainStepType getTrainStepType() {
        return trainStepType;
    }

    protected boolean shouldRenderSpecial(BlockState state) {
        return !isVisible(state) || bridgeTicks != 0;
    }

    protected void showBlockModel() {
        level.setBlock(worldPosition, getBlockState().setValue(TrainStepBlock.VISIBLE, true), 3);
        level.playSound(null, worldPosition, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, .5f, 1);
    }

    private List<BlockPos> getIncludedBlockPositionsLinear(Direction forcedMovement, boolean visualize) {
        List<BlockPos> positions = new ArrayList<>();
        BlockState state = getBlockState();
        TrainStepBlock block = (TrainStepBlock) state.getBlock();
        Direction.Axis axis = state.getValue(TrainStepBlock.FACING).getAxis();
        Direction facing = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        int chassisRange = visualize ? currentlySelectedRange : getRange();

        for (int offset : new int[] { 1, -1 }) {
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

    public enum SlideMode implements INamedIconOptions, StringRepresentable {
        SLIDE(AllIcons.I_OPEN_SLIDE),
        NO_SLIDE(AllIcons.I_CLOSE_SLIDE)

        ;

        private final String translationKey;
        private final AllIcons icon;

        SlideMode(AllIcons icon) {
            this.icon = icon;
            this.translationKey = "step.mode." + Lang.asId(name());
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
        return state.getOptionalValue(TrainStepBlock.OPEN)
                .orElse(false);
    }

    public class TrainStepOptionBehaviour<E extends Enum<E> & INamedIconOptions> extends BulkScrollOptionBehaviour<E> {
        Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter;
        private E[] options;

        public TrainStepOptionBehaviour(Class<E> enum_, Component label, SmartBlockEntity be, ValueBoxTransform slot,
                Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
            super(enum_, label, be, slot, groupGetter);
            this.groupGetter = groupGetter;
        }

        @Override
        public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
            // System.out.println("setValueSettings called, ctrlDown=" + ctrlDown);
            if (!ctrlDown) {
                super.setValueSettings(player, valueSetting, ctrlDown);
                return;
            }
            if (!valueSetting.equals(getValueSettings()))
                playFeedbackSound(this);
            for (SmartBlockEntity be : getBulk()) {
                if (be instanceof TrainStepBlockEntity cbe && cbe.slideMode != null)
                    cbe.slideMode.setValue(valueSetting.value());
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
                    if (be instanceof TrainStepBlockEntity cbe)
                        cbe.currentlySelectedRange = valueSetting.value() + 1;
            TrainStepRangeDisplay.display(TrainStepBlockEntity.this);
        }

        public List<? extends SmartBlockEntity> getBulk() {
            return groupGetter.apply(blockEntity);
        }

    }
}
