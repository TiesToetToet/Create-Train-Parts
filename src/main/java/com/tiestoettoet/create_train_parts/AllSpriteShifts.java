package com.tiestoettoet.create_train_parts;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;

import net.createmod.catnip.render.SpriteShiftEntry;
import net.createmod.catnip.render.SpriteShifter;

public class AllSpriteShifts {
    public static final CTSpriteShiftEntry TRAIN_STEP_TRAIN = omni("railway_casing"),
            TRAIN_STEP_SIDE = omni("railway_casing_side"),
            TRAIN_STEP_BRASS = omni("brass_casing"),
            TRAIN_STEP_COPPER = omni("copper_casing"),
            TRAIN_STEP_ANDESITE = omni("andesite_casing");

	public static final SpriteShiftEntry ARM_COLOURS = get("block/crossing/arm_colours", "block/crossing/arm_colours");

    private static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }

	private static SpriteShiftEntry get(String originalLocation, String targetLocation) {
		return SpriteShifter.get(CreateTrainParts.asResource(originalLocation), CreateTrainParts.asResource(targetLocation));
	}

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, Create.asResource("block/" + blockTextureName),
                Create.asResource("block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }
}
