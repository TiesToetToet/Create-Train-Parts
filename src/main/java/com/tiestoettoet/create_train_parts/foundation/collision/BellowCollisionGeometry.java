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

    /** Short pieces keep the drawn curve smooth. */
    private static final double RENDER_SEGMENT_LENGTH = 0.1;
    /**
     * Longer than an entity sinks into a member in one tick, while still
     * short enough that boxes on a curve stay close to the drawn frame.
     */
    private static final double COLLISION_SEGMENT_LENGTH = 0.75;

    private BellowCollisionGeometry() {
    }

    private record BellowInfo(Vec3 position, Vec3 outward, Vec3 up, Direction facing, BellowSize size) {
    }

    private record BellowPair(Vec3 first, Vec3 firstOutward, Vec3 firstUp, Vec3 second, Vec3 secondOutward,
            Vec3 secondUp, double distance, BellowSize size) {
    }

    /**
     * Builds the connection from this carriage to the next carriage. A
     * connection is owned only by its first carriage so it is never added twice.
     */
    public static List<BellowSegment> buildSegments(CarriageContraptionEntity entity, float partialTicks) {
        return buildSegments(entity, partialTicks, RENDER_SEGMENT_LENGTH);
    }

    /**
     * The same connection split into far fewer, longer pieces for collision.
     * Create resolves each overlap along whichever axis needs the smallest
     * push. Against a box shorter than an entity's penetration that axis runs
     * along the passage, and every following box repeats the push, carrying
     * the entity along to the far end of the bellow.
     */
    public static List<BellowSegment> buildCollisionSegments(CarriageContraptionEntity entity) {
        return buildSegments(entity, 1.0f, COLLISION_SEGMENT_LENGTH);
    }

    private static List<BellowSegment> buildSegments(CarriageContraptionEntity entity, float partialTicks,
            double segmentLength) {
        Carriage carriage = entity.getCarriage();
        if (carriage == null || carriage.train == null)
            return List.of();

        int carriageIndex = carriage.train.carriages.indexOf(carriage);
        if (carriageIndex < 0 || carriageIndex >= carriage.train.carriages.size() - 1)
            return List.of();

        Level level = entity.level();
        Carriage nextCarriage = carriage.train.carriages.get(carriageIndex + 1);
        CarriageContraptionEntity nextEntity = nextCarriage.getDimensional(level).entity.get();
        if (!isPositioned(entity) || !isPositioned(nextEntity))
            return List.of();

        BellowPair pair = findClosestPair(
                findBellows(entity, partialTicks),
                findBellows(nextEntity, partialTicks));
        if (pair == null)
            return List.of();

        return buildSegments(pair.first(), pair.firstOutward(), pair.firstUp(),
                pair.second(), pair.secondOutward(), pair.secondUp(), pair.size(), segmentLength);
    }

    /**
     * A client carriage has no valid position or rotation until its first
     * alignment, and gets reset again once removed, which is why Create skips
     * rendering it then too. Connecting to it in that state stretches the
     * bellow off towards wherever the entity happens to be.
     */
    private static boolean isPositioned(CarriageContraptionEntity entity) {
        if (entity == null || !entity.isAlive())
            return false;
        if (!entity.level().isClientSide())
            return true;
        return entity.validForRender && !entity.firstPositionUpdate;
    }

    /**
     * Builds the connection between two bellows that face each other. Both
     * frames point straight at each other, so leaving along their own facing
     * keeps the curve centred on them however they are angled. Anything derived
     * from the chord between the two blocks drifts sideways instead.
     *
     * @param first          centre of the first bellow block
     * @param firstOutward   unit vector the first frame faces
     * @param second         centre of the second bellow block
     * @param secondOutward  unit vector the second frame faces
     */
    public static List<BellowSegment> buildSegments(Vec3 first, Vec3 firstOutward, Vec3 second, Vec3 secondOutward,
            BellowSize size) {
        Vec3 up = new Vec3(0, 1, 0);
        return buildSegments(first, firstOutward, up, second, secondOutward, up, size, RENDER_SEGMENT_LENGTH);
    }

    /**
     * @param firstUp       unit vector pointing up in the first carriage's
     *                      frame, so the curve drops below the frame along the
     *                      carriage's own vertical and stays attached to it on
     *                      slopes
     * @param secondUp      the same for the second carriage
     * @param segmentLength rough length of each piece along the curve
     */
    private static List<BellowSegment> buildSegments(Vec3 first, Vec3 firstOutward, Vec3 firstUp,
            Vec3 second, Vec3 secondOutward, Vec3 secondUp, BellowSize size, double segmentLength) {
        double distance = first.distanceTo(second);
        float gapFromAnchor = 5 / 16f;
        Vec3 adjustedAnchor = first.add(firstOutward.scale(gapFromAnchor))
                .add(firstUp.scale(-20 / 16f));
        Vec3 adjustedAnchor2 = second.add(secondOutward.scale(gapFromAnchor))
                .add(secondUp.scale(-20 / 16f));

        double controlDistance = distance * 0.375;
        Vec3 control = adjustedAnchor.add(firstOutward.scale(controlDistance));
        Vec3 control2 = adjustedAnchor2.add(secondOutward.scale(controlDistance));

        int segmentCount = Math.max(2, (int) Math.round(distance / segmentLength) + 1);
        return BellowBezier.buildSegments(
                segmentCount, adjustedAnchor, control, control2, adjustedAnchor2, size);
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
            Vec3 up = entity.toGlobalVector(localCenter.add(0, 1, 0), partialTicks)
                    .add(anchorCorrection)
                    .subtract(worldPosition)
                    .normalize();

            bellows.add(new BellowInfo(worldPosition, outward, up, facing, BellowBlock.getSize(info.state())));
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
                        firstBellow.up(),
                        secondBellow.position(),
                        secondBellow.outward(),
                        secondBellow.up(),
                        Math.sqrt(distanceSqr),
                        firstBellow.size());
            }
        }
        return closest;
    }
}
