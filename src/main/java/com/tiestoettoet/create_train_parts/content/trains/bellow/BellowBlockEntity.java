package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.tiestoettoet.create_train_parts.content.trains.bellow.BellowBlock;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.tiestoettoet.create_train_parts.AllBlocks.BELLOW;

public class BellowBlockEntity extends SmartBlockEntity {
    private List<Vec3> segmentPositions = new ArrayList<>();
    private List<Float> segmentYRots = new ArrayList<>();
    private List<Float> segmentXRots = new ArrayList<>();

    public record SegmentData(List<Vec3> positions, List<Float> yRots, List<Float> xRots) {
    }

    public record SegmentResult(SegmentData data, boolean primary) {
    }

    public BellowBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public void tick() {
        super.tick();
        if (getBlockState().hasProperty(BellowBlock.VISIBLE)
                && getBlockState().getValue(BellowBlock.VISIBLE)) {
            return;
        }
        updateSegmentData();
        if (level == null || !level.isClientSide()) {
            return;
        }
        // System.out.println("All AABBs: " + getAllAABBs(segmentPositions,
        // segmentYRots, segmentXRots));
    }

    public void updateSegmentData() {
        if (level == null || !level.isClientSide()) {
            return;
        }

        if (getBlockState().hasProperty(BellowBlock.VISIBLE)
                && getBlockState().getValue(BellowBlock.VISIBLE)) {
            return;
        }

        float partialTicks = AnimationTickHolder.getPartialTicks();
        Vec3 selfPos = Vec3.atCenterOf(worldPosition);
        SegmentData data = computeSegmentData(level, selfPos, partialTicks, true);
        if (data != null) {
            segmentPositions = data.positions();
            segmentYRots = data.yRots();
            segmentXRots = data.xRots();
        }
    }

    public static SegmentData computeSegmentData(Level level, Vec3 selfPos, float partialTicks, boolean log) {
        SegmentResult result = computeSegmentResult(level, selfPos, partialTicks, log);
        return result == null ? null : result.data();
    }

