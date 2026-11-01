package de.nevergate.verkehr;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;

public final class TrafficNodeBlock extends OrientedBlock implements EntityBlock {
    public enum Kind { SPEED_SENSOR,SPEED_BOARD,JAM_SENSOR,CROSSING_CONTROLLER,CROSSBUCK,CROSSING_LIGHT,CROSSING_BARRIER,BUS_SENSOR,EMERGENCY_SENSOR,TUNNEL_CONTROLLER,TUNNEL_BARRIER,TUNNEL_LIGHT,TUNNEL_LANE,DETOUR_CONTROLLER,DETOUR_GUIDE,COUNTER }
    public static final IntegerProperty STATUS=IntegerProperty.create("status",0,3);
    public final Kind kind;
    public static boolean barrier(Kind k){return k==Kind.CROSSING_BARRIER||k==Kind.TUNNEL_BARRIER;}
    public static boolean sensor(Kind k){return k==Kind.SPEED_SENSOR||k==Kind.JAM_SENSOR||k==Kind.BUS_SENSOR||k==Kind.EMERGENCY_SENSOR||k==Kind.COUNTER;}
    public TrafficNodeBlock(Kind k){
        super(NgTraffic.metal().noOcclusion().lightLevel(s->k==Kind.TUNNEL_LIGHT&&s.getValue(STATUS)==1?15:k==Kind.CROSSING_LIGHT&&s.getValue(STATUS)>0?10:0),false);kind=k;
        registerDefaultState(defaultBlockState().setValue(STATUS,0));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(STATUS);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TrafficNodeEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=NgTraffic.TRAFFIC_NODE_ENTITY.get()?null:(w,p,state,e)->{if(e instanceof TrafficNodeEntity n)n.tick();};}
    @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){
        var d=s.getValue(FACING);boolean z=d.getAxis()==Direction.Axis.Z;
        if(sensor(kind))return box(0,0,0,16,2,16);
        if(barrier(kind)){
            if(s.getValue(STATUS)==1)return switch(d){case NORTH->box(0,0,5,3,16,11);case SOUTH->box(13,0,5,16,16,11);case EAST->box(5,0,0,11,16,3);default->box(5,0,13,11,16,16);};
            return z?box(0,0,5,16,16,11):box(5,0,0,11,16,16);
        }
        if(kind==Kind.TUNNEL_LIGHT)return box(1,12,1,15,16,15);
        if(kind==Kind.CROSSING_CONTROLLER||kind==Kind.TUNNEL_CONTROLLER||kind==Kind.DETOUR_CONTROLLER)return box(2,0,2,14,14,14);
        return Shapes.or(box(7,0,7,9,10,9),z?box(1,5,6,15,16,10):box(6,5,1,10,16,15));
    }
    @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult h){
        if(hand!=InteractionHand.MAIN_HAND||!player.getItemInHand(hand).isEmpty())return InteractionResult.PASS;
        if(!player.getAbilities().mayBuild||!l.mayInteract(player,p))return InteractionResult.FAIL;
        if(player instanceof ServerPlayer server&&l.getBlockEntity(p) instanceof TrafficNodeEntity n)server.openMenu(new SimpleMenuProvider((id,inv,pl)->new TrafficNodeMenu(id,inv,n),s.getBlock().getName()),b->{b.writeBlockPos(p);b.writeEnum(kind);});
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
