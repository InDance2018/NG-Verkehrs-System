package de.nevergate.verkehr;
import java.nio.file.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=NgTraffic.ID)
public final class TrafficOperationsProbe {
    private static final String mode=System.getProperty("ng.traffic.probe","");
    private static final BlockPos ROOT=new BlockPos(1008,80,1024),FAR=ROOT.east(48),BOARD=ROOT.east(),COUNTER=ROOT.south(2);
    private static int ticks,stage,at;private static boolean done;
    private static Path out(){return Path.of(System.getProperty("ng.traffic.output","probe-results8"));}
    private static TrafficNodeEntity node(ServerLevel l,BlockPos p){if(l.getBlockEntity(p) instanceof TrafficNodeEntity n)return n;throw new IllegalStateException("Missing traffic node "+p);}
    private static TrafficNodeEntity put(ServerLevel l,BlockPos p,TrafficNodeBlock.Kind k){l.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);l.setBlock(p,NgTraffic.TRAFFIC_NODES.get(k).get().defaultBlockState(),3);return node(l,p);}
    private static void check(boolean b,String s){if(!b)throw new IllegalStateException(s);}
    private static void finish(MinecraftServer s,String text){done=true;System.out.println("TRAFFIC_PROBE "+text);s.saveEverything(true,true,true);s.halt(false);}
    private static net.minecraft.world.entity.player.Player fake(ServerLevel l){var p=new net.minecraft.world.entity.player.Player(l,ROOT,0,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("00000000-0000-3000-8000-000000000008"),"NGTrafficProbe")){public boolean isSpectator(){return false;}public boolean isCreative(){return true;}};p.getAbilities().mayBuild=true;return p;}
    @SubscribeEvent public static void start(ServerStartedEvent e){
        if(mode.isBlank())return;check(java.util.Set.of("write","read","client").contains(mode),"Invalid runner mode");
        ticks=stage=0;done=false;var l=e.getServer().overworld();l.setChunkForced(ROOT.getX()>>4,ROOT.getZ()>>4,true);l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,true);l.getChunkAt(ROOT);l.getChunkAt(FAR);l.setDayTime(6000);l.setWeatherParameters(0,100000,false,false);
        System.out.println("TRAFFIC_PROBE START "+mode);
    }
    private static BlockPos at(int i){return ROOT.north(5).east(i%8*3).south(i/8*4);}
    private static void open(ServerPlayer p,TrafficNodeEntity n){
        p.teleportTo((ServerLevel)n.getLevel(),n.getBlockPos().getX()+.5,n.getBlockPos().getY()+2,n.getBlockPos().getZ()-3.5,0,20);
        p.openMenu(new SimpleMenuProvider((id,inv,pl)->new TrafficNodeMenu(id,inv,n),n.getBlockState().getBlock().getName()),b->{b.writeBlockPos(n.getBlockPos());b.writeEnum(n.kind());});
    }
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(mode.isBlank()||done||event.phase!=TickEvent.Phase.END)return;var server=event.getServer();var l=server.overworld();ticks++;
        try{
            if(mode.equals("write")){
                if(ticks==10){
                    var source=put(l,FAR,TrafficNodeBlock.Kind.SPEED_SENSOR);source.settings(0,80,32,3,100,0,1);source.types("minecraft:boat");var board=put(l,BOARD,TrafficNodeBlock.Kind.SPEED_BOARD);check(source.link(board,fake(l)),"Saved link failed");
                    var counter=put(l,COUNTER,TrafficNodeBlock.Kind.COUNTER);var tag=counter.saveWithoutMetadata();tag.putInt("Forward",70000);tag.putInt("Reverse",65537);counter.load(tag);
                }
                if(ticks==40){check(node(l,BOARD).online(),"Write link not online");finish(server,"WRITE PASS");return;}
            }
            if(mode.equals("read")){
                if(stage==0&&ticks>=30){
                    check(node(l,FAR).limit()==80&&node(l,FAR).radius()==32&&node(l,FAR).types().equals("minecraft:boat")&&node(l,BOARD).source()==node(l,FAR),"Restart lost traffic settings or identity");
                    check(node(l,COUNTER).forward()==70000&&node(l,COUNTER).reverse()==65537&&node(l,COUNTER).hourly()==0,"Restart lost totals or kept stale minute rate");
                    System.out.println("TRAFFIC_PROBE RESTART PASS");l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,false);stage=1;
                }else if(stage==1&&!l.hasChunkAt(FAR)){stage=2;at=ticks+12;System.out.println("TRAFFIC_PROBE CHUNK ACTUALLY UNLOADED");}
                else if(stage==2&&ticks>=at){check(!node(l,BOARD).online()&&node(l,BOARD).speed()==-1,"Unloaded speed source retained valid measurement");l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,true);l.getChunkAt(FAR);stage=3;at=ticks+12;}
                else if(stage==3&&ticks>=at){check(node(l,BOARD).online()&&node(l,BOARD).source()==node(l,FAR),"Chunk reload lost traffic connection");finish(server,"CHUNK RELOAD PASS");return;}
            }
            if(mode.equals("client")){
                if(ticks==10){
                    for(int i=0;i<TrafficNodeBlock.Kind.values().length;i++){var n=put(l,at(i),TrafficNodeBlock.Kind.values()[i]);n.settings(0,50,16,3,100,0,1);}
                    var pl=fake(l);node(l,at(0)).link(node(l,at(1)),pl);node(l,at(3)).link(node(l,at(4)),pl);node(l,at(3)).link(node(l,at(5)),pl);node(l,at(3)).link(node(l,at(6)),pl);
                    for(int i:new int[]{10,11,12,13})node(l,at(9)).link(node(l,at(i)),pl);node(l,at(13)).link(node(l,at(14)),pl);node(l,at(13)).text("Moers");node(l,at(9)).settings(1,50,16,3,100,0,1);
                    var count=node(l,at(15));var tag=count.saveWithoutMetadata();tag.putInt("Forward",70000);tag.putInt("Reverse",65537);count.load(tag);
                }
                var clients=server.getPlayerList().getPlayers();
                if(stage==0&&clients.size()>=2){for(var p:clients){p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();open(p,node(l,at(0)));}stage=1;}
                if(stage>=1&&stage<=16&&clients.size()>=2){
                    boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("view-"+p.getGameProfile().getName()+"-"+(stage-1)+".txt")))ready=false;
                    if(ready){
                        if(stage==1)check(node(l,at(0)).limit()==55,"Client edit not applied on server");
                        stage++;if(stage<=16)for(var p:clients)open(p,node(l,at(stage-1)));
                        else for(var p:clients){p.closeContainer();p.teleportTo(l,ROOT.getX()+(p.getGameProfile().getName().endsWith("1")?11:1),83,ROOT.getZ()-15,0,18);}
                    }
                }else if(stage==17){boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("done-"+p.getGameProfile().getName()+".txt")))ready=false;if(ready){finish(server,"TWO CLIENTS PASS");return;}}
            }
            if(ticks>1800)throw new IllegalStateException("Traffic integration timeout");
        }catch(Throwable t){t.printStackTrace();finish(server,"FAIL "+t);}
    }
}
