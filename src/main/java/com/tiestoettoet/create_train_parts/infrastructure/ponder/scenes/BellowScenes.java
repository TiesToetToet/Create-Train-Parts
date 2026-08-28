package com.tiestoettoet.create_train_parts.infrastructure.ponder.scenes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.station.StationBlock;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import com.tiestoettoet.create_train_parts.foundation.ponder.CreateTrainPartsSceneBuilder;
import com.tiestoettoet.create_train_parts.foundation.ponder.element.BellowConnectionElement;

import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.ParrotElement;
import net.createmod.ponder.api.element.ParrotPose;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BellowScenes {
	public static void bellow(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		CreateTrainPartsSceneBuilder createScene = new CreateTrainPartsSceneBuilder(scene);
		scene.title("bellow", "Bellows");
		scene.configureBasePlate(1, 0, 12);
		scene.scaleSceneView(.65f);
		scene.setSceneOffsetY(-1);
		scene.showBasePlate();

		for (int i = 13; i >= 0; i--) {
			scene.world().showSection(util.select().position(i, 1, 6), Direction.DOWN);
			scene.idle(1);
		}

		BlockState air = Blocks.AIR.defaultBlockState();
		scene.world().setBlock(util.grid().at(10, 2, 6), air, false);
		scene.world().setBlock(util.grid().at(6, 2, 6), air, false);
		scene.world().setBlock(util.grid().at(3, 2, 6), air, false);

		Selection station = util.select().position(11, 1, 3);
		Selection controls = util.select().fromTo(11, 3, 6, 10, 3, 6);
		Selection train1 = util.select().fromTo(12, 2, 5, 8, 2, 7)
			.substract(util.select().position(10, 2, 6));
		Selection train2 = util.select().fromTo(7, 2, 5, 2, 2, 7)
			.substract(util.select().position(6, 2, 6))
			.substract(util.select().position(3, 2, 6));

		BlockPos stationPos = util.grid().at(11, 1, 3);
		Vec3 stationTop = util.vector().topOf(stationPos);

		scene.world().showSection(station, Direction.DOWN);
		scene.world().restoreBlocks(util.select().position(10, 2, 6));
		ElementLink<WorldSectionElement> trainElement1 =
			scene.world().showIndependentSection(util.select().position(10, 2, 6), Direction.DOWN);
		scene.world().setBlock(util.grid().at(3, 2, 6), AllBlocks.SMALL_BOGEY.getDefaultState(), false);
		ElementLink<WorldSectionElement> trainElement2 =
			scene.world().showIndependentSection(util.select().position(3, 2, 6), Direction.DOWN);
		scene.world().setBlock(util.grid().at(6, 2, 6), AllBlocks.SMALL_BOGEY.getDefaultState(), false);
		scene.world().showSectionAndMerge(util.select().position(6, 2, 6), Direction.DOWN, trainElement2);
		scene.world().showSectionAndMerge(train1, Direction.DOWN, trainElement1);
		scene.idle(10);
		scene.world().showSectionAndMerge(train2, Direction.DOWN, trainElement2);
		scene.idle(10);

		ElementLink<WorldSectionElement> controlsElement = scene.world().showIndependentSection(controls, Direction.DOWN);
		scene.idle(3);
		scene.world().showSectionAndMerge(util.select().fromTo(3, 3, 5, 5, 4, 7), Direction.DOWN, trainElement2);
		scene.idle(3);

		scene.idle(20);

		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects().indicateSuccess(stationPos);

		scene.idle(20);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(9, 3, 6))
			.placeNearTarget()
			.attachKeyFrame()
			.text("To connect a Bellow, place it facing the direction of the other bellow");

		scene.idle(75);
		scene.world().showSectionAndMerge(util.select().position(9, 3, 6), Direction.DOWN, trainElement2);
		scene.idle(15);
		scene.world().showSectionAndMerge(util.select().position(7, 3, 6), Direction.DOWN, trainElement2);
		scene.idle(15);
		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(8, 2, 6))
			.placeNearTarget()
			.attachKeyFrame()
			.text("Make sure the arrows face each-other");

		scene.idle(90);

		scene.overlay().showText(70)
			.pointAt(stationTop)
			.placeNearTarget()
			.text("When assembling the train, the bellows will automatically connect to each other");
		scene.idle(50);
		scene.world().toggleControls(util.grid().at(11, 3, 6));
		scene.world().cycleBlockProperty(stationPos, StationBlock.ASSEMBLING);
		scene.effects().indicateSuccess(stationPos);
		scene.world().animateTrainStation(stationPos, true);
		ElementLink<BellowConnectionElement> bellowConnection =
			createScene.world().connectBellows(util.grid().at(9, 3, 6), util.grid().at(7, 3, 6), 15);

		scene.idle(20);
		scene.overlay().showText(70)
			.pointAt(stationTop)
			.placeNearTarget()
			.text("You can now safely walk from one carriage to the other");

		scene.idle(75);

		ElementLink<ParrotElement> birb =
			scene.special().createBirb(util.vector().centerOf(10, 3, 6), ParrotPose.FacePointOfInterestPose::new);
		scene.idle(15);
		scene.special().movePointOfInterest(util.grid().at(18, 3, 6));
		scene.idle(15);
		scene.world().animateTrainStation(stationPos, false);
		scene.world().moveSection(controlsElement, util.vector().of(18, 0, 0), 70);
		scene.world().moveSection(trainElement1, util.vector().of(18, 0, 0), 70);
		scene.world().moveSection(trainElement2, util.vector().of(18, 0, 0), 70);
		createScene.world().moveBellowConnection(bellowConnection, util.vector().of(18, 0, 0), 70);
		scene.world().animateBogey(util.grid().at(10, 2, 6), -18f, 70);
		scene.world().animateBogey(util.grid().at(6, 2, 6), -18f, 70);
		scene.world().animateBogey(util.grid().at(3, 2, 6), -18f, 70);
		scene.special().moveParrot(birb, util.vector().of(18, 0, 0), 70);

		scene.idle(10);
		scene.world().hideIndependentSection(controlsElement, null);
		scene.world().hideIndependentSection(trainElement1, null);
		createScene.world().hideBellowConnection(bellowConnection, null);
		scene.special().hideElement(birb, null);
		scene.idle(20);
		scene.world().hideIndependentSection(trainElement2, null);
		scene.idle(20);

	}
}
