package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class PedestrianBlock extends MountedBlock implements EntityBlock {
    public static final BooleanProperty ACTIVE=BooleanProperty.create("active");
    public enum Mount implements StringRepresentable {
        FREE, WALL, MAST, HEAVY_MAST;
        @Override public String getSerializedName() { return name().toLowerCase(java.util.Locale.ROOT); }
    }
    public static final EnumProperty<Mount> MOUNT=EnumProperty.create("mount",Mount.class);
    private final boolean button;
    public PedestrianBlock(boolean button) {
        super(NgTraffic.metal().noOcclusion().lightLevel(s->button?(s.getValue(ACTIVE)?5:0):15),true);
        this.button=button; registerDefaultState(defaultBlockState().setValue(ACTIVE,false).setValue(MOUNT,Mount.FREE).setValue(JOIN,Join.NONE));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> b) { super.createBlockStateDefinition(b); b.add(ACTIVE,MOUNT,JOIN); }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        var state=super.getStateForPlacement(context);
        if(!button) return state;
        var face=context.getClickedFace();
        if(face.getAxis().isHorizontal()) state=state.setValue(FACING,face);
        Mount mount=context.getLevel().getBlockState(context.getClickedPos()).is(NgTraffic.CONTENT.get("signal_mast").get())?Mount.HEAVY_MAST:context.getLevel().getBlockState(context.getClickedPos()).is(NgTraffic.CONTENT.get("post").get())?Mount.MAST:
            face.getAxis().isHorizontal()?Mount.WALL:Mount.FREE;
        return state.setValue(MOUNT,mount);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new PedestrianEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide || type!=NgTraffic.PEDESTRIAN_ENTITY.get()) return null;
        return (world,pos,s,entity)-> { if(entity instanceof PedestrianEntity p) p.tick(); };
    }
    @Override public double mountDepth(){return 5.5;}
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        if(button) {
            Mount mount=state.getValue(MOUNT);
            double front=mount==Mount.HEAVY_MAST?4.25:mount==Mount.MAST?4.75:mount==Mount.WALL?13.75:6;
            double back=front+2.25, low=mount==Mount.FREE?0:5;
            VoxelShape body=switch(state.getValue(FACING)) {
                case SOUTH -> box(5.5,low,16-back,10.5,low+6,16-front);
                case EAST -> box(16-back,low,5.5,16-front,low+6,10.5);
                case WEST -> box(front,low,5.5,back,low+6,10.5);
                default -> box(5.5,low,front,10.5,low+6,back);
            };
            if(mount==Mount.HEAVY_MAST)return Shapes.or(body,box(6.5,0,6.5,9.5,16,9.5));
            return mount==Mount.MAST?Shapes.or(body,box(7,0,7,9,16,9)):body;
        }
        return attachedShape(state,level,pos,context,state.getValue(FACING).getAxis()==net.minecraft.core.Direction.Axis.Z
            ? box(4.5,0,4.25,11.5,16,11.75):box(4.25,0,4.5,11.75,16,11.5));
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(!button || hand!=InteractionHand.MAIN_HAND || player.getItemInHand(hand).is(NgTraffic.SIGNAL_LINKER.get())) return InteractionResult.PASS;
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof PedestrianEntity entity) {
            var device=entity.controller(); String key;
            if(device==null) key="message.ng_verkehr.button_unlinked";
            else if(device.nightActive()) key="message.ng_verkehr.button_night";
            else if(!device.isEnabled()) key="message.ng_verkehr.button_stopped";
            else key=device.requestPedestrians(entity.group())?"message.ng_verkehr.ped_requested":"message.ng_verkehr.ped_wait";
            entity.tick(); player.displayClientMessage(Component.translatable(key,entity.group()==0?"A":"B"),true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
