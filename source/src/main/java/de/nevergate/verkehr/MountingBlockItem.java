package de.nevergate.verkehr;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.block.Block;
/** Vanilla placement validation and consumption, retaining the clicked support. */
public final class MountingBlockItem extends BlockItem {
    public MountingBlockItem(Block b){super(b,new Item.Properties());}
    @Override public InteractionResult useOn(UseOnContext c){
        var player=c.getPlayer();var level=c.getLevel();var pos=c.getClickedPos();
        if(!((MountedBlock)getBlock()).canMount()||getBlock() instanceof MobileSignalBlock||player==null||player.isShiftKeyDown()||MountedBlock.support(level.getBlockState(pos))==MountedBlock.Attachment.FREE)return super.useOn(c);
        if(!player.getAbilities().mayBuild||!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;
        BlockPos target=pos.immutable();
        return place(new BlockPlaceContext(c){
            @Override public BlockPos getClickedPos(){return target;}
            @Override public boolean canPlace(){return true;}
            @Override public boolean replacingClickedOnBlock(){return true;}
        });
    }
}
