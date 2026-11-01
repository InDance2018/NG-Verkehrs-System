package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WayfindingBlock extends MountedBlock implements EntityBlock {
    public enum Kind { TOWN, TOWN_EXIT, ROAD, MOTORWAY, EXIT, DISTANCE, DETOUR }
    public final Kind kind;
    public WayfindingBlock(Kind kind){super(NgTraffic.metal().noOcclusion(),true);this.kind=kind;}
    @Override public double mountDepth(){return 2.5;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new WayfindingEntity(pos,state);}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(hand!=InteractionHand.MAIN_HAND||!player.getItemInHand(hand).isEmpty())return InteractionResult.PASS;
        if(!player.getAbilities().mayBuild||!level.mayInteract(player,pos))return InteractionResult.FAIL;
        if(player instanceof ServerPlayer server&&level.getBlockEntity(pos) instanceof WayfindingEntity sign)
            server.openMenu(new SimpleMenuProvider((id,inventory,p)->new WayfindingMenu(id,inventory,sign),Component.translatable("menu.ng_verkehr.wayfinding")),b->WayfindingMenu.writeSnapshot(b,sign));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
