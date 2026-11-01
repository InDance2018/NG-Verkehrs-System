package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class RoadSystemBlock extends MountedBlock implements EntityBlock {
    public enum Role { CONTROLLER, TRAFFIC_SIGN, LANE_SIGN, PARKING_GUIDE, PARKING_BOARD, ENTRY_SENSOR, EXIT_SENSOR, AGGREGATOR, SPACE_SENSOR, BARRIER, HEIGHT_SIGN }
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty OPEN=net.minecraft.world.level.block.state.properties.BooleanProperty.create("open");
    public final Role role;
    @Override protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(OPEN);}
    public RoadSystemBlock(Role role){super(role==Role.ENTRY_SENSOR||role==Role.EXIT_SENSOR||role==Role.SPACE_SENSOR?NgTraffic.metal().noOcclusion().noCollission():NgTraffic.metal().noOcclusion(),role!=Role.CONTROLLER&&role!=Role.AGGREGATOR&&role!=Role.BARRIER);this.role=role;}
    @Override public boolean canMount(){return role==Role.TRAFFIC_SIGN||role==Role.LANE_SIGN||role==Role.PARKING_GUIDE||role==Role.PARKING_BOARD||role==Role.HEIGHT_SIGN;}
    @Override public double mountDepth(){return 2.5;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new RoadSystemEntity(pos,state);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){if(level.isClientSide||type!=NgTraffic.ROAD_SYSTEM_ENTITY.get())return null;return (world,pos,s,e)->{if(e instanceof RoadSystemEntity node)node.tick();};}
    @Override public VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context){return role==Role.BARRIER?box(0,0,0,16,16,16):role==Role.ENTRY_SENSOR||role==Role.EXIT_SENSOR||role==Role.SPACE_SENSOR?box(0,0,0,16,2,16):super.getShape(state,world,pos,context);}
    @Override public VoxelShape getCollisionShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context){
        if(role==Role.BARRIER)return state.getValue(OPEN)?Shapes.empty():state.getValue(FACING).getAxis()==net.minecraft.core.Direction.Axis.X?box(6,7,0,10,11,16):box(0,7,6,16,11,10);
        return super.getCollisionShape(state,world,pos,context);
    }
    @Override public void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moved){
        if(!state.is(replacement.getBlock())&&level.getBlockEntity(pos) instanceof RoadSystemEntity node&&!node.controller())node.detach();
        super.onRemove(state,level,pos,replacement,moved);
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(hand!=InteractionHand.MAIN_HAND||!player.getItemInHand(hand).isEmpty())return InteractionResult.PASS;
        if(!player.getAbilities().mayBuild||!level.mayInteract(player,pos))return InteractionResult.FAIL;
        if(player instanceof ServerPlayer server&&level.getBlockEntity(pos) instanceof RoadSystemEntity node)server.openMenu(new SimpleMenuProvider((id,inventory,p)->new RoadSystemMenu(id,inventory,node),Component.translatable("menu.ng_verkehr.road_system")),b->RoadSystemMenu.writeSnapshot(b,node));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
