package com.tiestoettoet.create_train_parts.content.trains.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.bogey.AbstractBogeyBlock;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.tiestoettoet.create_train_parts.AllPartialModels;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.tiestoettoet.create_train_parts.content.trains.bellow.BellowBlock;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Map;

import com.simibubi.create.content.contraptions.Contraption;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.block.Block;
import org.joml.Matrix4f;

import static com.tiestoettoet.create_train_parts.AllBlocks.BELLOW;

public class BellowRenderer {

    private static final List<AABB> activeBellows = new ArrayList<>();
    
    // Debug flag to control bounding box rendering
    public static boolean renderBoundingBoxes = true;

    /**
     * Get all active bellow bounding boxes for collision detection or debugging
     */
    public static List<AABB> getActiveBellowCollisions() {
        return new ArrayList<>(activeBellows);
    }

    /**
     * Record to hold both position and block information
     */
    public record BlockInfo(Vec3 worldPos, StructureBlockInfo blockInfo) {
        public BlockState getBlockState() {
            return blockInfo.state();
        }

        public Block getBlock() {
            return blockInfo.state().getBlock();
        }

        public BlockPos getLocalPos() {
            return blockInfo.pos();
        }
    }

    public record BellowInfo(Vec3 pos, BellowBlock block, Direction facing) {
    }

    /**
     * Record to hold a pair of bellows that should be connected
     */
    public record BellowPair(Vec3 pos1, BellowBlock bellow1, Vec3 pos2, BellowBlock bellow2, double distance) {
//        public Vec3 getPos1AsVec3() {
//            return Vec3.atCenterOf(pos1);
//        }
//
//        public Vec3 getPos2AsVec3() {
//            return Vec3.atCenterOf(pos2);
//        }
    }

