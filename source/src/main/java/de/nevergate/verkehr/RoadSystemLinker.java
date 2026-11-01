package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class RoadSystemLinker extends Item {
    public RoadSystemLinker(){super(new Item.Properties().stacksTo(1));}
    @Override public InteractionResult useOn(UseOnContext context){
        var player=context.getPlayer();var world=context.getLevel();var pos=context.getClickedPos();if(player==null||!player.getAbilities().mayBuild||!world.mayInteract(player,pos))return InteractionResult.FAIL;
        if(!(world.getBlockEntity(pos) instanceof RoadSystemEntity node))return InteractionResult.PASS;
        if(world.isClientSide)return InteractionResult.SUCCESS;
        var tag=context.getItemInHand().getOrCreateTag();
        if(node.controller()){
            if(!node.aggregator()&&!player.isShiftKeyDown()&&tag.contains("RoadController")&&tag.getString("RoadDimension").equals(world.dimension().location().toString())){
                var selected=BlockPos.of(tag.getLong("RoadController"));
                if(world.hasChunkAt(selected)&&world.mayInteract(player,selected)&&world.getBlockEntity(selected) instanceof RoadSystemEntity collector&&collector.aggregator()){
                    boolean linked=collector.attachZone(node);player.displayClientMessage(Component.translatable(linked?"message.ng_verkehr.zone_linked":"message.ng_verkehr.road_link_failed"),true);return linked?InteractionResult.CONSUME:InteractionResult.FAIL;
                }
            }
            tag.putLong("RoadController",pos.asLong());tag.putString("RoadDimension",world.dimension().location().toString());player.displayClientMessage(Component.translatable("message.ng_verkehr.road_selected"),true);return InteractionResult.CONSUME;
        }
        if(player.isShiftKeyDown()){node.detach();player.displayClientMessage(Component.translatable("message.ng_verkehr.road_detached"),true);return InteractionResult.CONSUME;}
        boolean linked=tag.contains("RoadController")&&tag.getString("RoadDimension").equals(world.dimension().location().toString());
        if(linked){var parent=BlockPos.of(tag.getLong("RoadController"));linked=world.hasChunkAt(parent)&&world.mayInteract(player,parent)&&world.getBlockEntity(parent) instanceof RoadSystemEntity controller&&controller.attach(node);}
        player.displayClientMessage(Component.translatable(linked?"message.ng_verkehr.road_linked":"message.ng_verkehr.road_link_failed"),true);return linked?InteractionResult.CONSUME:InteractionResult.FAIL;
    }
}
