package de.nevergate.verkehr.client;

import de.nevergate.verkehr.ControllerMenu;
import de.nevergate.verkehr.SignalKind;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import java.util.*;

/** Compact live device table with position-preserving selection. */
public final class ControllerScreen extends AbstractContainerScreen<ControllerMenu> {
    private static final float TEXT_SCALE=.75f;
    private static final int ROW_HEIGHT=11, LIST_TOP=59;
    private static final String[] DIRECTIONS={"Nord","Ost","Süd","West"};
    private static final String[] MOVEMENTS={"Geradeaus","Linksabbieger","Rechtsabbieger","Geradeaus + Links","Geradeaus + Rechts","Links + Rechts","Alle Richtungen"};
    private final List<CompactButton> rows=new ArrayList<>(),directions=new ArrayList<>(),crossings=new ArrayList<>();
    private CompactButton start,movement,legacy,auto,disconnect,resetYes,resetNo;
    private int confirmX,confirmY,confirmWidth;
    private final List<CompactButton> radii=new ArrayList<>();
    private int column,detailX,detailWidth,visibleRows,scroll;
    private boolean draggingScroll,programView;
    private final List<CompactButton> deviceWidgets=new ArrayList<>(),programWidgets=new ArrayList<>();
    private final List<CompactButton> settingButtons=new ArrayList<>();
    private CompactButton tab;
    private static final String[] PROGRAMS={"Kreuzung","Fußgängerüberweg","Baustellenwechsel"};
    private static final String[] PHASES={"Gestoppt","Fahrzeuggruppe A","Fahrzeuggruppe B","Überweg A","Überweg B","Geschützte Fahrten","Gelbblinken","Sicherheitsräumung","Vorrangphase"};
    public ControllerScreen(ControllerMenu menu,Inventory inventory,Component title) { super(menu,inventory,title); }
    @Override protected void init() {
        imageWidth=Math.min(740,width-12);imageHeight=Math.min(410,height-12);super.init();
        rows.clear();directions.clear();crossings.clear();radii.clear();deviceWidgets.clear();programWidgets.clear();settingButtons.clear();
        column=Math.round(imageWidth*.59f);detailX=leftPos+column+10;detailWidth=imageWidth-column-20;
        visibleRows=Math.max(1,(imageHeight-LIST_TOP-42)/ROW_HEIGHT);scroll=Math.min(scroll,maxScroll());
        for(int row=0;row<visibleRows;row++) {
            final int offset=row;
            rows.add(compact("",leftPos+7,topPos+LIST_TOP+row*ROW_HEIGHT,column-15,ROW_HEIGHT-1,()->select(rows.get(offset).renderedIndex,rows.get(offset).renderedRevision)));
        }
        for(int i=0;i<4;i++){
            final int choice=i;radii.add(compact(""+new int[]{16,24,32,48}[i],leftPos+41+i*23,topPos+29,21,13,()->send(16+choice)));
        }
        auto=compact("",leftPos+imageWidth-130,topPos+29,68,13,()->send(1));
        compact("Suchen",leftPos+imageWidth-58,topPos+29,51,13,()->send(2));
        int controlWidth=Math.max(20,(detailWidth-6)/4);
        for(int i=0;i<4;i++) {
            final int direction=i;
            directions.add(compact(new String[]{"N","O","S","W"}[i],detailX+i*(controlWidth+2),topPos+121,controlWidth,14,()->send(11+direction)));
        }
        legacy=compact("Bisherige A/B-Gruppe",detailX,topPos+139,detailWidth,14,()->send(15));
        movement=compact("",detailX,topPos+173,detailWidth,14,()->send(4));
        for(int i=0;i<2;i++) {
            final int group=i;
            crossings.add(compact("Überweg "+(i==0?"A":"B"),detailX+i*((detailWidth-3)/2+3),topPos+121,(detailWidth-3)/2,14,()->send(6+group)));
        }
        deviceWidgets.addAll(rows);deviceWidgets.addAll(directions);deviceWidgets.addAll(crossings);deviceWidgets.addAll(radii);deviceWidgets.add(legacy);deviceWidgets.add(movement);deviceWidgets.add(auto);
        for(var child:children())if(child instanceof CompactButton b&&!deviceWidgets.contains(b))deviceWidgets.add(b);
        tab=compact("",leftPos+imageWidth/2-43,topPos+6,86,13,()->{programView=!programView;updateWidgets();});
        int px=leftPos+8,pw=imageWidth-16;
        settingButtons.add(compact("",px,topPos+51,pw,16,()->setting(0,(menu.value(15)+1)%3)));
        String[] labels={"Fahrzeuge Grün","Abbieger Grün","Fußgänger Grün"};
        for(int field=1;field<=3;field++){
            final int f=field;int y=topPos+75+(field-1)*22;
            programWidgets.add(compact("−",px+pw-59,y,27,15,()->setting(f,menu.value(15+f)-(f==3?1:5))));
            programWidgets.add(compact("+",px+pw-28,y,27,15,()->setting(f,menu.value(15+f)+(f==3?1:5))));
        }
        for(int field=4;field<=6;field++){
            final int f=field;settingButtons.add(compact("",px+(field-4)*(pw/3),topPos+143,pw/3-3,16,()->setting(f,1-menu.value(15+f))));
        }
        for(int slot=0;slot<3;slot++){
            final int n=slot;int x=px+slot*(pw/3);
            programWidgets.add(compact("S"+(slot+1)+" speichern",x,topPos+166,pw/3-3,15,()->setting(7,n)));
            programWidgets.add(compact("S"+(slot+1)+" laden",x,topPos+184,pw/3-3,15,()->setting(8,n)));
        }
        programWidgets.addAll(settingButtons);
        start=compact("",leftPos+imageWidth-130,topPos+imageHeight-19,68,14,()->send(0));
        compact("Schließen",leftPos+imageWidth-58,topPos+imageHeight-19,51,14,this::onClose);
        disconnect=compact("Verbindung lösen",detailX,topPos+93,detailWidth,13,()->send(20));deviceWidgets.add(disconnect);
        deviceWidgets.add(compact("Alle lösen",leftPos+7,topPos+imageHeight-19,62,14,()->send(21)));
        deviceWidgets.add(compact("Erneut erkennen",leftPos+73,topPos+imageHeight-19,87,14,()->send(24)));
        confirmWidth=Math.min(270,imageWidth-24);confirmX=leftPos+(imageWidth-confirmWidth)/2;confirmY=topPos+(imageHeight-88)/2;
        int half=(confirmWidth-24)/2;
        resetYes=compact("Ja, alle lösen",confirmX+8,confirmY+65,half,15,()->send(22));
        resetNo=compact("Abbrechen",confirmX+16+half,confirmY+65,half,15,()->send(23));
        updateWidgets();
    }
    private CompactButton compact(String label,int x,int y,int w,int h,Runnable action) {
        return addRenderableWidget(new CompactButton(x,y,w,h,Component.literal(label),b->action.run()));
    }
    private void send(int action) { if(minecraft!=null&&minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,action); }
    private void setting(int field,int value) { send(26);send(field);send(Math.max(0,Math.min(127,value))); }
    private void select(int index,int revision) {
        if(index<0||index>=menu.positions.size())return;
        send(25);
        for(int shift=21;shift>=0;shift-=7)send((revision>>>shift)&127);
        // Each wire action fits the container packet's signed byte. Ordered
        // bank + row actions can address all 256 devices without stale indices.
        send(8+index/ControllerMenu.PAGE_SIZE);send(ControllerMenu.ROW_ACTION+index%ControllerMenu.PAGE_SIZE);
    }
    private int maxScroll() { return Math.max(0,menu.positions.size()-visibleRows); }
    private String type(int type) {
        var kind=SignalKind.forMenu(type);if(kind!=null)return kind.label;
        return switch(type){case 1->"Fußgängerampel";case 2->"Taster";case 4->"Nicht geladen";default->"Nicht verbunden";};
    }
    private String status(int type,int state) {
        if(type==13)return state==3?"Gelb · AN":"AUS";
        if(SignalKind.forMenu(type)!=null)return new String[]{"Rot","Rot-Gelb","Grün","Gelb","Aus"}[Math.max(0,Math.min(4,state))];
        if(type==1)return state==1?"Grün":"Rot";
        if(type==2)return state==1?"Wartet":"Bereit";return "–";
    }
    private int statusColor(int type,int state) {
        if(type==13)return state==3?0xFFE16B:0xA8B8C3;
        if(type==2)return state==1?0xFFE16B:0xA8B8C3;
        if(type==1)return state==1?0x70E8A2:0xFF8585;
        if(SignalKind.forMenu(type)==null)return 0xA8B8C3;
        return state==2?0x70E8A2:state==0?0xFF8585:0xFFE16B;
    }
    private void updateWidgets() {
        scroll=Math.max(0,Math.min(scroll,maxScroll()));
        for(var child:children())if(child instanceof Button button)button.active=true;
        int selected=menu.value(1),type=menu.value(5),direction=menu.value(2);
        boolean route=SignalKind.forMenu(type)!=null&&type!=13;
        for(int row=0;row<rows.size();row++) {
            var button=rows.get(row);int index=scroll+row;button.row=index;button.visible=index<menu.positions.size();button.chosen=index==selected;
            if(button.visible)button.setMessage(Component.literal((index+1)+" · "+type(menu.deviceType(index))+" · "+status(menu.deviceType(index),menu.deviceStatus(index))));
        }
        for(int i=0;i<directions.size();i++){var button=directions.get(i);button.visible=route;button.chosen=direction==i;}
        for(int i=0;i<crossings.size();i++){var button=crossings.get(i);button.visible=type==1||type==2;button.chosen=menu.value(4)==i;}
        legacy.visible=type==0||type==14;legacy.chosen=direction<0;
        movement.visible=type==0||type==14;movement.setMessage(Component.literal(MOVEMENTS[Math.min(6,Math.max(0,menu.value(3)))]));
        auto.setMessage(Component.literal(menu.value(10)==1?"Auto: AN":"Auto: AUS"));auto.chosen=menu.value(10)==1;
        for(int i=0;i<radii.size();i++)radii.get(i).chosen=menu.value(9)==new int[]{16,24,32,48}[i];
        start.setMessage(Component.literal(menu.value(0)==1?"Stoppen":"Starten"));start.chosen=menu.value(0)==1;
        disconnect.visible=selected>=0&&selected<menu.positions.size()&&type!=3;
        tab.setMessage(Component.literal(programView?"◀ Geräte":"Programm ▶"));
        for(var b:deviceWidgets)if(programView)b.visible=false;
        for(var b:programWidgets)b.visible=programView;
        if(!programView){for(var b:radii)b.visible=true;auto.visible=true;for(var b:deviceWidgets)if(!rows.contains(b)&&!directions.contains(b)&&!crossings.contains(b)&&b!=legacy&&b!=movement&&b!=disconnect)b.visible=true;}
        settingButtons.get(0).setMessage(Component.literal("Programm: "+PROGRAMS[Math.max(0,Math.min(2,menu.value(15)))]));
        String[] toggles={"Parallel","Gelbblinken","Auto Nacht"};
        for(int i=0;i<3;i++){var b=settingButtons.get(i+1);b.setMessage(Component.literal(toggles[i]+": "+(menu.value(19+i)==1?"AN":"AUS")));b.chosen=menu.value(19+i)==1;}
        resetYes.visible=resetNo.visible=menu.value(14)==1;
        if(menu.value(14)==1){for(var child:children())if(child instanceof Button button)button.active=false;resetYes.active=resetNo.active=true;}
    }
    @Override protected void containerTick() { super.containerTick();updateWidgets(); }
    private String fit(String value,int width) {
        int pixels=Math.max(0,(int)(width/TEXT_SCALE));if(font.width(value)<=pixels)return value;
        return font.plainSubstrByWidth(value,Math.max(0,pixels-font.width("…")))+"…";
    }
    private void small(GuiGraphics g,String value,int x,int y,int color,int maxWidth) {
        g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(TEXT_SCALE,TEXT_SCALE,1);
        g.drawString(font,fit(value,maxWidth),0,0,color,false);g.pose().popPose();
    }
    private void wrapped(GuiGraphics g,String value,int x,int y,int width,int color,int maxBottom) {
        var lines=font.split(Component.literal(value),Math.max(1,(int)(width/TEXT_SCALE)));
        for(var line:lines){if(y+7>maxBottom)break;g.pose().pushPose();g.pose().translate(x,y,0);g.pose().scale(TEXT_SCALE,TEXT_SCALE,1);g.drawString(font,line,0,0,color,false);g.pose().popPose();y+=9;}
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY) {
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xF018222A);
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+25,0xFF263943);
        if(!programView)g.fill(leftPos+column,topPos+45,leftPos+column+1,topPos+imageHeight-24,0xFF485A62);
        if(!programView&&maxScroll()>0){int x=leftPos+column-6,y=topPos+LIST_TOP,h=visibleRows*ROW_HEIGHT;
            g.fill(x,y,x+3,y+h,0xFF30444D);int thumb=Math.max(9,h*visibleRows/menu.positions.size());int offset=(h-thumb)*scroll/maxScroll();g.fill(x,y+offset,x+3,y+offset+thumb,0xFF7BA995);
        }
    }
    @Override protected void renderLabels(GuiGraphics g,int mouseX,int mouseY) {
        small(g,"NG · Ampelsteuergerät",8,9,0xFFFFFF,imageWidth/2-50);
        small(g,menu.value(0)==1?"LÄUFT":"GESTOPPT",imageWidth-65,9,menu.value(0)==1?0x70E8A2:0xFFE16B,60);
        if(programView){
            small(g,"Aktuell: "+PHASES[Math.max(0,Math.min(PHASES.length-1,menu.value(22)))]+" · "+menu.value(24)+" s",8,29,0x70E8A2,imageWidth-16);
            small(g,"Danach: "+PHASES[Math.max(0,Math.min(PHASES.length-1,menu.value(23)))],8,39,0xA9BDC7,imageWidth-16);
            String[] labels={"Fahrzeuge Grün","Abbieger Grün","Fußgänger Grün"};
            for(int i=0;i<3;i++)small(g,labels[i]+": "+menu.value(16+i)+" Sekunden",8,79+i*22,0xCFDDE2,imageWidth-83);
            small(g,"Ändern / Laden stoppt. Anschließend Starten drücken.",8,132,0xE5CC91,imageWidth-16);
            if(imageHeight>250)wrapped(g,"Kreuzung: A/B nach Achse. Überweg: Fahrzeuge nur A. Baustelle: Nord/Ost = A, Süd/West = B. Parallel nur bei geprüften Fahrwegen; runde Fahrzeugampeln sperren paralleles Fußgängergrün. Auto Nacht: 13000–23000 Weltzeit.",8,207,imageWidth-16,0xA9BDC7,imageHeight-39);
            return;
        }
        small(g,"GERÄTE · "+menu.positions.size(),8,47,0xA9BDC7,column-70);small(g,"ZUSTAND",column-57,47,0xA9BDC7,50);
        small(g,"Radius",8,32,0xA9BDC7,31);
        String hint=menu.value(10)==0?"Geräteliste aktualisiert sich automatisch.":menu.value(11)==0?"Mitte: Verknüpfer + Schleichen auf Boden.":menu.value(13)==0?"Erstes Gerät mit Verknüpfer verbinden.":menu.value(12)>0?menu.value(12)+" unklare Geräte: manuell zuordnen.":"Automatik aktiv · Geräteliste ist aktuell.";
        small(g,hint,8,imageHeight-35,menu.value(10)==1?0xE5CC91:0x91A8B4,column-14);
        int x=column+10,w=detailWidth,selected=menu.value(1),type=menu.value(5),bottom=imageHeight-26;
        if(menu.positions.isEmpty()){wrapped(g,"Noch keine verbundenen Geräte. Verknüpfer oder automatische Erkennung verwenden.",x,50,w,0xCFDDE2,bottom);return;}
        if(selected<0||selected>=menu.positions.size()){wrapped(g,"Gerät links auswählen.",x,50,w,0xCFDDE2,bottom);return;}
        var pos=menu.positions.get(selected);
        wrapped(g,type(type)+" #"+(selected+1),x,48,w,0xFFFFFF,67);
        small(g,pos.getX()+" / "+pos.getY()+" / "+pos.getZ(),x,71,0x91A8B4,w);
        small(g,status(type,menu.deviceStatus(selected)),x,83,statusColor(type,menu.deviceStatus(selected)),w);
        boolean route=SignalKind.forMenu(type)!=null&&type!=13;
        if(route){
            small(g,"Zufahrt: "+(menu.value(2)<0?"Gruppe "+(menu.value(4)==0?"A":"B"):DIRECTIONS[Math.min(3,menu.value(2))]),x,107,0xCFDDE2,w);
            if(type==0||type==14){small(g,"Fahrtrichtung",x,161,0x91A8B4,w);wrapped(g,"Ändern stoppt die Steuerung.",x,194,w,0xE5CC91,bottom);}
            else {
                wrapped(g,"Fahrt: "+MOVEMENTS[Math.min(6,Math.max(0,menu.value(3)))],x,143,w,0xCFDDE2,173);
                wrapped(g,menu.value(3)>0?(menu.value(19)==1?"Eigene Phase; geprüfte Fahrten parallel.":"Eigene Phase; alle anderen Rot."):"Läuft mit Fahrzeuggruppe "+(menu.value(4)==0?"A":"B")+".",x,180,w,0xE5CC91,bottom);
            }
        }else if(type==1||type==2){
            small(g,"Überweg zuordnen",x,107,0xCFDDE2,w);
            wrapped(g,"Automatik: Taster folgt naher Fußgängerampel. A/B hier manuell korrigierbar. Ändern stoppt die Steuerung.",x,145,w,0xE5CC91,bottom);
        }else wrapped(g,type==13?"Gelbes Warnlicht blinkt bei laufender Steuerung. Bei Stopp aus. Keine Fahrfreigabe.":"Gerät fehlt, ist anders verbunden oder nicht geladen.",x,110,w,0xE5CC91,bottom);
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical) {
        if(programView||menu.value(14)==1)return true;
        if(x>=leftPos&&x<leftPos+column&&y>=topPos+LIST_TOP&&y<topPos+LIST_TOP+visibleRows*ROW_HEIGHT){scroll=Math.max(0,Math.min(maxScroll(),scroll-(int)Math.signum(vertical)*3));updateWidgets();return true;}
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    private void scrollTo(double y) {
        int h=visibleRows*ROW_HEIGHT,thumb=Math.max(9,h*visibleRows/Math.max(1,menu.positions.size()));
        scroll=Math.max(0,Math.min(maxScroll(),(int)Math.round((y-topPos-LIST_TOP-thumb/2.0)*maxScroll()/Math.max(1,h-thumb))));updateWidgets();
    }
    @Override public boolean mouseClicked(double x,double y,int button) {
        if(!programView&&menu.value(14)!=1&&button==0&&maxScroll()>0&&x>=leftPos+column-8&&x<leftPos+column&&y>=topPos+LIST_TOP&&y<topPos+LIST_TOP+visibleRows*ROW_HEIGHT){draggingScroll=true;scrollTo(y);return true;}
        return super.mouseClicked(x,y,button);
    }
    @Override public boolean mouseDragged(double x,double y,int button,double dx,double dy) { if(draggingScroll&&button==0){scrollTo(y);return true;}return super.mouseDragged(x,y,button,dx,dy); }
    @Override public boolean mouseReleased(double x,double y,int button) { if(draggingScroll&&button==0){draggingScroll=false;return true;}return super.mouseReleased(x,y,button); }
    @Override public boolean keyPressed(int key,int scan,int modifiers) {
        if(menu.value(14)==1&&key==256){send(23);return true;}return super.keyPressed(key,scan,modifiers);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial) {
        updateWidgets();super.render(g,mouseX,mouseY,partial);
        if(menu.value(14)==1){
            g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xC0081117);
            g.fill(confirmX,confirmY,confirmX+confirmWidth,confirmY+88,0xFF263943);
            small(g,"Alle Verbindungen wirklich lösen?",confirmX+9,confirmY+10,0xFFCC88,confirmWidth-18);
            small(g,"Ampeln werden Rot, Taster und Warnlicht aus.",confirmX+9,confirmY+26,0xE4EEF2,confirmWidth-18);
            small(g,"Automatik AUS. Mitte und Radius bleiben erhalten.",confirmX+9,confirmY+39,0xA9BDC7,confirmWidth-18);
            small(g,"Bestätigung gilt fünf Sekunden.",confirmX+9,confirmY+51,0xA9BDC7,confirmWidth-18);
            resetYes.render(g,mouseX,mouseY,partial);resetNo.render(g,mouseX,mouseY,partial);return;
        }
        // Full names and coordinates remain readable even when a table cell is clipped.
        for(var row:rows)if(row.visible&&row.isHovered()){
            var p=menu.positions.get(row.row);
            g.pose().pushPose();g.pose().scale(TEXT_SCALE,TEXT_SCALE,1);
            g.renderTooltip(font,List.of(row.getMessage(),Component.literal(p.getX()+" / "+p.getY()+" / "+p.getZ())),Optional.empty(),(int)(mouseX/TEXT_SCALE),(int)(mouseY/TEXT_SCALE));g.pose().popPose();break;
        }
    }
    private final class CompactButton extends Button {
        private int row=-1,renderedIndex=-1,renderedRevision;
        private boolean chosen;
        CompactButton(int x,int y,int width,int height,Component message,OnPress action) { super(x,y,width,height,message,action,DEFAULT_NARRATION); }
        @Override protected void renderWidget(GuiGraphics g,int mouseX,int mouseY,float partial) {
            int x=getX(),y=getY(),w=getWidth(),h=getHeight();boolean focus=isHoveredOrFocused();
            g.fill(x,y,x+w,y+h,chosen?0xFF2B5749:focus?0xFF354C58:0xFF22323B);
            if(chosen||focus)g.fill(x,y,x+2,y+h,chosen?0xFF70E8A2:0xFF9AB7C8);
            if(row>=0){renderedIndex=row;renderedRevision=menu.revision();int t=menu.deviceType(row),state=menu.deviceStatus(row);small(g,""+(row+1),x+4,y+2,0x91A8B4,19);small(g,type(t),x+25,y+2,0xE4EEF2,w-79);small(g,status(t,state),x+w-49,y+2,statusColor(t,state),47);}
            else {String label=fit(getMessage().getString(),w-6);int tx=x+Math.max(3,(w-Math.round(font.width(label)*TEXT_SCALE))/2);small(g,label,tx,y+(h-7)/2,active?0xE4EEF2:0x91A8B4,w-6);}
        }
    }
}
