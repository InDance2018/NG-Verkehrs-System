package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class ServiceScreen extends AbstractContainerScreen<ServiceMenu> {
    private final Map<Integer,Button> buttons=new LinkedHashMap<>();
    public ServiceScreen(ServiceMenu m,Inventory i,Component t){super(m,i,t);}
    private List<Integer> actions(){return switch(menu.kind){
        case PARK_METER->List.of(4,1);
        case ENTRY_TICKET->List.of(0);
        case CASHIER->List.of(1);
        case EXIT_READER->List.of(2);
        case TARIFF->List.of(10,11,12,17,18);
        case RESERVATION_BOLLARD->List.of();
        case HEIGHT_WARNING->List.of(14);
        case PEDESTRIAN_CALL,BICYCLE_DETECTOR->List.of(3,13);
        case ACOUSTIC_SIGNAL,BICYCLE_LIGHT->List.of(13);
        case DYNAMIC_SPEED->List.of(15);
        case RAMP_METER->List.of(16,20);
        default->List.of();};}
    @Override protected void init(){
        imageWidth=Math.min(430,width-16);imageHeight=246;super.init();buttons.clear();int y=88;
        for(int id:actions()){add(id,y);y+=23;}
        add(19,Math.min(y,203));
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(leftPos+imageWidth-94,topPos+221,80,20).build());refresh();
    }
    private void add(int id,int y){buttons.put(id,addRenderableWidget(Button.builder(Component.empty(),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+14,topPos+y,imageWidth-28,20).build()));}
    private String money(int v){return v<0?"--":String.format(java.util.Locale.ROOT,"%d,%02d €",v/100,v%100);}
    private String label(int id){return switch(id){case 0->"Einfahrtsticket ausgeben / erneut abholen";case 1->"Bezahlen: "+money(menu.count(19));case 2->"Ticket prüfen und Ausfahrt öffnen";case 3->"Freigabe anfordern";case 4->"Parkdauer: "+menu.value(9)+" Minuten";case 10->"Stundentarif: "+money(menu.count(4));case 11->"Freiminuten: "+menu.value(6);case 12->"Tageshöchstpreis: "+(menu.count(7)==0?"ohne Begrenzung":money(menu.count(7)));case 13->"Anforderungsgruppe: "+(menu.value(10)==0?"A":"B");case 14->"Maximale Höhe: "+menu.value(11)/100.0+" m";case 15->"Reduziertes Tempo: "+menu.value(12)+" km/h";case 16->"Freigabeabstand: "+menu.value(13)/20+" Sekunden";case 17->"Eigenen Dauerparker-Ausweis für 30 Tage ausstellen";case 18->"Eigene Dauerparker-Ausweise widerrufen";case 20->menu.value(22)==0?"Zuflussregelung: Automatik":"Zuflussregelung: Gesperrt";default->"Verbindungen lösen";};}
    private void refresh(){buttons.forEach((id,b)->{b.setMessage(Component.literal(label(id)));b.active=id<10||menu.value(17)==1;});}
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF0C111B);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFF64C7FF);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,title,14,12,0xFF64C7FF,false);
        g.drawString(font,(menu.value(0)==1?"Verbunden":"Verbindung prüfen")+" · "+menu.value(18)+" Verbindungen",14,30,0xFFEEF3FB,false);
        String status=switch(menu.kind){case TARIFF->"Tarif gilt für alle verbundenen Parkgeräte";case FLOOR_BOARD->menu.count(14)<0?"Etage nicht erreichbar":"Freie Plätze: "+menu.count(14);case FAULT_MONITOR->"Störungen: "+menu.value(16);case HEIGHT_WARNING->menu.value(1)==1?"ZU HOCH – Ausweichroute →":"Höhenkontrolle aktiv";case DYNAMIC_SPEED->"Aktuelles Tempolimit: "+menu.value(21)+" km/h";case PEDESTRIAN_CALL,ACOUSTIC_SIGNAL,BICYCLE_DETECTOR,BICYCLE_LIGHT,RAMP_METER->menu.value(2)==1?"GRÜN":menu.value(3)==1?"BITTE WARTEN":"ROT";default->"Tickets gelten nur für ihren Inhaber und ihre Parkzone";};
        g.drawWordWrap(font,Component.literal(status),14,48,imageWidth-28,menu.value(1)==1?0xFFFF7777:0xFFB8DDEB);
    }
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g,x,y,p);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
