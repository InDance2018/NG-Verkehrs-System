package de.nevergate.verkehr;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
public final class OperationsMenu extends AbstractContainerMenu {
    public final BlockPos pos;public final OperationsBlock.Kind kind;private final OperationsEntity node;private final ContainerData data;
    public OperationsMenu(int id,Inventory inv,FriendlyByteBuf b){super(NgTraffic.OPERATIONS_MENU.get(),id);pos=b.readBlockPos();kind=b.readEnum(OperationsBlock.Kind.class);node=null;data=new SimpleContainerData(20);addDataSlots(data);}
    public OperationsMenu(int id,Inventory inv,OperationsEntity n){super(NgTraffic.OPERATIONS_MENU.get(),id);pos=n.getBlockPos();kind=n.kind();node=n;data=new ContainerData(){public int getCount(){return 20;}public void set(int i,int v){}public int get(int i){return switch(i){case 0->n.online()?1:0;case 1->n.alarm()?1:0;case 2->n.open()?1:0;case 3->n.progress();case 4->n.state();case 5->n.faults();case 6->n.activeUnits();case 7->n.amount()&65535;case 8->n.program();case 9->n.startHour();case 10->n.endHour();case 11->n.headway();case 12->n.speed();case 13->n.direction();case 14->n.linked();case 15->n.canEdit(inv.player)?1:0;case 16->n.enabled()?1:0;case 17->n.freeService()?1:0;case 18->n.visibility()?1:0;case 19->n.phase();default->0;};}};addDataSlots(data);}
    public int value(int i){return data.get(i);}public int amount(){return value(7)&65535;}
    @Override public boolean stillValid(Player p){return p.level().hasChunkAt(pos)&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockEntity(pos) instanceof OperationsEntity n&&n.kind()==kind&&(node==null||node==n);}
    @Override public boolean clickMenuButton(Player p,int button){return node!=null&&stillValid(p)&&node.action(p,button);}
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
