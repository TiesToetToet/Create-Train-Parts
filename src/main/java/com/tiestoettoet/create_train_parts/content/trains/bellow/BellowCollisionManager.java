package com.tiestoettoet.create_train_parts.content.trains.bellow;

import com.simibubi.create.content.contraptions.Contraption;

import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

public class BellowCollisionManager {

	private static final WeakHashMap<Contraption,
		List<SegmentCollision>> SEGMENTS = new WeakHashMap<>();

	public static void update(Contraption contraption,
							  List<SegmentCollision> segments) {
		SEGMENTS.put(contraption, segments);
	}

	public static List<SegmentCollision> get(Contraption contraption) {
		return SEGMENTS.getOrDefault(
			contraption,
			Collections.emptyList()
		);
	}

	public static void addSegment(Vec3 postion, Vec3 tangent, float stretch) {
		//
	}

}
