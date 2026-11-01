package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class SignalBlock extends MountedBlock implements EntityBlock {
    public static final IntegerProperty LIGHT=IntegerProperty.create("light",0,4);
    public final SignalKind kind;
    public SignalBlock() { this(SignalKind.VEHICLE); }
    public SignalBlock(SignalKind kind) {
        super(NgTraffic.metal().noOcclusion().lightLevel(s->lightEmission(kind,s)),true);
        this.kind=kind;
        registerDefaultState(defaultBlockState().setValue(LIGHT,0).setValue(JOIN,Join.NONE));
    }
    public static int lightEmission(SignalKind kind,BlockState state) {
        if(kind==SignalKind.MOBILE&&state.hasProperty(MobileSignalBlock.HALF)&&state.getValue(MobileSignalBlock.HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER)return 0;
        int phase=state.getValue(LIGHT);
        return kind==SignalKind.WARNING?(phase==3?15:0):(phase==4?0:15);
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { super.createBlockStateDefinition(b); b.add(LIGHT,JOIN); }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        if(kind==SignalKind.MOBILE&&!context.getLevel().getBlockState(context.getClickedPos().above()).canBeReplaced())return null;
        return super.getStateForPlacement(context);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new SignalEntity(pos,state); }
    @Override public double mountDepth(){return 5.5;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        if(kind==SignalKind.WARNING)return attachedShape(state,level,pos,context,state.getValue(FACING).getAxis()==net.minecraft.core.Direction.Axis.Z ? box(5,5,4.25,11,11,11):box(4.25,5,5,11,11,11));
        return attachedShape(state,level,pos,context,state.getValue(FACING).getAxis()==net.minecraft.core.Direction.Axis.Z ? box(5,0,4.25,11,16,11.75):box(4.25,0,5,11.75,16,11));
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide||type!=NgTraffic.SIGNAL_ENTITY.get()) return null;
        return (world,pos,s,entity)-> { if(entity instanceof SignalEntity signal) signal.tick(); };
    }
}
