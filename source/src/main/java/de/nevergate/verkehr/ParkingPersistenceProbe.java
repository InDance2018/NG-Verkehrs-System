package de.nevergate.verkehr;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Disabled unless explicitly started by the isolated development runner. Never touches a live server by default. */
@Mod.EventBusSubscriber(modid=NgTraffic.ID)
public final class ParkingPersistenceProbe{
    private static final BlockPos ZONE=new BlockPos(1168,80,1024),TOTAL=new BlockPos(1008,80,1024),BOARD=TOTAL.offset(1,0,0),GATE=TOTAL.offset(2,0,0),HEIGHT=TOTAL.offset(3,0,0);
    private static final java.util.Set<java.util.UUID> opened=new java.util.HashSet<>();private static int changeAt;
    private static void open(net.minecraft.server.level.ServerPlayer player,RoadSystemEntity n){
        player.openMenu(new net.minecraft.world.SimpleMenuProvider((id,inv,p)->new RoadSystemMenu(id,inv,n),net.minecraft.network.chat.Component.translatable("menu.ng_verkehr.road_system")),b->RoadSystemMenu.writeSnapshot(b,n));
    }
    private static String mode="";private static int ticks,stage;private static boolean done;
    @SubscribeEvent public static void started(ServerStartedEvent e){
        mode=System.getProperty("ng.parking.probe","");ticks=stage=0;done=false;opened.clear();
        if(!mode.equals("write")&&!mode.equals("read")&&!mode.equals("client")){mode="";return;}
        var l=e.getServer().overworld();l.setChunkForced(ZONE.getX()>>4,ZONE.getZ()>>4,true);l.setChunkForced(TOTAL.getX()>>4,TOTAL.getZ()>>4,true);
        l.getChunkAt(ZONE);l.getChunkAt(TOTAL);
        if(mode.equals("client")){l.setDayTime(6000);l.setWeatherParameters(0,1000000,false,false);}
        System.out.println("PARKING_PROBE START "+mode);
    }
    private static RoadSystemEntity node(ServerLevel l,BlockPos p){if(!(l.getBlockEntity(p) instanceof RoadSystemEntity n))throw new IllegalStateException("Missing node "+p);return n;}
    private static void check(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static RoadSystemEntity place(ServerLevel l,BlockPos p,net.minecraft.world.level.block.Block b){l.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);l.setBlock(p,b.defaultBlockState(),3);return node(l,p);}
    private static void finish(MinecraftServer server,String message){done=true;System.out.println("PARKING_PROBE "+message);server.saveEverything(true,true,true);server.halt(false);}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(mode.isEmpty()||done||e.phase!=TickEvent.Phase.END)return;ticks++;var server=e.getServer();var l=server.overworld();
        try{
            if(mode.equals("write")||mode.equals("client")){
                if(ticks==10){
                    var zone=place(l,ZONE,NgTraffic.ROAD_CONTROLLER.get());var d=zone.data();d.name="Persistent Deck";d.capacity=17;d.occupied=7;d.vehicleTypes="minecraft:boat";d.detection=1;d.autoDisplay=1;zone.configure(d);
                    var total=place(l,TOTAL,NgTraffic.PARKING_AGGREGATOR.get());while(!total.zones().isEmpty())total.removeZone(0);check(total.attachZone(zone),"Zone attach failed");
                    var board=place(l,BOARD,NgTraffic.PARKING_BOARD.get());check(total.attach(board),"Board attach failed");
                    var gate=place(l,GATE,NgTraffic.PARKING_BARRIER.get());check(zone.attach(gate),"Gate attach failed");var limit=gate.data();limit.heightCm=260;gate.configure(limit);
                    var height=place(l,HEIGHT,NgTraffic.HEIGHT_SIGN.get());limit=height.data();limit.heightCm=260;height.configure(limit);
                    if(mode.equals("client")){
                        var space=place(l,TOTAL.offset(0,0,2),NgTraffic.SPACE_SENSOR.get());zone.attach(space);
                        zone.attach(place(l,TOTAL.offset(1,0,2),NgTraffic.SPACE_SENSOR.get()));
                        var car=new net.minecraft.world.entity.vehicle.Boat(l,space.getBlockPos().getX()+.5,space.getBlockPos().getY()+.2,space.getBlockPos().getZ()+.5);car.setNoGravity(true);l.addFreshEntity(car);
                        var guide=place(l,TOTAL.offset(4,1,0),NgTraffic.PARKING_GUIDE.get());total.attach(guide);var route=guide.data();route.routeMode=2;guide.configure(route);
                    }
                }
                if(ticks==40&&mode.equals("write")){check(node(l,TOTAL).online()&&node(l,BOARD).data().free()==10,"Write network not active");finish(server,"WRITE PASS");}
                if(mode.equals("client")&&ticks>40){
                    var players=server.getPlayerList().getPlayers();
                    for(var player:players)if(!opened.contains(player.getUUID())){
                        player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);player.getAbilities().flying=true;player.onUpdateAbilities();
                        player.teleportTo(l,TOTAL.getX()+(player.getGameProfile().getName().endsWith("1")?1.5:4.5),TOTAL.getY()+1,TOTAL.getZ()-3.5,0,28);
                        open(player,node(l,TOTAL));opened.add(player.getUUID());
                    }
                    var out=java.nio.file.Path.of(System.getProperty("ng.parking.probeOutput","."));
                    if(stage==0&&players.size()>=2){stage=1;changeAt=ticks+80;}
                    if(stage==1&&ticks>=changeAt){node(l,ZONE).countPassage(java.util.UUID.randomUUID(),1,l.getGameTime());stage=2;System.out.println("PARKING_PROBE TWO CLIENTS LIVE UPDATE");}
                    if(stage==2&&java.nio.file.Files.exists(out.resolve("client-live-NGParking1.txt"))&&java.nio.file.Files.exists(out.resolve("client-live-NGParking2.txt"))){
                        check(node(l,TOTAL).zones().size()==1,"Stale client removal mutated server");for(var player:players)open(player,node(l,HEIGHT));stage=3;
                    }
                    if(stage==3&&node(l,HEIGHT).data().heightCm==270){for(var player:players)if(player.getGameProfile().getName().equals("NGParking1"))open(player,node(l,HEIGHT));stage=4;System.out.println("PARKING_PROBE CLIENT SAVE PASS");}
                    if(stage==4&&java.nio.file.Files.exists(out.resolve("client-done-NGParking1.txt"))&&java.nio.file.Files.exists(out.resolve("client-done-NGParking2.txt"))){System.out.println("PARKING_PROBE TWO CLIENTS PASS");finish(server,"CLIENT PASS");}
                    if(ticks>6000)throw new IllegalStateException("Two-client smoke timeout");
                }
                return;
            }
            if(stage==0&&ticks>=30){
                var zone=node(l,ZONE);check(zone.data().name.equals("Persistent Deck")&&zone.data().capacity==17&&zone.data().occupied==7&&zone.data().detection==1&&zone.data().vehicleTypes.equals("minecraft:boat"),"Actual restart lost zone settings");
                check(node(l,TOTAL).zones().size()==1&&node(l,TOTAL).online()&&node(l,BOARD).online()&&node(l,BOARD).data().free()==10,"Actual restart lost connections");
                check(node(l,GATE).data().heightCm==260&&node(l,HEIGHT).data().heightCm==260,"Actual restart lost height settings");
                System.out.println("PARKING_PROBE RESTART PASS");
                l.setChunkForced(ZONE.getX()>>4,ZONE.getZ()>>4,false);stage=1;
            }else if(stage==1&&!l.hasChunkAt(ZONE)){stage=2;changeAt=ticks+25;System.out.println("PARKING_PROBE CHUNK ACTUALLY UNLOADED tick="+ticks);}
            else if(stage==2&&ticks>=changeAt){
                check(!node(l,TOTAL).online()&&!node(l,BOARD).online(),"Unloaded zone reported available");
                l.setChunkForced(ZONE.getX()>>4,ZONE.getZ()>>4,true);l.getChunkAt(ZONE);stage=3;ticks=0;
            }else if(stage==3&&ticks>=30){
                check(node(l,ZONE).data().occupied==7&&node(l,TOTAL).online()&&node(l,BOARD).online()&&node(l,BOARD).data().free()==10,"Chunk reload did not restore saved network");finish(server,"CHUNK RELOAD PASS");
            }
            if(ticks>1200)throw new IllegalStateException("Probe timeout; actual chunk unload not confirmed");
        }catch(Throwable failure){failure.printStackTrace();finish(server,"FAIL "+failure);}
    }
}

