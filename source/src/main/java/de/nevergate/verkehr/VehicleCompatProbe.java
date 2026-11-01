package de.nevergate.verkehr;
import java.nio.file.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
@Mod.EventBusSubscriber(modid=NgTraffic.ID)
public final class VehicleCompatProbe {
    private static final String MODE=System.getProperty("ng.compat.probe","");
    private static final UUID PERSON=UUID.fromString("00000000-0000-3000-8000-000000000011");
    private static final BlockPos ROOT=new BlockPos(2000,80,2000);private static int ticks,stage,reloadAt;private static boolean done;
    private static Entity car;private static UUID fuelTx,washTx;
    private static Path marker(){return Path.of(System.getProperty("ng.compat.output","probe-results11")).resolve("vehicle-persistence.txt");}
    static Object field(Object o,String n)throws Exception{return o.getClass().getField(n).get(o);}
    static Object call(Object o,String n,Class<?>[] types,Object...args)throws Exception{return o.getClass().getMethod(n,types).invoke(o,args);}
    public static net.minecraft.network.Connection connection(){return new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND){
        @Override public void setListener(net.minecraft.network.PacketListener listener){}
        @Override public net.minecraft.network.ConnectionProtocol getProtocol(){return net.minecraft.network.ConnectionProtocol.PLAY;}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet){}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener){}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){}
    };}
    static ServerPlayer owner(ServerLevel l){var p=new ServerPlayer(l.getServer(),l,new GameProfile(PERSON,"NGCompatOwner"),ClientInformation.createDefault()){@Override public boolean isCreative(){return true;}@Override public void displayClientMessage(Component c,boolean b){}};p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(l.getServer(),connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile()));p.getAbilities().mayBuild=true;p.setPos(ROOT.getX()+.5,ROOT.getY()+1,ROOT.getZ()+.5);return p;}
    static void check(boolean b,String text){if(!b)throw new IllegalStateException(text);}
    static VehicleServiceEvent execute(ServerPlayer p,UUID tx,VehicleServiceEvent.Operation op,int amount){var event=new VehicleServiceEvent(car,p,tx,op,amount);MinecraftForge.EVENT_BUS.post(event);return event;}
    @SubscribeEvent public static void start(ServerStartedEvent event){if(MODE.isBlank())return;var l=event.getServer().overworld();l.setChunkForced(ROOT.getX()>>4,ROOT.getZ()>>4,true);l.getChunkAt(ROOT);System.out.println("VEHICLE_COMPAT START "+MODE);}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(MODE.isBlank()||done||event.phase!=TickEvent.Phase.END)return;var s=event.getServer();var l=s.overworld();ticks++;if(stage==0&&ticks!=30)return;
        try{
            var p=owner(l);
            if(stage==1){if(l.hasChunkAt(ROOT)){if(ticks>1800)throw new IllegalStateException("Chunk unload timeout");return;}check(NgVehiclesCompat.inspect(car)==null,"Unloaded vehicle still readable");System.out.println("VEHICLE_COMPAT ACTUAL CHUNK UNLOADED");l.setChunkForced(ROOT.getX()>>4,ROOT.getZ()>>4,true);l.getChunkAt(ROOT);stage=2;reloadAt=ticks+20;return;}
            if(stage==2){if(ticks<reloadAt)return;var old=car;car=l.getEntity(old.getUUID());check(car!=null&&car!=old,"Actual vehicle instance not restored after unload");var v=NgVehiclesCompat.inspect(car);check(v!=null&&v.fuelMilliLitres()==11000&&v.dirtPercent()==0,"Chunk reload lost native service state");check(execute(p,fuelTx,VehicleServiceEvent.Operation.FUEL,1000).completed()&&NgVehiclesCompat.inspect(car).fuelMilliLitres()==11000,"Chunk reload repeated fuel delivery");System.out.println("VEHICLE_COMPAT CHUNK RELOAD PASS");}
            else if(MODE.equals("write")){
                var type=ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("ng_fahrzeuge","ng_city"));car=type.create(l);car.setPos(ROOT.getX()+.5,ROOT.getY(),ROOT.getZ()+2.5);car.setNoGravity(true);car.noPhysics=true;
                for(int x=-2;x<=2;x++)for(int z=-2;z<=4;z++)l.setBlock(ROOT.offset(x,-1,z),Blocks.STONE.defaultBlockState(),3);
                call(field(car,"equipment"),"claim",new Class[]{Player.class},p);var systems=field(car,"systems");var tag=new CompoundTag();call(systems,"save",new Class[]{CompoundTag.class},tag);tag.putFloat("Fuel",10);tag.putFloat("Dirt",75);call(systems,"load",new Class[]{CompoundTag.class},tag);l.addFreshEntity(car);
                fuelTx=UUID.randomUUID();washTx=UUID.randomUUID();check(execute(p,fuelTx,VehicleServiceEvent.Operation.FUEL,1000).completed(),"Real fuel not confirmed");check(execute(p,washTx,VehicleServiceEvent.Operation.WASH,1).completed(),"Real wash not confirmed");
                Files.createDirectories(marker().getParent());Files.writeString(marker(),car.getUUID()+"\n"+fuelTx+"\n"+washTx+"\n");System.out.println("VEHICLE_COMPAT WRITE PASS");
            }else{
                var lines=Files.readAllLines(marker());car=l.getEntity(UUID.fromString(lines.get(0)));fuelTx=UUID.fromString(lines.get(1));washTx=UUID.fromString(lines.get(2));check(car!=null,"Vehicle UUID not restored");var v=NgVehiclesCompat.inspect(car);check(v!=null&&v.owner().equals(PERSON)&&v.fuelMilliLitres()==11000&&v.dirtPercent()==0,"Vehicle service state not restored from disk");
                check(execute(p,fuelTx,VehicleServiceEvent.Operation.FUEL,1000).completed()&&execute(p,washTx,VehicleServiceEvent.Operation.WASH,1).completed(),"Receipts not restored");check(NgVehiclesCompat.inspect(car).fuelMilliLitres()==11000,"Restart replay added fuel twice");check(!execute(p,fuelTx,VehicleServiceEvent.Operation.FUEL,2000).completed(),"Restart allowed changed transaction payload");System.out.println("VEHICLE_COMPAT RESTART AND REPLAY PASS");var anchor=ROOT.west(48);l.setChunkForced(anchor.getX()>>4,anchor.getZ()>>4,true);l.getChunkAt(anchor);l.setChunkForced(ROOT.getX()>>4,ROOT.getZ()>>4,false);stage=1;return;
            }
        }catch(Throwable t){t.printStackTrace();System.out.println("VEHICLE_COMPAT FAIL "+t);}
        done=true;s.halt(false);
    }
}
