package de.nevergate.verkehr;

import net.minecraft.core.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.shapes.*;

/** Two real block cells, one controller endpoint and one survival drop. */
public final class MobileSignalBlock extends SignalBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF=BlockStateProperties.DOUBLE_BLOCK_HALF;
    public MobileSignalBlock(){super(SignalKind.MOBILE);registerDefaultState(defaultBlockState().setValue(HALF,DoubleBlockHalf.LOWER));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(HALF);}
    public static BlockPos endpoint(BlockState state,BlockPos pos){return state.getBlock() instanceof MobileSignalBlock&&state.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){
        if(context.getClickedPos().getY()>=context.getLevel().getMaxBuildHeight()-1)return null;
        return super.getStateForPlacement(context);
    }
    @Override public void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moved){
        super.onPlace(state,level,pos,old,moved);
        if(!level.isClientSide&&!old.is(this)&&state.getValue(HALF)==DoubleBlockHalf.LOWER&&level.getBlockState(pos.above()).canBeReplaced())
            level.setBlock(pos.above(),state.setValue(HALF,DoubleBlockHalf.UPPER),3);
    }
    @Override public BlockState updateShape(BlockState state,Direction direction,BlockState neighbor,LevelAccessor level,BlockPos pos,BlockPos other){
        boolean lower=state.getValue(HALF)==DoubleBlockHalf.LOWER;
        if(direction==(lower?Direction.UP:Direction.DOWN)){
            if(!neighbor.is(this)||neighbor.getValue(HALF)==state.getValue(HALF))return Blocks.AIR.defaultBlockState();
            if(!lower)return state.setValue(LIGHT,neighbor.getValue(LIGHT)).setValue(FACING,neighbor.getValue(FACING));
        }
        return super.updateShape(state,direction,neighbor,level,pos,other);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return state.getValue(HALF)==DoubleBlockHalf.LOWER?super.newBlockEntity(pos,state):null;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){
        if(state.getValue(HALF)==DoubleBlockHalf.UPPER)return state.getValue(FACING).getAxis()==Direction.Axis.Z?box(5,0,4.25,11,16,11.75):box(4.25,0,5,11.75,16,11);
        return Shapes.or(box(1,0,2,15,2,14),box(7,2,7,9,16,10));
    }
    @Override public void playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide&&player.isCreative()){
            var other=state.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos.above();
            if(level.getBlockState(other).is(this))level.setBlock(other,Blocks.AIR.defaultBlockState(),35);
        }
        super.playerWillDestroy(level,pos,state,player);
    }
}
