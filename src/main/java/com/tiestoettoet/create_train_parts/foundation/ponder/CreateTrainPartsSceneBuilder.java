package com.tiestoettoet.create_train_parts.foundation.ponder;

import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlockEntity;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlockEntity;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlockEntity;
import com.tiestoettoet.create_train_parts.foundation.instruction.AnimateBellowConnectionInstruction;
import com.tiestoettoet.create_train_parts.foundation.instruction.AnimateWorldSectionInstructionCreateTrainParts;
import com.tiestoettoet.create_train_parts.foundation.ponder.element.BellowConnectionElement;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderSceneBuilder;
import net.createmod.ponder.foundation.element.ElementLinkImpl;
import net.createmod.ponder.foundation.element.WorldSectionElementImpl;
import net.createmod.ponder.foundation.instruction.AnimateWorldSectionInstruction;
import net.createmod.ponder.foundation.instruction.DisplayWorldSectionInstruction;
import net.createmod.ponder.foundation.instruction.FadeOutOfSceneInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CreateTrainPartsSceneBuilder extends PonderSceneBuilder {

    private final EffectInstructions effects;
    private final WorldInstructions world;
    private final SpecialInstructions special;

    public CreateTrainPartsSceneBuilder(SceneBuilder baseSceneBuilder) {
        this(baseSceneBuilder.getScene());
    }

    private CreateTrainPartsSceneBuilder(PonderScene ponderScene) {
        super(ponderScene);
        effects = new EffectInstructions();
        world = new WorldInstructions();
        special = new SpecialInstructions();
    }

    public EffectInstructions effects() {
        return effects;
    }

    public WorldInstructions world() {
        return world;
    }

    public SpecialInstructions special() {
        return special;
    }

    public class EffectInstructions extends PonderEffectInstructions {

    }

    public class WorldInstructions extends PonderWorldInstructions {

        public void showSectionImmediately(Selection selection) {
            addInstruction(
                    new DisplayWorldSectionInstruction(0, Direction.DOWN, selection, scene::getBaseWorldSection));
        }

        public void hideSectionImmediately(Selection selection) {
            addInstruction(scene -> {
                scene.getBaseWorldSection()
                        .erase(selection);
            });
        }

        public void hideIndependentSectionImmediately(ElementLink<WorldSectionElement> link) {
            addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.NORTH, link));
        }

        public void showSectionAndMergeImmediately(Selection selection,
                ElementLink<WorldSectionElement> link) {
            addInstruction(new DisplayWorldSectionInstruction(0, Direction.DOWN, selection, () -> scene.resolve(link)));
        }

        public void animateTrainStep(BlockPos position, boolean open) {
            modifyBlockEntityNBT(getScene().getSceneBuildingUtil().select().position(position), TrainStepBlockEntity.class,
                    nbt -> nbt.putBoolean("ForceOpen", open));
        }

        public void animateTrainSlide(BlockPos position, boolean open) {
            modifyBlockEntityNBT(getScene().getSceneBuildingUtil().select().position(position), TrainSlideBlockEntity.class,
                    nbt -> nbt.putBoolean("ForceOpen", open));
        }

        public void animateSlidingWindow(BlockPos position, boolean open) {
            modifyBlockEntityNBT(getScene().getSceneBuildingUtil().select().position(position), SlidingWindowBlockEntity.class,
                    nbt -> nbt.putBoolean("ForceOpen", open));
        }

        /**
         * Draws the flexible connection between two bellow blocks. Trains only
         * exist in a real level, so a scene has to place this by hand.
         *
         * @param ticks how long the connection takes to grow from the first
         *              bellow to the second, or 0 to show it at once
         */
        public ElementLink<BellowConnectionElement> connectBellows(BlockPos first, BlockPos second, int ticks) {
            ElementLink<BellowConnectionElement> link = new ElementLinkImpl<>(BellowConnectionElement.class);
            BellowConnectionElement element = new BellowConnectionElement(first, second);
            addInstruction(scene -> {
                scene.addElement(element);
                scene.linkElement(element, link);
                element.setVisible(true);
                element.forceApplyFade(1);
                element.connect(ticks);
            });
            return link;
        }

        /** Moves a connection along with the carriages carrying it. */
        public void moveBellowConnection(ElementLink<BellowConnectionElement> link, Vec3 offset, int duration) {
            addInstruction(AnimateBellowConnectionInstruction.move(link, offset, duration));
        }

        public void hideBellowConnection(ElementLink<BellowConnectionElement> link, Direction fadeOutDirection) {
            addInstruction(new FadeOutOfSceneInstruction<>(15, fadeOutDirection, link));
        }

        @Override
        public void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation,
                                  double zRotation, int duration) {
            addInstruction(
                    AnimateWorldSectionInstructionCreateTrainParts.rotate(link, new Vec3(xRotation, yRotation, zRotation), duration));
        }
    }

    public class SpecialInstructions extends PonderSpecialInstructions {

    }

}
