package de.nevergate.verkehr;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Explicit opt-in runner for isolated localhost validation; inactive in normal play. */
@Mod.EventBusSubscriber(modid=NgTraffic.ID)
public final class ConstructionProbe {
    private static final BlockPos ROOT=new BlockPos(1008,80,1024),CHILD=ROOT.east(48),GATE=ROOT.south(3);
    private static final String mode=System.getProperty("ng.construction.probe","");
    private static int ticks,stage,at;
    private static boolean done;
    private static final Set<UUID> joined=new HashSet<>();
    private static Path out(){return Path.of(System.getProperty("ng.construction.output","probe-results7"));}
    private static ConstructionEntity node(ServerLevel l,BlockPos p){if(l.getBlockEntity(p) instanceof ConstructionEntity n)return n;throw new IllegalStateException("Missing construction node "+p);}
    private static ConstructionEntity put(ServerLevel l,BlockPos p,net.minecraft.world.level.block.Block b){l.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);l.setBlock(p,b.defaultBlockState(),3);return node(l,p);}
    private static void check(boolean b,String s){if(!b)throw new IllegalStateException(s);}
    private static void finish(MinecraftServer s,String result){done=true;System.out.println("CONSTRUCTION_PROBE "+result);s.saveEverything(true,true,true);s.halt(false);}
    @SubscribeEvent public static void started(ServerStartedEvent e){
        if(mode.isBlank())return;
        if(!Set.of("write","read","client").contains(mode))throw new IllegalStateException("Invalid construction probe mode");
        ticks=stage=0;done=false;joined.clear();var l=e.getServer().overworld();l.setChunkForced(ROOT.getX()>>4,ROOT.getZ()>>4,true);l.setChunkForced(CHILD.getX()>>4,CHILD.getZ()>>4,true);l.getChunkAt(ROOT);l.getChunkAt(CHILD);l.setDayTime(6000);
        System.out.println("CONSTRUCTION_PROBE START "+mode);
    }
    private static net.minecraft.world.level.block.Block[] blocks(){return new net.minecraft.world.level.block.Block[]{NgTraffic.WORK_TRAILER.get(),NgTraffic.WORK_RUNNING_LAMP.get(),NgTraffic.WORK_TIMED.get(),NgTraffic.WORK_GATE.get(),NgTraffic.WORK_CABLE.get(),NgTraffic.WORK_HOSE.get(),NgTraffic.WORK_BRIDGE.get(),NgTraffic.WORK_MAST.get(),NgTraffic.WORK_ACCESS.get()};}
    private static BlockPos displayPos(int i){return ROOT.north(5).east(i*3);}
    private static void open(ServerPlayer p,ConstructionEntity n){p.teleportTo((ServerLevel)n.getLevel(),n.getBlockPos().getX()+.5,n.getBlockPos().getY()+2,n.getBlockPos().getZ()-3.5,0,20);p.openMenu(new SimpleMenuProvider((id,inv,pl)->new ConstructionMenu(id,inv,n),n.getBlockState().getBlock().getName()),b->{b.writeBlockPos(n.getBlockPos());b.writeEnum(n.kind());});}
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
        if(mode.isBlank()||done||event.phase!=TickEvent.Phase.END)return;
        var server=event.getServer();var l=server.overworld();ticks++;
        try{
            if(mode.equals("write")&&ticks==10){
                var player=new net.minecraft.world.entity.player.Player(l,ROOT,0,new com.mojang.authlib.GameProfile(java.util.UUID.fromString("00000000-0000-3000-8000-000000000007"),"NGConstructionProbe")){public boolean isSpectator(){return false;}public boolean isCreative(){return true;}};player.getAbilities().mayBuild=true;
                var root=put(l,ROOT,NgTraffic.WORK_RUNNING_LAMP.get());var child=put(l,CHILD,NgTraffic.WORK_RUNNING_LAMP.get());
                check(root.attach(child,player),"Link failed");root.mode(2);root.timing(22000,2000,false);
                var gate=put(l,GATE,NgTraffic.WORK_ACCESS.get());gate.setOwner(UUID.fromString("00000000-0000-3000-8000-000000000001"));gate.permit(UUID.fromString("10000000-0000-0000-0000-000000000002"),true,true);gate.timing(22000,2000,true);
            }
            if(mode.equals("write")&&ticks==40){check(node(l,ROOT).active()&&node(l,CHILD).active(),"Write lamps not lit");finish(server,"WRITE PASS");return;}
            if(mode.equals("read")){
                if(stage==0&&ticks>=30){
                    check(node(l,ROOT).linked()==2&&node(l,CHILD).control()==node(l,ROOT)&&node(l,CHILD).active(),"Restart lost lamp group");
                    check(node(l,GATE).permitted()==1&&node(l,GATE).start()==22000&&node(l,GATE).end()==2000&&node(l,GATE).schedule(),"Restart lost gate configuration");
                    System.out.println("CONSTRUCTION_PROBE RESTART PASS");l.setChunkForced(CHILD.getX()>>4,CHILD.getZ()>>4,false);stage=1;
                }else if(stage==1&&!l.hasChunkAt(CHILD)){stage=2;at=ticks+8;System.out.println("CONSTRUCTION_PROBE CHUNK ACTUALLY UNLOADED");}
                else if(stage==2&&ticks>=at){check(!node(l,ROOT).active(),"Missing group lamp did not darken group");l.setChunkForced(CHILD.getX()>>4,CHILD.getZ()>>4,true);l.getChunkAt(CHILD);stage=3;at=ticks+12;}
                else if(stage==3&&ticks>=at){check(node(l,CHILD).control()==node(l,ROOT)&&node(l,ROOT).active()&&node(l,CHILD).active(),"Reload did not restore group");finish(server,"CHUNK RELOAD PASS");return;}
            }
            if(mode.equals("client")){
                if(ticks==10){for(int i=0;i<blocks().length;i++)put(l,displayPos(i),blocks()[i]).mode(1);}
                var clients=server.getPlayerList().getPlayers();
                if(clients.size()>=2&&stage==0){
                    for(var p:clients){p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.getAbilities().flying=true;p.onUpdateAbilities();p.teleportTo(l,ROOT.getX()+8,82,ROOT.getZ()-12,0,20);joined.add(p.getUUID());open(p,node(l,displayPos(0)));}
                    node(l,displayPos(8)).setOwner(clients.get(0).getUUID());stage=1;
                }
                if(stage>=1&&stage<=9&&clients.size()>=2){
                    boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("view-"+p.getGameProfile().getName()+"-"+(stage-1)+".txt")))ready=false;
                    if(ready){
                        if(stage==1)check(node(l,displayPos(0)).mode()==2,"Real client mode button did not reach server");
                        stage++;
                        if(stage<=9)for(var p:clients)open(p,node(l,displayPos(stage-1)));
                        else for(var p:clients){p.closeContainer();p.teleportTo(l,ROOT.getX()+(p.getGameProfile().getName().endsWith("1")?12:1),82,ROOT.getZ()-13,0,14);}
                    }
                }else if(stage==10){
                    boolean ready=true;for(var p:clients)if(!Files.exists(out().resolve("done-"+p.getGameProfile().getName()+".txt")))ready=false;
                    if(ready){finish(server,"TWO CLIENTS PASS");return;}
                }
            }
            if(ticks>1800)throw new IllegalStateException("Construction probe timeout");
        }catch(Throwable t){t.printStackTrace();finish(server,"FAIL "+t);}
    }
}
