package com.tiestoettoet.create_train_parts.foundation.collision;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BellowSegment {

	private final Vec3 center;
	private final Vec3 tangent;
	private final float stretch;

	public BellowSegment(Vec3 center, Vec3 tangent, float stretch) {
		this.center = center;
		this.tangent = tangent.normalize();
		this.stretch = stretch;
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

	/**
	 * Half-width of the bellows.
	 */
	public double extentX() {
		return Math.abs(tangent.x) * (stretch / 2.0) + 0.25;
	}

	public double extentY() {
		return Math.abs(tangent.y) * (stretch / 2.0) + 1.0;
	}

	public double extentZ() {
		return Math.abs(tangent.z) * (stretch / 2.0) + 0.25;
	}
}
