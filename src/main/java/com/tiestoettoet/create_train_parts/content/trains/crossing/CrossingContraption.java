package com.tiestoettoet.create_train_parts.content.trains.crossing;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.Contraption;
import com.tiestoettoet.create_train_parts.AllBlocks;
import com.tiestoettoet.create_train_parts.AllContraptionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Queue;
import java.util.Set;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class CrossingContraption extends Contraption {
    protected Direction facing;

    public CrossingContraption() {
    }

    public CrossingContraption(Direction direction) {
        this.facing = direction;
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        System.out.println("CrossingContraption.assemble() called at " + pos);
        anchor = pos;

        // Use the standard searchMovedStructure which will discover connected blocks
        // starting from the crossing block itself
        boolean searchResult = searchMovedStructure(world, pos, null);
        System.out.println("searchMovedStructure result: " + searchResult + ", blocks found: " + blocks.size());

        if (!searchResult) {
            System.out.println("Assembly failed: searchMovedStructure returned false");
            return false;
        }

        if (facing != null) {
            expandBoundsAroundAxis(facing.getClockWise().getAxis());
        }

        if (blocks.isEmpty()) {
            System.out.println("Assembly failed: no blocks found");
            return false;
        }

        System.out.println("Assembly successful with " + blocks.size() + " blocks");
        return true;
    }

    @Override
    public ContraptionType getType() {
        // Register and return your custom ContraptionType
        return AllContraptionTypes.CROSSING.value();
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        // The crossing block itself should be the anchor (stay in the world)
        return pos.equals(anchor);
    }

    @Override
    public void addBlock(Level level, BlockPos pos, Pair<StructureTemplate.StructureBlockInfo, BlockEntity> capture) {
        System.out.println("Adding block to contraption at " + pos + ": "
                + capture.getLeft().state().getBlock().getClass().getSimpleName());
        super.addBlock(level, pos, capture);
    }

    @Override
    public CompoundTag writeNBT(HolderLookup.Provider registries, boolean spawnPacket) {
        CompoundTag tag = super.writeNBT(registries, spawnPacket);
        tag.putInt("Facing", facing.get3DDataValue());
        return tag;
    }

    @Override
    public void readNBT(Level world, CompoundTag tag, boolean spawnData) {
        facing = Direction.from3DDataValue(tag.getInt("Facing"));
        super.readNBT(world, tag, spawnData);
    }

    public Direction getFacing() {
        return facing;
    }

    @Override
    protected boolean moveBlock(Level world, Direction forcedDirection, Queue<BlockPos> frontier, Set<BlockPos> visited)
            throws AssemblyException {
        BlockPos pos = frontier.peek();
        if (pos == null)
            return super.moveBlock(world, forcedDirection, frontier, visited);

        BlockState state = world.getBlockState(pos);
        System.out.println("Processing block at " + pos + ": " + state.getBlock().getClass().getSimpleName());

        // Special handling for CrossingBlock - collect connected arm extenders
        if (AllBlocks.CROSSING.has(state)) {
            System.out.println("Found crossing block at " + pos);
            // Get the crossing block's properties
            boolean flipped = state.getValue(CrossingBlock.FLIPPED);
            Direction crossingFacing = state.getValue(HORIZONTAL_FACING);

            // Calculate the direction to the first arm extender
            Direction armDirection = crossingFacing.getClockWise();
            BlockPos armPos = pos.relative(armDirection);

            // Add connected arm extenders to the frontier if they haven't been visited
            int armCount = 0;
            while (world.getBlockState(armPos).getBlock() instanceof ArmExtenderBlock) {
                if (!visited.contains(armPos)) {
                    frontier.add(armPos);
                    armCount++;
                    System.out.println("Added arm extender " + armCount + " at " + armPos);
                }
                armPos = armPos.relative(armDirection);
            }
            System.out.println("Total arms found: " + armCount);
        }

        // Special handling for ArmExtenderBlock - ensure they connect to adjacent arm
        // extenders
        if (state.getBlock() instanceof ArmExtenderBlock) {
            System.out.println("Found arm extender at " + pos);
            boolean flipped = state.getValue(ArmExtenderBlock.FLIPPED);
            Direction armFacing = state.getValue(HORIZONTAL_FACING);

            // Connect to the next arm extender in the chain
            Direction chainDirection = flipped ? armFacing.getCounterClockWise() : armFacing.getClockWise();
            BlockPos nextArmPos = pos.relative(chainDirection);

            if (!visited.contains(nextArmPos)
                    && world.getBlockState(nextArmPos).getBlock() instanceof ArmExtenderBlock) {
                frontier.add(nextArmPos);
                System.out.println("Connected to next arm at " + nextArmPos);
            }

            // Connect to the previous arm extender in the chain (but NOT back to crossing
            // block)
            BlockPos prevArmPos = pos.relative(chainDirection.getOpposite());
            if (!visited.contains(prevArmPos)
                    && world.getBlockState(prevArmPos).getBlock() instanceof ArmExtenderBlock) {
                frontier.add(prevArmPos);
                System.out.println("Connected to previous arm at " + prevArmPos);
            }
        }

        return super.moveBlock(world, forcedDirection, frontier, visited);
    }

    @Override
    public boolean canBeStabilized(Direction facing, BlockPos localPos) {
        return false;
    }

}
