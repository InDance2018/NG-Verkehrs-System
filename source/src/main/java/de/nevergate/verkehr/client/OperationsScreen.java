package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
public final class OperationsScreen extends AbstractContainerScreen<OperationsMenu> {
    private final Map<Integer,Button> buttons=new LinkedHashMap<>();
    public OperationsScreen(OperationsMenu m,Inventory i,Component t){super(m,i,t);}
    private List<Integer> actions(){return switch(menu.kind){
        case CHARGER,FUEL_PUMP->List.of(0,1,11,23,10,19);
        case WASH_TUNNEL->List.of(0,1,12,23,10,19);
        case WASH_ENTRY->List.of(0,10,19);
        case CARD_READER->List.of(0,20,21,10,19);
        case PLATE_READER->List.of(22,10,19);
        case DELIVERY_ZONE,SCHOOL_WARNING->List.of(13,14,24,10,19);
        case BUS_ROUTE->List.of(15,10,19);
        case VISIBILITY_CONTROL->List.of(16,24,10,19);
        case WEATHER_WARNING->List.of(24,10,19);
        case TUNNEL_CALL->List.of(0,17,10,19);
        case ESCAPE_SIGN->List.of(18,10,19);
        default->List.of(10,19);};}
    @Override protected void init(){imageWidth=Math.min(440,width-16);imageHeight=Math.min(292,height-8);super.init();buttons.clear();int y=88;for(int id:actions()){buttons.put(id,addRenderableWidget(Button.builder(Component.empty(),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+14,topPos+y,imageWidth-28,20).build()));y+=23;}addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(leftPos+imageWidth-94,topPos+imageHeight-26,80,20).build());refresh();}
    private String label(int i){return switch(i){case 0->switch(menu.kind){case TUNNEL_CALL->"NOTRUF AUSLÖSEN";case CARD_READER->"Zufahrtskarte prüfen";default->"Leistung starten (kostenloser Betrieb erforderlich)";};case 1->"Eigenen laufenden Vorgang abbrechen";case 10->menu.value(16)==1?"Gerät: eingeschaltet":"Gerät: ausgeschaltet";case 11->"Menge: "+menu.amount()+(menu.kind==OperationsBlock.Kind.CHARGER?" Wh":" ml");case 12->"Waschprogramm: "+menu.value(8);case 13->"Zeitfenster ab: "+menu.value(9)+":00 (Spielzeit)";case 14->"Zeitfenster bis: "+menu.value(10)+":00 (Spielzeit)";case 15->"Linientakt: "+menu.value(11)+" Spielminuten";case 16->menu.value(18)==1?"Manuelle Sichtwarnung: AN":"Manuelle Sichtwarnung: AUS";case 17->"Notruf quittieren";case 18->"Fluchtwegrichtung: "+(menu.value(13)==0?"←":menu.value(13)==1?"→":"↑");case 19->"Verbindungen lösen";case 20->"Persönliche Zufahrtskarte ausstellen";case 21->"Alle Karten dieses Lesers widerrufen";case 22->"Alle Kennzeichenfreigaben entfernen";case 23->menu.value(17)==1?"Betriebsart: KOSTENLOS":"Betriebsart: Zahlung noch nicht angebunden";case 24->"Warnung: Tempo "+menu.value(12)+" km/h";default->"--";};}
    private void refresh(){buttons.forEach((i,b)->{b.setMessage(Component.literal(label(i)));b.active=i<10||menu.value(15)==1;});}
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF0C111B);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFF64C7FF);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){g.drawString(font,title,14,12,0xFF64C7FF,false);g.drawString(font,(menu.value(0)==1?"Bereit":"Gegenstelle / Betrieb prüfen")+" · "+menu.value(14)+" Verbindungen",14,30,0xFFEEF3FB,false);String text="";if(minecraft.level!=null&&minecraft.level.getBlockEntity(menu.pos) instanceof OperationsEntity n)text=n.detail();g.drawWordWrap(font,Component.literal(text),14,47,imageWidth-28,menu.value(1)==1?0xFFFF7777:0xFFB8DDEB);if(menu.value(4)==1){g.fill(14,74,imageWidth-14,79,0xFF273345);g.fill(14,74,14+(imageWidth-28)*Math.min(100,Math.max(0,menu.value(3)))/100,79,0xFF64C7FF);}}
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g,x,y,p);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
