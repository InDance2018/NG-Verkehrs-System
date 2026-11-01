package de.nevergate.verkehr;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Mount the button inside the existing mast cell, without a floating gap. */
public final class PedestrianButtonItem extends BlockItem {
    public PedestrianButtonItem(Block block) { super(block,new Item.Properties()); }
    @Override public InteractionResult useOn(UseOnContext context) {
        if(!context.getLevel().getBlockState(context.getClickedPos()).is(NgTraffic.CONTENT.get("post").get())&&!context.getLevel().getBlockState(context.getClickedPos()).is(NgTraffic.CONTENT.get("signal_mast").get())) return super.useOn(context);
        var player=context.getPlayer();
        if(player==null || !player.getAbilities().mayBuild) return InteractionResult.FAIL;
        BlockPos mast=context.getClickedPos().immutable();
        // Normal BlockItem placement performs consumption, placement sound,
        // collision validation and block-entity creation at the mast position.
        return place(new BlockPlaceContext(context) {
            @Override public BlockPos getClickedPos() { return mast; }
            @Override public boolean canPlace() { return true; }
            @Override public boolean replacingClickedOnBlock() { return true; }
        });
    }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag) {
        super.appendHoverText(stack,level,lines,flag);
        lines.add(Component.translatable("tooltip.ng_verkehr.button_mount"));
    }
}