    public static SegmentResult computeSegmentResult(Level level, Vec3 selfPos, float partialTicks, boolean log) {
        List<Vec3> newPositions = new ArrayList<>();
        List<Float> newYRots = new ArrayList<>();
        List<Float> newXRots = new ArrayList<>();

        Collection<Train> trains = Create.RAILWAYS.sided(level).trains.values();
        boolean found = false;
        boolean primary = false;
        int carriagePairsChecked = 0;
        int pairsWithBellows = 0;
        int pairsWithPair = 0;
        int pairsWithSelfMatch = 0;
        int pairsMissingYaw = 0;

        for (Train train : trains) {
            List<Carriage> carriages = train.carriages;
            for (int i = 0; i < carriages.size(); i++) {
                if (i >= carriages.size() - 1) {
                    continue;
                }

                carriagePairsChecked++;

                Carriage carriage = carriages.get(i);
                Carriage carriage2 = carriages.get(i + 1);
                CarriageContraptionEntity entity = carriage.getDimensional(level).entity.get();
                CarriageContraptionEntity entity2 = carriage2.getDimensional(level).entity.get();

                if (entity == null || entity2 == null) {
                    continue;
                }

                CarriageBogey bogey1 = carriage.trailingBogey();
                CarriageBogey bogey2 = carriage2.leadingBogey();
                boolean useAnchorLerp = level.isClientSide();
                List<BellowInfo> bogey1Bellows = findBellows(bogey1.carriage, partialTicks, useAnchorLerp);
                List<BellowInfo> bogey2Bellows = findBellows(bogey2.carriage, partialTicks, useAnchorLerp);
                if (!bogey1Bellows.isEmpty() && !bogey2Bellows.isEmpty()) {
                    pairsWithBellows++;
                }

                BellowPair bellowPair = findClosestBellowPair(bogey1Bellows, bogey2Bellows);
                if (bellowPair == null) {
                    continue;
                }

                pairsWithPair++;

                System.out.println("BellowPair1 pos: " + bogey1.getAnchorPosition() + " bellowpos2: " + bogey2.getAnchorPosition());

                boolean isFirst = isSameBellow(selfPos, bogey1.getAnchorPosition());
                boolean isSecond = isSameBellow(selfPos, bogey2.getAnchorPosition());
                if (!isFirst && !isSecond) {
                    continue;
                }

                pairsWithSelfMatch++;
                primary = isFirst;

                LerpedFloat bogey1yaw = getBogeyYaw(bogey1);
                LerpedFloat bogey2yaw = getBogeyYaw(bogey2);
                if (bogey1yaw == null || bogey2yaw == null) {
                    pairsMissingYaw++;
                    continue;
                }

                CarriageBogey carriageLeadingBogey = carriage.leadingBogey();
                CarriageBogey carriage2TrailingBogey = carriage2.trailingBogey();
                LerpedFloat carriageLeadingYaw = getBogeyYaw(carriageLeadingBogey);
                LerpedFloat carriage2TrailingYaw = getBogeyYaw(carriage2TrailingBogey);
                if (carriageLeadingYaw == null || carriage2TrailingYaw == null) {
                    continue;
                }

                float carriageYaw1 = carriageLeadingBogey == bogey1
                        ? carriageLeadingYaw.getValue(partialTicks)
                        : (carriageLeadingYaw.getValue(partialTicks) + bogey1yaw.getValue(partialTicks)) / 2f;
                float carriageYaw2 = carriage2TrailingBogey == bogey2
                        ? bogey2yaw.getValue(partialTicks)
                        : (bogey2yaw.getValue(partialTicks) + carriage2TrailingYaw.getValue(partialTicks)) / 2f;

                Vec3 anchor = bellowPair.pos1;
                Vec3 anchor2 = bellowPair.pos2;
                float gapFromAnchor = 2 / 16f;
                double couplingDistance = bellowPair.distance;
                int couplingSegments = (int) Math.round(couplingDistance * 10) + 1;
                float controlDistance = (float) (couplingDistance * 0.5);

                float yaw1Radians = (float) Math.toRadians(carriageYaw1);
                float yaw2Radians = (float) Math.toRadians(carriageYaw2);

                Vec3 anchorDirection = anchor2.subtract(anchor).normalize();
                Vec3 adjustedAnchor = anchor.add(anchorDirection.scale(gapFromAnchor))
                        .add(0, -20 / 16f, 0);
                Vec3 adjustedAnchor2 = anchor2.subtract(anchorDirection.scale(gapFromAnchor))
                        .add(0, -20 / 16f, 0);

                Vec3 control = adjustedAnchor.add(
                        Math.sin(yaw1Radians) * 0.75 * controlDistance,
                        0,
                        Math.cos(yaw1Radians) * 0.75 * controlDistance);

                Vec3 control2 = adjustedAnchor2.add(
                        -Math.sin(yaw2Radians) * 0.75 * controlDistance,
                        0,
                        -Math.cos(yaw2Radians) * 0.75 * controlDistance);

                for (int j = 0; j < couplingSegments; j++) {
                    float t = (float) j / (float) (couplingSegments - 1);
                    Vec3 curvePosition = cubicBezier(adjustedAnchor, control, control2, adjustedAnchor2, t);
                    Vec3 tangent = cubicBezierDerivative(adjustedAnchor, control, control2, adjustedAnchor2, t)
                            .normalize();

                    float segmentYRot = AngleHelper.deg(Mth.atan2(tangent.z, tangent.x)) - 90;
                    float segmentXRot = AngleHelper
                            .deg(Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));

                    newPositions.add(curvePosition);
                    newYRots.add(segmentYRot);
                    newXRots.add(segmentXRot);
                }

                found = true;
                break;
            }

            if (found) {
                break;
            }
        }

        // if (log) {
        // if (found) {
        // System.out.println("BellowBE: segments=" + newPositions.size() + " self=" +
        // selfPos);
        // } else {
        // System.out.println("BellowBE: no segments. trains=" + trains.size()
        // + " pairsChecked=" + carriagePairsChecked
        // + " pairsWithBellows=" + pairsWithBellows
        // + " pairsWithPair=" + pairsWithPair
        // + " pairsWithSelfMatch=" + pairsWithSelfMatch
        // + " pairsMissingYaw=" + pairsMissingYaw
        // + " self=" + selfPos);
        // }
        // }

