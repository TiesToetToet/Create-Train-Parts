package com.tiestoettoet.create_train_parts.foundation.collision;

import com.tiestoettoet.create_train_parts.AllPartialModels;

import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BellowBezier {
	/**
	 * Calculate a point on a cubic Bezier curve given the parameter t (0 to 1)
	 * P(t) = (1-t)³P0 + 3(1-t)²tP1 + 3(1-t)t²P2 + t³P3
	 */
	public static Vec3 cubicBezier(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
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
	public static Vec3 cubicBezierDerivative(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
		float oneMinusT = 1.0f - t;
		float oneMinusTSquared = oneMinusT * oneMinusT;
		float tSquared = t * t;

		Vec3 term1 = p1.subtract(p0).scale(3 * oneMinusTSquared);
		Vec3 term2 = p2.subtract(p1).scale(6 * oneMinusT * t);
		Vec3 term3 = p3.subtract(p2).scale(3 * tSquared);

		return term1.add(term2).add(term3);
	}

	public static List<BellowSegment> buildSegments(int couplingSegments, Vec3 adjustedAnchor, Vec3 control, Vec3 control2, Vec3 adjustedAnchor2) {
		List<BellowSegment> segments = new ArrayList<>(couplingSegments);
		for (int j = 0; j < couplingSegments; j++) {
			float t = (float) j / (float) (couplingSegments - 1); // Parameter along curve (0 to 1)

			// Calculate position on the Bézier curve
			Vec3 curvePosition = cubicBezier(adjustedAnchor, control, control2, adjustedAnchor2, t);

			// Calculate tangent direction for rotation
			Vec3 tangent = cubicBezierDerivative(adjustedAnchor, control, control2, adjustedAnchor2, t)
				.normalize();

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

			segments.add(new BellowSegment(
				curvePosition,
				tangent,
				segmentStretch
			));
		}
		return segments;
	}
}