    public static void renderAll(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        Collection<Train> trains = CreateClient.RAILWAYS.trains.values();
        VertexConsumer vb = buffer.getBuffer(RenderType.solid());
        BlockState air = Blocks.AIR.defaultBlockState();
        float partialTicks = AnimationTickHolder.getPartialTicks();
        Level level = Minecraft.getInstance().level;

        activeBellows.clear();

        for (Train train : trains) {
            List<Carriage> carriages = train.carriages;
            for (int i = 0; i < carriages.size(); i++) {
                if (i >= carriages.size() - 1) {
                    continue;
                }
                Carriage carriage = carriages.get(i);
                CarriageContraptionEntity entity = carriage.getDimensional(level).entity.get();
                Carriage carriage2 = carriages.get(i + 1);
                CarriageContraptionEntity entity2 = carriage2.getDimensional(level).entity.get();

                if (entity == null || entity2 == null)
                    continue;

                CarriageBogey bogey1 = carriage.trailingBogey();
                CarriageBogey bogey2 = carriage2.leadingBogey();
                List<BellowInfo> bogey1Bellows = findBellows(bogey1.carriage);
                List<BellowInfo> bogey2Bellows = findBellows(bogey2.carriage);
//                Vec3 anchor = bogey1.couplingAnchors.getSecond();
//                Vec3 anchor2 = bogey2.couplingAnchors.getFirst();

                // Find the closest bellow pair for rendering
                BellowPair bellowPair = findClosestBellowPair(bogey1Bellows, bogey2Bellows);
                if (bellowPair == null) {
                    continue;
                }

                Vec3 anchor = bellowPair.pos1;
                Vec3 anchor2 = bellowPair.pos2;

//                Vec3 bellow1 =

//                System.out.println("Bogey1 Bellows: " + bogey1Bellows + ", Bogey2 Bellows: " + bogey2Bellows);
//                System.out.println("Found " + (bellowPair == null ? 0 : 1) + " bellow pairs");

//                if (bellowPair != null) {
//                    System.out.println("  Pair: " + bellowPair.pos1 + " <-> " + bellowPair.pos2 + " (distance: "
//                            + String.format("%.2f", bellowPair.distance) + ")");
//                }

                // Skip rendering if no bellow pair found
                if (bellowPair == null) {
                    continue;
                }

                // Now render bellows for the pair instead of using coupling anchors
//                System.out.println("Rendering bellow between " + bellowPair.pos1 + " and " + bellowPair.pos2);

                // TODO: Use bellowPair positions for the bellow rendering logic below

                // System.out.println("Anchor1: " + anchor + ", Anchor2: " + anchor2);

                if (anchor == null || anchor2 == null)
                    continue;
                if (!anchor.closerThan(camera, 64))
                    continue;

                AbstractBogeyBlock<?> bogeyType1 = null;
                AbstractBogeyBlock<?> bogeyType2 = null;
                LerpedFloat bogey1yaw = null;
                LerpedFloat bogey2yaw = null;
                LerpedFloat bogey1pitch = null;
                LerpedFloat bogey2pitch = null;

                for (Field f : bogey1.getClass().getDeclaredFields()) {
                    try {
                        f.setAccessible(true);
                        if (f.getName().equals("type")) {
                            Object value = f.get(bogey1);
                            bogeyType1 = (AbstractBogeyBlock<?>) value;
                            // System.out.println("Type field: " + value);
                        }
                        if (f.getName().equals("yaw")) {
                            Object value = f.get(bogey1);
                            bogey1yaw = (LerpedFloat) value;
                            // System.out.println("Yaw field: " + value);
                        }
                        if (f.getName().equals("pitch")) {
                            Object value = f.get(bogey1);
                            bogey1pitch = (LerpedFloat) value;
                            // System.out.println("Pitch field: " + value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                for (Field f : bogey2.getClass().getDeclaredFields()) {
                    try {
                        f.setAccessible(true);
                        if (f.getName().equals("type")) {
                            Object value = f.get(bogey2);
                            bogeyType2 = (AbstractBogeyBlock<?>) value;
                            // System.out.println("Type field: " + value);
                        }
                        if (f.getName().equals("yaw")) {
                            Object value = f.get(bogey2);
                            bogey2yaw = (LerpedFloat) value;
                            // System.out.println("Yaw field: " + value);
                        }
                        if (f.getName().equals("pitch")) {
                            Object value = f.get(bogey2);
                            bogey2pitch = (LerpedFloat) value;
                            // System.out.println("Pitch field: " + value);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                if (bogeyType1 == null || bogeyType2 == null) {
                    continue;
                }

                if (bogey1yaw == null || bogey2yaw == null) {
                    continue;
                }
                if (bogey1pitch == null || bogey2pitch == null) {
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
                        ? carriageLeadingYaw.getValue()
                        : (carriageLeadingYaw.getValue() + bogey1yaw.getValue()) / 2f;
                float carriageYaw2 = carriage2TrailingBogey == bogey2
                        ? bogey2yaw.getValue()
                        : (bogey2yaw.getValue() + carriage2TrailingYaw.getValue()) / 2f;

                //find bellow pair for this bogey (and the one behind) assuming a carriage can contain max 2 bellows 
                

                // System.out.println("Bogey types: " + bogeyType1 + ", " + bogeyType2);

                int lightCoords = getPackedLightCoords(entity, partialTicks);

                // We no longer need overall rotation since each segment calculates its own

                ms.pushPose();

                {
                    ms.pushPose();
                    ms.translate(anchor.x - camera.x, anchor.y - camera.y, anchor.z - camera.z);
                    // CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air)
                    // .rotateYDegrees(-yRot)
                    // .rotateXDegrees(xRot)
                    // .light(lightCoords)
                    // .renderInto(ms, vb);

                    // System.out.println(bogey1);
                    // System.out.println(bogey1ctp);
                    // System.out.println(bogey1ctp.type);

                    float gapFromAnchor = 2 / 16f;
                    // distance between BellowPair
                    double couplingDistance = bellowPair.distance;

                    // System.out.println("Coupling distance: " + couplingDistance);
                    int couplingSegments = (int) Math.round(couplingDistance * 10) + 1; // 4x more segments (4 * 4 = 16)

                    float controlDistance = (float) (couplingDistance * 0.5); // Adjust control point distance

                    // Convert yaw angle to direction vector and add controlDistance in that
                    // direction

                    System.out.println("Yaw1: " + Math.toDegrees(Math.toRadians(carriageYaw1)) + ", Yaw2: " + Math.toDegrees(Math.toRadians(carriageYaw2)));
                    float yaw1Radians = (float) Math.toRadians(carriageYaw1);
                    float yaw2Radians = (float) Math.toRadians(carriageYaw2);

                        Vec3 anchorDirection = anchor2.subtract(anchor).normalize();
                            Vec3 adjustedAnchor = anchor.add(anchorDirection.scale(gapFromAnchor))
                                .add(0, -20 / 16f, 0);
                            Vec3 adjustedAnchor2 = anchor2.subtract(anchorDirection.scale(gapFromAnchor))
                                .add(0, -20 / 16f, 0);

                    Vec3 control = adjustedAnchor.add(
                            Math.sin(yaw1Radians) * 0.75 * controlDistance, // X component
                            0, // Y component (no vertical offset)
                            Math.cos(yaw1Radians) * 0.75 * controlDistance // Z component
                    );

                    Vec3 control2 = adjustedAnchor2.add(
                            -Math.sin(yaw2Radians) * 0.75 * controlDistance, // X component (negative for incoming direction)
                            0, // Y component (no vertical offset)
                            -Math.cos(yaw2Radians) * 0.75 *  controlDistance // Z component (negative for incoming direction)
                    );


                    // Render segments along the cubic Bezier curve
                    for (int j = 0; j < couplingSegments; j++) {
                        float t = (float) j / (float) (couplingSegments - 1); // Parameter along curve (0 to 1)

                        // Calculate position on the Bezier curve
                        Vec3 curvePosition = cubicBezier(adjustedAnchor, control, control2, adjustedAnchor2, t);

                        // Calculate tangent direction for rotation
                        Vec3 tangent = cubicBezierDerivative(adjustedAnchor, control, control2, adjustedAnchor2, t)
                                .normalize();
                        System.out.println("Bezier with points: " + adjustedAnchor + ", " + control + ", " + control2 + ", " + adjustedAnchor2);

                        // Calculate the distance to the next segment to determine proper scaling
                        float segmentStretch;
                        if (j < couplingSegments - 1) {
                            float nextT = (float) (j + 1) / (float) (couplingSegments - 1);
                            Vec3 nextPosition = cubicBezier(adjustedAnchor, control, control2, adjustedAnchor2, nextT);
                            segmentStretch = (float) (curvePosition.distanceTo(nextPosition) * 8); // Scale
                            // overlap
                        } else {
                            // For the last segment, use the previous segment's stretch to avoid gaps
                            float prevT = (float) (j - 1) / (float) (couplingSegments - 1);
                            Vec3 prevPosition = cubicBezier(adjustedAnchor, control, control2, adjustedAnchor2, prevT);
                            segmentStretch = (float) (prevPosition.distanceTo(curvePosition) * 8);
                        }

                        // Calculate rotation from tangent
                        float segmentYRot = AngleHelper.deg(Mth.atan2(tangent.z, tangent.x)) - 90;
                        float segmentXRot = AngleHelper
                            .deg(Math.atan2(tangent.y, Math.sqrt(tangent.x * tangent.x + tangent.z * tangent.z)));

                        System.out.println("Segment " + j + " y rotation: " + segmentYRot);

                        ms.pushPose();

                        AABB bellowBottom = new AABB(curvePosition.x - anchor.x + 8, curvePosition.y - anchor.y - 2.5, curvePosition.z - anchor.z - 2, curvePosition.x - anchor.x - 8, curvePosition.y - anchor.y + 26.5, curvePosition.z - anchor.z + 2);

                        activeBellows.add(bellowBottom);

                        // Translate to the curve position
                        ms.translate(
                                curvePosition.x - anchor.x,
                                curvePosition.y - anchor.y,
                                curvePosition.z - anchor.z);

                        CachedBuffers.partial(AllPartialModels.BELLOW_CABLE, air)
                                .rotateYDegrees(-segmentYRot)
                                .rotateXDegrees(segmentXRot)
                                .scale(1, 1, segmentStretch)
                                .translate(0, 1, 0)
                                .light(lightCoords)
                                .renderInto(ms, vb);

                        ms.popPose();
                    }
                    ms.popPose();
                }

                // {
                // ms.pushPose();
                // Vec3 translation = position2.subtract(position)
                // .add(anchor2)
                // .subtract(camera);
                // ms.translate(translation.x, translation.y, translation.z);
                // CachedBuffers.partial(AllPartialModels.TRAIN_COUPLING_HEAD, air)
                // .rotateYDegrees(-yRot + 180)
                // .rotateXDegrees(-xRot)
                // .light(lightCoords2)
                // .renderInto(ms, vb);
                // ms.popPose();
                // }

                ms.popPose();
            }
        }

        // Render bounding boxes for debugging
        if (renderBoundingBoxes) {
            renderBoundingBoxes(ms, buffer, camera);
        }
    }

    public static float getRotationFromTangent(Vec3 vec) {
        // Round x and z down to the nearest whole number
        int x1 = (int) Math.floor(vec.x);
        int z1 = (int) Math.floor(vec.z);

        // Neighboring point (example: one block forward on X)
        int x2 = x1 + 1;
        int z2 = z1 + 1;

        // Tangent / direction vector
        double dx = x2 - x1;
        double dz = z2 - z1;

        // Rotation in radians
        double angle = Math.atan2(dz, dx);

        // Convert to degrees
        return (float) Math.toDegrees(angle);
    }

    /**
     * Render all active bellow bounding boxes as wireframes for debugging
     */
    private static void renderBoundingBoxes(PoseStack ms, MultiBufferSource buffer, Vec3 camera) {
        if (activeBellows.isEmpty()) {
            return;
        }

        // Use a simpler approach with filled quads that have transparency
        VertexConsumer renderer = buffer.getBuffer(RenderType.debugFilledBox());
        
        for (AABB boundingBox : activeBellows) {
            renderTransparentBox(ms, renderer, boundingBox, camera, 1.0f, 0.0f, 0.0f, 0.3f); // Semi-transparent red
        }
    }

    /**
     * Render a semi-transparent box for a given AABB
     */
    private static void renderTransparentBox(PoseStack ms, VertexConsumer vertexConsumer, AABB aabb, Vec3 camera, 
                                           float red, float green, float blue, float alpha) {
        ms.pushPose();
        
        // Translate relative to camera
        ms.translate(-camera.x, -camera.y, -camera.z);
        
        Matrix4f matrix = ms.last().pose();
        
        // Define the 8 corners of the box
        float minX = (float) aabb.minX;
        float minY = (float) aabb.minY;
        float minZ = (float) aabb.minZ;
        float maxX = (float) aabb.maxX;
        float maxY = (float) aabb.maxY;
        float maxZ = (float) aabb.maxZ;
        
        // Render the 6 faces of the box
        // Bottom face (Y = minY)
        addQuad(matrix, vertexConsumer, 
                minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ,
                red, green, blue, alpha);
        
        // Top face (Y = maxY)
        addQuad(matrix, vertexConsumer, 
                minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, minX, maxY, minZ,
                red, green, blue, alpha);
        
        // North face (Z = minZ)
        addQuad(matrix, vertexConsumer, 
                minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ, maxX, minY, minZ,
                red, green, blue, alpha);
        
        // South face (Z = maxZ)
        addQuad(matrix, vertexConsumer, 
                maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ, minX, minY, maxZ,
                red, green, blue, alpha);
        
        // West face (X = minX)
        addQuad(matrix, vertexConsumer, 
                minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ, minX, minY, minZ,
                red, green, blue, alpha);
        
        // East face (X = maxX)
        addQuad(matrix, vertexConsumer, 
                maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, maxX, minY, maxZ,
                red, green, blue, alpha);
        
        ms.popPose();
    }

    /**
     * Helper method to add a quad (4 vertices forming a face)
     */
    private static void addQuad(Matrix4f matrix, VertexConsumer vertexConsumer,
                               float x1, float y1, float z1, float x2, float y2, float z2,
                               float x3, float y3, float z3, float x4, float y4, float z4,
                               float red, float green, float blue, float alpha) {
        vertexConsumer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha);
        vertexConsumer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha);
        vertexConsumer.addVertex(matrix, x3, y3, z3).setColor(red, green, blue, alpha);
        vertexConsumer.addVertex(matrix, x4, y4, z4).setColor(red, green, blue, alpha);
    }

    public static int getPackedLightCoords(Entity pEntity, float pPartialTicks) {
        BlockPos blockpos = BlockPos.containing(pEntity.getLightProbePosition(pPartialTicks));
        return LightTexture.pack(getBlockLightLevel(pEntity, blockpos), getSkyLightLevel(pEntity, blockpos));
    }

    protected static int getSkyLightLevel(Entity pEntity, BlockPos pPos) {
        return pEntity.level().getBrightness(LightLayer.SKY, pPos);
    }

    protected static int getBlockLightLevel(Entity pEntity, BlockPos pPos) {
        return pEntity.isOnFire() ? 15 : pEntity.level().getBrightness(LightLayer.BLOCK, pPos);
    }

    /**
     * Calculate a point on a cubic Bezier curve given the parameter t (0 to 1)
     * P(t) = (1-t)³P0 + 3(1-t)²tP1 + 3(1-t)t²P2 + t³P3
     */
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

    /**
     * Calculate the derivative (tangent) of a cubic Bezier curve at parameter t
     * P'(t) = 3(1-t)²(P1-P0) + 6(1-t)t(P2-P1) + 3t²(P3-P2)
     */
    private static Vec3 cubicBezierDerivative(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float oneMinusT = 1.0f - t;
        float oneMinusTSquared = oneMinusT * oneMinusT;
        float tSquared = t * t;

        Vec3 term1 = p1.subtract(p0).scale(3 * oneMinusTSquared);
        Vec3 term2 = p2.subtract(p1).scale(6 * oneMinusT * t);
        Vec3 term3 = p3.subtract(p2).scale(3 * tSquared);

        return term1.add(term2).add(term3);
    }

//    private static Vec3 getDisplacement(Carriage carriage) {
//        CarriageContraptionEntity entity = carriage.anyAvailableEntity();
//        if (entity == null)
//            return Vec3.ZERO;
//
//        Contraption contraption = entity.getContraption();
//        if (contraption == null)
//            return Vec3.ZERO;
//
//        Couple<CarriageBogey> bogeys = carriage.bogeys();
//        CarriageBogey leadingBogey = bogeys.getFirst();
//
//
//
//        return null;
//
//    }

//    private static List<Map<BlockPos, BellowBlock>> findBellows(Carriage carriage) {
    private static List<BellowInfo> findBellows(Carriage carriage) {
        CarriageContraptionEntity entity = carriage.anyAvailableEntity();
        if (entity == null)
            return new ArrayList<>();

        Contraption contraption = entity.getContraption();
        if (contraption == null)
            return new ArrayList<>();

        Map<BlockPos, StructureBlockInfo> blocks = contraption.getBlocks();
        List<BellowInfo> bellows = new ArrayList<>();

        for (Map.Entry<BlockPos, StructureBlockInfo> entry : blocks.entrySet()) {
            StructureBlockInfo info = entry.getValue();
            if (info.state().getBlock() == BELLOW.get()) {
                BlockPos localPos = entry.getKey(); // This is local position in contraption

                // Convert local position to world position
                Vec3 worldVec3 = entity.toGlobalVector(localPos.getCenter(), AnimationTickHolder.getPartialTicks());
                BlockPos worldPos = BlockPos.containing(worldVec3);

                // System.out.println("Found bellow at local " + localPos + " -> world " +
                // worldPos);
                BellowBlock bellowBlock = (BellowBlock) info.state().getBlock();
                Direction facing = info.state().getValue(HorizontalDirectionalBlock.FACING);
                bellows.add(new BellowInfo(worldVec3, bellowBlock, facing));
            }
        }
        return bellows;
    }

    /**
     * Find the closest bellow pairs between two sets of bellows
     * This uses a greedy approach to pair up bellows by shortest distance
     */
    private static BellowPair findClosestBellowPair(List<BellowInfo> bellows1,
            List<BellowInfo> bellows2) {
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
                if (!areFacingEachOther(info1.facing(), info2.facing())) {
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

    private static boolean areFacingEachOther(Direction facing1, Direction facing2) {
        return facing1 == facing2.getOpposite();
    }

    private static LerpedFloat getBogeyYaw(CarriageBogey bogey) {
        for (Field f : bogey.getClass().getDeclaredFields()) {
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

}
