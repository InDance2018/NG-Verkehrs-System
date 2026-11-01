package de.nevergate.verkehr;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ConstructionLinker extends Item {
    public ConstructionLinker(){super(new Item.Properties().stacksTo(1));}
    @Override public InteractionResult useOn(UseOnContext ctx){
        var p=ctx.getPlayer();var level=ctx.getLevel();var pos=ctx.getClickedPos();
        if(p==null||!p.getAbilities().mayBuild||!level.mayInteract(p,pos))return InteractionResult.FAIL;
        if(level.isClientSide)return InteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof ConstructionEntity node)||node.kind()!=ConstructionBlock.Kind.LAMP)return InteractionResult.PASS;
        var tag=ctx.getItemInHand().getOrCreateTag();
        if(p.isShiftKeyDown()||!tag.hasUUID("WorkLeader")){
            if(node.control()==null)node.disconnect(p);
            if(node.control()!=node)return InteractionResult.FAIL;
            tag.putUUID("WorkLeader",node.identity());tag.putLong("WorkPos",pos.asLong());tag.putString("WorkDimension",level.dimension().location().toString());
            p.displayClientMessage(Component.translatable("message.ng_verkehr.work_selected"),true);return InteractionResult.CONSUME;
        }
        var cp=BlockPos.of(tag.getLong("WorkPos"));
        boolean ok=tag.getString("WorkDimension").equals(level.dimension().location().toString())&&pos.distSqr(cp)<=64*64&&level.hasChunkAt(cp)&&level.getBlockEntity(cp) instanceof ConstructionEntity leader&&leader.identity().equals(tag.getUUID("WorkLeader"))&&leader.attach(node,p);
        p.displayClientMessage(Component.translatable(ok?"message.ng_verkehr.work_linked":"message.ng_verkehr.work_link_failed"),true);
        return ok?InteractionResult.CONSUME:InteractionResult.FAIL;
    }
    @Override public void appendHoverText(ItemStack s,Level l,List<Component> lines,TooltipFlag f){lines.add(Component.translatable("tooltip.ng_verkehr.work_linker"));}
}
