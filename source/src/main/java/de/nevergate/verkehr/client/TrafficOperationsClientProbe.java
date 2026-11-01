package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import java.nio.file.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=NgTraffic.ID,value=Dist.CLIENT)
public final class TrafficOperationsClientProbe {
    private static final String id=System.getProperty("ng.traffic.client","");
    private static int ticks,stage,menuId=-1,wait,doneTicks;private static boolean pressed,finished;
    private static Path out(){return Path.of(System.getProperty("ng.traffic.output","probe-results8"));}
    private static void mark(String file,String value)throws Exception{Files.createDirectories(out());Files.writeString(out().resolve(file),value);}
    private static void shot(Minecraft m,String phase){Screenshot.grab(m.gameDirectory,"traffic-"+id+"-"+phase+".png",m.getMainRenderTarget(),text->System.out.println("TRAFFIC_CLIENT SCREENSHOT "+phase));}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(id.isBlank()||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();ticks++;mc.getToasts().clear();
        try{
            if(finished){if(++doneTicks>40)mc.stop();return;}
            if(ticks>6000)throw new IllegalStateException("Traffic client timeout");
            if(mc.player==null)return;
            if(stage==16&&!(mc.screen instanceof TrafficNodeScreen)){if(++wait>40){shot(mc,"world");mark("done-"+id+".txt","PASS: sixteen menus, live speed-limit update and high counters");finished=true;}return;}
            if(!(mc.screen instanceof TrafficNodeScreen screen)||!(mc.player.containerMenu instanceof TrafficNodeMenu menu))return;
            if(menu.containerId!=menuId){menuId=menu.containerId;wait=0;pressed=false;}wait++;if(wait<20)return;
            for(var child:screen.children())if(child instanceof AbstractWidget w&&(w.getX()<0||w.getY()<0||w.getX()+w.getWidth()>screen.width||w.getY()+w.getHeight()>screen.height))throw new IllegalStateException("Clipped traffic widget");
            if(stage==0){
                if(id.endsWith("1")&&!pressed){for(var child:screen.children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Tempolimit:")){b.onPress();pressed=true;break;}if(!pressed)throw new IllegalStateException("Speed-limit button missing");return;}
                if(menu.value(1)!=55)return;
            }
            if(stage==15&&(menu.count(10)!=70000||menu.count(12)!=65537))throw new IllegalStateException("Actual network truncated high counter");
            shot(mc,"menu-"+stage);mark("view-"+id+"-"+stage+".txt","PASS "+menu.kind);stage++;wait=0;
        }catch(Throwable t){t.printStackTrace();try{mark("failed-"+id+".txt",t.toString());}catch(Exception ignored){}finished=true;}
    }
}
