package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PedestrianEntity extends BlockEntity {
    private BlockPos controller;
    private int group;
    private boolean automatic;
    private long connectionGeneration;
    public PedestrianEntity(BlockPos pos,BlockState state) { super(NgTraffic.PEDESTRIAN_ENTITY.get(),pos,state); }
    public void link(BlockPos pos) { link(pos,0); }
    public void link(BlockPos pos,int group) { automatic=false;controller=pos.immutable(); this.group=group==1?1:0; rememberGeneration();setChanged(); tick(); }
    private void rememberGeneration() {
        connectionGeneration=0;
        if(level!=null&&controller!=null&&level.hasChunkAt(controller)&&level.getBlockEntity(controller) instanceof DeviceEntity device&&device.isController()){
            device.allowConnection(worldPosition);connectionGeneration=device.connectionGeneration();
        }
    }
    public void disconnect() { controller=null;automatic=false;connectionGeneration=0;setChanged();tick(); }
    public boolean hasController() { return controller!=null; }
    public boolean automatic() { return automatic; }
    public boolean isButton() { return getBlockState().is(NgTraffic.PEDESTRIAN_BUTTON.get()); }
    public void autoLink(BlockPos pos,int group) { controller=pos.immutable();this.group=group==1?1:0;automatic=true;rememberGeneration();setChanged();tick(); }
    public int group() { return group; }
    public boolean linkedTo(BlockPos pos) { return pos.equals(controller); }
    public void assignGroup(int group) { automatic=false;this.group=group==1?1:0; setChanged();tick(); }
    public DeviceEntity controller() {
        if(level==null || controller==null || !level.hasChunkAt(controller)) return null;
        if(level.getBlockEntity(controller) instanceof DeviceEntity d&&d.isController()){
            if(!d.validConnection(worldPosition,connectionGeneration)){disconnect();return null;}return d;
        }
        return null;
    }
    public void tick() {
        if(level==null || level.isClientSide) return;
        var device=controller();
        if(device!=null) device.registerDevice(worldPosition);
        boolean button=getBlockState().getBlock()==NgTraffic.PEDESTRIAN_BUTTON.get();
        boolean active=device!=null && (button?device.pedestrianBusy(group):device.pedestrianGreen(group));
        if(getBlockState().getValue(PedestrianBlock.ACTIVE)!=active)
            level.setBlock(worldPosition,getBlockState().setValue(PedestrianBlock.ACTIVE,active),3);
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); if(controller!=null) tag.putLong("Controller",controller.asLong()); tag.putInt("Group",group);tag.putBoolean("Automatic",automatic);tag.putLong("ConnectionGeneration",connectionGeneration); }
    @Override public void load(CompoundTag tag) { super.load(tag);connectionGeneration=tag.getLong("ConnectionGeneration"); automatic=tag.getBoolean("Automatic");controller=tag.contains("Controller")?BlockPos.of(tag.getLong("Controller")):null; group=tag.getInt("Group")==1?1:0; }
}
