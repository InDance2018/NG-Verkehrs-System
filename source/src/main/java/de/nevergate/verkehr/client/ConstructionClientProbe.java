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
public final class ConstructionClientProbe {
    private static final String id=System.getProperty("ng.construction.client","");
    private static int ticks,stage,seen=-1,wait,doneTicks;
    private static boolean pressed,finished;
    private static Path out(){return Path.of(System.getProperty("ng.construction.output","probe-results7"));}
    private static void mark(String file,String text)throws Exception{Files.createDirectories(out());Files.writeString(out().resolve(file),text);}
    private static void shot(Minecraft mc,String part){Screenshot.grab(mc.gameDirectory,"construction-"+id+"-"+part+".png",mc.getMainRenderTarget(),m->System.out.println("CONSTRUCTION_CLIENT SCREENSHOT "+part));}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(id.isBlank()||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();mc.getToasts().clear();ticks++;
        try{
            if(finished){if(++doneTicks>40)mc.stop();return;}
            if(ticks>6000)throw new IllegalStateException("Client probe timeout");
            if(mc.player==null)return;
            if(stage==9&&!(mc.screen instanceof ConstructionScreen)){
                if(++wait>=40){shot(mc,"world");mark("done-"+id+".txt","PASS: 9 menus, live server button and world rendered");finished=true;}return;
            }
            if(!(mc.screen instanceof ConstructionScreen screen)||!(mc.player.containerMenu instanceof ConstructionMenu menu))return;
            if(menu.containerId!=seen){seen=menu.containerId;wait=0;pressed=false;}wait++;
            if(wait<20)return;
            for(var child:screen.children())if(child instanceof AbstractWidget w&&
               (w.getX()<0||w.getY()<0||w.getX()+w.getWidth()>screen.width||w.getY()+w.getHeight()>screen.height))throw new IllegalStateException("Clipped construction widget");
            if(stage==0){
                if(id.endsWith("1")&&!pressed){
                    for(var child:screen.children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Pfeiltafel:")){b.onPress();pressed=true;break;}
                    if(!pressed)throw new IllegalStateException("Missing trailer mode button");return;
                }
                if(menu.value(0)!=2)return;
            }
            shot(mc,"menu-"+stage);mark("view-"+id+"-"+stage+".txt","PASS: "+menu.kind+"; all widgets in bounds");stage++;wait=0;
        }catch(Throwable t){t.printStackTrace();try{mark("failed-"+id+".txt",t.toString());}catch(Exception ignored){}finished=true;}
    }
}
