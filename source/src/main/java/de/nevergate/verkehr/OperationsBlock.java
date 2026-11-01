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
public final class OperationsBlock extends OrientedBlock implements EntityBlock {
    public enum Kind { CHARGER,CHARGE_CONTROL,FUEL_PUMP,FUEL_CASHIER,WASH_ENTRY,WASH_TUNNEL,ROAD_BOLLARD,CARD_READER,PLATE_READER,DELIVERY_ZONE,BUS_DISPLAY,BUS_ROUTE,BUS_SENSOR,SCHOOL_WARNING,WORK_EXIT,WEATHER_WARNING,VISIBILITY_CONTROL,TUNNEL_CALL,ESCAPE_SIGN,OPERATIONS_DESK }
    public static final IntegerProperty STATUS=IntegerProperty.create("status",0,3);
    public final Kind kind;
    public OperationsBlock(Kind k){super(NgTraffic.metal().noOcclusion(),false);kind=k;registerDefaultState(defaultBlockState().setValue(STATUS,0));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(STATUS);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new OperationsEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide||t!=NgTraffic.OPERATIONS_ENTITY.get()?null:(w,p,state,e)->{if(e instanceof OperationsEntity n)n.tick();};}
    @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){
        if(kind==Kind.ROAD_BOLLARD)return s.getValue(STATUS)==1?box(4,0,4,12,2,12):box(4,0,4,12,16,12);
        if(kind==Kind.BUS_SENSOR)return box(0,0,0,16,1,16);
        if(kind==Kind.WASH_TUNNEL)return s.getValue(FACING).getAxis()==Direction.Axis.Z?Shapes.or(box(-16,0,0,-12,32,16),box(28,0,0,32,32,16)):Shapes.or(box(0,0,-16,16,32,-12),box(0,0,28,16,32,32));
        return box(2,0,2,14,16,14);
    }
    @Override public InteractionResult use(BlockState s,Level l,BlockPos pos,Player p,InteractionHand hand,BlockHitResult hit){
        if(hand!=InteractionHand.MAIN_HAND||p.getItemInHand(hand).is(NgTraffic.OPERATIONS_LINKER.get()))return InteractionResult.PASS;
        if(!l.mayInteract(p,pos))return InteractionResult.FAIL;
        if(p instanceof ServerPlayer sp&&l.getBlockEntity(pos) instanceof OperationsEntity n)sp.openMenu(new SimpleMenuProvider((id,inv,pl)->new OperationsMenu(id,inv,n),s.getBlock().getName()),b->{b.writeBlockPos(pos);b.writeEnum(kind);});
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public boolean isSignalSource(BlockState s){return true;}
    @Override public int getSignal(BlockState s,BlockGetter l,BlockPos p,Direction d){return s.getValue(STATUS)==2?15:0;}
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,net.minecraft.world.entity.LivingEntity e,net.minecraft.world.item.ItemStack stack){super.setPlacedBy(l,p,s,e,stack);if(!l.isClientSide&&e instanceof Player pl&&l.getBlockEntity(p) instanceof OperationsEntity n)n.setOwner(pl.getUUID());}
}
