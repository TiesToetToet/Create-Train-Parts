package com.tiestoettoet.create_train_parts.infrastructure.ponder.scenes;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlock;
import com.tiestoettoet.create_train_parts.content.decoration.slidingWindow.SlidingWindowBlockEntity;
import com.tiestoettoet.create_train_parts.foundation.gui.AllIcons;
import com.tiestoettoet.create_train_parts.foundation.ponder.CreateTrainPartsSceneBuilder;
import net.createmod.catnip.math.Pointing;
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

public class SlidingWindowScenes {
    public static void modes(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        CreateTrainPartsSceneBuilder createScene = new CreateTrainPartsSceneBuilder(scene);
        scene.title("window_modes", "Configuring Modes");
        scene.configureBasePlate(1, 0, 5);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();

        Selection window = util.select().position(3, 2, 2);
        for (int i = 4; i >= 2; i--) {
            scene.world().showSection(util.select().position(i, 1, 2), Direction.DOWN);
            scene.idle(1);
        }

        scene.idle(9);

        ElementLink<WorldSectionElement> windowElement = scene.world().showIndependentSection(window, Direction.DOWN);

        scene.idle(10);

        Vec3 target = util.vector().centerOf(2, 3, 2);

        scene.overlay().showText(50)
                .pointAt(target)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Sliding Windows normally open upwards");

        scene.idle(60);

        createScene.world().animateSlidingWindow(util.grid().at(3, 2, 2), true);

        scene.idle(10);

        scene.overlay().showText(50)
                .pointAt(target)
                .placeNearTarget()
                .attachKeyFrame()
                .text("But you can configure it to open another Direction");

        scene.idle(60);

        createScene.world().animateSlidingWindow(util.grid().at(3, 2, 2), false);

        scene.idle(10);

        scene.overlay().showControls(util.vector().centerOf(3, 3, 2), Pointing.DOWN, 75)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showControls(util.vector().centerOf(4, 2, 2), Pointing.RIGHT, 30).showing(AllIcons.I_SLIDING_WINDOW_UP);
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 2), SlidingWindowBlockEntity.class, be -> be.setMode(SlidingWindowBlockEntity.SelectionMode.RIGHT));

        scene.idle(35);

        scene.overlay().showControls(util.vector().centerOf(4, 2, 2), Pointing.RIGHT, 40).showing(AllIcons.I_SLIDING_WINDOW_RIGHT);

        scene.idle(50);

        createScene.world().animateSlidingWindow(util.grid().at(3, 2, 2), true);

        scene.idle(20);


        ElementLink<WorldSectionElement> secondWindowElement = scene.world().showIndependentSection(util.select().position(3, 3, 2), Direction.DOWN);

        scene.idle(20);

        scene.overlay().showText(50)
                .pointAt(util.vector().centerOf(3, 3, 2))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Connected Windows with the same Mode will open together");

        scene.idle(60);

        createScene.world().animateSlidingWindow(util.grid().at(3, 2, 2), false);

        scene.idle(10);


        createScene.world().hideIndependentSectionImmediately(windowElement);
        createScene.world().hideIndependentSectionImmediately(secondWindowElement);
        window = util.select().fromTo(3, 2, 2, 3, 3, 2);
        windowElement = scene.world().showIndependentSectionImmediately(window);
        scene.world().modifyBlockEntity(util.grid().at(3, 3, 2), SlidingWindowBlockEntity.class, be -> be.setMode(SlidingWindowBlockEntity.SelectionMode.RIGHT));


        scene.idle(10);

        createScene.world().animateSlidingWindow(util.grid().at(3, 2, 2), true);
        createScene.world().animateSlidingWindow(util.grid().at(3, 3, 2), true);

        scene.idle(50);

        secondWindowElement = scene.world().showIndependentSection(util.select().fromTo(4, 2, 2, 4, 3, 2), Direction.DOWN);
        createScene.world().animateSlidingWindow(util.grid().at(3, 2, 2), false);
        createScene.world().animateSlidingWindow(util.grid().at(3, 3, 2), false);

        scene.idle(14);

        createScene.world().hideIndependentSectionImmediately(windowElement);
        createScene.world().hideIndependentSectionImmediately(secondWindowElement);
        window = util.select().fromTo(3, 2, 2, 4, 3, 2);
        scene.world().setBlocks(
                util.select().fromTo(3, 2, 2, 4, 3, 2),
                AllBlocks.GLASS_SLIDING_WINDOW.getDefaultState()
                        .setValue(SlidingWindowBlock.MODE, SlidingWindowBlockEntity.SelectionMode.LEFT)
                        .setValue(SlidingWindowBlock.VISIBLE, false)
                        .setValue(SlidingWindowBlock.OPEN, false),
                false
        );
        windowElement = scene.world().showIndependentSectionImmediately(window);
        scene.overlay().showControls(util.vector().centerOf(4, 4, 2), Pointing.DOWN, 75)
                .rightClick()
                .withItem(AllItems.WRENCH.asStack());
        scene.overlay().showControls(util.vector().centerOf(5, 3, 2), Pointing.RIGHT, 30).showing(AllIcons.I_SLIDING_WINDOW_RIGHT);

        scene.overlay().showText(50)
                .pointAt(util.vector().centerOf(2, 3, 2))
                .placeNearTarget()
                .attachKeyFrame()
                .text("Holding Control changes all Windows");

        scene.idle(35);

