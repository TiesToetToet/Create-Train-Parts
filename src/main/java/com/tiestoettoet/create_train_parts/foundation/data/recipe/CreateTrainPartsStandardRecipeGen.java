package com.tiestoettoet.create_train_parts.foundation.data.recipe;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.api.data.recipe.BaseRecipeProvider;
import com.simibubi.create.content.decoration.palettes.AllPaletteBlocks;
import com.simibubi.create.content.decoration.palettes.AllPaletteStoneTypes;
import com.simibubi.create.content.equipment.toolbox.ToolboxDyeingRecipe;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import com.simibubi.create.foundation.data.recipe.CreateStandardRecipeGen;
import com.simibubi.create.foundation.data.recipe.Mods;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.tiestoettoet.create_train_parts.CreateTrainParts;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;

import net.createmod.catnip.platform.CatnipServices;
import net.createmod.catnip.platform.services.RegisteredObjectsHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;

import net.minecraftforge.common.crafting.conditions.NotCondition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.simibubi.create.foundation.data.recipe.CommonMetal.*;

/**
 * Create's own Data Generation for all vanilla recipe types.
 *
 * @see ShapedRecipeBuilder
 * @see ShapelessRecipeBuilder
 * @see SimpleCookingRecipeBuilder
 * @see SmithingTransformRecipeBuilder
 * @see SpecialRecipeBuilder
 */
@SuppressWarnings("unused")
public final class CreateTrainPartsStandardRecipeGen extends BaseRecipeProvider {
	final List<GeneratedRecipe> all = new ArrayList<>();

	/*
	 * Recipes are added through fields, so one can navigate to the right one easily
	 *
	 * (Ctrl-o) in Eclipse
	 */

	private Marker MATERIALS = enterFolder("materials");

