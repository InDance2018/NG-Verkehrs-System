package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public final class DeviceBlock extends OrientedBlock implements EntityBlock {
    public enum Kind { CONTROLLER, DISPLAY, PARKING }
    public final Kind kind;
    public DeviceBlock(Kind kind) { super(NgTraffic.metal().noOcclusion(),kind!=Kind.CONTROLLER); this.kind=kind; }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new DeviceEntity(pos,state); }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide || kind!=Kind.CONTROLLER || type!=NgTraffic.DEVICE.get()) return null;
        return (world,pos,s,entity)-> { if(entity instanceof DeviceEntity device) device.tick(); };
    }
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(kind!=Kind.CONTROLLER || hand!=InteractionHand.MAIN_HAND || !player.getItemInHand(hand).isEmpty()) return InteractionResult.PASS;
        if(!player.getAbilities().mayBuild) return InteractionResult.FAIL;
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof DeviceEntity device) {
            if(player.isShiftKeyDown()) {
                device.setEnabled(!device.isEnabled());
                player.displayClientMessage(Component.translatable(device.isEnabled()?"message.ng_verkehr.controller_started":"message.ng_verkehr.controller_stopped"),true);
            } else if(player instanceof ServerPlayer server) {
                var snapshot=device.devices();
                server.openMenu(new SimpleMenuProvider((id,inventory,p)->new ControllerMenu(id,inventory,device,snapshot),Component.translatable("menu.ng_verkehr.controller")),buffer->ControllerMenu.writeSnapshot(buffer,device,snapshot));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
