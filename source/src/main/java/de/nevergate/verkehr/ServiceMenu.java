package de.nevergate.verkehr;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class ServiceMenu extends AbstractContainerMenu {
    public final BlockPos pos;public final ServiceBlock.Kind kind;private final ServiceEntity node;private final ContainerData data;
    public ServiceMenu(int id,Inventory i,FriendlyByteBuf b){super(NgTraffic.SERVICE_MENU.get(),id);pos=b.readBlockPos();kind=b.readEnum(ServiceBlock.Kind.class);node=null;data=new SimpleContainerData(23);addDataSlots(data);}
    public ServiceMenu(int id,Inventory inv,ServiceEntity n){
        super(NgTraffic.SERVICE_MENU.get(),id);node=n;pos=n.getBlockPos();kind=n.kind();
        data=new ContainerData(){public int get(int i){return switch(i){case 0->n.online()?1:0;case 1->n.alarm()?1:0;case 2->n.green()?1:0;case 3->n.waiting()?1:0;case 4->n.rate()&65535;case 5->n.rate()>>>16;case 6->n.freeMinutes();case 7->n.cap()&65535;case 8->n.cap()>>>16;case 9->n.duration();case 10->n.group();case 11->n.height();case 12->n.limit();case 13->n.interval();case 14->n.free()&65535;case 15->n.free()>>>16;case 16->n.faults();case 17->n.canEdit(inv.player)?1:0;case 18->n.linked();case 19->(int)n.quote(inv.player)&65535;case 20->(int)n.quote(inv.player)>>>16;case 21->n.effectiveSpeed();case 22->n.mode();default->0;};}public void set(int i,int v){}public int getCount(){return 23;}};addDataSlots(data);
    }
    public int value(int i){return data.get(i);}public int count(int i){return (data.get(i)&65535)|((data.get(i+1)&65535)<<16);}
    @Override public boolean stillValid(Player p){return p.level().hasChunkAt(pos)&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockEntity(pos) instanceof ServiceEntity n&&(node==null||node==n);}
    @Override public boolean clickMenuButton(Player p,int b){return node!=null&&stillValid(p)&&node.action(p,b);}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
