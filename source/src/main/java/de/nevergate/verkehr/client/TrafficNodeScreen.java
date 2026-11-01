package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class TrafficNodeScreen extends AbstractContainerScreen<TrafficNodeMenu> {
    private final Map<Integer,Button> buttons=new LinkedHashMap<>();
    public TrafficNodeScreen(TrafficNodeMenu m,Inventory i,Component t){super(m,i,t);}
    @Override protected void init(){
        imageWidth=Math.min(430,width-16);imageHeight=252;super.init();buttons.clear();int y=84;
        if(menu.kind==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER||menu.kind==TrafficNodeBlock.Kind.DETOUR_CONTROLLER){add(0,y);y+=25;}
        if(menu.kind==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER){add(5,y);y+=25;}
        if(menu.kind==TrafficNodeBlock.Kind.SPEED_SENSOR){add(1,y);y+=25;}
        if(TrafficNodeBlock.sensor(menu.kind)||menu.kind==TrafficNodeBlock.Kind.CROSSING_CONTROLLER){add(2,y);y+=25;}
        if(menu.kind==TrafficNodeBlock.Kind.JAM_SENSOR){add(3,y);y+=25;add(4,y);y+=25;}
        if(menu.kind==TrafficNodeBlock.Kind.BUS_SENSOR||menu.kind==TrafficNodeBlock.Kind.EMERGENCY_SENSOR){add(5,y);y+=25;}
        if(menu.kind==TrafficNodeBlock.Kind.DETOUR_CONTROLLER){add(6,y);y+=25;}
        add(7,y);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(leftPos+imageWidth-100,topPos+imageHeight-27,86,20).build());refresh();
    }
    private void add(int id,int y){buttons.put(id,addRenderableWidget(Button.builder(Component.empty(),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+14,topPos+y,imageWidth-28,20).build()));}
    private Component label(int id){return switch(id){
        case 0->Component.translatable("label.ng_verkehr.traffic_mode_"+menu.kind.name().toLowerCase(java.util.Locale.ROOT)+"_"+menu.value(0));
        case 1->Component.translatable("label.ng_verkehr.traffic_limit",menu.value(1));
        case 2->Component.translatable("label.ng_verkehr.traffic_radius",menu.value(2));
        case 3->Component.translatable("label.ng_verkehr.traffic_queue",menu.value(3));
        case 4->Component.translatable("label.ng_verkehr.traffic_delay",menu.value(4)/20);
        case 5->menu.kind==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER?Component.translatable("label.ng_verkehr.traffic_tunnel_light_"+menu.value(5)):Component.translatable("label.ng_verkehr.traffic_group",menu.value(5)==0?"A":"B");
        case 6->Component.translatable("label.ng_verkehr.traffic_direction",menu.value(6)==0?"←":menu.value(6)==1?"↑":"→");
        default->Component.translatable("label.ng_verkehr.traffic_unlink");
    };}
    private void refresh(){buttons.forEach((id,b)->{b.setMessage(label(id));b.active=menu.value(19)==1;});}
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF0C111B);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFF64C7FF);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,title,14,12,0xFF64C7FF,false);
        g.drawString(font,Component.translatable("label.ng_verkehr.traffic_status",Component.translatable(menu.value(14)==1?"label.ng_verkehr.traffic_online":"label.ng_verkehr.traffic_offline"),menu.value(18)),14,30,0xFFEEF3FB,false);
        if(menu.kind==TrafficNodeBlock.Kind.COUNTER){
            g.drawString(font,Component.translatable("label.ng_verkehr.traffic_counts",menu.count(10),menu.count(12)),14,47,0xFFEEF3FB,false);
            g.drawString(font,Component.translatable("label.ng_verkehr.traffic_hourly",menu.count(16)),14,62,0xFFAAB7CC,false);
        }else if(menu.kind==TrafficNodeBlock.Kind.SPEED_SENSOR||menu.kind==TrafficNodeBlock.Kind.SPEED_BOARD)
            g.drawString(font,Component.translatable("label.ng_verkehr.traffic_speed",menu.value(8)<0?"--":Integer.toString(menu.value(8)),menu.value(1)),14,48,menu.value(15)==1?0xFFFF7777:0xFF8FF0D3,false);
        else if(menu.kind==TrafficNodeBlock.Kind.JAM_SENSOR)
            g.drawString(font,Component.translatable("label.ng_verkehr.traffic_slow",menu.value(9),Component.translatable(menu.value(15)==1?"options.on":"options.off")),14,48,0xFFEEF3FB,false);
        else g.drawString(font,Component.translatable("label.ng_verkehr.traffic_alarm",Component.translatable(menu.value(15)==1?"options.on":"options.off")),14,48,0xFFEEF3FB,false);
        int hintY=menu.kind==TrafficNodeBlock.Kind.JAM_SENSOR?188:menu.kind==TrafficNodeBlock.Kind.DETOUR_CONTROLLER||menu.kind==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER?164:menu.kind==TrafficNodeBlock.Kind.SPEED_SENSOR||menu.kind==TrafficNodeBlock.Kind.BUS_SENSOR||menu.kind==TrafficNodeBlock.Kind.EMERGENCY_SENSOR?164:menu.kind==TrafficNodeBlock.Kind.CROSSING_CONTROLLER||menu.kind==TrafficNodeBlock.Kind.COUNTER||menu.kind==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER?139:114;
        g.drawWordWrap(font,Component.translatable("label.ng_verkehr.traffic_hint_"+menu.kind.name().toLowerCase(java.util.Locale.ROOT)),14,hintY,imageWidth-28,0xFFAAB7CC);
    }
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g,x,y,p);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
