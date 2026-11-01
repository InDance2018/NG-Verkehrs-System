package de.nevergate.verkehr;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Select a controller, then connect signals without typing coordinates. */
public final class SignalLinkerItem extends Item {
    public SignalLinkerItem() { super(new Item.Properties().stacksTo(1)); }

    @Override public InteractionResult useOn(UseOnContext context) {
        var player=context.getPlayer();
        if(player==null || !player.getAbilities().mayBuild) return InteractionResult.FAIL;
        var level=context.getLevel();
        if(level.isClientSide) return InteractionResult.SUCCESS;
        var pos=MobileSignalBlock.endpoint(level.getBlockState(context.getClickedPos()),context.getClickedPos());
        var stack=context.getItemInHand();
        if(level.getBlockEntity(pos) instanceof DeviceEntity device && device.isController()) {
            var tag=stack.getOrCreateTag();
            tag.putLong("SelectedController",pos.asLong());
            tag.putString("ControllerDimension",level.dimension().location().toString());
            player.displayClientMessage(Component.translatable("message.ng_verkehr.controller_selected"),true);
            return InteractionResult.CONSUME;
        }
        var target=level.getBlockEntity(pos);
        boolean centre=player.isShiftKeyDown()&&!(target instanceof SignalEntity)&&!(target instanceof PedestrianEntity);
        if(!centre&&!(target instanceof SignalEntity)&&!(target instanceof PedestrianEntity))return fail(context,"message.ng_verkehr.select_device");
        var tag=stack.getTag();
        if(tag==null || !tag.contains("SelectedController",Tag.TAG_LONG))
            return fail(context,"message.ng_verkehr.select_controller_first");
        if(!tag.getString("ControllerDimension").equals(level.dimension().location().toString()))
            return fail(context,"message.ng_verkehr.wrong_dimension");
        var controller=BlockPos.of(tag.getLong("SelectedController"));
        if(pos.distSqr(controller)>256*256)
            return fail(context,"message.ng_verkehr.controller_too_far");
        if(!level.hasChunkAt(controller) || !(level.getBlockEntity(controller) instanceof DeviceEntity device) || !device.isController())
            return fail(context,"message.ng_verkehr.controller_missing");
        if(centre){
            if(!device.setAutoCenter(pos))return fail(context,"message.ng_verkehr.controller_too_far");
            player.displayClientMessage(Component.translatable("message.ng_verkehr.center_set",device.autoRadius()),true);return InteractionResult.CONSUME;
        }
        int group=player.isShiftKeyDown()?1:0;
        if(target instanceof SignalEntity signal) {
            signal.link(controller,group);device.setAutoAnchor(signal);
            player.displayClientMessage(signal.kind()==SignalKind.WARNING?Component.translatable("message.ng_verkehr.warning_linked"):signal.fixedRole()?Component.translatable("message.ng_verkehr.route_linked",group==0?"Nord":"Ost"):Component.translatable("message.ng_verkehr.signal_linked",group==0?"A":"B"),true);
        } else if(target instanceof PedestrianEntity pedestrian) {
            pedestrian.link(controller,group);device.setAutoAnchor(pedestrian);
            player.displayClientMessage(Component.translatable("message.ng_verkehr.ped_linked",group==0?"A":"B"),true);
        }
        return InteractionResult.CONSUME;
    }

    private InteractionResult fail(UseOnContext context,String key) {
        context.getPlayer().displayClientMessage(Component.translatable(key),true);
        return InteractionResult.FAIL;
    }

    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.ng_verkehr.linker_center"));
        lines.add(Component.translatable("tooltip.ng_verkehr.linker_select"));
        lines.add(Component.translatable("tooltip.ng_verkehr.linker_connect"));
        lines.add(Component.translatable("tooltip.ng_verkehr.linker_group_b"));
        lines.add(Component.translatable("tooltip.ng_verkehr.linker_pedestrian"));
        lines.add(Component.translatable("tooltip.ng_verkehr.linker_controller"));
    }
}
