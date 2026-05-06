package com.tiestoettoet.create_train_parts;

import com.simibubi.create.Create;
import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.content.contraptions.Contraption;
import com.tiestoettoet.create_train_parts.content.trains.crossing.CrossingContraption;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AllContraptionTypes {


    public static final Map<String, ContraptionType> BY_LEGACY_NAME = new HashMap<>();

    public static final Holder.Reference<ContraptionType> CROSSING = register("crossing", CrossingContraption::new);

    private static Holder.Reference<ContraptionType> register(String name, Supplier<? extends Contraption> factory) {
        ContraptionType type = new ContraptionType(factory);
        BY_LEGACY_NAME.put(name, type);

        return Registry.registerForHolder(CreateBuiltInRegistries.CONTRAPTION_TYPE, Create.asResource(name), type);
    }

    public static void init() {
    }
}
