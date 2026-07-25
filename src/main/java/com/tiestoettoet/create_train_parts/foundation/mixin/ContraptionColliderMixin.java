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

        double halfLength = segment.stretch() / 16.0;
        double halfHeight = 1.0;
        double halfWidth = 0.25;
        populate.append(
                localCenter.x,
                localCenter.y,
                localCenter.z,
                axisExtent(localTangent.x, localUp.x, localSide.x, halfLength, halfHeight, halfWidth),
                axisExtent(localTangent.y, localUp.y, localSide.y, halfLength, halfHeight, halfWidth),
                axisExtent(localTangent.z, localUp.z, localSide.z, halfLength, halfHeight, halfWidth));
    }

    private static double axisExtent(double tangent, double up, double side,
            double halfLength, double halfHeight, double halfWidth) {
        return Math.abs(tangent) * halfLength
                + Math.abs(up) * halfHeight
                + Math.abs(side) * halfWidth;
    }
}
