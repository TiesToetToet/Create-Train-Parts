package com.tiestoettoet.create_train_parts.item;
import com.tiestoettoet.create_train_parts.CreateTrainParts;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(CreateTrainParts.MOD_ID);



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }



}
