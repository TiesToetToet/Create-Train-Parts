package com.tiestoettoet.create_train_parts.foundation.blockEntity.behaviour.scrollValue;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class BulkScrollOptionBehaviour<E extends Enum<E> & INamedIconOptions> extends ScrollOptionBehaviour<E> {
    public E[] options;
    Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter;

    public BulkScrollOptionBehaviour(Class<E> enum_, Component label, SmartBlockEntity be, ValueBoxTransform slot,
                                     Function<SmartBlockEntity, List<? extends SmartBlockEntity>> groupGetter) {
        super(enum_, label, be, slot);
        this.groupGetter = groupGetter;
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlDown) {
        if (!ctrlDown) {
            super.setValueSettings(player, valueSetting, ctrlDown);
            return;
        }
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        for (SmartBlockEntity be : getBulk()) {
            if (be instanceof SlidingWindowBlockEntity cbe && cbe.selectionMode != null)
                cbe.selectionMode.setValue(valueSetting.value());
        }
    }

    public List<? extends SmartBlockEntity> getBulk() {
        return groupGetter.apply(blockEntity);
    }

}