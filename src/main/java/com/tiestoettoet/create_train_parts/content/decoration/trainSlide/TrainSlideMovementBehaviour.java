package com.tiestoettoet.create_train_parts.content.decoration.trainSlide;

import com.simibubi.create.content.contraptions.elevator.ElevatorColumn;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import net.minecraft.client.renderer.MultiBufferSource;

import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.elevator.ElevatorContraption;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.content.decoration.slidingDoor.DoorControlBehaviour;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.station.GlobalStation;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class TrainSlideMovementBehaviour implements MovementBehaviour {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    static class TrainSlideAnimationData {
        LerpedFloat animation = LerpedFloat.linear();
        DoorControlBehaviour doorControl;
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
        boolean open = TrainSlideBlockEntity.isOpen(structureBlockInfo.state());

        if (!context.world.isClientSide())
            tickOpen(context, open);


        TrainSlideAnimationData ae;
        if (!(context.temporaryData instanceof TrainSlideAnimationData)) {
            context.temporaryData = ae = new TrainSlideAnimationData();
        } else {
            ae = (TrainSlideAnimationData) context.temporaryData;
        }

        boolean wasSettled = ae.animation.settled();
        ae.animation.chase(open ? 1 : 0, .15f, Chaser.LINEAR);
        ae.animation.tickChaser();

        if (!wasSettled && ae.animation.settled() && !open)
            context.world.playLocalSound(context.position.x, context.position.y, context.position.z,
                    SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, .125f, 1, false);


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
        if (info == null || !info.state().hasProperty(TrainSlideBlock.OPEN))
            return;

        toggleSlide(pos, contraption, info);

        Direction facing = getSlideFacing(context);
        BlockPos inWorldSlide = BlockPos.containing(context.position)
                .relative(facing);
        BlockState inWorldSlideState = context.world.getBlockState(inWorldSlide);
        if (inWorldSlideState.getBlock() instanceof TrainSlideBlock sb && inWorldSlideState.hasProperty(TrainSlideBlock.OPEN))
            if (inWorldSlideState.hasProperty(FACING) && inWorldSlideState.getOptionalValue(FACING)
                    .orElse(Direction.UP)
                    .getAxis() == facing.getAxis())
                sb.setOpen(null, context.world, inWorldSlideState, inWorldSlide, shouldOpen);

        if (shouldOpen)
            context.world.playSound(null, BlockPos.containing(context.position), SoundEvents.IRON_DOOR_OPEN,
                    SoundSource.BLOCKS, .125f, 1);
    }

    private void toggleSlide(BlockPos pos, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
        BlockState newState = info.state().cycle(TrainSlideBlock.OPEN);
//        toggleSlideRow(info.state(), pos, null, null, null, contraption);
//        BlockState updatedState =
        contraption.entity.setBlock(pos, new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));


        info = contraption.getBlocks()
                .get(pos.relative(info.state().getValue(FACING)));
        if (info != null && info.state().hasProperty(TrainSlideBlock.OPEN)) {
            newState = info.state().cycle(TrainSlideBlock.OPEN);
            contraption.entity.setBlock(pos.relative(info.state().getValue(FACING)), new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));
            contraption.invalidateColliders();
        }
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
        Contraption contraption = context.contraption;
        boolean canOpen = context.motion.length() < 1 / 128f && !contraption.entity.isStalled()
                || contraption instanceof ElevatorContraption ec && ec.arrived;

        if (!canOpen) {
            context.temporaryData = null;
            return false;
        }

        if (context.temporaryData instanceof TrainSlideAnimationData
                && ((TrainSlideAnimationData) context.temporaryData).doorControl instanceof DoorControlBehaviour dcb)
            if (dcb.blockEntity != null && !dcb.blockEntity.isRemoved())
                return shouldOpenAt(dcb, context);

        DoorControlBehaviour doorControls = null;

        if (context.contraption.entity instanceof CarriageContraptionEntity cce)
            doorControls = getTrainStationSlideControl(cce, context);
        if (contraption instanceof ElevatorContraption ec) {
            doorControls = getElevatorDoorControl(ec, context);
        }

        if (doorControls == null)
            return false;

        TrainSlideAnimationData ae;
        if (!(context.temporaryData instanceof TrainSlideAnimationData))
            context.temporaryData = ae = new TrainSlideAnimationData();
        else
            ae = (TrainSlideAnimationData) context.temporaryData;

        ae.doorControl = doorControls;

        return shouldOpenAt(doorControls, context);
    }

    protected boolean shouldOpenAt(DoorControlBehaviour controller, MovementContext context) {
        if (controller.mode == DoorControl.ALL)
            return true;
        if (controller.mode == DoorControl.NONE)
            return false;
        return controller.mode.matches(getSlideFacing(context));
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

    protected Direction getSlideFacing(MovementContext context) {
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
    public void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices, MultiBufferSource buffer) {

        if (!(context.temporaryData instanceof TrainSlideAnimationData ae))
            return;

        TrainSlideMovementBehaviourRenderer.renderInContraption(context, renderWorld, matrices, buffer, ae.animation);
    }
}
