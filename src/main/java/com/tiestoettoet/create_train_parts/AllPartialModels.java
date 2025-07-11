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
    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW_SIDE = new HashMap<>();
    public static final Map<ResourceLocation, PartialModel> SLIDING_WINDOW_BACK = new HashMap<>();

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


    private static void putTrainStep(String path) {
        for (Direction facing : Iterate.directions) {
            if (facing == Direction.UP || facing == Direction.DOWN)
                continue;
            for (Direction face : Iterate.directions) {
                for (TrainStepBlock.ConnectedState state : TrainStepBlock.ConnectedState.values()) {

                    TRAIN_STEP.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/steps_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
                    TRAIN_STEP_SLIDE.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/slide_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
//                    System.out.println("Model path: " + path + "/slide");
                    TRAIN_STEP_PIVOT.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/pivot_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
                    TRAIN_STEP_MOVE.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/move_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
                    TRAIN_STEP_FLAP.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/flap_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
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

                    TRAIN_SLIDE.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/slide_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
                    TRAIN_SLIDE_TOP.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/top_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
//                    System.out.println("Model path: " + path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName());
                    TRAIN_SLIDE_CENTRE.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/centre_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
                    TRAIN_SLIDE_BOTTOM.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/bottom_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
//                    TRAIN_STEP_FLAP.put(CreateTrainParts.asResource(path + "/" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()), block(path + "/flap_" + facing.getSerializedName() + "_" + state.getSerializedName() + "_" + face.getSerializedName()));
                }
            }
        }
    }

    private static void putSlidingWindow(String type) {
        SLIDING_WINDOW.put(CreateTrainParts.asResource("sliding_windows/" + type + "_main"), block("sliding_windows/" + type + "_main"));
        SLIDING_WINDOW_SIDE.put(CreateTrainParts.asResource("sliding_windows/" + type + "_side"), block("sliding_windows/" + type + "_side"));
        SLIDING_WINDOW_SIDE.put(CreateTrainParts.asResource("sliding_windows/" + type + "_side_left"), block("sliding_windows/" + type + "_side_left"));
        SLIDING_WINDOW_SIDE.put(CreateTrainParts.asResource("sliding_windows/" + type + "_side_right"), block("sliding_windows/" + type + "_side_right"));
        SLIDING_WINDOW_SIDE.put(CreateTrainParts.asResource("sliding_windows/" + type + "_side_none"), block("sliding_windows/" + type + "_side_none"));
        SLIDING_WINDOW_SIDE.put(CreateTrainParts.asResource("sliding_windows/" + type + "_side_up"), block("sliding_windows/" + type + "_side_up"));
        SLIDING_WINDOW_SIDE.put(CreateTrainParts.asResource("sliding_windows/" + type + "_side_down"), block("sliding_windows/" + type + "_side_down"));
        SLIDING_WINDOW_SIDE.put(CreateTrainParts.asResource("sliding_windows/" + type + "_side_none_vertical"), block("sliding_windows/" + type + "_side_none_vertical"));
        SLIDING_WINDOW_BACK.put(CreateTrainParts.asResource("sliding_windows/" + type + "_back"), block("sliding_windows/" + type + "_back"));
    }



    private static PartialModel block(String path) {
        return PartialModel.of(CreateTrainParts.asResource("block/" + path));
    }

    public static void init() {
        //
    }
}
