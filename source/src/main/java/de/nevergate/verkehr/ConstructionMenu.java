package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public final class ConstructionMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final ConstructionBlock.Kind kind;
    private final ConstructionEntity node;
    private final ContainerData data;
    public ConstructionMenu(int id,Inventory inv,FriendlyByteBuf b){
        super(NgTraffic.CONSTRUCTION_MENU.get(),id);pos=b.readBlockPos();kind=b.readEnum(ConstructionBlock.Kind.class);node=null;data=new SimpleContainerData(10);addDataSlots(data);
    }
    public ConstructionMenu(int id,Inventory inv,ConstructionEntity n){
        super(NgTraffic.CONSTRUCTION_MENU.get(),id);pos=n.getBlockPos();kind=n.kind();node=n;
        data=new ContainerData(){
            public int get(int i){return switch(i){case 0->n.mode();case 1->n.start();case 2->n.end();case 3->n.step();case 4->n.schedule()?1:0;case 5->n.width();case 6->n.linked();case 7->n.active()?1:0;case 8->n.permitted();case 9->n.canEdit(inv.player)?1:0;default->0;};}
            public void set(int i,int v){}public int getCount(){return 10;}
        };addDataSlots(data);
    }
    public int value(int i){return data.get(i);}
    @Override public boolean stillValid(Player p){return p.level().hasChunkAt(pos)&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&p.level().getBlockEntity(pos) instanceof ConstructionEntity e&&(node==null||e==node);}
    @Override public boolean clickMenuButton(Player p,int button){
        return node!=null&&stillValid(p)&&button>=0&&button<=6&&node.action(p,button);
    }
    @Override public ItemStack quickMoveStack(Player p,int slot){return ItemStack.EMPTY;}
}
