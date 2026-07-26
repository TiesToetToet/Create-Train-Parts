package com.tiestoettoet.create_train_parts.foundation.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BellowSegment {

	private final Vec3 center;
	private final Vec3 tangent;
	private final float stretch;
	private final BellowSize size;

	public BellowSegment(Vec3 center, Vec3 tangent, float stretch, BellowSize size) {
		this.center = center;
		this.tangent = tangent.normalize();
		this.stretch = stretch;
		this.size = size;
	}

	public Vec3 center() {
		return center;
	}

	public Vec3 tangent() {
		return tangent;
	}

	public float stretch() {
		return stretch;
	}

	public BellowSize size() {
		return size;
	}

	/**
	 * Half-width of the bellows.
	 */
	public double extentX() {
		return Math.abs(tangent.x) * (stretch / 16.0) + size.halfWidth();
	}

	public double extentY() {
		return Math.abs(tangent.y) * (stretch / 16.0) + (size.top() - size.bottom()) / 2.0;
	}

	public double extentZ() {
		return Math.abs(tangent.z) * (stretch / 16.0) + size.halfWidth();
	}
}
