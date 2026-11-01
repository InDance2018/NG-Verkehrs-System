package de.nevergate.verkehr;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=NgTraffic.ID)
public final class OperationsProbe {
    private static final String MODE=System.getProperty("ng.operations.probe","");
    private static final BlockPos ROOT=new BlockPos(1008,80,1024),FAR=ROOT.east(48);
    private static final UUID PERSON=UUID.fromString("00000000-0000-3000-8000-000000000009");
    private static net.minecraft.world.entity.Entity testCar;private static UUID testOwner;private static int executed;private static boolean washing,completionStarted;
    private static int ticks,stage,at;private static boolean done;
    private static Path out(){return Path.of(System.getProperty("ng.operations.output","probe-results10"));}
    private static OperationsEntity node(ServerLevel l,BlockPos p){return (OperationsEntity)l.getBlockEntity(p);}
    private static OperationsEntity put(ServerLevel l,BlockPos p,OperationsBlock.Kind k){l.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);l.removeBlock(p,false);l.setBlock(p,NgTraffic.OPERATIONS.get(k).get().defaultBlockState(),3);return node(l,p);}
    private static void check(boolean b,String s){if(!b)throw new IllegalStateException(s);}
    private static void finish(MinecraftServer s,String text){done=true;System.out.println("OPERATIONS_PROBE "+text);s.halt(false);}
    private static net.minecraft.world.entity.player.Player fake(ServerLevel l){return new net.minecraft.world.entity.player.Player(l,ROOT,0,new com.mojang.authlib.GameProfile(PERSON,"NGOperationsProbe")){public boolean isSpectator(){return false;}public boolean isCreative(){return true;}};}
    private static BlockPos at(int i){return ROOT.north(5).east(i%5*5).south(i/5*5);}
    private static void open(ServerPlayer p,OperationsEntity n){p.teleportTo((ServerLevel)n.getLevel(),n.getBlockPos().getX()+.5,n.getBlockPos().getY()+2,n.getBlockPos().getZ()-3.5,0,20);p.openMenu(new SimpleMenuProvider((id,inv,pl)->new OperationsMenu(id,inv,n),n.getBlockState().getBlock().getName()),b->{b.writeBlockPos(n.getBlockPos());b.writeEnum(n.kind());});}
    @SubscribeEvent public static void start(ServerStartedEvent e){if(MODE.isBlank())return;var l=e.getServer().overworld();l.setChunkForced(ROOT.getX()>>4,ROOT.getZ()>>4,true);l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,true);l.getChunkAt(ROOT);l.getChunkAt(FAR);l.setDayTime(6000);System.out.println("OPERATIONS_PROBE START "+MODE);}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(MODE.isBlank()||done||e.phase!=TickEvent.Phase.END)return;var s=e.getServer();var l=s.overworld();ticks++;
        try{
            if(MODE.equals("write")){
                if(ticks==10){var t=put(l,FAR,OperationsBlock.Kind.BUS_ROUTE);t.route("42","NeverGate Bahnhof");var m=put(l,ROOT,OperationsBlock.Kind.BUS_SENSOR);check(m.connect(t,fake(l)),"Persistent link failed");var call=put(l,ROOT.east(3),OperationsBlock.Kind.TUNNEL_CALL);check(call.action(fake(l),0),"Persistent emergency failed");}
                if(ticks==40){check(node(l,ROOT).online(),"Persistent link not online");finish(s,"WRITE PASS");return;}
            }
            if(MODE.equals("read")){
                if(stage==0&&ticks>=30){check(node(l,ROOT).linked(OperationsBlock.Kind.BUS_ROUTE)==node(l,FAR)&&node(l,FAR).line().equals("42"),"Restart lost route");check(node(l,ROOT.east(3)).alarm(),"Restart lost emergency");System.out.println("OPERATIONS_PROBE RESTART PASS");l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,false);stage=1;}
                else if(stage==1&&!l.hasChunkAt(FAR)){stage=2;at=ticks+12;System.out.println("OPERATIONS_PROBE CHUNK ACTUALLY UNLOADED");}
                else if(stage==2&&ticks>=at){check(!node(l,ROOT).online()&&node(l,ROOT).linked(OperationsBlock.Kind.BUS_ROUTE)==null,"Unloaded route still usable");l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,true);l.getChunkAt(FAR);stage=3;at=ticks+12;}
                else if(stage==3&&ticks>=at){check(node(l,ROOT).online()&&node(l,FAR).line().equals("42"),"Chunk reload lost route");finish(s,"CHUNK RELOAD PASS");return;}
            }
            if(MODE.equals("client")){
                if(ticks==10){for(int i=0;i<OperationsBlock.Kind.values().length;i++){var pos=at(i);l.setChunkForced(pos.getX()>>4,pos.getZ()>>4,true);l.getChunkAt(pos);put(l,pos,OperationsBlock.Kind.values()[i]);}}
                var clients=s.getPlayerList().getPlayers();
                if(stage==0&&clients.size()>=2){for(var p:clients){p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();open(p,node(l,at(0)));}stage=1;}
                if(stage>=1&&stage<=20&&clients.size()>=2){boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("view-"+p.getGameProfile().getName()+"-"+(stage-1)+".txt")))ready=false;if(ready){if(stage==1)check(!node(l,at(0)).enabled(),"Client enabled update missing");if(stage==6&&!washing){var p=clients.get(0);testOwner=p.getUUID();var n=node(l,at(5));var vehicleType=net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getValue(new net.minecraft.resources.ResourceLocation("ng_fahrzeuge","ng_city"));check(vehicleType!=null,"Actual NG-Fahrzeuge fixture missing");testCar=vehicleType.create(l);testCar.setPos(n.getBlockPos().getX()+.5,n.getBlockPos().getY()+.2,n.getBlockPos().getZ()+2.5);testCar.noPhysics=true;testCar.setNoGravity(true);var equipment=testCar.getClass().getField("equipment").get(testCar);equipment.getClass().getMethod("claim",net.minecraft.world.entity.player.Player.class).invoke(equipment,p);var systems=testCar.getClass().getField("systems").get(testCar);var vehicleTag=new net.minecraft.nbt.CompoundTag();systems.getClass().getMethod("save",net.minecraft.nbt.CompoundTag.class).invoke(systems,vehicleTag);vehicleTag.putFloat("Fuel",10);vehicleTag.putFloat("Dirt",75);systems.getClass().getMethod("load",net.minecraft.nbt.CompoundTag.class).invoke(systems,vehicleTag);l.addFreshEntity(testCar);n.action(p,23);check(n.action(p,0),"Real-player wash did not start");washing=true;}
                    stage++;if(stage<=20)for(var p:clients)open(p,node(l,at(stage-1)));else for(var p:clients){p.closeContainer();p.teleportTo(l,ROOT.getX()+11,83,ROOT.getZ()-15,0,18);}}}
                else if(stage==21){boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("done-"+p.getGameProfile().getName()+".txt")))ready=false;if(ready){var n=node(l,at(5));if(!completionStarted){check(washing&&n.state()==4&&NgVehiclesCompat.inspect(testCar).dirtPercent()==75,"Moving away from wash did not abort without execution");var p=clients.stream().filter(x->x.getUUID().equals(testOwner)).findFirst().orElseThrow();p.teleportTo(l,n.getBlockPos().getX()+.5,n.getBlockPos().getY()+2,n.getBlockPos().getZ()-3.5,0,20);check(n.action(p,0),"Final real-player wash did not start");completionStarted=true;at=ticks+220;}else if(ticks>=at){check(n.state()==2&&NgVehiclesCompat.inspect(testCar).dirtPercent()==0,"Real-player wash did not complete exactly once");System.out.println("OPERATIONS_PROBE REAL VEHICLE WASH AND ABORT PASS");if(testCar!=null)testCar.discard();finish(s,"TWO CLIENTS PASS");return;}}}
            }
            if(ticks>1800)throw new IllegalStateException("Service integration timeout");
        }catch(Throwable t){t.printStackTrace();finish(s,"FAIL "+t);}
    }
}

