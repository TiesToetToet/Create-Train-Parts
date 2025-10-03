package com.tiestoettoet.create_train_parts;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tiestoettoet.create_train_parts.item.ModItems;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

//import static com.tiestoettoet.create_train_parts.AllBlocks.REGISTRATE;

// The value here should match an entry in the META-INF/neoforge.neoforge.mods.toml file
@Mod(CreateTrainParts.MOD_ID)
public class CreateTrainParts {

    public static final String MOD_ID = "create_train_parts";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static CreateRegistrate registrate;

    private static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
                .setTooltipModifierFactory(
                        item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                                .andThen(TooltipModifier.mapNull(KineticStats.create(item))));
    }

    public CreateTrainParts(IEventBus modEventBus, ModContainer modContainer) {
        onCtor(modEventBus, modContainer);
    }

    public static void onCtor(IEventBus modEventBus, ModContainer modContainer) {

        ModLoadingContext modLoadingContext = ModLoadingContext.get();
        // IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        // registrate =

        REGISTRATE.registerEventListeners(modEventBus);

        com.tiestoettoet.create_train_parts.AllCreativeModeTabs.register(modEventBus); // Only ONCE!

        AllBlocks.register();

        AllBlockEntityTypes.register();

        // modEventBus.addListener(CreateTrainParts::commonSetup);
        modEventBus.addListener(CreateTrainParts::onRegister);

        // NeoForge.EVENT_BUS.register(this);

        ModItems.register(modEventBus);
    }

    public static void onRegister(final RegisterEvent event) {
        AllContraptionTypes.init();
    }

    public static CreateRegistrate registrate() {
        return REGISTRATE;
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("create_train_parts is starting up on the server side!");
    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("create_train_parts is starting up on the client side!");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}