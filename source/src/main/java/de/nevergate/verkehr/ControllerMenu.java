package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Live atomic snapshots; revision-bound row selection never aliases another device. */
public final class ControllerMenu extends AbstractContainerMenu {
    public final BlockPos controllerPos;
    public List<BlockPos> positions;
    private final Level level;
    private final DeviceEntity controller;
    private final int[] values,published;
    private int revision,wireSequence,requestBytes,requestedRevision;
    private boolean selectionReady;
    private int selected=-1,page,settingFrame=-1;
    private long resetUntil=-1;
    public static final int HEADER=28, PAGE_SIZE=96, ROW_ACTION=32, MAX_DEVICES=256, ROW_DATA=8;
    public static final int COUNT=HEADER+MAX_DEVICES*ROW_DATA, REV_LOW=COUNT+1, REV_HIGH=COUNT+2, COMMIT=COUNT+3;

    public ControllerMenu(int id,Inventory inventory,FriendlyByteBuf buffer) {
        this(id,inventory,buffer.readBlockPos(),readPositions(buffer),null);
    }
    public ControllerMenu(int id,Inventory inventory,DeviceEntity controller,List<BlockPos> positions) {
        this(id,inventory,controller.getBlockPos(),positions,controller);
    }
    private ControllerMenu(int id,Inventory inventory,BlockPos pos,List<BlockPos> positions,DeviceEntity controller) {
        super(NgTraffic.CONTROLLER_MENU.get(),id);this.level=inventory.player.level();this.controller=controller;
        this.controllerPos=pos.immutable();this.positions=List.copyOf(positions);this.values=new int[COMMIT+1];this.published=new int[COMMIT+1];
        if(!positions.isEmpty()) selected=0;
        refresh();
        addDataSlots(new ContainerData() {
            public int get(int index) { return values[index]; }
            public void set(int index,int value) { values[index]=value;if(index==COMMIT&&controller==null)publishSnapshot(); }
            public int getCount() { return values.length; }
        });
    }
    private static List<BlockPos> readPositions(FriendlyByteBuf buffer) {
        int count=buffer.readVarInt();if(count<0||count>256)throw new IllegalArgumentException("Invalid device count");
        var list=new ArrayList<BlockPos>();for(int i=0;i<count;i++)list.add(buffer.readBlockPos());return list;
    }
    public static void writeSnapshot(FriendlyByteBuf buffer,DeviceEntity device,List<BlockPos> positions) {
        buffer.writeBlockPos(device.getBlockPos());buffer.writeVarInt(positions.size());positions.forEach(buffer::writeBlockPos);
    }
    private int[] data() { return controller==null?published:values; }
    public int value(int index) { return data()[index]; }
    public int revision() { return revision; }
    public int deviceType(int index) { return index>=0&&index<positions.size()?data()[HEADER+index*ROW_DATA]:3; }
    public int deviceStatus(int index) { return index>=0&&index<positions.size()?data()[HEADER+index*ROW_DATA+1]:0; }
    private static int combine(int low,int high) { return (low&65535)|((high&65535)<<16); }
    private void publishSnapshot() {
        int count=values[COUNT];if(count<0||count>MAX_DEVICES)return;
        var next=new ArrayList<BlockPos>();
        for(int i=0;i<count;i++){
            int base=HEADER+i*ROW_DATA;
            next.add(new BlockPos(combine(values[base+2],values[base+3]),combine(values[base+4],values[base+5]),combine(values[base+6],values[base+7])));
        }
        System.arraycopy(values,0,published,0,values.length);positions=List.copyOf(next);revision=combine(values[REV_LOW],values[REV_HIGH]);
    }
    private void reconcile() {
        if(controller==null)return;
        var next=controller.devices();if(next.equals(positions))return;
        BlockPos selectedPos=selected>=0&&selected<positions.size()?positions.get(selected):null;
        positions=List.copyOf(next);selected=selectedPos==null?-1:positions.indexOf(selectedPos);
        revision=(revision+1)&0xFFFFFFF;selectionReady=false;resetUntil=-1;
        page=Math.min(page,Math.max(0,(positions.size()-1)/PAGE_SIZE));
    }
    private void refreshLive() { reconcile();refresh(); }
    private BlockEntity target(int index) {
        if(index<0||index>=positions.size()||!level.hasChunkAt(positions.get(index)))return null;
        var entity=level.getBlockEntity(positions.get(index));
        return entity instanceof SignalEntity s&&s.linkedTo(controllerPos) || entity instanceof PedestrianEntity p&&p.linkedTo(controllerPos)?entity:null;
    }
    private void refresh() {
        if(controller==null)return;
        values[0]=controller.isEnabled()?1:0;values[1]=selected;values[2]=-1;values[3]=0;values[4]=0;values[5]=-1;
        values[6]=controller.pedestrianBusy(0)?1:0;values[7]=controller.pedestrianBusy(1)?1:0;
        values[8]=page;values[9]=controller.autoRadius();values[10]=controller.autoEnabled()?1:0;values[11]=controller.autoCenter()!=null?1:0;
        values[12]=controller.autoUnclear();values[13]=controller.hasAutoAnchor()?1:0;values[14]=resetUntil>=level.getGameTime()?1:0;
        for(int field=0;field<7;field++)values[15+field]=controller.setting(field);
        values[22]=controller.phase();values[23]=controller.nextPhase();values[24]=controller.remaining();
        for(int slot=0;slot<3;slot++)values[25+slot]=controller.presetPresent(slot)?1:0;
        for(int i=0;i<positions.size();i++) {
            var entity=target(i);int type=level.hasChunkAt(positions.get(i))?3:4,status=0;
            if(entity instanceof SignalEntity s){type=s.menuType();status=s.getBlockState().getValue(SignalBlock.LIGHT);}
            else if(entity instanceof PedestrianEntity p){type=p.getBlockState().is(NgTraffic.PEDESTRIAN_BUTTON.get())?2:1;status=p.getBlockState().getValue(PedestrianBlock.ACTIVE)?1:0;}
            int base=HEADER+i*ROW_DATA;values[base]=type;values[base+1]=status;
            var pos=positions.get(i);int[] coordinates={pos.getX(),pos.getY(),pos.getZ()};
            for(int axis=0;axis<3;axis++){values[base+2+axis*2]=coordinates[axis]&65535;values[base+3+axis*2]=coordinates[axis]>>>16;}
            if(i==selected){values[5]=type;if(entity instanceof SignalEntity s){values[2]=s.approach();values[3]=s.movement();values[4]=s.group();}else if(entity instanceof PedestrianEntity p)values[4]=p.group();}
        }
        values[COUNT]=positions.size();values[REV_LOW]=revision&65535;values[REV_HIGH]=revision>>>16;
        values[COMMIT]=++wireSequence&65535;
    }
    @Override public void broadcastChanges() { refreshLive();super.broadcastChanges(); }
    @Override public boolean stillValid(Player player) {
        return player.getAbilities().mayBuild && level==player.level() && player.distanceToSqr(controllerPos.getX()+.5,controllerPos.getY()+.5,controllerPos.getZ()+.5)<=64 &&
            level.getBlockEntity(controllerPos) instanceof DeviceEntity d&&d.isController()&&(controller==null||d==controller);
    }
    @Override public boolean clickMenuButton(Player player,int action) {
        if(controller==null||!stillValid(player))return false;
        // Four base-128 digits carry the client's list revision. Consume the
        // whole frame even if discovery changes the list between these packets.
        if(requestBytes>0){
            if(action<0||action>127){requestBytes=0;selectionReady=false;return false;}
            requestedRevision=(requestedRevision<<7)|action;
            if(--requestBytes==0)selectionReady=requestedRevision==revision;
            return true;
        }
        // Dedicated two-packet settings frame; never aliases row selections.
        if(settingFrame>=0){int field=settingFrame;if(field==9){settingFrame=action>=0&&action<=8?action:-1;return settingFrame>=0;}settingFrame=-1;if(action<0||action>127)return false;
            if(field<7)controller.changeSetting(field,action);else if(field==7)controller.savePreset(action);else if(field==8)controller.loadPreset(action);
            refreshLive();return true;
        }
        if(action==26){settingFrame=9;selectionReady=false;resetUntil=-1;return true;}
        if(action==25){requestBytes=4;requestedRevision=0;selectionReady=false;resetUntil=-1;return true;}
        if(action==21){resetUntil=level.getGameTime()+100;refreshLive();return true;}
        if(action==22){if(resetUntil<level.getGameTime())return false;resetUntil=-1;controller.disconnectAll();refreshLive();return true;}
        resetUntil=-1;
        if(action==23){refreshLive();return true;}
        if(action==24){controller.rediscoverDisconnected();refreshLive();return true;}
        if(action==20){if(selected<0||selected>=positions.size())return false;boolean changed=controller.disconnectDevice(positions.get(selected));refreshLive();return changed;}
        // Selection banks are a wire detail only; the client shows one scrollable table.
        if(action>=8&&action<=10){int bank=action-8;if(!selectionReady||requestedRevision!=revision||bank*PAGE_SIZE>=positions.size())return false;page=bank;refreshLive();return true;}
        if(action>=ROW_ACTION&&action<ROW_ACTION+PAGE_SIZE){int index=page*PAGE_SIZE+action-ROW_ACTION;if(!selectionReady||requestedRevision!=revision||index>=positions.size())return false;selectionReady=false;selected=index;refreshLive();return true;}
        if(action==1){controller.setAutoEnabled(!controller.autoEnabled());refreshLive();return true;}
        if(action==2){controller.scanSignals();refreshLive();return true;}
        if(action>=16&&action<=19){controller.setAutoRadius(DeviceEntity.AUTO_RADII[action-16]);refreshLive();return true;}
        if(action==0){controller.setEnabled(!controller.isEnabled());refreshLive();return true;}
        var entity=target(selected);
        if(action>=11&&action<=15&&entity instanceof SignalEntity s&&s.kind()!=SignalKind.WARNING){
            if(action==15&&s.fixedRole())return false;
            int approach=action==15?-1:action-11;
            if(s.approach()!=approach){controller.setEnabled(false);s.assignRoute(approach,s.movement());}
        }
        else if((action==6||action==7)&&entity instanceof PedestrianEntity p){
            if(p.group()!=action-6){controller.setEnabled(false);p.assignGroup(action-6);}
        }
        else if(action==3&&entity instanceof SignalEntity s&&s.kind()!=SignalKind.WARNING){controller.setEnabled(false);s.assignRoute(s.approach()==3?(s.fixedRole()?0:-1):s.approach()+1,s.movement());}
        else if(action==4&&entity instanceof SignalEntity s&&!s.fixedRole()){controller.setEnabled(false);s.assignRoute(Math.max(0,s.approach()),(s.movement()+1)%3);}
        else if(action==5&&entity instanceof PedestrianEntity p){controller.setEnabled(false);p.assignGroup(1-p.group());}
        else return false;
        refreshLive();return true;
    }
    @Override public ItemStack quickMoveStack(Player player,int index) { return ItemStack.EMPTY; }
}
