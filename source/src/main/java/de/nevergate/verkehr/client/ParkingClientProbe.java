package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Opt-in client smoke probe for an isolated localhost server and artificial test users. */
@Mod.EventBusSubscriber(modid=NgTraffic.ID,value=Dist.CLIENT)
public final class ParkingClientProbe{
    private static int stage,ticks;private static boolean pendingHeight;
    private static final String id=System.getProperty("ng.parking.clientProbe","");
    private static Path out(){return Path.of(System.getProperty("ng.parking.probeOutput","."));}
    private static void mark(String type,String text)throws Exception{Files.createDirectories(out());Files.writeString(out().resolve("client-"+type+"-"+id+".txt"),text);}
    private static void shot(Minecraft mc,String phase){Screenshot.grab(mc.gameDirectory,"parking-"+id+"-"+phase+".png",mc.getMainRenderTarget(),m->System.out.println("PARKING_CLIENT SCREENSHOT "+m.getString()));}
    private static Button button(RoadSystemScreen screen,String... starts){
        for(var child:screen.children())if(child instanceof Button b)for(String prefix:starts)if(b.getMessage().getString().startsWith(prefix))return b;
        throw new IllegalStateException("Missing button "+java.util.Arrays.toString(starts));
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(id.isBlank()||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();mc.getToasts().clear();ticks++;
        try{
            if(ticks>9000)throw new IllegalStateException("Client smoke timeout");
            if(stage==5){if(ticks>=30){shot(mc,"world");mark("done","PASS live menu, unsaved input, real network, height controls, rendered world");System.out.println("PARKING_CLIENT "+id+" PASS");stage=6;ticks=0;}return;}
            if(stage==6){if(ticks>=30)mc.stop();return;}
            if(!(mc.screen instanceof RoadSystemScreen screen)||!(mc.player.containerMenu instanceof RoadSystemMenu menu))return;
            if(stage==0&&menu.role==RoadSystemBlock.Role.AGGREGATOR&&menu.online&&ticks>20){
                for(var child:screen.children())if(child instanceof AbstractWidget w){if(w.getX()<0||w.getY()<0||w.getX()+w.getWidth()>screen.width||w.getY()+w.getHeight()>screen.height)throw new IllegalStateException("Clipped parking widget");}
                for(var child:screen.children())if(child instanceof EditBox box){box.setValue("Draft "+id);break;}
                shot(mc,"aggregate-initial");
                // Deliberately stale request must not remove any linked zone.
                WayfindingNetwork.CHANNEL.send(new WayfindingNetwork.RemoveZone(menu.containerId,menu.revision-1,0),PacketDistributor.SERVER.noArg());stage=1;ticks=0;
            }else if(stage==1&&menu.snapshot.occupied==8&&ticks>=10){
                boolean found=false;for(var child:screen.children())if(child instanceof EditBox box){found=box.getValue().equals("Draft "+id);break;}
                if(!found||menu.zones.size()!=1||menu.zones.get(0).occupied()!=menu.snapshot.occupied)throw new IllegalStateException("Live update lost draft or stale click removed zone");
                shot(mc,"aggregate-live");mark("live","PASS occupied=8; draft retained; one zone retained");stage=2;ticks=0;
            }else if(stage==2&&menu.role==RoadSystemBlock.Role.HEIGHT_SIGN){
                boolean heightVisible=false;for(var child:screen.children())if(child instanceof Button btn&&(btn.getMessage().getString().startsWith("Maximale Höhe")||btn.getMessage().getString().startsWith("Maximum height")))heightVisible=true;
                if(!heightVisible)button(screen,"Parkoptionen","Parking options").onPress();stage=3;ticks=0;
            }else if(stage==3&&ticks>15){
                if(id.endsWith("1")){button(screen,"Maximale Höhe","Maximum height").onPress();shot(mc,"height-edited");button(screen,"Fertig","Done").onPress();}
                stage=4;ticks=0;
            }else if(stage==4&&menu.role==RoadSystemBlock.Role.HEIGHT_SIGN&&menu.snapshot.heightCm==270&&ticks>15){
                shot(mc,"height-live");mc.player.closeContainer();mc.setScreen(null);
                mc.player.setPos(1010.5,82,id.endsWith("1")?1017.5:1009.5);mc.player.setYRot(0);mc.player.setXRot(20);stage=5;ticks=0;
            }
        }catch(Throwable failure){
            failure.printStackTrace();try{mark("failed",failure.toString());}catch(Exception ignored){}stage=6;ticks=0;
        }
    }
}

