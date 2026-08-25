package com.tiestoettoet.create_train_parts.foundation.collision;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.tiestoettoet.create_train_parts.content.trains.bellow.BellowBlock;

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

    private record BellowInfo(Vec3 position, Vec3 outward, Direction facing, BellowSize size) {
    }

    private record BellowPair(Vec3 first, Vec3 firstOutward, Vec3 second, Vec3 secondOutward, double distance,
            BellowSize size) {
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

        // Both frames point straight at each other, so leaving along their own
        // facing keeps the curve centred on them however the carriages sit.
        // Anything derived from the chord between the two blocks drifts
        // sideways as soon as the connection is angled.
        float gapFromAnchor = 5 / 16f;
        Vec3 adjustedAnchor = pair.first()
                .add(pair.firstOutward().scale(gapFromAnchor))
                .add(0, -20 / 16f, 0);
        Vec3 adjustedAnchor2 = pair.second()
                .add(pair.secondOutward().scale(gapFromAnchor))
                .add(0, -20 / 16f, 0);

        double controlDistance = pair.distance() * 0.375;
        Vec3 control = adjustedAnchor.add(pair.firstOutward().scale(controlDistance));
        Vec3 control2 = adjustedAnchor2.add(pair.secondOutward().scale(controlDistance));

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
        Vec3 anchorCorrection = interpolatedAnchor.subtract(anchor);

        for (Map.Entry<BlockPos, StructureBlockInfo> entry : contraption.getBlocks().entrySet()) {
            StructureBlockInfo info = entry.getValue();
            if (!(info.state().getBlock() instanceof BellowBlock))
                continue;

            Direction facing = info.state().getValue(HorizontalDirectionalBlock.FACING);
            Vec3 localCenter = entry.getKey().getCenter();
            Vec3 worldPosition = entity.toGlobalVector(localCenter, partialTicks)
                    .add(anchorCorrection);

            // The frame sits on the face opposite the block's facing, so that
            // is the direction the connection leaves from.
            Vec3 localOutward = Vec3.atLowerCornerOf(facing.getOpposite().getNormal());
            Vec3 outward = entity.toGlobalVector(localCenter.add(localOutward), partialTicks)
                    .add(anchorCorrection)
                    .subtract(worldPosition)
                    .normalize();

            bellows.add(new BellowInfo(worldPosition, outward, facing, BellowBlock.getSize(info.state())));
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
                        firstBellow.outward(),
                        secondBellow.position(),
                        secondBellow.outward(),
                        Math.sqrt(distanceSqr),
                        firstBellow.size());
            }
        }
        return closest;
    }
}
