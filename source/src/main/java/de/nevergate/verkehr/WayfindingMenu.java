package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Each editor starts from a revision; stale simultaneous saves cannot overwrite a newer edit. */
public final class WayfindingMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final WayfindingBlock.Kind kind;
    public final int revision;
    public final WayfindingData snapshot;
    private final WayfindingEntity sign;
    public WayfindingMenu(int id,Inventory inventory,FriendlyByteBuf b){
        super(NgTraffic.WAYFINDING_MENU.get(),id);pos=b.readBlockPos();kind=b.readEnum(WayfindingBlock.Kind.class);revision=b.readInt();snapshot=WayfindingNetwork.readData(b);sign=null;
    }
    public WayfindingMenu(int id,Inventory inventory,WayfindingEntity sign){
        super(NgTraffic.WAYFINDING_MENU.get(),id);this.sign=sign;pos=sign.getBlockPos();kind=sign.kind();revision=sign.revision();snapshot=sign.data();
    }
    public static void writeSnapshot(FriendlyByteBuf b,WayfindingEntity sign){b.writeBlockPos(sign.getBlockPos());b.writeEnum(sign.kind());b.writeInt(sign.revision());WayfindingNetwork.writeData(sign.data(),b);}
    @Override public boolean stillValid(Player player){
        return player.getAbilities().mayBuild&&player.level().hasChunkAt(pos)&&player.level().mayInteract(player,pos)&&player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&player.level().getBlockEntity(pos) instanceof WayfindingEntity current&&(sign==null||current==sign);
    }
    public boolean save(Player player,int expectedRevision,WayfindingData data){
        if(sign==null||!stillValid(player)||expectedRevision!=revision||sign.revision()!=revision)return false;
        sign.update(data);return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
}
