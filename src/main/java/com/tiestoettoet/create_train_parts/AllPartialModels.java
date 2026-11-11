package com.tiestoettoet.create_train_parts;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.Create;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.kinetics.gantry.GantryShaftBlock;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.content.logistics.box.PackageStyles.PackageStyle;

import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
public class AllPartialModels {

//    public static final PartialModel
//
//        STEPS = new
//
//
//    ;

//    public static final Map<ResourceLocation, Couple<PartialModel>> TRAIN_STEP = new HashMap<>();

    public static final Map<ResourceLocation, PartialModel> TRAIN_STEP = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> TRAIN_STEP_SLIDE = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> TRAIN_STEP_PIVOT = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> TRAIN_STEP_MOVE = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> TRAIN_STEP_FLAP= new HashMap<>();

    public static final Map<ResourceLocation, PartialModel> TRAIN_SLIDE = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> TRAIN_SLIDE_BOTTOM = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> TRAIN_SLIDE_CENTRE = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> TRAIN_SLIDE_TOP = new HashMap<>();

    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW_UP = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW_RIGHT = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW_DOWN = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW_LEFT = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW_BACK = new HashMap<>();
    public static final PartialModel ARM = block("crossing/arm");
    public static final PartialModel ARM_1 = block("crossing/arm_1");
    public static final PartialModel ARM_2 = block("crossing/arm_2");
    public static final PartialModel ARM_FLIPPED = block("crossing/arm_flipped");
    public static final PartialModel ARM_FLIPPED_1 = block("crossing/arm_flipped_1");
    public static final PartialModel ARM_FLIPPED_2 = block("crossing/arm_flipped_2");
    public static final PartialModel ARM_EXTENDER_1 = block("crossing/arm_extender_1");
    public static final PartialModel ARM_EXTENDER_2 = block("crossing/arm_extender_2");
    public static final PartialModel ARM_EXTENDER_FLIPPED_1 = block("crossing/arm_extender_flipped_1");
    public static final PartialModel ARM_EXTENDER_FLIPPED_2 = block("crossing/arm_extender_flipped_2");
    public static final PartialModel CROSSING_LAMP = block("crossing/lamp");
    public static final PartialModel LIGHTS = block("crossing/lights");
    public static final PartialModel LIGHTS_FLIPPED = block("crossing/lights_flipped");

	public static final PartialModel BELL_HOLDER = block("crossing/bell_holder");
	public static final PartialModel BELL = block("crossing/bell");
    public static final PartialModel BELLOW_CABLE = block("bellow/bellow_cable");
    public static final PartialModel BELLOW_END_BOTTOM = block("bellow/bellow_end_bottom");
    public static final PartialModel BELLOW_END_TOP = block("bellow/bellow_end_top");
    public static final PartialModel BELLOW_END_LEFT = block("bellow/bellow_end_left");
    public static final PartialModel BELLOW_END_RIGHT = block("bellow/bellow_end_right");
	public static final PartialModel BELLOW_ARROW = block("bellow/arrow");

    static {
        putTrainStep("train_step_andesite");
        putTrainStep("train_step_brass");
        putTrainStep("train_step_copper");
        putTrainStep("train_step_train");
        putTrainSlide("train_slide_andesite");
        putTrainSlide("train_slide_brass");
        putTrainSlide("train_slide_copper");
        putTrainSlide("train_slide_train");
        putSlidingWindow("glass");
        putSlidingWindow("andesite");
        putSlidingWindow("brass");
        putSlidingWindow("copper");
        putSlidingWindow("train");
    }

	private static boolean modelExists(String modelPath) {
		// Builds the path to match your generated resources folder structure
		String fullPath = "/assets/create_train_parts/models/block/" + modelPath + ".json";

		// If getResource returns null, the file doesn't exist
		return CreateTrainParts.class.getResource(fullPath) != null;
	}

    private static void putTrainStep(String path) {
        for (Direction facing : Iterate.directions) {
            if (facing == Direction.UP || facing == Direction.DOWN)
                continue;
            for (Direction face : Iterate.directions) {
                for (TrainStepBlock.ConnectedState state : TrainStepBlock.ConnectedState.values()) {
					String suffix = facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName();
					ResourceLocation key = CreateTrainParts.asResource(path + "/" + suffix);

					String stepsPath = path + "/steps_" + suffix;
					if (modelExists(stepsPath)) TRAIN_STEP.put(key, block(stepsPath));

					String slidePath = path + "/slide_" + suffix;
					if (modelExists(slidePath)) TRAIN_STEP_SLIDE.put(key, block(slidePath));

					String pivotPath = path + "/pivot_" + suffix;
					if (modelExists(pivotPath)) TRAIN_STEP_PIVOT.put(key, block(pivotPath));

					String movePath = path + "/move_" + suffix;
					if (modelExists(movePath)) TRAIN_STEP_MOVE.put(key, block(movePath));

					String flapPath = path + "/flap_" + suffix;
					if (modelExists(flapPath)) TRAIN_STEP_FLAP.put(key, block(flapPath));
                }
            }
        }

    }

    private static void putTrainSlide(String path) {
        for (Direction facing : Iterate.directions) {
            if (facing == Direction.UP || facing == Direction.DOWN)
                continue;
            for (Direction face : Iterate.directions) {
                for (TrainStepBlock.ConnectedState state : TrainStepBlock.ConnectedState.values()) {
					String suffix = facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName();
					ResourceLocation key = CreateTrainParts.asResource(path + "/" + suffix);

					String slidePath = path + "/slide_" + suffix;
					if (modelExists(slidePath)) TRAIN_SLIDE.put(key, block(slidePath));

					String topPath = path + "/top_" + suffix;
					if (modelExists(topPath)) TRAIN_SLIDE_TOP.put(key, block(topPath));

					String centrePath = path + "/centre_" + suffix;
					if (modelExists(centrePath)) TRAIN_SLIDE_CENTRE.put(key, block(centrePath));

					String bottomPath = path + "/bottom_" + suffix;
					if (modelExists(bottomPath)) TRAIN_SLIDE_BOTTOM.put(key, block(bottomPath));
                }
            }
        }
    }

    private static void putSlidingWindow(String type) {
        SLIDING_WINDOW.put(CreateTrainParts.asResource("sliding_windows/" + type + "_main"), block("sliding_windows/" + type + "_main"));
        SLIDING_WINDOW_UP.put(CreateTrainParts.asResource("sliding_windows/" + type + "_up"), block("sliding_windows/" + type + "_up"));
        SLIDING_WINDOW_RIGHT.put(CreateTrainParts.asResource("sliding_windows/" + type + "_right"), block("sliding_windows/" + type + "_right"));
        SLIDING_WINDOW_DOWN.put(CreateTrainParts.asResource("sliding_windows/" + type + "_down"), block("sliding_windows/" + type + "_down"));
        SLIDING_WINDOW_LEFT.put(CreateTrainParts.asResource("sliding_windows/" + type + "_left"), block("sliding_windows/" + type + "_left"));
        SLIDING_WINDOW_BACK.put(CreateTrainParts.asResource("sliding_windows/" + type + "_back"), block("sliding_windows/" + type + "_back"));
    }



    private static PartialModel block(String path) {
        return PartialModel.of(CreateTrainParts.asResource("block/" + path));
    }

    public static void init() {
        //
    }
}
