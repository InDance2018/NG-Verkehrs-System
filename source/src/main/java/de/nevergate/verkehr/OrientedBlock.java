package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

public class OrientedBlock extends HorizontalDirectionalBlock {
    private final boolean panel;
    public OrientedBlock(Properties properties,boolean panel) {
        super(properties); this.panel=panel;
        registerDefaultState(stateDefinition.any().setValue(FACING,net.minecraft.core.Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(FACING); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite()); }
    @Override public BlockState rotate(BlockState state,Rotation rotation) { return state.setValue(FACING,rotation.rotate(state.getValue(FACING))); }
    @Override public BlockState mirror(BlockState state,Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        if(!panel) return Shapes.block();
        return state.getValue(FACING).getAxis()==net.minecraft.core.Direction.Axis.Z ? box(1,1,7,15,15,9):box(7,1,1,9,15,15);
    }
}