	GeneratedRecipe
		ANDESITE_SLIDING_WINDOW = create(AllBlocks.ANDESITE_SLIDING_WINDOW).returns(1)
				.unlockedBy(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR::get)
				.viaShapeless(b -> b.requires(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR.get())
					.requires(com.simibubi.create.AllBlocks.ANDESITE_CASING.get())),
	BRASS_SLIDING_WINDOW = create(AllBlocks.BRASS_SLIDING_WINDOW).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR::get)
			.viaShapeless(b -> b.requires(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR.get())
					.requires(com.simibubi.create.AllBlocks.BRASS_CASING.get())),
	COPPER_SLIDING_WINDOW = create(AllBlocks.COPPER_SLIDING_WINDOW).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR::get)
			.viaShapeless(b -> b.requires(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR.get())
					.requires(com.simibubi.create.AllBlocks.COPPER_CASING.get())),
	GLASS_SLIDING_WINDOW = create(AllBlocks.GLASS_SLIDING_WINDOW).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR::get)
			.viaShapeless(b -> b.requires(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR.get())
					.requires(AllPaletteBlocks.FRAMED_GLASS.get())),
	TRAIN_SLIDING_WINDOW = create(AllBlocks.TRAIN_SLIDING_WINDOW).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR::get)
			.viaShapeless(b -> b.requires(com.simibubi.create.AllBlocks.FRAMED_GLASS_TRAPDOOR.get())
					.requires(com.simibubi.create.AllBlocks.RAILWAY_CASING.get())),

	TRAIN_STEP_ANDESITE = create(AllBlocks.TRAIN_STEP_ANDESITE).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.ANDESITE_CASING::get)
			.viaShaped(b -> b.define('X', ItemTags.STAIRS)
					.define('#', com.simibubi.create.AllBlocks.ANDESITE_CASING.get())
					.pattern("X#")),
	TRAIN_STEP_BRASS = create(AllBlocks.TRAIN_STEP_BRASS).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.BRASS_CASING::get)
			.viaShaped(b -> b.define('X', ItemTags.STAIRS)
					.define('#', com.simibubi.create.AllBlocks.BRASS_CASING.get())
					.pattern("X#")),
	TRAIN_STEP_COPPER = create(AllBlocks.TRAIN_STEP_COPPER).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.COPPER_CASING::get)
			.viaShaped(b -> b.define('X', ItemTags.STAIRS)
					.define('#', com.simibubi.create.AllBlocks.COPPER_CASING.get())
					.pattern("X#")),
	TRAIN_STEP_TRAIN = create(AllBlocks.TRAIN_STEP_TRAIN).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.RAILWAY_CASING::get)
			.viaShaped(b -> b.define('X', ItemTags.STAIRS)
					.define('#', com.simibubi.create.AllBlocks.RAILWAY_CASING.get())
					.pattern("X#")),

	TRAIN_SLIDE_ANDESITE = create(AllBlocks.TRAIN_SLIDE_ANDESITE).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.ANDESITE_CASING::get)
			.viaShaped(b -> b.define('X', AllBlocks.TRAIN_STEP_ANDESITE.get())
					.define('#', com.simibubi.create.AllBlocks.ANDESITE_CASING.get())
					.pattern("X#")),
	TRAIN_SLIDE_BRASS = create(AllBlocks.TRAIN_SLIDE_BRASS).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.BRASS_CASING::get)
			.viaShaped(b -> b.define('X', AllBlocks.TRAIN_STEP_BRASS.get())
					.define('#', com.simibubi.create.AllBlocks.BRASS_CASING.get())
					.pattern("X#")),
	TRAIN_SLIDE_COPPER = create(AllBlocks.TRAIN_SLIDE_COPPER).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.COPPER_CASING::get)
			.viaShaped(b -> b.define('X', AllBlocks.TRAIN_STEP_COPPER.get())
					.define('#', com.simibubi.create.AllBlocks.COPPER_CASING.get())
					.pattern("X#")),
	TRAIN_SLIDE_TRAIN = create(AllBlocks.TRAIN_SLIDE_TRAIN).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.RAILWAY_CASING::get)
			.viaShaped(b -> b.define('X', AllBlocks.TRAIN_STEP_TRAIN.get())
					.define('#', com.simibubi.create.AllBlocks.RAILWAY_CASING.get())
					.pattern("X#")),

    CROSSING = create(AllBlocks.CROSSING).returns(1)
			.unlockedBy(com.simibubi.create.AllBlocks.ANDESITE_CASING::get)
            .viaShaped(b -> b.define('C', com.simibubi.create.AllBlocks.ANDESITE_CASING.get())
                    .define('T', AllItems.ELECTRON_TUBE)
                    .define('F', ItemTags.FENCES)
                    .define('S', AllItems.IRON_SHEET)
                    .pattern("TC ")
                    .pattern("TCF")
                    .pattern(" S ")),

    ARM_EXTENDER = create(AllBlocks.ARM_EXTENDER).returns(4)
			.unlockedBy(com.simibubi.create.AllBlocks.ANDESITE_CASING::get)
            .viaShaped(b -> b.define('F', ItemTags.FENCES)
					.pattern("FF")),

	POLE = create(AllBlocks.POLE).returns(4)
		.unlockedBy(com.simibubi.create.AllBlocks.ANDESITE_CASING::get)
		.viaShapeless(b -> b.requires(com.simibubi.create.AllBlocks.ANDESITE_CASING.get())
		.requires(com.simibubi.create.AllBlocks.SHAFT));


	/*
	 * End of recipe list
	 */

	static class Marker {
	}

	String currentFolder = "";

	Marker enterFolder(String folder) {
		currentFolder = folder;
		return new Marker();
	}

	GeneratedRecipeBuilder create(Supplier<ItemLike> result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}

	GeneratedRecipeBuilder create(ResourceLocation result) {
		return new GeneratedRecipeBuilder(currentFolder, result);
	}

	GeneratedRecipeBuilder create(ItemProviderEntry<? extends ItemLike> result) {
		return create(result::get);
	}

	GeneratedRecipe createSpecial(Supplier<? extends SimpleCraftingRecipeSerializer<?>> serializer, String recipeType,
								  String path) {
		ResourceLocation location = CreateTrainParts.asResource(recipeType + "/" + currentFolder + "/" + path);
		return register(consumer -> {
			SpecialRecipeBuilder b = SpecialRecipeBuilder.special(serializer.get());
			b.save(consumer, location.toString());
		});
	}

	GeneratedRecipe blastCrushedMetal(Supplier<? extends ItemLike> result, Supplier<? extends ItemLike> ingredient) {
		return create(result::get).withSuffix("_from_crushed")
			.viaCooking(ingredient)
			.rewardXP(.1f)
			.inBlastFurnace();
	}

	GeneratedRecipe blastModdedCrushedMetal(ItemEntry<? extends Item> ingredient, CommonMetal metal) {
		for (Mods mod : metal.mods) {
			String metalName = metal.getName(mod);
			ResourceLocation ingot = mod.ingotOf(metalName);
			String modId = mod.getId();
			create(ingot).withSuffix("_compat_" + modId)
				.whenModLoaded(modId)
				.viaCooking(ingredient::get)
				.rewardXP(.1f)
				.inBlastFurnace();
		}
		return null;
	}

	GeneratedRecipe recycleGlass(BlockEntry<? extends Block> ingredient) {
		return create(() -> Blocks.GLASS).withSuffix("_from_" + ingredient.getId()
				.getPath())
			.viaCooking(ingredient::get)
			.forDuration(50)
			.inFurnace();
	}

	GeneratedRecipe recycleGlassPane(BlockEntry<? extends Block> ingredient) {
		return create(() -> Blocks.GLASS_PANE).withSuffix("_from_" + ingredient.getId()
				.getPath())
			.viaCooking(ingredient::get)
			.forDuration(50)
			.inFurnace();
	}

	GeneratedRecipe metalCompacting(List<ItemProviderEntry<? extends ItemLike>> variants,
									List<Supplier<TagKey<Item>>> ingredients) {
		GeneratedRecipe result = null;
		for (int i = 0; i + 1 < variants.size(); i++) {
			ItemProviderEntry<? extends ItemLike> currentEntry = variants.get(i);
			ItemProviderEntry<? extends ItemLike> nextEntry = variants.get(i + 1);
			Supplier<TagKey<Item>> currentIngredient = ingredients.get(i);
			Supplier<TagKey<Item>> nextIngredient = ingredients.get(i + 1);

			result = create(nextEntry).withSuffix("_from_compacting")
				.unlockedBy(currentEntry::get)
				.viaShaped(b -> b.pattern("###")
					.pattern("###")
					.pattern("###")
					.define('#', currentIngredient.get()));

			result = create(currentEntry).returns(9)
				.withSuffix("_from_decompacting")
				.unlockedBy(nextEntry::get)
				.viaShapeless(b -> b.requires(nextIngredient.get()));
		}
		return result;
	}

	GeneratedRecipe conversionCycle(List<ItemProviderEntry<? extends ItemLike>> cycle) {
		GeneratedRecipe result = null;
		for (int i = 0; i < cycle.size(); i++) {
			ItemProviderEntry<? extends ItemLike> currentEntry = cycle.get(i);
			ItemProviderEntry<? extends ItemLike> nextEntry = cycle.get((i + 1) % cycle.size());
			result = create(nextEntry).withSuffix("_from_conversion")
				.unlockedBy(currentEntry::get)
				.viaShapeless(b -> b.requires(currentEntry.get()));
		}
		return result;
	}

	GeneratedRecipe clearData(ItemProviderEntry<? extends ItemLike> item) {
		return create(item).withSuffix("_clear")
			.unlockedBy(item::get)
			.viaShapeless(b -> b.requires(item.get()));
	}

	@Override
	public void buildRecipes(Consumer<FinishedRecipe> output) {
		all.forEach(c -> c.register(output));
		CreateTrainParts.LOGGER.info("{} registered {} recipe{}", getName(), all.size(), all.size() == 1 ? "" : "s");
	}

	protected GeneratedRecipe register(GeneratedRecipe recipe) {
		all.add(recipe);
		return recipe;
	}

	class GeneratedRecipeBuilder {

		private String path;
		private String suffix;
		private Supplier<? extends ItemLike> result;
		private ResourceLocation compatDatagenOutput;
		List<ICondition> recipeConditions;

		private Supplier<ItemPredicate> unlockedBy;
		private int amount;

		private GeneratedRecipeBuilder(String path) {
			this.path = path;
			this.recipeConditions = new ArrayList<>();
			this.suffix = "";
			this.amount = 1;
		}

		public GeneratedRecipeBuilder(String path, Supplier<? extends ItemLike> result) {
			this(path);
			this.result = result;
		}

		public GeneratedRecipeBuilder(String path, ResourceLocation result) {
			this(path);
			this.compatDatagenOutput = result;
		}

		GeneratedRecipeBuilder returns(int amount) {
			this.amount = amount;
			return this;
		}

		GeneratedRecipeBuilder unlockedBy(Supplier<? extends ItemLike> item) {
			this.unlockedBy = () -> ItemPredicate.Builder.item()
				.of(item.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder unlockedByTag(Supplier<TagKey<Item>> tag) {
			this.unlockedBy = () -> ItemPredicate.Builder.item()
				.of(tag.get())
				.build();
			return this;
		}

		GeneratedRecipeBuilder whenModLoaded(String modid) {
			return withCondition(new ModLoadedCondition(modid));
		}

		GeneratedRecipeBuilder whenModMissing(String modid) {
			return withCondition(new NotCondition(new ModLoadedCondition(modid)));
		}

		GeneratedRecipeBuilder withCondition(ICondition condition) {
			recipeConditions.add(condition);
			return this;
		}

		GeneratedRecipeBuilder withSuffix(String suffix) {
			this.suffix = suffix;
			return this;
		}

		// FIXME 5.1 refactor - recipe categories as markers instead of sections?
		GeneratedRecipe viaShaped(UnaryOperator<ShapedRecipeBuilder> builder) {
			return register(consumer -> {
				ShapedRecipeBuilder b =
					builder.apply(ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result.get(), amount));
				if (unlockedBy != null)
					b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));
				b.save(consumer, createLocation("crafting"));
			});
		}

		GeneratedRecipe viaShapeless(UnaryOperator<ShapelessRecipeBuilder> builder) {
			return register(consumer -> {
				ShapelessRecipeBuilder b =
					builder.apply(ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, result.get(), amount));
				if (unlockedBy != null)
					b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));

				b.save(result -> {
					consumer.accept(!recipeConditions.isEmpty()
						? new ConditionSupportingShapelessRecipeResult(result, recipeConditions)
						: result);
				}, createLocation("crafting"));
			});
		}

		GeneratedRecipe viaNetheriteSmithing(Supplier<? extends Item> base, Supplier<Ingredient> upgradeMaterial) {
			return register(consumer -> {
				SmithingTransformRecipeBuilder b =
					SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
						Ingredient.of(base.get()), upgradeMaterial.get(), RecipeCategory.COMBAT, result.get()
							.asItem());
				b.unlocks("has_item", inventoryTrigger(ItemPredicate.Builder.item()
					.of(base.get())
					.build()));
				b.save(consumer, createLocation("crafting"));
			});
		}

		private ResourceLocation createSimpleLocation(String recipeType) {
			return CreateTrainParts.asResource(recipeType + "/" + getRegistryName().getPath() + suffix);
		}

		private ResourceLocation createLocation(String recipeType) {
			return CreateTrainParts.asResource(recipeType + "/" + path + "/" + getRegistryName().getPath() + suffix);
		}

		private ResourceLocation getRegistryName() {
			return compatDatagenOutput == null ? CatnipServices.REGISTRIES.getKeyOrThrow(result.get()
				.asItem()) : compatDatagenOutput;
		}

		GeneratedCookingRecipeBuilder viaCooking(Supplier<? extends ItemLike> item) {
			return unlockedBy(item).viaCookingIngredient(() -> Ingredient.of(item.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingTag(Supplier<TagKey<Item>> tag) {
			return unlockedByTag(tag).viaCookingIngredient(() -> Ingredient.of(tag.get()));
		}

		GeneratedCookingRecipeBuilder viaCookingIngredient(Supplier<Ingredient> ingredient) {
			return new GeneratedCookingRecipeBuilder(ingredient);
		}

		class GeneratedCookingRecipeBuilder {

			private Supplier<Ingredient> ingredient;
			private float exp;
			private int cookingTime;

			private final RecipeSerializer<? extends AbstractCookingRecipe> FURNACE = RecipeSerializer.SMELTING_RECIPE,
				SMOKER = RecipeSerializer.SMOKING_RECIPE, BLAST = RecipeSerializer.BLASTING_RECIPE,
				CAMPFIRE = RecipeSerializer.CAMPFIRE_COOKING_RECIPE;

			GeneratedCookingRecipeBuilder(Supplier<Ingredient> ingredient) {
				this.ingredient = ingredient;
				cookingTime = 200;
				exp = 0;
			}

			GeneratedCookingRecipeBuilder forDuration(int duration) {
				cookingTime = duration;
				return this;
			}

			GeneratedCookingRecipeBuilder rewardXP(float xp) {
				exp = xp;
				return this;
			}

			GeneratedRecipe inFurnace() {
				return inFurnace(b -> b);
			}

			GeneratedRecipe inFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
				return create(FURNACE, builder, 1);
			}

			GeneratedRecipe inSmoker() {
				return inSmoker(b -> b);
			}

			GeneratedRecipe inSmoker(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
				create(FURNACE, builder, 1);
				create(CAMPFIRE, builder, 3);
				return create(SMOKER, builder, .5f);
			}

			GeneratedRecipe inBlastFurnace() {
				return inBlastFurnace(b -> b);
			}

			GeneratedRecipe inBlastFurnace(UnaryOperator<SimpleCookingRecipeBuilder> builder) {
				create(FURNACE, builder, 1);
				return create(BLAST, builder, .5f);
			}

			private GeneratedRecipe create(RecipeSerializer<? extends AbstractCookingRecipe> serializer,
										   UnaryOperator<SimpleCookingRecipeBuilder> builder, float cookingTimeModifier) {
				return register(consumer -> {
					boolean isOtherMod = compatDatagenOutput != null;

					SimpleCookingRecipeBuilder b = builder.apply(SimpleCookingRecipeBuilder.generic(ingredient.get(),
						RecipeCategory.MISC, isOtherMod ? Items.DIRT : result.get(), exp,
						(int) (cookingTime * cookingTimeModifier), serializer));

					if (unlockedBy != null)
						b.unlockedBy("has_item", inventoryTrigger(unlockedBy.get()));

					b.save(result -> {
						consumer.accept(
							isOtherMod ? new ModdedCookingRecipeResult(result, compatDatagenOutput, recipeConditions)
								: result);
					}, createSimpleLocation(CatnipServices.REGISTRIES.getKeyOrThrow(serializer)
						.getPath()));
				});
			}
		}
	}

	@Override
	public String getName() {
		return "Create Train Parts' Standard Recipes";
	}

	public CreateTrainPartsStandardRecipeGen(PackOutput output) {
		super(output, CreateTrainParts.MOD_ID);
	}

	private record ModdedCookingRecipeResult(FinishedRecipe wrapped, ResourceLocation outputOverride,
											 List<ICondition> conditions) implements FinishedRecipe {
		@Override
		public ResourceLocation getId() {
			return wrapped.getId();
		}

		@Override
		public RecipeSerializer<?> getType() {
			return wrapped.getType();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return wrapped.serializeAdvancement();
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return wrapped.getAdvancementId();
		}

		@Override
		public void serializeRecipeData(JsonObject object) {
			wrapped.serializeRecipeData(object);
			object.addProperty("result", outputOverride.toString());

			JsonArray conds = new JsonArray();
			conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			object.add("conditions", conds);
		}
	}

	private record ConditionSupportingShapelessRecipeResult(FinishedRecipe wrapped, List<ICondition> conditions)
		implements FinishedRecipe {
		@Override
		public ResourceLocation getId() {
			return wrapped.getId();
		}

		@Override
		public RecipeSerializer<?> getType() {
			return wrapped.getType();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return wrapped.serializeAdvancement();
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return wrapped.getAdvancementId();
		}

		@Override
		public void serializeRecipeData(@NotNull JsonObject pJson) {
			wrapped.serializeRecipeData(pJson);

			JsonArray conds = new JsonArray();
			conditions.forEach(c -> conds.add(CraftingHelper.serialize(c)));
			pJson.add("conditions", conds);
		}
	}
}
