package com.tiestoettoet.create_train_parts.foundation.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BellowSegment {

	private final Vec3 center;
	private final Vec3 tangent;
	private final float length;
	private final BellowSize size;

	public BellowSegment(Vec3 center, Vec3 tangent, float length, BellowSize size) {
		this.center = center;
		this.tangent = tangent.normalize();
		this.length = length;
		this.size = size;
	}

	public Vec3 center() {
		return center;
	}

	public Vec3 tangent() {
		return tangent;
	}

	/** Length of this segment along the curve, in blocks. */
	public float length() {
		return length;
	}

	public BellowSize size() {
		return size;
	}

	/**
	 * Half-width of the bellows.
	 */
	public double extentX() {
		return Math.abs(tangent.x) * (length / 2.0) + size.halfWidth();
	}

	public double extentY() {
		return Math.abs(tangent.y) * (length / 2.0) + (size.top() - size.bottom()) / 2.0;
	}

	public double extentZ() {
		return Math.abs(tangent.z) * (length / 2.0) + size.halfWidth();
	}
}
