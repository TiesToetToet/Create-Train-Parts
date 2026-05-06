package com.tiestoettoet.create_train_parts;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlockEntity;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlockEntity;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlockEntity;
import com.tiestoettoet.create_train_parts.content.trains.crossing.*;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class AllBlockEntityTypes {
    private static final CreateRegistrate REGISTRATE = CreateTrainParts.registrate();

    public static final BlockEntityEntry<TrainStepBlockEntity> TRAIN_STEP =
            REGISTRATE.blockEntity("train_step", TrainStepBlockEntity::new)
                    .renderer(() -> AllBlockEntityRenderers::trainStepRenderer)
                    .validBlocks(AllBlocks.TRAIN_STEP_ANDESITE, AllBlocks.TRAIN_STEP_BRASS, AllBlocks.TRAIN_STEP_COPPER,
                                 AllBlocks.TRAIN_STEP_TRAIN)
                    .register();

    public static final BlockEntityEntry<TrainSlideBlockEntity> TRAIN_SLIDE =
        REGISTRATE.blockEntity("train_slide", TrainSlideBlockEntity::new)
            .renderer(() -> AllBlockEntityRenderers::trainSlideRenderer)
            .validBlocks(AllBlocks.TRAIN_SLIDE_ANDESITE, AllBlocks.TRAIN_SLIDE_BRASS, AllBlocks.TRAIN_SLIDE_COPPER, AllBlocks.TRAIN_SLIDE_TRAIN)
            .register();

    public static final BlockEntityEntry<SlidingWindowBlockEntity> SLIDING_WINDOW =
            REGISTRATE.blockEntity("sliding_window", SlidingWindowBlockEntity::new)
                    .renderer(() -> AllBlockEntityRenderers::slidingWindowRenderer)
                    .validBlocks(AllBlocks.GLASS_SLIDING_WINDOW, AllBlocks.ANDESITE_SLIDING_WINDOW, AllBlocks.BRASS_SLIDING_WINDOW, AllBlocks.COPPER_SLIDING_WINDOW, AllBlocks.TRAIN_SLIDING_WINDOW)
                    .register();

    public static final BlockEntityEntry<CrossingBlockEntity> CROSSING =
            REGISTRATE.blockEntity("crossing", CrossingBlockEntity::new)
//                    .visual(() -> CrossingVisual::new, false)
                    .validBlocks(AllBlocks.CROSSING)
                    .renderer(() -> AllBlockEntityRenderers::crossingRenderer)
                    .register();
//    public static final BlockEntityEntry<ArmExtenderBlockEntity> ARM_EXTENDER =
//            REGISTRATE.blockEntity("arm_extender", ArmExtenderBlockEntity::new)
//                    .validBlocks(AllBlocks.ARM_EXTENDER)
//                    .renderer(() -> ArmExtenderRenderer::new)
//                    .register();

    public static void register() {

    }
}