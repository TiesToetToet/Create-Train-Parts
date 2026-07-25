package com.tiestoettoet.create_train_parts.foundation.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.collision.CollisionList;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowCollisionGeometry;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSegment;

import net.minecraft.world.phys.Vec3;

@Mixin(ContraptionCollider.class)
public abstract class ContraptionColliderMixin {

    private static final double FRAME_HALF_WIDTH = 8 / 16.0;
    private static final double FRAME_BOTTOM = -3.5 / 16.0;
    private static final double FRAME_TOP = 28.5 / 16.0;
    private static final double FRAME_HALF_THICKNESS = 1 / 16.0;
    private static final double RENDER_Y_OFFSET = 1.0;

    /**
     * Adds the flexible connection to Create's dense collider list. The list
     * returned by Contraption is cached, so it must be copied rather than
     * mutated with geometry that changes every tick.
     */
    @ModifyExpressionValue(
            method = "collideEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/contraptions/Contraption;getSimplifiedEntityColliders()Lcom/simibubi/create/foundation/collision/CollisionList;"))
    private static CollisionList createTrainParts$addBellowColliders(
            CollisionList original, AbstractContraptionEntity contraptionEntity) {
        if (!(contraptionEntity instanceof CarriageContraptionEntity carriageEntity))
            return original;

        List<BellowSegment> segments = BellowCollisionGeometry.buildSegments(carriageEntity, 1.0f);
        if (segments.isEmpty())
            return original;

        CollisionList combined = new CollisionList();
        CollisionList.Populate populate = new CollisionList.Populate(combined);
        if (original != null) {
            for (int i = 0; i < original.size; i++)
                populate.appendFrom(original, i);
        }

        for (BellowSegment segment : segments)
            appendSegment(populate, contraptionEntity, segment);
        return combined;
    }

    private static void appendSegment(CollisionList.Populate populate,
            AbstractContraptionEntity contraptionEntity, BellowSegment segment) {
        Vec3 worldCenter = segment.center();
        Vec3 localCenter = ContraptionCollider.worldToLocalPos(worldCenter, contraptionEntity);
        Vec3 localTangent = ContraptionCollider.worldToLocalPos(
                worldCenter.add(segment.tangent()), contraptionEntity)
                .subtract(localCenter)
                .normalize();
        Vec3 localUp = ContraptionCollider.worldToLocalPos(
                worldCenter.add(0, 1, 0), contraptionEntity)
                .subtract(localCenter)
                .normalize();
        Vec3 localSide = localTangent.cross(localUp);
        if (localSide.lengthSqr() < 1.0e-6)
            localSide = localTangent.cross(new Vec3(1, 0, 0));
        localSide = localSide.normalize();
        localUp = localSide.cross(localTangent).normalize();
        localCenter = localCenter.add(localUp.scale(RENDER_Y_OFFSET));

        double halfLength = segment.stretch() / 16.0;
        double verticalCenter = (FRAME_BOTTOM + FRAME_TOP) / 2.0;
        double verticalHalfHeight = (FRAME_TOP - FRAME_BOTTOM) / 2.0;

        // Bottom and top horizontal members of the bellow frame.
        appendOrientedBox(populate,
            localCenter.add(localUp.scale(FRAME_BOTTOM)),
            localTangent, localUp, localSide,
            halfLength, FRAME_HALF_THICKNESS, FRAME_HALF_WIDTH);
        appendOrientedBox(populate,
            localCenter.add(localUp.scale(FRAME_TOP)),
            localTangent, localUp, localSide,
            halfLength, FRAME_HALF_THICKNESS, FRAME_HALF_WIDTH);

        // Left and right vertical members. Their centers are raised to match
        // the actual rendered model instead of being centred on the curve.
        Vec3 verticalOffset = localUp.scale(verticalCenter);
        appendOrientedBox(populate,
            localCenter.add(verticalOffset).add(localSide.scale(FRAME_HALF_WIDTH)),
            localTangent, localUp, localSide,
            halfLength, verticalHalfHeight, FRAME_HALF_THICKNESS);
        appendOrientedBox(populate,
            localCenter.add(verticalOffset).subtract(localSide.scale(FRAME_HALF_WIDTH)),
            localTangent, localUp, localSide,
            halfLength, verticalHalfHeight, FRAME_HALF_THICKNESS);
        }

        private static void appendOrientedBox(CollisionList.Populate populate, Vec3 center,
            Vec3 tangent, Vec3 up, Vec3 side,
            double halfLength, double halfHeight, double halfWidth) {
        populate.append(
            center.x,
            center.y,
            center.z,
            axisExtent(tangent.x, up.x, side.x, halfLength, halfHeight, halfWidth),
            axisExtent(tangent.y, up.y, side.y, halfLength, halfHeight, halfWidth),
            axisExtent(tangent.z, up.z, side.z, halfLength, halfHeight, halfWidth));
    }

    private static double axisExtent(double tangent, double up, double side,
            double halfLength, double halfHeight, double halfWidth) {
        return Math.abs(tangent) * halfLength
                + Math.abs(up) * halfHeight
                + Math.abs(side) * halfWidth;
    }
}
