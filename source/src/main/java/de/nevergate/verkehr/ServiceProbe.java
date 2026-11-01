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
public final class ServiceProbe {
    private static final String MODE=System.getProperty("ng.service.probe","");
    private static final BlockPos ROOT=new BlockPos(1008,80,1024),FAR=ROOT.east(48);
    private static final UUID PERSON=UUID.fromString("00000000-0000-3000-8000-000000000009");
    private static int ticks,stage,at;private static boolean done;
    private static Path out(){return Path.of(System.getProperty("ng.service.output","probe-results9"));}
    private static ServiceEntity node(ServerLevel l,BlockPos p){return (ServiceEntity)l.getBlockEntity(p);}
    private static ServiceEntity put(ServerLevel l,BlockPos p,ServiceBlock.Kind k){l.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);l.removeBlock(p,false);l.setBlock(p,NgTraffic.SERVICES.get(k).get().defaultBlockState(),3);return node(l,p);}
    private static void check(boolean b,String s){if(!b)throw new IllegalStateException(s);}
    private static void finish(MinecraftServer s,String text){done=true;System.out.println("SERVICE_PROBE "+text);s.saveEverything(true,true,true);s.halt(false);}
    private static net.minecraft.world.entity.player.Player fake(ServerLevel l){return new net.minecraft.world.entity.player.Player(l,ROOT,0,new com.mojang.authlib.GameProfile(PERSON,"NGServiceProbe")){public boolean isSpectator(){return false;}public boolean isCreative(){return true;}};}
    private static BlockPos at(int i){return ROOT.north(5).east(i%8*3).south(i/8*4);}
    private static void open(ServerPlayer p,ServiceEntity n){p.teleportTo((ServerLevel)n.getLevel(),n.getBlockPos().getX()+.5,n.getBlockPos().getY()+2,n.getBlockPos().getZ()-3.5,0,20);p.openMenu(new SimpleMenuProvider((id,inv,pl)->new ServiceMenu(id,inv,n),n.getBlockState().getBlock().getName()),b->{b.writeBlockPos(n.getBlockPos());b.writeEnum(n.kind());});}
    @SubscribeEvent public static void start(ServerStartedEvent e){if(MODE.isBlank())return;var l=e.getServer().overworld();l.setChunkForced(ROOT.getX()>>4,ROOT.getZ()>>4,true);l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,true);l.getChunkAt(ROOT);l.getChunkAt(FAR);l.setDayTime(6000);System.out.println("SERVICE_PROBE START "+MODE);}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(MODE.isBlank()||done||e.phase!=TickEvent.Phase.END)return;var s=e.getServer();var l=s.overworld();ticks++;
        try{
            if(MODE.equals("write")){
                if(ticks==10){var t=put(l,FAR,ServiceBlock.Kind.TARIFF);t.configure(650,25,2800,60,0,30,220,120);var m=put(l,ROOT,ServiceBlock.Kind.PARK_METER);check(m.connect(t,fake(l)),"Persistent link failed");var ledger=ParkingLedger.get(l);var token=ledger.issue(PERSON,t.identity(),ParkingLedger.now(l),true,ParkingLedger.DAY);check(token!=null,"Persistent pass not issued");}
                if(ticks==40){check(node(l,ROOT).online(),"Persistent link not online");finish(s,"WRITE PASS");return;}
            }
            if(MODE.equals("read")){
                if(stage==0&&ticks>=30){check(node(l,ROOT).tariff()==node(l,FAR)&&node(l,ROOT).rate()==650,"Restart lost tariff");check(ParkingLedger.get(l).existing(PERSON,node(l,FAR).identity(),true,ParkingLedger.now(l))!=null,"Restart lost ledger");System.out.println("SERVICE_PROBE RESTART PASS");l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,false);stage=1;}
                else if(stage==1&&!l.hasChunkAt(FAR)){stage=2;at=ticks+12;System.out.println("SERVICE_PROBE CHUNK ACTUALLY UNLOADED");}
                else if(stage==2&&ticks>=at){check(!node(l,ROOT).online()&&node(l,ROOT).tariff()==null,"Unloaded tariff still usable");l.setChunkForced(FAR.getX()>>4,FAR.getZ()>>4,true);l.getChunkAt(FAR);stage=3;at=ticks+12;}
                else if(stage==3&&ticks>=at){check(node(l,ROOT).online()&&node(l,ROOT).rate()==650,"Chunk reload lost service");finish(s,"CHUNK RELOAD PASS");return;}
            }
            if(MODE.equals("client")){
                if(ticks==10){for(int i=0;i<ServiceBlock.Kind.values().length;i++)put(l,at(i),ServiceBlock.Kind.values()[i]);var tariff=node(l,at(4));for(int i:new int[]{0,1,2,3,5})check(node(l,at(i)).connect(tariff,fake(l)),"Client tariff link");}
                var clients=s.getPlayerList().getPlayers();
                if(stage==0&&clients.size()>=2){for(var p:clients){p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();open(p,node(l,at(0)));}stage=1;}
                if(stage>=1&&stage<=15&&clients.size()>=2){boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("view-"+p.getGameProfile().getName()+"-"+(stage-1)+".txt")))ready=false;if(ready){if(stage==1)check(node(l,at(0)).duration()==75,"Client duration update missing");stage++;if(stage<=15)for(var p:clients)open(p,node(l,at(stage-1)));else for(var p:clients){p.closeContainer();p.teleportTo(l,ROOT.getX()+11,83,ROOT.getZ()-15,0,18);}}}
                else if(stage==16){boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("done-"+p.getGameProfile().getName()+".txt")))ready=false;if(ready){finish(s,"TWO CLIENTS PASS");return;}}
            }
            if(ticks>1800)throw new IllegalStateException("Service integration timeout");
        }catch(Throwable t){t.printStackTrace();finish(s,"FAIL "+t);}
    }
}
