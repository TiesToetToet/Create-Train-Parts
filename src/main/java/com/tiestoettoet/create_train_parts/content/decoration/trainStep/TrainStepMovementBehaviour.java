package com.tiestoettoet.create_train_parts.content.decoration.trainStep;

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
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class TrainStepMovementBehaviour implements MovementBehaviour {

    public static final BooleanProperty OPEN = BooleanProperty.create("open");

    static class TrainStepAnimationData {
        LerpedFloat animation = LerpedFloat.linear();
        DoorControlBehaviour doorControls;
    }

    @Override
    public boolean mustTickWhileDisabled() {return true;}

    @Override
    public void tick(MovementContext context) {
        StructureTemplate.StructureBlockInfo structureBlockInfo = context.contraption.getBlocks()
            .get(context.localPos);
        if (structureBlockInfo == null)
            return;
        boolean open = TrainStepBlockEntity.isOpen(structureBlockInfo.state());

        if (!context.world.isClientSide())
            tickOpen(context, open);

        TrainStepAnimationData ad;
        if(!(context.temporaryData instanceof TrainStepAnimationData))
            context.temporaryData = ad = new TrainStepAnimationData();
        else
            ad = (TrainStepAnimationData) context.temporaryData;


        boolean wasSettled = ad.animation.settled();
        ad.animation.chase(open ? 1 : 0, .15f, Chaser.LINEAR);
        ad.animation.tickChaser();

        if (!wasSettled && ad.animation.settled() && !open)
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
        if (info == null || !info.state().hasProperty(TrainStepBlock.OPEN))
            return;

        toggleStep(pos, contraption, info);

        Direction facing = getStepFacing(context);
        BlockPos inWorldStep = BlockPos.containing(context.position)
                .relative(facing);
        BlockState inWorldStepState = context.world.getBlockState(inWorldStep);
        if (inWorldStepState.getBlock() instanceof TrainStepBlock sb && inWorldStepState.hasProperty(TrainStepBlock.OPEN))
            if (inWorldStepState.hasProperty(FACING) && inWorldStepState.getOptionalValue(FACING)
                    .orElse(Direction.UP)
                    .getAxis() == facing.getAxis())
                sb.setOpen(null, context.world, inWorldStepState, inWorldStep, shouldOpen);

        if (shouldOpen)
            context.world.playSound(null, BlockPos.containing(context.position), SoundEvents.IRON_DOOR_OPEN,
                    SoundSource.BLOCKS, .125f, 1);
    }

    private void toggleStep(BlockPos pos, Contraption contraption, StructureTemplate.StructureBlockInfo info) {
        BlockState newState = info.state().cycle(TrainStepBlock.OPEN);
        contraption.entity.setBlock(pos, new StructureTemplate.StructureBlockInfo(info.pos(), newState, info.nbt()));
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

        if (context.temporaryData instanceof TrainStepAnimationData wr && wr.doorControls instanceof DoorControlBehaviour dcb)
            if (dcb.blockEntity != null && !dcb.blockEntity.isRemoved())
                return shouldOpenAt(dcb, context);

        DoorControlBehaviour doorControls = null;

        if (context.contraption.entity instanceof CarriageContraptionEntity cce)
            doorControls = getTrainStationStepControl(cce, context);
        if (contraption instanceof ElevatorContraption ec) {
            doorControls = getElevatorDoorControl(ec, context);
        }

        if (doorControls == null)
            return false;

        TrainStepAnimationData ad;
        if(!(context.temporaryData instanceof TrainStepAnimationData))
            context.temporaryData = ad = new TrainStepAnimationData();
        else
            ad = (TrainStepAnimationData) context.temporaryData;

        ad.doorControls = doorControls;
        return shouldOpenAt(doorControls, context);
    }

    protected boolean shouldOpenAt(DoorControlBehaviour controller, MovementContext context) {
        if (controller.mode == DoorControl.ALL)
            return true;
        if (controller.mode == DoorControl.NONE)
            return false;
        return controller.mode.matches(getStepFacing(context));
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

    protected DoorControlBehaviour getTrainStationStepControl(CarriageContraptionEntity cce, MovementContext context) {
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

    protected Direction getStepFacing(MovementContext context) {
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
        if(!(context.temporaryData instanceof TrainStepAnimationData ad))
            return;

        TrainStepMovementBehaviourRenderer.renderInContraption(context, renderWorld, matrices, buffer, ad.animation);
    }
}
