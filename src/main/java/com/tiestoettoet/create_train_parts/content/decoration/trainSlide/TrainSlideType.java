package com.tiestoettoet.create_train_parts.content.decoration.trainSlide;

import net.minecraft.util.StringRepresentable;

public enum TrainSlideType implements StringRepresentable {
    ANDESITE,
    BRASS,
    COPPER,
    TRAIN;

    // private final String name;

    // private TrainStepType(String name) {
    // this.name = name;
    // }

    // public String toString() {
    // return this.name;
    // }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(); // Converts enum names to lowercase strings
    }

}
