package com.tiestoettoet.create_train_parts.content.decoration.slidingWindow;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.elevator.ElevatorColumn;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.content.decoration.slidingDoor.DoorControlBehaviour;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;
import java.util.Map;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class SlidingWindowMovementBehaviour implements MovementBehaviour {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    static class SlidingWindowAnimationData {
        LerpedFloat animation = LerpedFloat.linear();
    }

    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    @Override
    public void tick(MovementContext context) {
        StructureTemplate.StructureBlockInfo structureBlockInfo = context.contraption.getBlocks()
                .get(context.localPos);
        if (structureBlockInfo == null)
            return;
        boolean open = SlidingWindowBlockEntity.isOpen(structureBlockInfo.state());
        // System.out.println();

        if (!context.world.isClientSide())
            tickOpen(context, open);

        SlidingWindowAnimationData swad;
        if (!(context.temporaryData instanceof SlidingWindowAnimationData)) {
            context.temporaryData = swad = new SlidingWindowAnimationData();
        } else {
            swad = (SlidingWindowAnimationData) context.temporaryData;
        }

        boolean wasSettled = swad.animation.settled();
        swad.animation.chase(open ? 1 : 0, .15f, LerpedFloat.Chaser.LINEAR);
        swad.animation.tickChaser();

        if (!wasSettled && swad.animation.settled() && !open) {
            context.world.playLocalSound(context.position.x, context.position.y, context.position.z,
                    SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, .125f, 1, false);
        }

    }

    protected void tickOpen(MovementContext context, boolean currentlyOpen) {
        boolean shouldOpen = shouldOpen(context);
        if (!shouldUpdate(context, shouldOpen))
            return;
        if (currentlyOpen == shouldOpen)
            return;
        BlockPos pos = context.localPos;
        Contraption contraption = context.contraption;

        StructureTemplate.StructureBlockInfo info = contraption.getBlocks()
                .get(pos);
        if (info == null || !info.state().hasProperty(SlidingWindowBlock.OPEN))
            return;

        toggleWindow(pos, contraption, info);

        Direction facing = getWindowFacing(context);
        BlockPos inWorldWindow = BlockPos.containing(context.position)
                .relative(facing);
        BlockState inWorldWindowState = context.world.getBlockState(inWorldWindow);
        if (inWorldWindowState.getBlock() instanceof SlidingWindowBlock sb
                && inWorldWindowState.hasProperty(SlidingWindowBlock.OPEN))
            if (inWorldWindowState.hasProperty(FACING) && inWorldWindowState.getOptionalValue(FACING)
                    .orElse(Direction.UP)
                    .getAxis() == facing.getAxis())
                sb.setOpen(null, context.world, inWorldWindowState, inWorldWindow, false);

    }

    private void toggleWindow(BlockPos pos, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
        BlockState newState = info.state().cycle(SlidingWindowBlock.OPEN);
        // toggleSlideRow(info.state(), pos, null, null, null, contraption);
        // BlockState updatedState =
        contraption.entity.setBlock(pos, new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));

        info = contraption.getBlocks()
                .get(pos.relative(info.state().getValue(FACING)));
        if (info != null && info.state().hasProperty(SlidingWindowBlock.OPEN)) {
            newState = info.state().cycle(SlidingWindowBlock.OPEN);
            contraption.entity.setBlock(pos.relative(info.state().getValue(FACING)),
                    new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));
        }
        // Always invalidate colliders when toggling window state
        contraption.invalidateColliders();
    }

    protected boolean shouldUpdate(MovementContext context, boolean shouldOpen) {
        if (context.firstMovement && shouldOpen)
            return false;
        if (!context.data.contains("Open")) {
            context.data.putBoolean("Open", shouldOpen);
            return true;
        }
        boolean wasOpen = context.data.getBoolean("Open");
        context.data.putBoolean("Open", shouldOpen);
        return wasOpen != shouldOpen;
    }

    protected boolean shouldOpen(MovementContext context) {
        if (context.disabled)
            return false;
        BlockState state = context.state;
        SlidingWindowBlockEntity.SelectionMode mode = state.getValue(SlidingWindowBlock.MODE);
        if (mode == SlidingWindowBlockEntity.SelectionMode.DOWN ||
                mode == SlidingWindowBlockEntity.SelectionMode.UP) {
            return false;
        }
        Contraption contraption = context.contraption;
        boolean canOpen = context.motion.length() < 1 / 128f && !contraption.entity.isStalled()
                || contraption instanceof ElevatorContraption ec && ec.arrived;

        if (!canOpen) {
            context.temporaryData = null;
            return false;
        }

        if (context.temporaryData instanceof WeakReference<?> wr && wr.get() instanceof DoorControlBehaviour dcb)
            if (dcb.blockEntity != null && !dcb.blockEntity.isRemoved())
                return shouldOpenAt(dcb, context);

        context.temporaryData = null;
        DoorControlBehaviour doorControls = null;

        if (context.contraption.entity instanceof CarriageContraptionEntity cce)
            doorControls = getTrainStationSlideControl(cce, context);
        if (contraption instanceof ElevatorContraption ec) {
            doorControls = getElevatorDoorControl(ec, context);
        }

        if (doorControls == null)
            return false;

        context.temporaryData = new WeakReference<>(doorControls);
        return shouldOpenAt(doorControls, context);
    }

    protected boolean shouldOpenAt(DoorControlBehaviour controller, MovementContext context) {
        if (controller.mode == DoorControl.ALL)
            return true;
        if (controller.mode == DoorControl.NONE)
            return false;
        return controller.mode.matches(getWindowFacing(context));
    }

    protected DoorControlBehaviour getElevatorDoorControl(ElevatorContraption ec, MovementContext context) {
        Integer currentTargetY = ec.getCurrentTargetY(context.world);
        if (currentTargetY == null)
            return null;
        ElevatorColumn.ColumnCoords ColumnCoords = ec.getGlobalColumn();
        if (ColumnCoords == null)
            return null;
        ElevatorColumn elevatorColumn = ElevatorColumn.get(context.world, ColumnCoords);
        if (elevatorColumn == null)
            return null;
        return BlockEntityBehaviour.get(context.world, elevatorColumn.contactAt(currentTargetY),
                DoorControlBehaviour.TYPE);
    }

    protected DoorControlBehaviour getTrainStationSlideControl(CarriageContraptionEntity cce, MovementContext context) {
        Carriage carriage = cce.getCarriage();
        if (carriage == null || carriage.train == null)
            return null;
        GlobalStation currentStation = carriage.train.getCurrentStation();
        if (currentStation == null)
            return null;

        BlockPos stationPos = currentStation.getBlockEntityPos();
        ResourceKey<Level> stationDim = currentStation.getBlockEntityDimension();
        MinecraftServer server = context.world.getServer();
        if (server == null)
            return null;
        ServerLevel stationLevel = server.getLevel(stationDim);
        if (stationLevel == null || !stationLevel.isLoaded(stationPos))
            return null;
        return BlockEntityBehaviour.get(stationLevel, stationPos, DoorControlBehaviour.TYPE);
    }

    protected Direction getWindowFacing(MovementContext context) {
        Direction stateFacing = context.state.getValue(FACING);
        Direction originalFacing = Direction.get(Direction.AxisDirection.POSITIVE, stateFacing.getAxis());
        Vec3 centerOfContraption = context.contraption.bounds.getCenter();
        Vec3 diff = Vec3.atCenterOf(context.localPos)
                .add(Vec3.atLowerCornerOf(stateFacing.getNormal())
                        .scale(-.45f))
                .subtract(centerOfContraption);
        if (originalFacing.getAxis()
                .choose(diff.x, diff.y, diff.z) < 0)
            originalFacing = originalFacing.getOpposite();

        Vec3 directionVec = Vec3.atLowerCornerOf(originalFacing.getNormal());
        directionVec = context.rotation.apply(directionVec);
        return Direction.getNearest(directionVec.x, directionVec.y, directionVec.z);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
            ContraptionMatrices matrices, MultiBufferSource buffer) {
        SlidingWindowMovementBehaviourRenderer.renderInContraption(context, renderWorld, matrices, buffer);
    }
}
