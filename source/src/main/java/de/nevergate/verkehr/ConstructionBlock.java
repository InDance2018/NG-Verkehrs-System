package de.nevergate.verkehr;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Every collision occupies real block cells. No oversized invisible barrier. */
public final class ConstructionBlock extends OrientedBlock implements EntityBlock {
    public enum Kind { TRAILER,LAMP,TIMED,GATE,CABLE,HOSE,BRIDGE,MAST,ACCESS,EXTENSION }
    public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
    public static final IntegerProperty MODE=IntegerProperty.create("mode",0,2);
    public final Kind kind;
    public ConstructionBlock(Kind kind){
        super(NgTraffic.metal().noOcclusion().lightLevel(s->s.getValue(ACTIVE)?kind==Kind.MAST?15:kind==Kind.LAMP||kind==Kind.TRAILER?10:0:0),false);
        this.kind=kind;
        registerDefaultState(defaultBlockState().setValue(ACTIVE,false).setValue(MODE,1));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){super.createBlockStateDefinition(b);b.add(ACTIVE,MODE);}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new ConstructionEntity(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){
        return l.isClientSide||t!=NgTraffic.CONSTRUCTION_ENTITY.get()?null:(w,p,state,e)->{if(e instanceof ConstructionEntity c)c.tick();};
    }
    @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,LivingEntity by,ItemStack stack){
        super.setPlacedBy(l,p,s,by,stack);
        if(!l.isClientSide&&l.getBlockEntity(p) instanceof ConstructionEntity c&&by instanceof Player player)c.setOwner(player.getUUID());
    }
    public static boolean moving(Kind k){return k==Kind.TIMED||k==Kind.ACCESS;}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){
        String id=switch(kind){case TRAILER->"work_warning_trailer";case LAMP->"work_running_lamp";case TIMED->"work_timed_barrier";case GATE->"work_extending_gate";case CABLE->"work_cable_bridge";case HOSE->"work_hose_bridge";case BRIDGE->"work_footbridge";case MAST->"work_light_mast";case ACCESS->"work_access_gate";case EXTENSION->"work_gate_extension";};
        return EquipmentShapes.get(id,s);
    }
    @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
        if(hand!=InteractionHand.MAIN_HAND||!player.getItemInHand(hand).isEmpty())return InteractionResult.PASS;
        if(!player.getAbilities().mayBuild||!l.mayInteract(player,p))return InteractionResult.FAIL;
        if(l.getBlockEntity(p) instanceof ConstructionEntity node){
            var root=node.control();
            if(root==null&&node.kind()==Kind.LAMP)root=node;
            if(root==null)return InteractionResult.FAIL;
            final var selected=root;
            if(player instanceof ServerPlayer server)server.openMenu(new SimpleMenuProvider((id,inv,pl)->new ConstructionMenu(id,inv,selected),selected.getBlockState().getBlock().getName()),b->{b.writeBlockPos(selected.getBlockPos());b.writeEnum(selected.kind());});
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moved){
        if(!s.is(next.getBlock())&&!l.isClientSide&&l.getBlockEntity(p) instanceof ConstructionEntity e)e.removed();
        super.onRemove(s,l,p,next,moved);
    }
}
