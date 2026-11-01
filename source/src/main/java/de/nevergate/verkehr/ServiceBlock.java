package de.nevergate.verkehr;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class ServiceBlock extends OrientedBlock implements EntityBlock {
    public enum Kind { PARK_METER,ENTRY_TICKET,CASHIER,EXIT_READER,TARIFF,RESERVATION_BOLLARD,FLOOR_BOARD,HEIGHT_WARNING,PEDESTRIAN_CALL,ACOUSTIC_SIGNAL,BICYCLE_DETECTOR,BICYCLE_LIGHT,DYNAMIC_SPEED,RAMP_METER,FAULT_MONITOR }
    public static final IntegerProperty STATUS=IntegerProperty.create("status",0,3);
    public final Kind kind;
    public ServiceBlock(Kind k){super(NgTraffic.metal().noOcclusion(),false);kind=k;registerDefaultState(defaultBlockState().setValue(STATUS,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(STATUS);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ServiceEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=NgTraffic.SERVICE_ENTITY.get()?null:(w,p,state,e)->{if(e instanceof ServiceEntity n)n.tick();};}
    @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){
        if(kind==Kind.BICYCLE_DETECTOR)return box(0,0,0,16,2,16);
        if(kind==Kind.RESERVATION_BOLLARD)return s.getValue(STATUS)==1?box(3,0,3,13,2,13):box(3,0,3,13,15,13);
        if(kind==Kind.BICYCLE_LIGHT||kind==Kind.RAMP_METER||kind==Kind.ACOUSTIC_SIGNAL)return box(5,0,5,11,16,11);
        return box(2,0,2,14,16,14);
    }
    @Override public InteractionResult use(BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){
        if(hand!=InteractionHand.MAIN_HAND||p.getItemInHand(hand).is(NgTraffic.SERVICE_LINKER.get()))return InteractionResult.PASS;
        if(!l.mayInteract(p,pos))return InteractionResult.FAIL;
        if(p instanceof ServerPlayer sp&&l.getBlockEntity(pos) instanceof ServiceEntity n){
            if(kind==Kind.PEDESTRIAN_CALL&&!p.isShiftKeyDown()){n.requestWalk(p);return InteractionResult.CONSUME;}
            sp.openMenu(new SimpleMenuProvider((id,inv,pl)->new ServiceMenu(id,inv,n),s.getBlock().getName()),b->{b.writeBlockPos(pos);b.writeEnum(kind);});
        }return InteractionResult.sidedSuccess(l.isClientSide);
    }
}
