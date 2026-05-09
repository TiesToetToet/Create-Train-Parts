package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.*;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.ServerSpeedProvider;
import com.tiestoettoet.create_train_parts.foundation.gui.AllIcons;
import com.tiestoettoet.create_train_parts.foundation.utility.CreateTrainPartsLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.simibubi.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;
import static com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingBlock.OPEN;

public class CrossingBlockEntity extends KineticBlockEntity implements IControlContraption {
    LerpedFloat animation;
    int bridgeTicks;
    public boolean running;
    protected float angle;
    protected double sequencedAngleLimit;
    protected boolean assembleNextTick;
    protected ScrollOptionBehaviour<CrossingBarrierMode> barrierMode;

    public ControlledContraptionEntity movedContraption;
    // boolean deferUpdate;
    // Map<String, BlockState> neighborStates = new HashMap<>();

    protected AssemblyException lastException;
    Object openObj = null;

    public CrossingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        animation = LerpedFloat.linear()
                .startWithValue(isOpen(getBlockState()) ? 1 : 0);
        sequencedAngleLimit = -1;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        lastException = AssemblyException.read(tag, registries);
        super.read(tag, registries, clientPacket);
        invalidateRenderBoundingBox();

        if (tag.contains("ForceOpen"))
            openObj = tag.getBoolean("ForceOpen");
    }

    public void assemble() {
//        System.out.println("CrossingBlockEntity.assemble() called");
        if (!(level.getBlockState(worldPosition)
                .getBlock() instanceof CrossingBlock))
            return;

        Direction direction = getBlockState().getValue(HORIZONTAL_FACING);
        CrossingContraption contraption = new CrossingContraption(direction);
//        System.out.println("Created CrossingContraption with direction: " + direction);
        try {
            boolean assembleResult = contraption.assemble(level, worldPosition);
//            System.out.println("Contraption assemble result: " + assembleResult);
            if (!assembleResult)
                return;
            lastException = null;
        } catch (AssemblyException e) {
//            System.out.println("Assembly exception: " + e.getMessage());
            lastException = e;
            sendData();
            return;
        }

        contraption.removeBlocksFromWorld(level, BlockPos.ZERO);
        movedContraption = ControlledContraptionEntity.create(level, this, contraption);
        BlockPos anchor = worldPosition;
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        movedContraption.setRotationAxis(direction.getAxis());
        level.addFreshEntity(movedContraption);

        // System.out.println(movedContraption);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);

        running = true;
        angle = 90;
        sendData();
    }

    public void disassemble() {
//        System.out.println(
//                "Disassemble called - running: " + running + ", movedContraption: " + (movedContraption != null));
        if (!running && movedContraption == null)
            return;
        angle = 0;
        sequencedAngleLimit = -1;
        if (movedContraption != null) {
//            System.out.println("Disassembling contraption");
            movedContraption.disassemble();
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, worldPosition);
        }

        movedContraption = null;
        running = false;
        assembleNextTick = false;
        sendData();
    }

    @Override
    public void tick() {
        super.tick();
        BlockState block = getBlockState();
        boolean open = isOpen(getBlockState());

        // Get speed from the kinetic network (this block receives power)
        float speed = Math.abs(getSpeed());
        boolean shouldOpen = getSpeed() < 0;

        if (speed < 0) {
            speed = -speed;
        }
        speed = speed / 50f * 0.05f * 0.25f;

        float targetValue = shouldOpen ? 0 : 1;
        animation.chase(targetValue, speed, LerpedFloat.Chaser.LINEAR);
        animation.tickChaser();

        if (level.isClientSide()) {
            if (bridgeTicks < 2 && open)
                bridgeTicks++;
            else if (bridgeTicks > 0 && !open)
                bridgeTicks--;
        }

        if (animation.settled() && open == (animation.getValue() != 0)) {
            return;
        }

        block = block.setValue(OPEN, animation.getValue() != 0);
        level.setBlock(worldPosition, block, 10);

        if (!level.isClientSide) {
            assembleNextTick = false;
            if (running) {
                // Disassemble when speed is 0 OR when animation reaches the "closed" state
                // For shouldOpen=true (negative speed), closed is when animation < 0.1
                // For shouldOpen=false (positive speed), closed is when animation > 0.9 (since
                // target is 1)
                // boolean isInClosedState = shouldOpen ?
                // (animation.settled() && animation.getValue() < 0.1f) :
                // (animation.settled() && animation.getValue() > 0.9f);

                if (animation.getValue() == 0) {
//                    System.out.println("Disassembling: speed=" + speed + ", shouldOpen=" + shouldOpen + ", animValue="
//                            + animation.getValue() + ", settled=" + animation.settled());
                    if (movedContraption != null)
                        movedContraption.getContraption()
                                .stop(level);
                    disassemble();
                    return;
                }
            } else {
                if (speed == 0) {
                    return;
                }
                // Only assemble when we have speed AND animation is not in closed position
                boolean isInClosedState = shouldOpen ? (animation.settled() && animation.getValue() < 0.1f)
                        : (animation.settled() && animation.getValue() > 0.9f);

                if (!isInClosedState) {
//                    System.out.println("Assembling: speed=" + speed + ", shouldOpen=" + shouldOpen + ", animValue="
//                            + animation.getValue() + ", settled=" + animation.settled());
                    assemble();
                }
            }
        }

        if (movedContraption == null)
            return;

        // Apply the same smooth animation curve as in the renderer
        float rawValue = animation.getValue();
        float smoothValue = 0.5f * (1 - Mth.cos(Mth.PI * rawValue));

        // Convert radians to degrees: 1.56 radians ≈ 89.4 degrees (roughly 90 degrees)
        float angleInRadians = (float) (1.56 * smoothValue);
        float angleInDegrees = (float) Math.toDegrees(angleInRadians);

        // Apply direction-specific angle signs
        Direction facing = getBlockState().getValue(HORIZONTAL_FACING);
        float finalAngle;
        switch (facing) {
            case EAST:
            case SOUTH:
                // East and South need negative angles
                finalAngle = -angleInDegrees;
                break;
            case NORTH:
            case WEST:
                // North and West need positive angles
                finalAngle = angleInDegrees;
                break;
            default:
                finalAngle = -angleInDegrees; // fallback
                break;
        }

        movedContraption.setAngle(finalAngle);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    protected boolean shouldRenderSpecial(BlockState state) {
        return true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        barrierMode = new ScrollOptionBehaviour<>(CrossingBarrierMode.class,
                CreateTrainPartsLang.translateDirect("crossing.mode"), this, getBarrierModeSlot());
        barrierMode.withCallback($ -> onBarrierModeChanged());
        behaviours.add(barrierMode);
        barrierMode.requiresWrench();
    }

    private void onBarrierModeChanged() {
        Direction facing = getBlockState().getValue(HORIZONTAL_FACING);
        Level world = level;
        Boolean barrierModeActive = barrierMode.get() == CrossingBarrierMode.BARRIER;
        Direction direction = facing.getClockWise(); // Arms are always placed in the clockwise direction relative to facing
        BlockPos armPos = worldPosition.relative(direction);
        while (world.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
            BlockState currentArmState = world.getBlockState(armPos);
            BlockState newArmState = currentArmState
                    .setValue(ArmExtenderBlock.BARRIER, barrierModeActive);
//                    .setValue(ArmExtenderBlock.FLIPPED, newFlipped);
            world.setBlock(armPos, newArmState, 3);
            armPos = armPos.relative(direction);
        }

        //set the barrier mode to the blockstate aswell
        BlockState blockState = world.getBlockState(worldPosition);
        if (blockState.getBlock() instanceof CrossingBlock) {
            world.setBlock(worldPosition, blockState.setValue(CrossingBlock.BARRIER, barrierModeActive), 3);
        }
    }

    public static boolean isOpen(BlockState state) {
        return state.getOptionalValue(OPEN)
                .orElse(false);
    }

    protected ValueBoxTransform getBarrierModeSlot() {
        return new CrossingValueBoxTransform();
    }

    private class CrossingValueBoxTransform extends CenteredSideValueBoxTransform {

        public CrossingValueBoxTransform() {
            super((state, d) -> d == state.getValue(HORIZONTAL_FACING) || d == state.getValue(HORIZONTAL_FACING).getOpposite() || d == state.getValue(HORIZONTAL_FACING).getClockWise());
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 18, 10.5);
        }

    }

    public enum CrossingBarrierMode implements INamedIconOptions {
        NO_BARRIER(AllIcons.I_CROSSING_NO_BARRIER),
        BARRIER(AllIcons.I_CROSSING_BARRIER),
        ;

        private String translationKey;
        private AllIcons icon;

        CrossingBarrierMode(AllIcons icon) {
            this.icon = icon;
            translationKey = "crossing.mode." + Lang.asId(name());
        }

        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }
    }

    @Override
    public boolean isAttachedTo(AbstractContraptionEntity contraption) {
        return movedContraption == contraption;
    }

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        this.movedContraption = contraption;
        if (!level.isClientSide) {
            this.running = true;
            sendData();
        }
    }

    @Override
    public void onStall() {
        if (!level.isClientSide) {
            sendData();
        }
    }

    @Override
    public boolean isValid() {
        return !isRemoved();
    }

    @Override
    public BlockPos getBlockPosition() {
        return worldPosition;
    }
}
