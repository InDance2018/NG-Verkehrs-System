package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;
import io.netty.buffer.Unpooled;

public final class RoadSystemMenu extends AbstractContainerMenu {
    public BlockPos pos;public RoadSystemBlock.Role role;public int revision,count,spaceCount,liveFrame;public boolean online;public RoadSystemData snapshot;
    private final RoadSystemEntity node;private final Player viewer;private byte[] last=new byte[0];
    public List<RoadSystemEntity.ZoneInfo> zones=List.of();
    public RoadSystemMenu(int id,Inventory inventory,FriendlyByteBuf b){super(NgTraffic.ROAD_SYSTEM_MENU.get(),id);node=null;viewer=inventory.player;readSnapshot(b);}
    public RoadSystemMenu(int id,Inventory inventory,RoadSystemEntity node){super(NgTraffic.ROAD_SYSTEM_MENU.get(),id);this.node=node;viewer=inventory.player;var b=new FriendlyByteBuf(Unpooled.buffer());try{writeSnapshot(b,node);readSnapshot(b);}finally{b.release();}}
    private void readSnapshot(FriendlyByteBuf b){
        var newPos=b.readBlockPos();var newRole=b.readEnum(RoadSystemBlock.Role.class);int newRevision=b.readInt(),newCount=b.readVarInt(),newSpaces=b.readVarInt();boolean newOnline=b.readBoolean();var newData=readData(b);
        int size=b.readVarInt();if(size<0||size>16)throw new IllegalArgumentException("Invalid parking zone count");var result=new ArrayList<RoadSystemEntity.ZoneInfo>();
        for(int i=0;i<size;i++)result.add(new RoadSystemEntity.ZoneInfo(b.readBlockPos(),b.readUtf(96),b.readBoolean(),b.readVarInt(),b.readVarInt()));
        if(pos!=null&&(!pos.equals(newPos)||role!=newRole))throw new IllegalArgumentException("Wrong parking menu target");
        pos=newPos;role=newRole;revision=newRevision;count=newCount;spaceCount=newSpaces;online=newOnline;snapshot=newData;zones=List.copyOf(result);liveFrame++;
    }
    public void applyLive(byte[] bytes){if(node!=null||bytes.length>8192)return;var b=new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));try{readSnapshot(b);}finally{b.release();}}
    public static void writeSnapshot(FriendlyByteBuf b,RoadSystemEntity node){
        node.prepareMenuSnapshot();
        b.writeBlockPos(node.getBlockPos());b.writeEnum(node.role());b.writeInt(node.revision());b.writeVarInt(node.deviceCount());b.writeVarInt(node.connectedSpaceCount());b.writeBoolean(node.online());writeData(node.data(),b);
        var zones=node.availableZones();b.writeVarInt(zones.size());for(var z:zones){b.writeBlockPos(z.pos());b.writeUtf(z.name(),96);b.writeBoolean(z.online());b.writeVarInt(z.capacity());b.writeVarInt(z.occupied());}
    }
    @Override public void broadcastChanges(){
        super.broadcastChanges();if(node==null||!(viewer instanceof ServerPlayer player)||!stillValid(player)||player.level().getGameTime()%5!=0)return;
        var b=new FriendlyByteBuf(Unpooled.buffer());try{writeSnapshot(b,node);byte[] bytes=new byte[b.readableBytes()];b.getBytes(0,bytes);
            if(!Arrays.equals(last,bytes)){readSnapshot(b);last=bytes;WayfindingNetwork.CHANNEL.send(new WayfindingNetwork.LiveSystem(containerId,bytes),PacketDistributor.PLAYER.with(player));}
        }finally{b.release();}
    }
    public static void writeData(RoadSystemData d,FriendlyByteBuf b){d=d.copy();b.writeUtf(d.name,96);b.writeVarInt(d.mode);b.writeVarInt(d.speed);b.writeVarInt(d.capacity);b.writeVarInt(d.occupied);b.writeVarInt(d.direction);b.writeUtf(d.vehicleTypes,1024);b.writeVarInt(d.detection);b.writeVarInt(d.counting);b.writeVarInt(d.heightCm);b.writeVarInt(d.routeMode);b.writeLong(d.routeTarget);b.writeVarInt(d.autoDisplay);}
    public static RoadSystemData readData(FriendlyByteBuf b){var d=new RoadSystemData();d.name=b.readUtf(96);d.mode=b.readVarInt();d.speed=b.readVarInt();d.capacity=b.readVarInt();d.occupied=b.readVarInt();d.direction=b.readVarInt();d.vehicleTypes=b.readUtf(1024);d.detection=b.readVarInt();d.counting=b.readVarInt();d.heightCm=b.readVarInt();d.routeMode=b.readVarInt();d.routeTarget=b.readLong();d.autoDisplay=b.readVarInt();return d.copy();}
    @Override public boolean stillValid(Player player){return player.getAbilities().mayBuild&&player.level().hasChunkAt(pos)&&player.level().mayInteract(player,pos)&&player.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=64&&player.level().getBlockEntity(pos) instanceof RoadSystemEntity current&&(node==null||current==node);}
    public boolean save(Player player,int expected,RoadSystemData data){
        if(node==null||!stillValid(player)||expected!=revision||node.revision()!=revision)return false;var next=data.copy();
        if(!node.controller()){var old=node.data();old.name=next.name;old.direction=next.direction;old.heightCm=next.heightCm;
            if(node.role()==RoadSystemBlock.Role.PARKING_GUIDE){long target=next.routeTarget;if(next.routeMode==1&&node.availableZones().stream().noneMatch(z->z.pos().asLong()==target))return false;old.routeMode=next.routeMode;old.routeTarget=next.routeTarget;}next=old;}
        node.configure(next);return true;
    }
    public boolean removeZone(Player player,int expected,int index){return node!=null&&stillValid(player)&&node.aggregator()&&expected==revision&&node.revision()==revision&&node.removeZone(index);}
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
}

