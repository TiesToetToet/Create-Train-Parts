package com.tiestoettoet.create_train_parts.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tiestoettoet.create_train_parts.content.decoration.trainSlide.TrainSlideBlock;
import com.tiestoettoet.create_train_parts.content.decoration.trainStep.TrainStepBlock;
import com.tiestoettoet.create_train_parts.foundation.ponder.CreateTrainPartsSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.swing.text.Element;

public class CrossingScenes {
        public static void crossing(SceneBuilder builder, SceneBuildingUtil util) {
            CreateSceneBuilder scene = new CreateSceneBuilder(builder);
            CreateTrainPartsSceneBuilder createScene = new CreateTrainPartsSceneBuilder(scene);
            scene.title("crossing", "Crossing");
            scene.configureBasePlate(1, 0, 5);
            scene.setSceneOffsetY(-1);
            scene.showBasePlate();

            scene.world().showIndependentSection(util.select().fromTo(4, 1, 0, 4, 2, 1), Direction.DOWN);
            scene.world().showIndependentSection(util.select().fromTo(3, 0, 2, 4, 1, 2), Direction.DOWN);

            scene.idle(30);

            scene.overlay().showText(50)
                .pointAt(util.vector().of(3, 2, 2))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Crossing Gates need kinetic power to operate");

            scene.idle(60);

            scene.world().toggleRedstonePower(util.select().fromTo(4, 1, 1, 4, 2, 2));
            scene.effects().indicateRedstone(util.grid().at(4, 2, 1));
            scene.world().modifyKineticSpeed(util.select().fromTo(3, 0, 2, 4, 1, 2), f -> -f);

            scene.idle(50);

            scene.overlay().showText(50)
                .pointAt(util.vector().of(4, 3, 1))
                .placeNearTarget()
                .text("Reverse the power to change direction");

            scene.idle(60);

            scene.world().toggleRedstonePower(util.select().fromTo(4, 1, 1, 4, 2, 2));
            scene.effects().indicateRedstone(util.grid().at(4, 2, 1));
            scene.world().modifyKineticSpeed(util.select().fromTo(3, 0, 2, 4, 1, 2), f -> -f);

            scene.idle(30);

            scene.overlay().showText(50)
                .pointAt(util.vector().of(3, 2, 2))
                .placeNearTarget()
                    .attachKeyFrame()
                .text("If you want to extend the Crossing Gate, simply place down Arm Extenders");

            scene.idle(60);

            ElementLink<WorldSectionElement> arm1 = scene.world().showIndependentSection(util.select().position(2, 1, 2), Direction.DOWN);
            scene.idle(5);
            ElementLink<WorldSectionElement> arm2 = scene.world().showIndependentSection(util.select().position(1, 1, 2), Direction.DOWN);

            scene.idle(30);

            scene.world().toggleRedstonePower(util.select().fromTo(4, 1, 1, 4, 2, 2));
            scene.effects().indicateRedstone(util.grid().at(4, 2, 1));
            scene.world().modifyKineticSpeed(util.select().fromTo(3, 0, 2, 4, 1, 2), f -> -f);

            Selection arms = util.select().fromTo(2, 1, 2, 1, 1, 2);
            createScene.world().hideIndependentSectionImmediately(arm1);
            createScene.world().hideIndependentSectionImmediately(arm2);
            ElementLink<WorldSectionElement> armsElement = scene.world().showIndependentSectionImmediately(arms);
            scene.world().configureCenterOfRotation(armsElement, util.vector().of(3.5, 1.5, 2.5));
            createScene.world().rotateSection(armsElement, 0, 0, -89.38, 63);


        }

