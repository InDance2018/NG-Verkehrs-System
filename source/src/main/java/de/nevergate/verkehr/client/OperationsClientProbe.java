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
public final class OperationsClientProbe {
    private static final String ID=System.getProperty("ng.operations.client","");
    private static int ticks,stage,menuId=-1,completedMenu=-1,wait,doneTicks;private static boolean pressed,finished;
    private static Path out(){return Path.of(System.getProperty("ng.operations.output","probe-results10"));}
    private static void mark(String file,String text)throws Exception{Files.createDirectories(out());Files.writeString(out().resolve(file),text);}
    private static void shot(Minecraft m,String label){Screenshot.grab(m.gameDirectory,"service-"+ID+"-"+label+".png",m.getMainRenderTarget(),x->{});}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(ID.isBlank()||e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();ticks++;mc.getToasts().clear();
        try{
            if(finished){if(mc.level==null||++doneTicks>3600)mc.stop();return;}if(ticks>6000)throw new IllegalStateException("Service client timeout");if(mc.player==null)return;
            if(stage==20&&!(mc.screen instanceof OperationsScreen)){if(++wait>40){shot(mc,"world");mark("done-"+ID+".txt","PASS: 20 menus and synchronized enabled change");finished=true;}return;}
            if(!(mc.screen instanceof OperationsScreen screen)||!(mc.player.containerMenu instanceof OperationsMenu menu))return;
            if(menu.containerId==completedMenu)return;
            if(menu.containerId!=menuId){menuId=menu.containerId;wait=0;pressed=false;}if(++wait<20)return;
            for(var child:screen.children())if(child instanceof AbstractWidget w&&(w.getX()<0||w.getY()<0||w.getX()+w.getWidth()>screen.width||w.getY()+w.getHeight()>screen.height))throw new IllegalStateException("Clipped service widget");
            if(stage==0){if(ID.endsWith("1")&&!pressed){for(var child:screen.children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Gerät:")){b.onPress();pressed=true;break;}if(!pressed)throw new IllegalStateException("Duration button missing");return;}if(menu.value(16)!=0)return;}
            if(menu.value(15)!=1)return;boolean found=false;for(var child:screen.children())if(child instanceof Button b&&b.getMessage().getString().startsWith("Gerät:")&&b.active)found=true;if(!found)throw new IllegalStateException("Admin controls missing after permission sync");

            shot(mc,"menu-"+stage);mark("view-"+ID+"-"+stage+".txt","PASS "+menu.kind);completedMenu=menu.containerId;stage++;wait=0;
        }catch(Throwable t){t.printStackTrace();try{mark("failed-"+ID+".txt",t.toString());}catch(Exception ignored){}finished=true;}
    }
}
