package com.tiestoettoet.create_train_parts.foundation.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowCollisionGeometry;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSegment;
import com.tiestoettoet.create_train_parts.foundation.collision.BellowSize;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(value = ContraptionCollider.class, remap = false)
public abstract class ContraptionColliderMixin {

	/**
	 * Adds the flexible connection to Create's simplified collider list. The list
	 * returned by Contraption is cached, so it must be copied rather than
	 * mutated with geometry that changes every tick.
	 */
	@ModifyExpressionValue(
		method = "collideEntities",
		at = @At(
			value = "INVOKE",
			target = "Lcom/simibubi/create/content/contraptions/Contraption;getSimplifiedEntityColliders()Ljava/util/Optional;",
			remap = false),
		remap = false)
	private static Optional<List<AABB>> createTrainParts$addBellowColliders(
		Optional<List<AABB>> original, AbstractContraptionEntity contraptionEntity) {
		if (!(contraptionEntity instanceof CarriageContraptionEntity carriageEntity))
			return original;

		List<BellowSegment> segments = BellowCollisionGeometry.buildCollisionSegments(carriageEntity);
		if (segments.isEmpty())
			return original;

		List<AABB> combined = original.map(ArrayList::new).orElseGet(ArrayList::new);

		for (BellowSegment segment : segments)
			appendSegment(combined, contraptionEntity, segment);

		return Optional.of(combined);
	}

	private static void appendSegment(List<AABB> colliders,
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
		appendOrientedBox(colliders,
			localCenter.add(localUp.scale(frameBottom - halfThickness)),
			localTangent, localUp, localSide,
			halfLength, halfThickness, frameHalfWidth + 2 * halfThickness);

		// The top member used to be one-way to stop jitter when jumping into
		// it from inside, but that jitter came from the boxes being too short
		// (see BellowCollisionGeometry.buildCollisionSegments), and leaving it
		// out let heads poke through and entities on the roof drop inside.
		appendOrientedBox(colliders,
			localCenter.add(localUp.scale(frameTop + halfThickness)),
			localTangent, localUp, localSide,
			halfLength, halfThickness, frameHalfWidth + 2 * halfThickness);

		// Left and right walls, again offset so their inner faces line up with
		// the rendered frame. Colliders are axis aligned in the carriage's
		// frame, so a wall running at an angle to it, as it does through a
		// turn, gets a box that reaches into the passage by halfLength times
		// the sine of that angle. Pushing the wall out by that much keeps the
		// full width walkable all the way round the corner.
		Vec3 wallNormalAxis = Math.abs(localSide.x) >= Math.abs(localSide.z)
			? new Vec3(1, 0, 0)
			: new Vec3(0, 0, 1);
		double sideAlongNormal = Math.max(Math.abs(localSide.dot(wallNormalAxis)), 1.0e-3);
		double wallOffset = (frameHalfWidth + halfLength * Math.abs(localTangent.dot(wallNormalAxis)))
			/ sideAlongNormal + halfThickness;
		Vec3 verticalOffsetVec = localUp.scale(verticalCenter);
		appendOrientedBox(colliders,
			localCenter.add(verticalOffsetVec).add(localSide.scale(wallOffset)),
			localTangent, localUp, localSide,
			halfLength, verticalHalfHeight, halfThickness);
		appendOrientedBox(colliders,
			localCenter.add(verticalOffsetVec).subtract(localSide.scale(wallOffset)),
			localTangent, localUp, localSide,
			halfLength, verticalHalfHeight, halfThickness);
	}

	private static void appendOrientedBox(List<AABB> colliders, Vec3 center,
										  Vec3 tangent, Vec3 up, Vec3 side,
										  double halfLength, double halfHeight, double halfWidth) {
		double halfX = axisExtent(tangent.x, up.x, side.x, halfLength, halfHeight, halfWidth);
		double halfY = axisExtent(tangent.y, up.y, side.y, halfLength, halfHeight, halfWidth);
		double halfZ = axisExtent(tangent.z, up.z, side.z, halfLength, halfHeight, halfWidth);
		colliders.add(AABB.ofSize(center, halfX * 2.0, halfY * 2.0, halfZ * 2.0));
	}

	private static double axisExtent(double tangent, double up, double side,
									 double halfLength, double halfHeight, double halfWidth) {
		return Math.abs(tangent) * halfLength
			+ Math.abs(up) * halfHeight
			+ Math.abs(side) * halfWidth;
	}
}
