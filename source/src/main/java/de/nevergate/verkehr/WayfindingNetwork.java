package de.nevergate.verkehr;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;

public final class WayfindingNetwork {
    public static final SimpleChannel CHANNEL=ChannelBuilder.named(new ResourceLocation(NgTraffic.ID,"wayfinding")).networkProtocolVersion(3).simpleChannel();
    public record Save(int menuId,int revision,WayfindingData data){}
    public record SaveSystem(int menuId,int revision,RoadSystemData data){}
    public record RemoveZone(int menuId,int revision,int index){}
    public record LiveSystem(int menuId,byte[] data){}
    public static void register(){
        CHANNEL.messageBuilder(LiveSystem.class,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((message,b)->{b.writeVarInt(message.menuId);b.writeByteArray(message.data);})
            .decoder(b->new LiveSystem(b.readVarInt(),b.readByteArray(8192)))
            .consumerMainThread((message,context)->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->de.nevergate.verkehr.client.TrafficClient.receiveSystem(message))).add();
        CHANNEL.messageBuilder(RemoveZone.class,NetworkDirection.PLAY_TO_SERVER)
            .encoder((message,b)->{b.writeVarInt(message.menuId);b.writeInt(message.revision);b.writeVarInt(message.index);})
            .decoder(b->new RemoveZone(b.readVarInt(),b.readInt(),b.readVarInt()))
            .consumerMainThread((message,context)->{var player=context.getSender();if(player!=null&&player.containerMenu instanceof RoadSystemMenu menu&&menu.containerId==message.menuId){boolean removed=menu.removeZone(player,message.revision,message.index);player.displayClientMessage(Component.translatable(removed?"message.ng_verkehr.zone_removed":"message.ng_verkehr.sign_conflict"),false);}}).add();
        CHANNEL.messageBuilder(SaveSystem.class,NetworkDirection.PLAY_TO_SERVER)
            .encoder((message,b)->{b.writeVarInt(message.menuId);b.writeInt(message.revision);RoadSystemMenu.writeData(message.data,b);})
            .decoder(b->new SaveSystem(b.readVarInt(),b.readInt(),RoadSystemMenu.readData(b)))
            .consumerMainThread((message,context)->{
                var player=context.getSender();if(player!=null&&player.containerMenu instanceof RoadSystemMenu menu&&menu.containerId==message.menuId){boolean saved=menu.save(player,message.revision,message.data);player.displayClientMessage(Component.translatable(saved?"message.ng_verkehr.sign_saved":"message.ng_verkehr.sign_conflict"),false);player.closeContainer();}
            }).add();
        CHANNEL.messageBuilder(Save.class,NetworkDirection.PLAY_TO_SERVER)
            .encoder((message,b)->{b.writeVarInt(message.menuId);b.writeInt(message.revision);writeData(message.data,b);})
            .decoder(b->new Save(b.readVarInt(),b.readInt(),readData(b)))
            .consumerMainThread((message,context)->{
                var player=context.getSender();
                if(player!=null&&player.containerMenu instanceof WayfindingMenu menu&&menu.containerId==message.menuId){
                    boolean saved=menu.save(player,message.revision,message.data);
                    player.displayClientMessage(Component.translatable(saved?"message.ng_verkehr.sign_saved":"message.ng_verkehr.sign_conflict"),false);
                    player.closeContainer();
                }
            }).add();
    }
    public static void writeData(WayfindingData data,FriendlyByteBuf b){
        var safe=data.copy();for(int i=0;i<WayfindingData.ROWS;i++){b.writeUtf(safe.targets[i],WayfindingData.MAX_TEXT*2);b.writeUtf(safe.distances[i],WayfindingData.MAX_DISTANCE*2);b.writeByte(safe.directions[i]);}
        b.writeByte(safe.laneCount);for(int lane:safe.lanes)b.writeByte(lane);
    }
    public static WayfindingData readData(FriendlyByteBuf b){
        var data=new WayfindingData();for(int i=0;i<WayfindingData.ROWS;i++){data.targets[i]=b.readUtf(WayfindingData.MAX_TEXT*2);data.distances[i]=b.readUtf(WayfindingData.MAX_DISTANCE*2);data.directions[i]=b.readUnsignedByte();}
        data.laneCount=b.readUnsignedByte();for(int i=0;i<WayfindingData.MAX_LANES;i++)data.lanes[i]=b.readUnsignedByte();return data.copy();
    }
}
