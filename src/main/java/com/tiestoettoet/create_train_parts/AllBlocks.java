package com.tiestoettoet.create_train_parts;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import com.tiestoettoet.create_train_parts.foundation.data.BuilderTransformers;
import com.tiestoettoet.create_train_parts.content.trains.crossing.ArmExtenderBlock;
import com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingBlock;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class AllBlocks {
    private static final CreateRegistrate REGISTRATE = CreateTrainParts.registrate();

    static {
        REGISTRATE.setCreativeTab(AllCreativeModeTabs.BASE_CREATIVE_TAB);
    }
    public static final BlockEntry<TrainStepBlock> TRAIN_STEP_ANDESITE = REGISTRATE.block("train_step_andesite", TrainStepBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.trainStep("andesite", () -> AllSpriteShifts.TRAIN_STEP_ANDESITE))
            .register();
    public static final BlockEntry<TrainStepBlock> TRAIN_STEP_BRASS = REGISTRATE.block("train_step_brass", TrainStepBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.trainStep("brass", () -> AllSpriteShifts.TRAIN_STEP_BRASS))
            .register();
    public static final BlockEntry<TrainStepBlock> TRAIN_STEP_COPPER = REGISTRATE.block("train_step_copper", TrainStepBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.trainStep("copper", () -> AllSpriteShifts.TRAIN_STEP_COPPER))
            .register();

    public static final BlockEntry<TrainStepBlock> TRAIN_STEP_TRAIN = REGISTRATE.block("train_step_train", TrainStepBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL)
                    .sound(SoundType.NETHERITE_BLOCK))

            .transform(BuilderTransformers.trainStep("train",
                    () -> com.simibubi.create.AllSpriteShifts.RAILWAY_CASING_SIDE,
                    () -> com.simibubi.create.AllSpriteShifts.RAILWAY_CASING))
            .register();

    public static final BlockEntry<TrainSlideBlock> TRAIN_SLIDE_ANDESITE = REGISTRATE.block("train_slide_andesite", TrainSlideBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.trainSlide("andesite", () -> AllSpriteShifts.TRAIN_STEP_ANDESITE))
            .register();

    public static final BlockEntry<TrainSlideBlock> TRAIN_SLIDE_BRASS = REGISTRATE.block("train_slide_brass", TrainSlideBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.trainSlide("brass", () -> AllSpriteShifts.TRAIN_STEP_BRASS))
            .register();

    public static final BlockEntry<TrainSlideBlock> TRAIN_SLIDE_COPPER = REGISTRATE.block("train_slide_copper", TrainSlideBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(BuilderTransformers.trainSlide("copper", () -> AllSpriteShifts.TRAIN_STEP_COPPER))
            .register();

    public static final BlockEntry<TrainSlideBlock> TRAIN_SLIDE_TRAIN = REGISTRATE.block("train_slide_train", TrainSlideBlock::new)
            .properties(p -> p.mapColor(MapColor.PODZOL)
                    .sound(SoundType.NETHERITE_BLOCK))
            .transform(BuilderTransformers.trainSlide("train",
                    () -> com.simibubi.create.AllSpriteShifts.RAILWAY_CASING_SIDE,
                    () -> com.simibubi.create.AllSpriteShifts.RAILWAY_CASING))
            .register();

    public static final BlockEntry<SlidingWindowBlock> GLASS_SLIDING_WINDOW = REGISTRATE.block("glass_sliding_window", SlidingWindowBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .sound(SoundType.GLASS))
            .transform(BuilderTransformers.slidingWindow("glass",
                    () -> com.simibubi.create.AllSpriteShifts.FRAMED_GLASS))
            .register();

    public static final BlockEntry<SlidingWindowBlock> ANDESITE_SLIDING_WINDOW = REGISTRATE.block("andesite_sliding_window", SlidingWindowBlock::new)
            .properties(p -> p.mapColor(MapColor.STONE)
                    .sound(SoundType.STONE))
            .transform(BuilderTransformers.slidingWindow("andesite",
                    () -> com.simibubi.create.AllSpriteShifts.ANDESITE_CASING))
            .register();

    public static final BlockEntry<SlidingWindowBlock> BRASS_SLIDING_WINDOW = REGISTRATE.block("brass_sliding_window", SlidingWindowBlock::new)
            .properties(p -> p.mapColor(MapColor.GOLD)
                    .sound(SoundType.METAL))
            .transform(BuilderTransformers.slidingWindow("brass",
                    () -> com.simibubi.create.AllSpriteShifts.BRASS_CASING))
            .register();

    public static final BlockEntry<SlidingWindowBlock> COPPER_SLIDING_WINDOW = REGISTRATE.block("copper_sliding_window", SlidingWindowBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE)
                    .sound(SoundType.COPPER))
            .transform(BuilderTransformers.slidingWindow("copper",
                    () -> com.simibubi.create.AllSpriteShifts.COPPER_CASING))
            .register();

    public static final BlockEntry<SlidingWindowBlock> TRAIN_SLIDING_WINDOW = REGISTRATE.block("train_sliding_window", SlidingWindowBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN)
                    .sound(SoundType.NETHERITE_BLOCK))
            .transform(BuilderTransformers.slidingWindow("train",
                    () -> com.simibubi.create.AllSpriteShifts.RAILWAY_CASING))
            .register();

    public static final BlockEntry<CrossingBlock> CROSSING = REGISTRATE.block("crossing", CrossingBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .sound(SoundType.NETHERITE_BLOCK))
            .transform(BuilderTransformers.crossing())
            .register();

    public static final BlockEntry<ArmExtenderBlock> ARM_EXTENDER = REGISTRATE.block("arm_extender", ArmExtenderBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY)
                    .sound(SoundType.NETHERITE_BLOCK))
            .transform(BuilderTransformers.armExtender())
            .register();

//
//    public static final BlockEntry<TrainStepBlock> TRAIN_STEP_BRASS = REGISTRATE.block("train_step_brass", TrainStepBlock::new)
//            .transform(BuilderTransformers.trainStep("brass", () -> AllSpriteShifts.TRAIN_STEP_BRASS))
//            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
//            .lang("Brass Step")
//            .register();
//
//    public static final BlockEntry<TrainStepBlock> TRAIN_STEP_COPPER = REGISTRATE.block("train_step_copper", TrainStepBlock::new)
//            .transform(BuilderTransformers.trainStep("copper", () -> AllSpriteShifts.TRAIN_STEP_COPPER))
//            .properties(p -> p.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
//            .lang("Copper Step")
//            .register();

//    public static final BlockEntry<TrainStepBlock> TRAIN_STEP_TRAIN = REGISTRATE.block("train_step_train", TrainStepBlock::new)
//            .transform(BuilderTransformers.trainStep("train", () -> AllSpriteShifts.TRAIN_STEP_SIDE,
//                    () -> AllSpriteShifts.TRAIN_STEP_TRAIN))
//            .properties(p -> p.mapColor(MapColor.TERRACOTTA_CYAN)
//                    .sound(SoundType.NETHERITE_BLOCK))
//            .lang("Train Step")
//            .register();


    public static void register() {
    }
}
