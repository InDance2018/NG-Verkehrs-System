package de.nevergate.verkehr;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class TrafficNodeLinker extends Item {
    public TrafficNodeLinker(){super(new Item.Properties().stacksTo(1));}
    @Override public InteractionResult useOn(UseOnContext ctx){
        var p=ctx.getPlayer();var l=ctx.getLevel();var at=ctx.getClickedPos();
        if(p==null||!p.getAbilities().mayBuild||!l.mayInteract(p,at))return InteractionResult.FAIL;
        if(l.isClientSide)return InteractionResult.SUCCESS;
        var hit=l.getBlockEntity(at);var tag=ctx.getItemInHand().getOrCreateTag();
        if(hit instanceof TrafficNodeEntity n&&(p.isShiftKeyDown()||!tag.hasUUID("TrafficIdentity"))){
            tag.putUUID("TrafficIdentity",n.identity());tag.putLong("TrafficSource",at.asLong());tag.putString("TrafficDimension",l.dimension().location().toString());p.displayClientMessage(Component.translatable("message.ng_verkehr.traffic_selected"),true);return InteractionResult.CONSUME;
        }
        var selected=BlockPos.of(tag.getLong("TrafficSource"));
        if(!tag.hasUUID("TrafficIdentity")||!tag.getString("TrafficDimension").equals(l.dimension().location().toString())||selected.distSqr(at)>128*128||!l.hasChunkAt(selected))return fail(ctx);
        var chunk=((net.minecraft.server.level.ServerLevel)l).getChunkSource().getChunkNow(selected.getX()>>4,selected.getZ()>>4);
        if(chunk==null||!(chunk.getBlockEntity(selected) instanceof TrafficNodeEntity n)||!n.identity().equals(tag.getUUID("TrafficIdentity")))return fail(ctx);
        boolean ok=hit instanceof TrafficNodeEntity target?n.link(target,p):hit instanceof RoadSystemEntity road?n.linkRoad(road,p):hit instanceof DeviceEntity signal?n.linkSignal(signal,p):false;
        if(!ok)return fail(ctx);p.displayClientMessage(Component.translatable("message.ng_verkehr.traffic_linked"),true);return InteractionResult.CONSUME;
    }
    private InteractionResult fail(UseOnContext c){c.getPlayer().displayClientMessage(Component.translatable("message.ng_verkehr.traffic_link_failed"),true);return InteractionResult.FAIL;}
    @Override public void appendHoverText(ItemStack s,Level l,List<Component> lines,TooltipFlag f){lines.add(Component.translatable("tooltip.ng_verkehr.traffic_linker"));}
}
