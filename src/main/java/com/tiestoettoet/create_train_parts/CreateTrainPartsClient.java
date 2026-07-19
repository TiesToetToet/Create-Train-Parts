package com.tiestoettoet.create_train_parts;

import com.simibubi.create.CreateClient;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.ftb.FTBIntegration;
import com.simibubi.create.compat.sodium.SodiumCompat;
import com.simibubi.create.content.trains.schedule.hat.TrainHatInfoReloadListener;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import com.tiestoettoet.create_train_parts.foundation.ClientResourceReloadListener;
import com.tiestoettoet.create_train_parts.foundation.ponder.CreateTrainPartsPonderPlugin;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.createmod.ponder.foundation.PonderIndex;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CreateTrainParts.MOD_ID, dist = Dist.CLIENT)
public class CreateTrainPartsClient {

	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

    public CreateTrainPartsClient(net.neoforged.bus.api.IEventBus modEventBus) {
        onCtorClient(modEventBus);
    }

    public static void onCtorClient(net.neoforged.bus.api.IEventBus modEventBus) {
        net.neoforged.bus.api.IEventBus neoEventBus = NeoForge.EVENT_BUS;

        modEventBus.addListener(CreateTrainPartsClient::clientInit);

        AllInstanceTypes.init();

//        AllCreateTrainPartsPonderScenes.register();

        Mods.FTBLIBRARY.executeIfInstalled(() -> () -> FTBIntegration.init(modEventBus, neoEventBus));
        Mods.SODIUM.executeIfInstalled(() -> () -> SodiumCompat.init(modEventBus, neoEventBus));
    }

    public static void clientInit(final FMLClientSetupEvent event) {
        SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.PARTIAL);

        System.out.println("Create Train Parts Client Init");
        AllPartialModels.init();
        PonderIndex.addPlugin(new CreateTrainPartsPonderPlugin());
    }
}
