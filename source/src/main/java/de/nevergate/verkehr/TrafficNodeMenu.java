package de.nevergate.verkehr;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class TrafficNodeMenu extends AbstractContainerMenu {
    public final BlockPos pos;public final TrafficNodeBlock.Kind kind;private final TrafficNodeEntity node;private final ContainerData data;
    public TrafficNodeMenu(int id,Inventory inv,FriendlyByteBuf b){super(NgTraffic.TRAFFIC_NODE_MENU.get(),id);pos=b.readBlockPos();kind=b.readEnum(TrafficNodeBlock.Kind.class);node=null;data=new SimpleContainerData(20);addDataSlots(data);}
    public TrafficNodeMenu(int id,Inventory inv,TrafficNodeEntity n){
        super(NgTraffic.TRAFFIC_NODE_MENU.get(),id);node=n;pos=n.getBlockPos();kind=n.kind();
        data=new ContainerData(){public int get(int i){return switch(i){case 0->n.mode();case 1->n.limit();case 2->n.radius();case 3->n.threshold();case 4->n.delay();case 5->n.group();case 6->n.direction();case 7->n.status();case 8->n.speed();case 9->n.slow();case 10->n.forward()&65535;case 11->n.forward()>>>16;case 12->n.reverse()&65535;case 13->n.reverse()>>>16;case 14->n.online()?1:0;case 15->n.alarm()?1:0;case 16->n.hourly()&65535;case 17->n.hourly()>>>16;case 18->n.connectionCount();case 19->n.canEdit(inv.player)?1:0;default->0;};}public void set(int i,int v){}public int getCount(){return 20;}};addDataSlots(data);
    }
    public int value(int i){return data.get(i);}
    public int count(int i){return (data.get(i)&65535)|((data.get(i+1)&65535)<<16);}
    @Override public boolean stillValid(Player p){return p.level().hasChunkAt(pos)&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockEntity(pos) instanceof TrafficNodeEntity n&&(node==null||node==n);}
    @Override public boolean clickMenuButton(Player p,int b){return node!=null&&stillValid(p)&&node.action(p,b);}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