//        createScene.world().hideIndependentSectionImmediately(windowElement);
//
//        window = util.select().fromTo(3, 2, 4, 4, 3, 4);
//        windowElement = scene.world().showIndependentSectionImmediately(window);


//        scene.world().setBlocks(util.select().fromTo(3, 2, 2, 4, 3, 2), AllBlocks.GLASS_SLIDING_WINDOW.getDefaultState().setValue(SlidingWindowBlock.MODE, SlidingWindowBlockEntity.SelectionMode.LEFT).setValue(SlidingWindowBlock.VISIBLE, false).setValue(SlidingWindowBlock.OPEN, false), false);
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 2), SlidingWindowBlockEntity.class, be -> be.setMode(SlidingWindowBlockEntity.SelectionMode.LEFT));
        scene.world().modifyBlockEntity(util.grid().at(3, 3, 2), SlidingWindowBlockEntity.class, be -> be.setMode(SlidingWindowBlockEntity.SelectionMode.LEFT));
        scene.world().modifyBlockEntity(util.grid().at(4, 2, 2), SlidingWindowBlockEntity.class, be -> be.setMode(SlidingWindowBlockEntity.SelectionMode.LEFT));
        scene.world().modifyBlockEntity(util.grid().at(4, 3, 2), SlidingWindowBlockEntity.class, be -> be.setMode(SlidingWindowBlockEntity.SelectionMode.LEFT));
        scene.overlay().showControls(util.vector().centerOf(5, 3, 2), Pointing.RIGHT, 40).showing(AllIcons.I_SLIDING_WINDOW_LEFT);

        scene.idle(50);

        createScene.world().animateSlidingWindow(util.grid().at(3, 2, 2), true);
        createScene.world().animateSlidingWindow(util.grid().at(3, 3, 2), true);
        createScene.world().animateSlidingWindow(util.grid().at(4, 2, 2), true);
        createScene.world().animateSlidingWindow(util.grid().at(4, 3, 2), true);

    }

    public static void trainBehaviour(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        CreateTrainPartsSceneBuilder createScene = new CreateTrainPartsSceneBuilder(scene);
        scene.title("train_behaviour", "Window Behaviour on Trains");
        scene.configureBasePlate(1, 0, 12);
        scene.scaleSceneView(.65f);
        scene.setSceneOffsetY(-1);
        scene.showBasePlate();

        for (int i = 13; i >= 0; i--) {
            scene.world().showSection(util.select().position(i, 1, 6), Direction.DOWN);
            scene.idle(1);
        }

        BlockState air = Blocks.AIR.defaultBlockState();
//        scene.world().setBlock(util.grid().at(10, 2, 6), air, false);
//        scene.world().setBlock(util.grid().at(6, 2, 6), air, false);
//        scene.world().setBlock(util.grid().at(3, 2, 6), air, false);

        Selection station = util.select().position(11, 1, 3);
        Selection controls = util.select().fromTo(9, 3, 6, 10, 3, 6);
        Selection train1 = util.select().fromTo(12, 2, 5, 8, 3, 7);
//                .substract(util.select().position(10, 2, 6));


        Selection train2 = util.select().fromTo(7, 2, 5, 2, 5, 7);

        scene.world().showSection(station, Direction.DOWN);

        scene.special().movePointOfInterest(util.grid().at(18, 3, 6));

        ElementLink<WorldSectionElement> trainElement1 = scene.world().showIndependentSection(train1, Direction.DOWN);
        scene.world().moveSection(trainElement1, util.vector().of(-20, 0, 0), 0);
        ElementLink<WorldSectionElement> trainElement2 = scene.world().showIndependentSection(train2, Direction.DOWN);
        scene.world().moveSection(trainElement2, util.vector().of(-20, 0, 0), 0);
        ElementLink<ParrotElement> birb = scene.special().createBirb(util.vector().centerOf( -10, 3, 6), ParrotPose.FacePointOfInterestPose::new);
        scene.world().toggleControls(util.grid().at(11, 3, 6));

        //        scene.world().animateBogey(util.grid().at(10, 2, 6), -20f, 0);
//        scene.world().animateBogey(util.grid().at(6, 2, 6), -20f, 0);
//        scene.world().animateBogey(util.grid().at(3, 2, 6), -20f, 0);
//        scene.idle(10);

        scene.world().moveSection(trainElement1, util.vector().of(20, 0, 0), 70);
        scene.world().moveSection(trainElement2, util.vector().of(20, 0, 0), 70);
        scene.special().moveParrot(birb, util.vector().of(20, 0, 0), 70);
        scene.world().animateBogey(util.grid().at(10, 2, 6), 20f, 70);
        scene.world().animateBogey(util.grid().at(6, 2, 6), 20f, 70);
        scene.world().animateBogey(util.grid().at(3, 2, 6), 20f, 70);

        scene.idle(70);

        scene.world().animateTrainStation(util.grid().at(11, 1, 3), true);
        createScene.world().animateSlidingWindow(util.grid().at(2, 3, 5), true);
        createScene.world().animateSlidingWindow(util.grid().at(5, 3, 5), true);
        scene.special().movePointOfInterest(util.grid().at(4, 3, 4));

        scene.idle(2);
        Vec3 target = util.vector().centerOf(4, 4, 4);
        scene.overlay().showText(50)
                .pointAt(target)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Windows that are set to open Left or Right open automatically at Train Stations");

        scene.idle(80);

        target = util.vector().centerOf(5, 6, 4);
        scene.overlay().showText(50)
                .pointAt(target)
                .placeNearTarget()
                .attachKeyFrame()
                .text("Windows that are set to open Up or Down can only be opened manually");

        scene.idle(60);

        createScene.world().animateSlidingWindow(util.grid().at(3, 4, 5), true);
        createScene.world().animateSlidingWindow(util.grid().at(4, 4, 5), true);
        createScene.world().animateSlidingWindow(util.grid().at(6, 4, 5), true);
        createScene.world().animateSlidingWindow(util.grid().at(7, 4, 5), true);

        scene.idle(30);

        scene.special().movePointOfInterest(util.grid().at(18, 3, 6));

        scene.idle(5);

        createScene.world().animateSlidingWindow(util.grid().at(2, 3, 5), false);
        createScene.world().animateSlidingWindow(util.grid().at(5, 3, 5), false);
        scene.world().animateTrainStation(util.grid().at(11, 1, 3), false);

        scene.idle(5);

        scene.world().moveSection(trainElement1, util.vector().of(20, 0, 0), 70);
        scene.world().moveSection(trainElement2, util.vector().of(20, 0, 0), 70);
        scene.special().moveParrot(birb, util.vector().of(20, 0, 0), 70);
        scene.world().animateBogey(util.grid().at(10, 2, 6), 20f, 70);
        scene.world().animateBogey(util.grid().at(6, 2, 6), 20f, 70);
        scene.world().animateBogey(util.grid().at(3, 2, 6), 20f, 70);
    }
}
