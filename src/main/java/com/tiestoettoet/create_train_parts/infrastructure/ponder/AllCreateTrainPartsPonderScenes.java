package com.tiestoettoet.create_train_parts.infrastructure.ponder;

import com.tiestoettoet.create_train_parts.AllBlocks;
import com.tiestoettoet.create_train_parts.infrastructure.ponder.scenes.CrossingScenes;
import com.tiestoettoet.create_train_parts.infrastructure.ponder.scenes.SlidingWindowScenes;
import com.tiestoettoet.create_train_parts.infrastructure.ponder.scenes.TrainStepSlideScenes;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

public class AllCreateTrainPartsPonderScenes {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);

        HELPER.forComponents(AllBlocks.TRAIN_STEP_ANDESITE, AllBlocks.TRAIN_STEP_BRASS, AllBlocks.TRAIN_STEP_COPPER, AllBlocks.TRAIN_STEP_TRAIN)
                .addStoryBoard("train_step/assembly", TrainStepSlideScenes::assembly)
                .addStoryBoard("train_step/steps", TrainStepSlideScenes::steps);

        HELPER.forComponents(AllBlocks.TRAIN_SLIDE_ANDESITE, AllBlocks.TRAIN_SLIDE_BRASS, AllBlocks.TRAIN_SLIDE_COPPER, AllBlocks.TRAIN_SLIDE_TRAIN)
                .addStoryBoard("train_step/assembly", TrainStepSlideScenes::assembly);

        HELPER.forComponents(AllBlocks.GLASS_SLIDING_WINDOW, AllBlocks.ANDESITE_SLIDING_WINDOW, AllBlocks.BRASS_SLIDING_WINDOW, AllBlocks.COPPER_SLIDING_WINDOW, AllBlocks.TRAIN_SLIDING_WINDOW)
                .addStoryBoard("sliding_window/window", SlidingWindowScenes::modes)
                .addStoryBoard("sliding_window/window_train", SlidingWindowScenes::trainBehaviour);

        HELPER.forComponents(AllBlocks.CROSSING)
                .addStoryBoard("crossing/crossing", CrossingScenes::crossing)
                .addStoryBoard("crossing/railroad_crossing", CrossingScenes::railway_crossing);
    }

}
