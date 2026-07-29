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

	/**
	 * Splits the curve into segments. Straight runs meet exactly end to end,
	 * because overlapping segments would draw the same surfaces twice with
	 * different texture offsets and z-fight. Where the curve bends, each segment
	 * grows just far enough to close the wedge its neighbour would leave open.
	 */
	public static List<BellowSegment> buildSegments(int couplingSegments, Vec3 adjustedAnchor, Vec3 control, Vec3 control2, Vec3 adjustedAnchor2, BellowSize size) {
		int sampleCount = Math.max(2, couplingSegments);
		List<Vec3> centers = new ArrayList<>(sampleCount);
		List<Vec3> tangents = new ArrayList<>(sampleCount);
		List<Double> lengths = new ArrayList<>(sampleCount);

		Vec3 start = cubicBezier(adjustedAnchor, control, control2, adjustedAnchor2, 0);
		for (int j = 1; j < sampleCount; j++) {
			float t = (float) j / (float) (sampleCount - 1); // Parameter along curve (0 to 1)
			Vec3 end = cubicBezier(adjustedAnchor, control, control2, adjustedAnchor2, t);

			Vec3 delta = end.subtract(start);
			double length = delta.length();
			if (length < 1.0e-6)
				continue;

			centers.add(start.add(end).scale(0.5));
			tangents.add(delta.scale(1 / length));
			lengths.add(length);
			start = end;
		}

		double reach = size.frameReach();
		List<BellowSegment> segments = new ArrayList<>(centers.size());
		for (int j = 0; j < centers.size(); j++) {
			Vec3 tangent = tangents.get(j);
			double startOverlap = j == 0 ? 0 : overlap(reach, tangents.get(j - 1), tangent);
			double endOverlap = j == centers.size() - 1 ? 0 : overlap(reach, tangent, tangents.get(j + 1));

			segments.add(new BellowSegment(
				centers.get(j).add(tangent.scale((endOverlap - startOverlap) / 2)),
				tangent,
				(float) (lengths.get(j) + startOverlap + endOverlap),
				size
			));
		}
		return segments;
	}

	/** How far a segment must reach past a joint to keep it closed. */
	private static double overlap(double reach, Vec3 tangent, Vec3 nextTangent) {
		double angle = Math.acos(Mth.clamp(tangent.dot(nextTangent), -1, 1));
		return reach * Math.tan(angle / 2);
	}
}
