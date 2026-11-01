package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class WayfindingEntity extends BlockEntity {
    private WayfindingData data=new WayfindingData();
    private int revision;
    public WayfindingEntity(BlockPos pos,BlockState state){super(NgTraffic.WAYFINDING_ENTITY.get(),pos,state);}
    public WayfindingData data(){return data.copy();}
    public int revision(){return revision;}
    public WayfindingBlock.Kind kind(){return ((WayfindingBlock)getBlockState().getBlock()).kind;}
    public void update(WayfindingData value){data=value.copy();revision++;setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag){
        super.saveAdditional(tag);tag.putInt("Revision",revision);tag.putInt("LaneCount",data.laneCount);
        for(int i=0;i<WayfindingData.ROWS;i++){tag.putString("Target"+i,data.targets[i]);tag.putString("Distance"+i,data.distances[i]);tag.putInt("Direction"+i,data.directions[i]);}
        for(int i=0;i<WayfindingData.MAX_LANES;i++)tag.putInt("Lane"+i,data.lanes[i]);
    }
    @Override public void load(CompoundTag tag){
        super.load(tag);var loaded=new WayfindingData();revision=tag.getInt("Revision");
        if(tag.contains("LaneCount"))loaded.laneCount=tag.getInt("LaneCount");
        for(int i=0;i<WayfindingData.ROWS;i++){loaded.targets[i]=tag.getString("Target"+i);loaded.distances[i]=tag.getString("Distance"+i);if(tag.contains("Direction"+i))loaded.directions[i]=tag.getInt("Direction"+i);}
        for(int i=0;i<WayfindingData.MAX_LANES;i++)if(tag.contains("Lane"+i))loaded.lanes[i]=tag.getInt("Lane"+i);
        data=loaded.copy();
    }
    @Override public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
    @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(2);}
}
