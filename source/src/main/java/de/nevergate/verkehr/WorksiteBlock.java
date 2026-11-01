package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/** Independent portable warning equipment. Scheduled ticks persist with its chunk. */
public final class WorksiteBlock extends OrientedBlock {
    public static final IntegerProperty MODE=IntegerProperty.create("mode",0,2);
    public static final BooleanProperty LIT=BooleanProperty.create("lit");
    public final boolean arrow;
    public WorksiteBlock(boolean arrow) {
        super(NgTraffic.metal().noOcclusion().lightLevel(s->s.getValue(LIT)?10:0),false);
        this.arrow=arrow;registerDefaultState(defaultBlockState().setValue(MODE,arrow?1:2).setValue(LIT,true));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { super.createBlockStateDefinition(b);b.add(MODE,LIT); }
    public boolean shouldLight(BlockState state,long tick) {
        return state.getValue(MODE)!=0&&(!arrow&&state.getValue(MODE)==1||Math.floorMod(tick,20)<10);
    }
    private void update(ServerLevel level,BlockPos pos,BlockState state) {
        boolean lit=shouldLight(state,level.getGameTime());
        if(state.getValue(LIT)!=lit)level.setBlock(pos,state.setValue(LIT,lit),3);
        if(state.getValue(MODE)!=0)level.scheduleTick(pos,this,5);
    }
    @Override public void onPlace(BlockState state,Level level,BlockPos pos,BlockState old,boolean moved) {
        super.onPlace(state,level,pos,old,moved);
        if(level instanceof ServerLevel server&&!old.is(this))update(server,pos,state);
    }
    @Override public void tick(BlockState state,ServerLevel level,BlockPos pos,RandomSource random) { update(level,pos,state); }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(hand!=InteractionHand.MAIN_HAND||player.getItemInHand(hand).is(NgTraffic.SIGNAL_LINKER.get()))return InteractionResult.PASS;
        if(!player.getAbilities().mayBuild)return InteractionResult.FAIL;
        if(level instanceof ServerLevel server){
            int mode=(state.getValue(MODE)+1)%3;state=state.setValue(MODE,mode);level.setBlock(pos,state,3);update(server,pos,state);
            String key=mode==0?"work_off":arrow?(mode==1?"work_left":"work_right"):(mode==1?"work_steady":"work_blink");
            player.displayClientMessage(Component.translatable("message.ng_verkehr."+key),true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return EquipmentShapes.get(arrow?"work_arrow_board":"work_lamp",state);
    }
}
