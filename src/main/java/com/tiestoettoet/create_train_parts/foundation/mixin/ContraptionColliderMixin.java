package com.tiestoettoet.create_train_parts.foundation.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.collision.CollisionList;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowCollisionGeometry;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSegment;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSize;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

@Mixin(ContraptionCollider.class)
public abstract class ContraptionColliderMixin {

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
            CollisionList original, AbstractContraptionEntity contraptionEntity,
            @Local Entity collidingEntity) {
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
            appendSegment(populate, contraptionEntity, collidingEntity, segment);
        return combined;
    }

    private static void appendSegment(CollisionList.Populate populate,
            AbstractContraptionEntity contraptionEntity, Entity collidingEntity,
            BellowSegment segment) {
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

        BellowSize size = segment.size();
        double frameHalfWidth = size.halfWidth();
        double frameBottom = size.bottom();
        double frameTop = size.top();
        double halfThickness = BellowSize.MEMBER_HALF_THICKNESS;
        double verticalOffset = size.verticalOffset();
        localCenter = localCenter.add(localUp.scale(verticalOffset));

        double halfLength = segment.length() / 2.0;
        double verticalCenter = (frameBottom + frameTop) / 2.0;
        double verticalHalfHeight = (frameTop - frameBottom) / 2.0;

        // Every member is a slab sitting completely outside the walkable
        // interior, so a collision can only ever push an entity away from the
        // passage instead of sideways through it.
        appendOrientedBox(populate,
            localCenter.add(localUp.scale(frameBottom - halfThickness)),
            localTangent, localUp, localSide,
            halfLength, halfThickness, frameHalfWidth + 2 * halfThickness);
        // Create treats every vertical collider contact as a floor contact. If
        // the top member is present while a player jumps into it from below,
        // that ceiling contact repeatedly toggles onGround and causes severe
        // correction jitter. Make this member one-way: it remains solid when
        // approached from above, but is omitted for an entity below it.
        double renderedTopY = worldCenter.y + verticalOffset + frameTop;
        if (collidingEntity.getBoundingBox().minY >= renderedTopY - halfThickness) {
            appendOrientedBox(populate,
                localCenter.add(localUp.scale(frameTop + halfThickness)),
                localTangent, localUp, localSide,
                halfLength, halfThickness, frameHalfWidth + 2 * halfThickness);
        }

        // Left and right walls, again offset so their inner faces line up with
        // the rendered frame.
        Vec3 verticalOffsetVec = localUp.scale(verticalCenter);
        appendOrientedBox(populate,
            localCenter.add(verticalOffsetVec).add(localSide.scale(frameHalfWidth + halfThickness)),
            localTangent, localUp, localSide,
            halfLength, verticalHalfHeight, halfThickness);
        appendOrientedBox(populate,
            localCenter.add(verticalOffsetVec).subtract(localSide.scale(frameHalfWidth + halfThickness)),
            localTangent, localUp, localSide,
            halfLength, verticalHalfHeight, halfThickness);
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
