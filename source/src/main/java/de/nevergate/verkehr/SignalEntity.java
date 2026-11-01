package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SignalEntity extends BlockEntity {
    private BlockPos controller;
    private int group;
    private boolean automatic;
    private long connectionGeneration;
    private int approach=-1, movement;
    public SignalEntity(BlockPos pos,BlockState state) { super(NgTraffic.SIGNAL_ENTITY.get(),pos,state);if(kind().movement>=0){approach=0;movement=kind().movement;} }
    public SignalKind kind() { return ((SignalBlock)getBlockState().getBlock()).kind; }
    public boolean fixedRole() { return kind()!=SignalKind.VEHICLE&&kind()!=SignalKind.MOBILE; }
    public int menuType() { return kind().menuType; }
    public boolean isLeftArrow() { return getBlockState().is(NgTraffic.SIGNAL_LEFT.get()); }
    public void link(BlockPos pos,int group) { automatic=false; this.controller=pos.immutable(); this.group=group==1?1:0; approach=fixedRole()?this.group:-1; movement=fixedRole()?kind().movement:0; rememberGeneration();setChanged(); tick(); }
    private void rememberGeneration() {
        connectionGeneration=0;
        if(level!=null&&controller!=null&&level.hasChunkAt(controller)&&level.getBlockEntity(controller) instanceof DeviceEntity device&&device.isController()){
            device.allowConnection(worldPosition);connectionGeneration=device.connectionGeneration();
        }
    }
    public void disconnect() { controller=null;automatic=false;connectionGeneration=0;setChanged();tick(); }
    public boolean hasController() { return controller!=null; }
    public boolean automatic() { return automatic; }
    public void autoLink(BlockPos pos,int approach) {
        controller=pos.immutable();this.approach=approach;group=approach%2;
        movement=fixedRole()?kind().movement:0;automatic=true;rememberGeneration();setChanged();tick();
    }
    public boolean linkedTo(BlockPos pos) { return pos.equals(controller); }
    public int group() { return group; }
    public int approach() { return approach; }
    public int movement() { return movement; }
    public void assignRoute(int approach,int movement) {
        automatic=false;this.approach=Math.max(-1,Math.min(3,approach)); this.movement=this.approach<0?0:Math.max(0,Math.min(2,movement));
        if(fixedRole()){this.approach=Math.max(0,this.approach);this.movement=kind().movement;}
        if(this.approach>=0) group=this.approach%2;
        setChanged();tick();
    }
    public void tick() {
        if(level==null||level.isClientSide) return;
        int light=0;
        if(controller!=null&&level.hasChunkAt(controller)&&level.getBlockEntity(controller) instanceof DeviceEntity d&&d.isController()) {
            if(!d.validConnection(worldPosition,connectionGeneration)){disconnect();return;}
            d.registerDevice(worldPosition);
            if(kind()==SignalKind.WARNING) light=d.isEnabled()&&Math.floorMod(level.getGameTime(),20)<10?3:0;
            else light=d.signalLight(this);
        }
        if(getBlockState().getValue(SignalBlock.LIGHT)!=light) level.setBlock(worldPosition,getBlockState().setValue(SignalBlock.LIGHT,light),3);
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if(controller!=null) tag.putLong("Controller",controller.asLong()); tag.putBoolean("Automatic",automatic);tag.putLong("ConnectionGeneration",connectionGeneration); tag.putInt("Group",group); tag.putInt("Approach",approach); tag.putInt("Movement",movement); }
    @Override public void load(CompoundTag tag) { super.load(tag);connectionGeneration=tag.getLong("ConnectionGeneration"); automatic=tag.getBoolean("Automatic"); controller=tag.contains("Controller")?BlockPos.of(tag.getLong("Controller")):null; group=Math.max(0,Math.min(1,tag.getInt("Group"))); approach=tag.contains("Approach")?Math.max(-1,Math.min(3,tag.getInt("Approach"))):-1; movement=approach<0?0:Math.max(0,Math.min(2,tag.getInt("Movement")));if(fixedRole()){approach=Math.max(0,approach);movement=kind().movement;} }
}