        public static void railway_crossing(SceneBuilder builder, SceneBuildingUtil util) {
                CreateSceneBuilder scene = new CreateSceneBuilder(builder);
                CreateTrainPartsSceneBuilder createScene = new CreateTrainPartsSceneBuilder(scene);
                scene.title("railway_crossing", "Crossing");
                scene.configureBasePlate(17, 0, 12);
                scene.scaleSceneView(.65f);
                scene.setSceneOffsetY(-1);
                scene.showBasePlate();

                scene.world().showIndependentSectionImmediately(util.select().fromTo(26, 1, 2, 26, 1, 4));

                for (int i = 45; i >= 0; i--) {
                        scene.world().showSection(util.select().position(i, 1, 6), Direction.DOWN);
                        scene.idle(1);
                }

                // scene.idle(20);

                scene.world().showSection(util.select().fromTo(25, 1, 4, 25, 1, 4), Direction.DOWN);

                scene.idle(15);

                Selection arms = util.select().fromTo(24, 1, 4, 20, 1, 4);

                ElementLink<WorldSectionElement> armsElement = scene.world().showIndependentSectionImmediately(arms);
                scene.world().configureCenterOfRotation(armsElement, util.vector().of(25.5, 1.5, 4.5));
                createScene.world().rotateSection(armsElement, 0, 0, -44.69, 0);
//                createScene.world().hideIndependentSectionImmediately(armsElement);
//                scene.world().showIndependentSection(arms, Direction.DOWN);
                // createScene.world().hideIndependentSectionImmediately(armsElement);

                scene.idle(20);

                scene.world().showSection(util.select().fromTo(17, 1 , 3, 17, 2, 3), Direction.DOWN);
                scene.world().showSection(util.select().fromTo(28, 1 , 3, 28, 2, 3), Direction.DOWN);
                scene.world().showSection(util.select().position(26, 2, 3), Direction.DOWN);

                scene.idle(20);

                scene.overlay().showText(50)
                                .pointAt(util.vector().of(22, 3, 4))
                                .placeNearTarget()
                                .attachKeyFrame()
                                .text("When using Crossing Gates with Signals, you can create a safe Railway Crossing");


//                scene.world().toggleRedstonePower(util.select().position(26, 1, 3));
//                scene.world().modifyKineticSpeed(util.select().fromTo(25, 0, 4, 26, 1, 4), f -> -f);

                scene.idle(60);

                // BlockState air = Blocks.AIR.defaultBlockState();
                // scene.world().setBlock(util.grid().at(26, 2, 6), air, false);
                // scene.world().setBlock(util.grid().at(22, 2, 6), air, false);
                // scene.world().setBlock(util.grid().at(19, 2, 6), air, false);

                // Selection station = util.select().position(11, 1, 3);
                // Selection controls = util.select().fromTo(9, 3, 6, 10, 3, 6);
                Selection train1 = util.select().fromTo(28, 2, 5, 24, 3, 7);
                // .substract(util.select().position(10, 2, 6));
                //
                //
                //
                Selection train2 = util.select().fromTo(23, 2, 5, 18, 3, 7);
                //
                // for (int i = 7; i >= 2; i--) {
                // scene.world().showSection(util.select().fromTo(i, 1, 1, i, 1, 3),
                // Direction.DOWN);
                // scene.idle(1);
                // }
                //
                // scene.world().showSection(station, Direction.DOWN);
                //
                scene.special().movePointOfInterest(util.grid().at(44, 3, 6));
                //
                ElementLink<WorldSectionElement> trainElement1 = scene.world().showIndependentSection(train1,
                                Direction.DOWN);
                ElementLink<WorldSectionElement> trainElement2 = scene.world().showIndependentSection(train2,
                                Direction.DOWN);
                scene.world().moveSection(trainElement1, util.vector().of(-20, 0, 0), 0);
                scene.world().moveSection(trainElement2, util.vector().of(-20, 0, 0), 0);
                ElementLink<ParrotElement> birb = scene.special().createBirb(util.vector().centerOf(6, 3, 6),
                                ParrotPose.FacePointOfInterestPose::new);
                scene.world().toggleControls(util.grid().at(27, 3, 6));
                //
                // scene.world().animateBogey(util.grid().at(10, 2, 6), -20f, 0);
                // scene.world().animateBogey(util.grid().at(6, 2, 6), -20f, 0);
                // scene.world().animateBogey(util.grid().at(3, 2, 6), -20f, 0);
                // scene.idle(10);
                //
                scene.world().moveSection(trainElement1, util.vector().of(52, 0, 0), 300);
                scene.world().moveSection(trainElement2, util.vector().of(52, 0, 0), 300);
                scene.special().moveParrot(birb, util.vector().of(52, 0, 0), 300);
                scene.world().animateBogey(util.grid().at(26, 2, 6), 68f, 300);
                scene.world().animateBogey(util.grid().at(22, 2, 6), 68f, 300);
                scene.world().animateBogey(util.grid().at(19, 2, 6), 68f, 300);
                //
                 scene.idle(35);

                 scene.world().toggleRedstonePower(util.select().position(17, 2, 3));
                 scene.effects().indicateRedstone(util.grid().at(17, 2, 3));
                 scene.world().toggleRedstonePower(util.select().fromTo(26, 1, 3, 26, 2, 3));
                 scene.effects().indicateRedstone(util.grid().at(26, 2, 3));
                 scene.world().modifyKineticSpeed(util.select().fromTo(25, 0, 4, 26, 1, 4), f -> -f);
                 createScene.world().rotateSection(armsElement, 0, 0, 89.38, 63);

                 scene.idle(63);

                 scene.world().toggleRedstonePower(util.select().position(17, 2, 3));

                 scene.idle(40);

                 scene.world().toggleRedstonePower(util.select().position(28, 2, 3));
                 scene.effects().indicateRedstone(util.grid().at(28, 2, 3));



                 scene.idle(55);

                 scene.world().toggleRedstonePower(util.select().fromTo(26, 1, 3, 26, 2, 3));
                 scene.world().toggleRedstonePower(util.select().position(28, 2, 3));
                 scene.world().modifyKineticSpeed(util.select().fromTo(25, 0, 4, 26, 1, 4), f -> -f);
                 createScene.world().rotateSection(armsElement, 0, 0, -89.38, 63);
                //
                // scene.world().animateTrainStation(util.grid().at(11, 1, 3), true);
                // scene.special().movePointOfInterest(util.grid().at(4, 3, 4));
                //
                // scene.idle(2);
                // Vec3 target = util.vector().centerOf(4, 3, 4);
                // scene.overlay().showText(50)
                // .pointAt(target)
                // .placeNearTarget()
                // .attachKeyFrame()
                // .text("Sometimes the Train is too far away from the Station to enter the
                // Train");
                //
                // scene.idle(80);
                //
                // createScene.world().hideIndependentSectionImmediately(trainElement2);
                // train2 = train2
                // .substract(util.select().fromTo(5, 2, 5, 6, 2, 5))
                // .substract(util.select().fromTo(2, 2, 5, 3, 2, 5));
                //
                // scene.world().setBlocks(util.select().fromTo(5, 2, 5, 6, 2, 5), air, false);
                // scene.world().setBlocks(util.select().fromTo(2, 2, 5, 3, 2, 5), air, false);
                //
                // trainElement2 = scene.world().showIndependentSectionImmediately(train2);
                //
                // scene.overlay().showText(50)
                // .pointAt(target)
                // .placeNearTarget()
                // .text("Use Train Steps or Train Slides for easier access");
                // scene.world().setBlocks(util.select().fromTo(5, 2, 5, 6, 2, 5),
                // AllBlocks.ANDESITE_CASING.getDefaultState(), false);
                // scene.world().setBlocks(util.select().fromTo(2, 2, 5, 3, 2, 5),
                // AllBlocks.ANDESITE_CASING.getDefaultState(), false);
                // Selection casings1 = util.select().fromTo(5, 2, 5, 6, 2, 5);
                // Selection casings2 = util.select().fromTo(2, 2, 5, 3, 2, 5);
                // scene.world().hideSection(casings1, Direction.NORTH);
                // scene.world().hideSection(casings2, Direction.NORTH);
                //
                // scene.idle(30);
                //
                // BlockState step =
                // com.tiestoettoet.create_train_parts.AllBlocks.TRAIN_STEP_ANDESITE.getDefaultState().setValue(TrainStepBlock.VISIBLE,
                // false);
                // BlockState slide =
                // com.tiestoettoet.create_train_parts.AllBlocks.TRAIN_SLIDE_ANDESITE.getDefaultState().setValue(TrainSlideBlock.VISIBLE,
                // false);
                //
                // scene.world().setBlocks(util.select().position(5, 2, 5),
                // step.setValue(TrainStepBlock.OPEN, false).setValue(TrainStepBlock.CONNECTED,
                // TrainStepBlock.ConnectedState.LEFT), false);
                // scene.world().setBlocks(util.select().position(6, 2, 5),
                // step.setValue(TrainStepBlock.OPEN, false).setValue(TrainStepBlock.CONNECTED,
                // TrainStepBlock.ConnectedState.RIGHT), false);
                // scene.world().setBlocks(util.select().position(2, 2, 5),
                // slide.setValue(TrainSlideBlock.OPEN,
                // false).setValue(TrainSlideBlock.CONNECTED,
                // TrainSlideBlock.ConnectedState.LEFT), false);
                // scene.world().setBlocks(util.select().position(3, 2, 5),
                // slide.setValue(TrainSlideBlock.OPEN,
                // false).setValue(TrainSlideBlock.CONNECTED,
                // TrainSlideBlock.ConnectedState.RIGHT), false);
                // Selection steps = util.select().fromTo(5, 2, 5, 6, 2, 5);
                // Selection slides = util.select().fromTo(2, 2, 5, 3, 2, 5);
                //
                // scene.idle(10);
                //
                // ElementLink<WorldSectionElement> stepsElement =
                // scene.world().showIndependentSection(steps, Direction.DOWN);
                // ElementLink<WorldSectionElement> slidesElement =
                // scene.world().showIndependentSection(slides, Direction.DOWN);
                //
                // scene.idle(14);
                //
                // createScene.world().hideIndependentSectionImmediately(stepsElement);
                // createScene.world().hideIndependentSectionImmediately(slidesElement);
                // createScene.world().hideIndependentSectionImmediately(trainElement2);
                //
                //// scene.world().restoreBlocks(util.select().fromTo(5, 2, 5, 6, 2,
                // 5).substract(util.select().fromTo(5, 2, 5, 6, 2,
                // 5)).substract(util.select().fromTo(2, 2, 5, 3, 2, 5)));
                //// scene.world().setBlocks(util.select().position(5, 2, 5),
                // step.setValue(TrainStepBlock.OPEN, false).setValue(TrainStepBlock.CONNECTED,
                // TrainStepBlock.ConnectedState.LEFT), false);
                //// scene.world().setBlocks(util.select().position(6, 2, 5),
                // step.setValue(TrainStepBlock.OPEN, false).setValue(TrainStepBlock.CONNECTED,
                // TrainStepBlock.ConnectedState.RIGHT), false);
                //// scene.world().setBlocks(util.select().position(2, 2, 5),
                // slide.setValue(TrainSlideBlock.OPEN,
                // false).setValue(TrainSlideBlock.CONNECTED,
                // TrainSlideBlock.ConnectedState.LEFT), false);
                //// scene.world().setBlocks(util.select().position(2, 2, 5),
                // slide.setValue(TrainSlideBlock.OPEN,
                // false).setValue(TrainSlideBlock.CONNECTED,
                // TrainSlideBlock.ConnectedState.RIGHT), false);
                // train2 = util.select().fromTo(7, 2, 5, 2, 3, 7);
                // trainElement2 = scene.world().showIndependentSectionImmediately(train2);
                //
                // scene.idle(20);
                //
                // scene.overlay().showText(50)
                // .pointAt(target)
                // .placeNearTarget()
                // .attachKeyFrame()
                // .text("The Steps and Slides open automatically when it arrives at the
                // Station");
                //
                // scene.idle(40);
                //
                // createScene.world().animateTrainStep(util.grid().at(5, 2, 5), true);
                // createScene.world().animateTrainStep(util.grid().at(6, 2, 5), true);
                // createScene.world().animateTrainSlide(util.grid().at(2, 2, 5), true);
                // createScene.world().animateTrainSlide(util.grid().at(3, 2, 5), true);
                //
                // scene.idle(40);
                //
                // scene.overlay().showText(50)
                // .pointAt(target)
                // .placeNearTarget()
                // .attachKeyFrame()
                // .text("And close when the Train leaves the Station");
                //
                // scene.idle(35);
                //
                // scene.special().movePointOfInterest(util.grid().at(18, 3, 6));
                //
                // scene.idle(5);
                //
                // createScene.world().animateTrainStep(util.grid().at(5, 2, 5), false);
                // createScene.world().animateTrainStep(util.grid().at(6, 2, 5), false);
                // createScene.world().animateTrainSlide(util.grid().at(2, 2, 5), false);
                // createScene.world().animateTrainSlide(util.grid().at(3, 2, 5), false);
                // scene.world().animateTrainStation(util.grid().at(11, 1, 3), false);
                //
                // scene.idle(5);
                //
                // scene.world().moveSection(trainElement1, util.vector().of(20, 0, 0), 70);
                // scene.world().moveSection(trainElement2, util.vector().of(20, 0, 0), 70);
                // scene.special().moveParrot(birb, util.vector().of(20, 0, 0), 70);
                // scene.world().animateBogey(util.grid().at(10, 2, 6), 20f, 70);
                // scene.world().animateBogey(util.grid().at(6, 2, 6), 20f, 70);
                // scene.world().animateBogey(util.grid().at(3, 2, 6), 20f, 70);

                //
        }
}