        return found ? new SegmentResult(new SegmentData(newPositions, newYRots, newXRots), primary) : null;
    }

    public List<Vec3> getSegmentPositions() {
        return segmentPositions;
    }

    public List<Float> getSegmentYRots() {
        return segmentYRots;
    }

    public List<Float> getSegmentXRots() {
        return segmentXRots;
    }

    public static List<List<AABB>> getAllAABBs(List<Vec3> segmentPositions, List<Float> segmentYRots,
            List<Float> segmentXRots) {
        int segmentCount = Math.min(segmentPositions.size(), Math.min(segmentYRots.size(), segmentXRots.size()));
        List<List<AABB>> all = new ArrayList<>(segmentCount);

        for (int i = 0; i < segmentCount; i++) {
            Vec3 segmentPos = segmentPositions.get(i);
            float yRot = segmentYRots.get(i);
            float xRot = segmentXRots.get(i);

            List<AABB> segmentBoxes = new ArrayList<>(4);
            segmentBoxes.add(buildBox(segmentPos, yRot, xRot, 8, -3.5, -2, -8, -3.5, 2));
            segmentBoxes.add(buildBox(segmentPos, yRot, xRot, 8, -3.5, 2, 8, 28.5, -2));
            segmentBoxes.add(buildBox(segmentPos, yRot, xRot, 8, 28.5, -2, -8, 28.5, 2));
            segmentBoxes.add(buildBox(segmentPos, yRot, xRot, -8, -3.5, 2, -8, 28.5, -2));
            all.add(segmentBoxes);
        }

        return all;
    }

    private static AABB buildBox(Vec3 segmentPos, float yRotDeg, float xRotDeg,
            double x1, double y1, double z1, double x2, double y2, double z2) {
        double minX = Math.min(x1, x2) / 16.0;
        double minY = Math.min(y1, y2) / 16.0;
        double minZ = Math.min(z1, z2) / 16.0;
        double maxX = Math.max(x1, x2) / 16.0;
        double maxY = Math.max(y1, y2) / 16.0;
        double maxZ = Math.max(z1, z2) / 16.0;

        Vec3[] corners = new Vec3[] {
                new Vec3(minX, minY, minZ),
                new Vec3(minX, minY, maxZ),
                new Vec3(minX, maxY, minZ),
                new Vec3(minX, maxY, maxZ),
                new Vec3(maxX, minY, minZ),
                new Vec3(maxX, minY, maxZ),
                new Vec3(maxX, maxY, minZ),
                new Vec3(maxX, maxY, maxZ)
        };

        double worldMinX = Double.POSITIVE_INFINITY;
        double worldMinY = Double.POSITIVE_INFINITY;
        double worldMinZ = Double.POSITIVE_INFINITY;
        double worldMaxX = Double.NEGATIVE_INFINITY;
        double worldMaxY = Double.NEGATIVE_INFINITY;
        double worldMaxZ = Double.NEGATIVE_INFINITY;

        for (Vec3 corner : corners) {
            Vec3 rotated = rotateLocal(corner, yRotDeg, xRotDeg);
            Vec3 world = rotated.add(segmentPos);

            worldMinX = Math.min(worldMinX, world.x);
            worldMinY = Math.min(worldMinY, world.y);
            worldMinZ = Math.min(worldMinZ, world.z);
            worldMaxX = Math.max(worldMaxX, world.x);
            worldMaxY = Math.max(worldMaxY, world.y);
            worldMaxZ = Math.max(worldMaxZ, world.z);
        }

        return new AABB(worldMinX, worldMinY, worldMinZ, worldMaxX, worldMaxY, worldMaxZ);
    }

    private static Vec3 rotateLocal(Vec3 local, float yRotDeg, float xRotDeg) {
        float yRot = (float) Math.toRadians(-yRotDeg);
        float xRot = (float) Math.toRadians(xRotDeg);
        return local.yRot(yRot).xRot(xRot);
    }

    private static boolean isSameBellow(Vec3 selfPos, Vec3 bellowPos) {
        if (selfPos == null || bellowPos == null) {
            return false;
        }
        return selfPos.distanceToSqr(bellowPos) < 0.25;
    }

    private record BellowInfo(Vec3 pos, BellowBlock block, Direction facing) {
    }

    private record BellowPair(Vec3 pos1, BellowBlock bellow1, Vec3 pos2, BellowBlock bellow2, double distance) {
    }

    private static List<BellowInfo> findBellows(Carriage carriage, float partialTicks, boolean useAnchorLerp) {
        CarriageContraptionEntity entity = carriage.anyAvailableEntity();
        if (entity == null) {
            return new ArrayList<>();
        }

        Contraption contraption = entity.getContraption();
        if (contraption == null) {
            return new ArrayList<>();
        }

        Map<BlockPos, StructureBlockInfo> blocks = contraption.getBlocks();
        List<BellowInfo> bellows = new ArrayList<>();
        Vec3 anchor = entity.getAnchorVec();
        Vec3 prevAnchor = entity.getPrevAnchorVec();
        Vec3 anchorLerp = prevAnchor.lerp(anchor, partialTicks);
        Vec3 anchorOffset = useAnchorLerp ? anchorLerp.subtract(anchor) : Vec3.ZERO;

        for (Map.Entry<BlockPos, StructureBlockInfo> entry : blocks.entrySet()) {
            StructureBlockInfo info = entry.getValue();
            if (info.state().getBlock() == BELLOW.get()) {
                BlockPos localPos = entry.getKey();
                Vec3 worldVec3 = entity.toGlobalVector(localPos.getCenter(), partialTicks)
                    .add(anchorOffset);

                BellowBlock bellowBlock = (BellowBlock) info.state().getBlock();
                Direction facing = info.state()
                        .getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING);
                bellows.add(new BellowInfo(worldVec3, bellowBlock, facing));
            }
        }
        return bellows;
    }

    private static BellowPair findClosestBellowPair(List<BellowInfo> bellows1, List<BellowInfo> bellows2) {
        if (bellows1.isEmpty() || bellows2.isEmpty()) {
            return null;
        }

        double minDistance = Double.MAX_VALUE;
        int closestIndex1 = -1;
        int closestIndex2 = -1;

        for (int i = 0; i < bellows1.size(); i++) {
            BellowInfo info1 = bellows1.get(i);
            for (int j = 0; j < bellows2.size(); j++) {
                BellowInfo info2 = bellows2.get(j);
                if (info1.facing() != info2.facing().getOpposite()) {
                    continue;
                }
                double distance = info1.pos().distanceToSqr(info2.pos());
                if (distance < minDistance) {
                    minDistance = distance;
                    closestIndex1 = i;
                    closestIndex2 = j;
                }
            }
        }

        if (closestIndex1 == -1 || closestIndex2 == -1) {
            return null;
        }

        BellowInfo info1 = bellows1.get(closestIndex1);
        BellowInfo info2 = bellows2.get(closestIndex2);
        return new BellowPair(info1.pos(), info1.block(), info2.pos(), info2.block(), Math.sqrt(minDistance));
    }

    private static LerpedFloat getBogeyYaw(CarriageBogey bogey) {
        for (java.lang.reflect.Field f : bogey.getClass().getDeclaredFields()) {
            try {
                f.setAccessible(true);
                if (f.getName().equals("yaw")) {
                    return (LerpedFloat) f.get(bogey);
                }
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    private static Vec3 cubicBezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float oneMinusT = 1.0f - t;
        float oneMinusTSquared = oneMinusT * oneMinusT;
        float oneMinusTCubed = oneMinusTSquared * oneMinusT;
        float tSquared = t * t;
        float tCubed = tSquared * t;

        return p0.scale(oneMinusTCubed)
                .add(p1.scale(3 * oneMinusTSquared * t))
                .add(p2.scale(3 * oneMinusT * tSquared))
                .add(p3.scale(tCubed));
    }

    private static Vec3 cubicBezierDerivative(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float oneMinusT = 1.0f - t;
        float oneMinusTSquared = oneMinusT * oneMinusT;
        float tSquared = t * t;

        Vec3 term1 = p1.subtract(p0).scale(3 * oneMinusTSquared);
        Vec3 term2 = p2.subtract(p1).scale(6 * oneMinusT * t);
        Vec3 term3 = p3.subtract(p2).scale(3 * tSquared);

        return term1.add(term2).add(term3);
    }
}
