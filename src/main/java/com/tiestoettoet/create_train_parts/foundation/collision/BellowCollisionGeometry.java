package com.tiestoettoet.create_train_parts.foundation.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.tiestoettoet.create_train_parts.foundation.mixin.CarriageBogeyAccessor;
import com.tiestoettoet.create_train_parts.content.trains.bellow.BellowBlock;

import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.Vec3;

/**
 * Builds the same world-space bellow curve on either logical side. Keeping this
 * independent from the renderer allows the server to use it for collision.
 */
public final class BellowCollisionGeometry {

    private BellowCollisionGeometry() {
    }

    private record BellowInfo(Vec3 position, Direction facing, BellowSize size) {
    }

    private record BellowPair(Vec3 first, Vec3 second, double distance, BellowSize size) {
    }

    /**
     * Builds the connection from this carriage to the next carriage. A
     * connection is owned only by its first carriage so it is never added twice.
     */
    public static List<BellowSegment> buildSegments(CarriageContraptionEntity entity, float partialTicks) {
        Carriage carriage = entity.getCarriage();
        if (carriage == null || carriage.train == null)
            return List.of();

        int carriageIndex = carriage.train.carriages.indexOf(carriage);
        if (carriageIndex < 0 || carriageIndex >= carriage.train.carriages.size() - 1)
            return List.of();

        Level level = entity.level();
        Carriage nextCarriage = carriage.train.carriages.get(carriageIndex + 1);
        CarriageContraptionEntity nextEntity = nextCarriage.getDimensional(level).entity.get();
        if (nextEntity == null)
            return List.of();

        BellowPair pair = findClosestPair(
                findBellows(entity, partialTicks),
                findBellows(nextEntity, partialTicks));
        if (pair == null)
            return List.of();

        CarriageBogey trailingBogey = carriage.trailingBogey();
        CarriageBogey nextLeadingBogey = nextCarriage.leadingBogey();
        LerpedFloat trailingYaw = getYaw(trailingBogey);
        LerpedFloat nextLeadingYaw = getYaw(nextLeadingBogey);
        LerpedFloat leadingYaw = getYaw(carriage.leadingBogey());
        LerpedFloat nextTrailingYaw = getYaw(nextCarriage.trailingBogey());

        float carriageYaw1 = carriage.leadingBogey() == trailingBogey
                ? leadingYaw.getValue(partialTicks)
                : (leadingYaw.getValue(partialTicks) + trailingYaw.getValue(partialTicks)) / 2f;
        float carriageYaw2 = nextCarriage.trailingBogey() == nextLeadingBogey
                ? nextLeadingYaw.getValue(partialTicks)
                : (nextLeadingYaw.getValue(partialTicks) + nextTrailingYaw.getValue(partialTicks)) / 2f;

        Vec3 anchor = pair.first();
        Vec3 anchor2 = pair.second();
        Vec3 anchorDirection = anchor2.subtract(anchor).normalize();
        float gapFromAnchor = 2 / 16f;
        Vec3 adjustedAnchor = anchor.add(anchorDirection.scale(gapFromAnchor)).add(0, -20 / 16f, 0);
        Vec3 adjustedAnchor2 = anchor2.subtract(anchorDirection.scale(gapFromAnchor)).add(0, -20 / 16f, 0);

        float controlDistance = (float) (pair.distance() * 0.5);
        float yaw1Radians = (float) Math.toRadians(carriageYaw1);
        float yaw2Radians = (float) Math.toRadians(carriageYaw2);
        Vec3 control = adjustedAnchor.add(
                Math.sin(yaw1Radians) * 0.75 * controlDistance,
                0,
                Math.cos(yaw1Radians) * 0.75 * controlDistance);
        Vec3 control2 = adjustedAnchor2.add(
                -Math.sin(yaw2Radians) * 0.75 * controlDistance,
                0,
                -Math.cos(yaw2Radians) * 0.75 * controlDistance);

        int segmentCount = Math.max(2, (int) Math.round(pair.distance() * 10) + 1);
        return BellowBezier.buildSegments(
                segmentCount, adjustedAnchor, control, control2, adjustedAnchor2, pair.size());
    }

    private static List<BellowInfo> findBellows(CarriageContraptionEntity entity, float partialTicks) {
        Contraption contraption = entity.getContraption();
        if (contraption == null)
            return List.of();

        List<BellowInfo> bellows = new ArrayList<>();
        Vec3 anchor = entity.getAnchorVec();
        Vec3 interpolatedAnchor = entity.getPrevAnchorVec().lerp(anchor, partialTicks);

        for (Map.Entry<BlockPos, StructureBlockInfo> entry : contraption.getBlocks().entrySet()) {
            StructureBlockInfo info = entry.getValue();
            if (!(info.state().getBlock() instanceof BellowBlock))
                continue;

            Vec3 worldPosition = entity.toGlobalVector(entry.getKey().getCenter(), partialTicks)
                    .add(interpolatedAnchor.subtract(anchor));
            Direction facing = info.state().getValue(HorizontalDirectionalBlock.FACING);
            bellows.add(new BellowInfo(worldPosition, facing, BellowBlock.getSize(info.state())));
        }
        return bellows;
    }

    private static BellowPair findClosestPair(List<BellowInfo> first, List<BellowInfo> second) {
        BellowPair closest = null;
        double closestDistanceSqr = Double.MAX_VALUE;

        for (BellowInfo firstBellow : first) {
            for (BellowInfo secondBellow : second) {
                if (firstBellow.facing() != secondBellow.facing().getOpposite())
                    continue;
                if (!firstBellow.size().equals(secondBellow.size()))
                    continue;

                double distanceSqr = firstBellow.position().distanceToSqr(secondBellow.position());
                if (distanceSqr >= closestDistanceSqr)
                    continue;

                closestDistanceSqr = distanceSqr;
                closest = new BellowPair(
                        firstBellow.position(),
                        secondBellow.position(),
                        Math.sqrt(distanceSqr),
                        firstBellow.size());
            }
        }
        return closest;
    }

    private static LerpedFloat getYaw(CarriageBogey bogey) {
        return ((CarriageBogeyAccessor) (Object) bogey).createTrainParts$getYaw();
    }
}
