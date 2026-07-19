package com.tiestoettoet.create_train_parts.foundation.data.recipe;

import com.simibubi.create.api.data.recipe.ProcessingRecipeGen;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CreateTrainPartsRecipeProvider extends RecipeProvider {
    static final List<ProcessingRecipeGen<?, ?, ?>> CUSTOM_GENERATORS = new ArrayList<>();
    static final List<ProcessingRecipeGen<?, ?, ?>> GENERATORS = new ArrayList<>();
    static final int BUCKET = FluidType.BUCKET_VOLUME;
    static final int BOTTLE = 250;

    public CreateTrainPartsRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        buildMachineRecipes(recipeOutput);
    }

    private void buildMachineRecipes(RecipeOutput output) {

    }

    public static void registerAllProcessing(DataGenerator gen, PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
//        GENERATORS.add(new CreateIceAgeFillingRecipeGen(output, registries));

        gen.addProvider(true, new DataProvider() {

            @Override
            public String getName() {
                return "Create Train Parts' Processing Recipes";
            }

            @Override
            public CompletableFuture<?> run(CachedOutput dc) {
                return CompletableFuture.allOf(GENERATORS.stream()
                        .map(gen -> gen.run(dc))
                        .toArray(CompletableFuture[]::new));
            }
        });
    }

    protected static class I {

    }
}
