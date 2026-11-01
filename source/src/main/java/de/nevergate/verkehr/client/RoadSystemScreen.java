package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public final class RoadSystemScreen extends AbstractContainerScreen<RoadSystemMenu>{
    private RoadSystemData draft,base;private EditBox name,capacity,occupied,vehicleTypes;private int page,tab,frame,lastZoneCount;
    private Button modeButton,speedButton,directionButton,detectionButton,countingButton,autoButton,heightButton,routeButton;
    private boolean aggregate(){return menu.role==RoadSystemBlock.Role.AGGREGATOR;}
    private boolean control(){return menu.role==RoadSystemBlock.Role.CONTROLLER;}
    private Component tr(String key,Object... args){return Component.translatable("label.ng_verkehr."+key,args);}
    private Component mode(){return Component.translatable("mode.ng_verkehr."+draft.mode);}
    private Component detection(){return Component.translatable("detection.ng_verkehr."+draft.detection);}
    private Component route(){
        if(draft.routeMode==0)return tr("route_total");if(draft.routeMode==2)return tr("route_auto");
        for(var z:menu.zones)if(z.pos().asLong()==draft.routeTarget)return Component.literal(z.name().isBlank()?z.pos().toShortString():z.name());
        return tr("route_missing");
    }
    public RoadSystemScreen(RoadSystemMenu menu,Inventory inventory,Component title){super(menu,inventory,title);base=menu.snapshot.copy();draft=base.copy();frame=menu.liveFrame;lastZoneCount=menu.zones.size();tab=menu.role==RoadSystemBlock.Role.HEIGHT_SIGN||menu.role==RoadSystemBlock.Role.BARRIER||menu.role==RoadSystemBlock.Role.SPACE_SENSOR?1:0;}
    private Button button(Component text,int x,int y,int w,java.util.function.Consumer<Button> action){return addRenderableWidget(Button.builder(text,action::accept).bounds(leftPos+x,topPos+y,w,18).build());}
    @Override protected void init(){
        imageWidth=Math.min(430,width-16);imageHeight=264;super.init();clearWidgets();
        modeButton=speedButton=directionButton=detectionButton=countingButton=autoButton=heightButton=routeButton=null;
        name=new EditBox(font,leftPos+14,topPos+43,imageWidth-28,18,tr("name"));name.setMaxLength(48);name.setValue(draft.name);addRenderableWidget(name);
        capacity=occupied=vehicleTypes=null;
        if(aggregate()){
            page=Math.min(page,Math.max(0,(menu.zones.size()-1)/4));
            for(int row=0;row<4;row++){final int index=page*4+row;if(index>=menu.zones.size())break;
                button(Component.literal("×"),imageWidth-35,74+row*28,20,b->{WayfindingNetwork.CHANNEL.send(new WayfindingNetwork.RemoveZone(menu.containerId,menu.revision,index),PacketDistributor.SERVER.noArg());});
            }
            var prev=button(Component.literal("<"),14,188,32,b->{capture();page--;init();});prev.active=page>0;
            var next=button(Component.literal(">"),54,188,32,b->{capture();page++;init();});next.active=(page+1)*4<menu.zones.size();
        }else{
            button(tr(tab==0?"parking_options":"basic_options"),imageWidth-120,8,106,b->{capture();tab=1-tab;init();});
            if(tab==0){
                modeButton=button(mode(),14,70,imageWidth-28,b->{draft.mode=(draft.mode+1)%5;b.setMessage(mode());});modeButton.active=control();
                speedButton=button(Component.literal(draft.speed+" km/h"),14,92,100,b->{draft.speed=draft.speed>=130?20:draft.speed+10;b.setMessage(Component.literal(draft.speed+" km/h"));});speedButton.active=control();
                directionButton=button(Component.literal(WayfindingData.arrow(draft.direction)),124,92,45,b->{draft.direction=(draft.direction+1)%5;b.setMessage(Component.literal(WayfindingData.arrow(draft.direction)));});
                capacity=new EditBox(font,leftPos+14,topPos+131,100,18,tr("capacity"));capacity.setMaxLength(7);capacity.setFilter(s->s.matches("[0-9]*"));capacity.setValue(""+draft.capacity);capacity.setEditable(control()&&draft.counting==0);addRenderableWidget(capacity);
                occupied=new EditBox(font,leftPos+imageWidth-114,topPos+131,100,18,tr("occupied"));occupied.setMaxLength(7);occupied.setFilter(s->s.matches("[0-9]*"));occupied.setValue(""+draft.occupied);occupied.setEditable(control()&&draft.counting==0);addRenderableWidget(occupied);
                vehicleTypes=new EditBox(font,leftPos+14,topPos+171,imageWidth-28,18,tr("vehicle_types"));vehicleTypes.setMaxLength(1024);vehicleTypes.setValue(draft.vehicleTypes);vehicleTypes.setEditable(control());addRenderableWidget(vehicleTypes);
                detectionButton=button(detection(),14,194,imageWidth-28,b->{draft.detection=(draft.detection+1)%3;b.setMessage(detection());});detectionButton.active=control();
            }else{
                if(control()){
                    countingButton=button(tr("counting_"+draft.counting),14,78,imageWidth-28,b->{draft.counting=1-draft.counting;b.setMessage(tr("counting_"+draft.counting));});
                    autoButton=button(tr("auto_display_"+draft.autoDisplay),14,106,imageWidth-28,b->{draft.autoDisplay=1-draft.autoDisplay;b.setMessage(tr("auto_display_"+draft.autoDisplay));});
                }
                if(menu.role==RoadSystemBlock.Role.BARRIER||menu.role==RoadSystemBlock.Role.HEIGHT_SIGN)
                    heightButton=button(tr("height_value",String.format(java.util.Locale.ROOT,"%.1f",draft.heightCm/100.0)),14,78,imageWidth-28,b->{draft.heightCm=draft.heightCm>=650?150:draft.heightCm+10;b.setMessage(tr("height_value",String.format(java.util.Locale.ROOT,"%.1f",draft.heightCm/100.0)));});
                if(menu.role==RoadSystemBlock.Role.PARKING_GUIDE)
                    routeButton=button(route(),14,78,imageWidth-28,b->{
                        if(draft.routeMode==0){draft.routeMode=2;}
                        else if(draft.routeMode==2){if(menu.zones.isEmpty())draft.routeMode=0;else{draft.routeMode=1;draft.routeTarget=menu.zones.get(0).pos().asLong();}}
                        else{int index=-1;for(int i=0;i<menu.zones.size();i++)if(menu.zones.get(i).pos().asLong()==draft.routeTarget)index=i;
                            if(index+1>=menu.zones.size())draft.routeMode=0;else draft.routeTarget=menu.zones.get(index+1).pos().asLong();}
                        b.setMessage(route());
                    });
            }
        }
        button(Component.translatable("gui.cancel"),14,237,100,b->onClose());
        button(Component.translatable("gui.done"),imageWidth-114,237,100,b->{capture();WayfindingNetwork.CHANNEL.send(new WayfindingNetwork.SaveSystem(menu.containerId,menu.revision,draft),PacketDistributor.SERVER.noArg());b.active=false;});
    }
    private void capture(){
        if(name!=null)draft.name=name.getValue();if(vehicleTypes!=null)draft.vehicleTypes=vehicleTypes.getValue();
        if(capacity!=null)try{draft.capacity=Integer.parseInt(capacity.getValue());}catch(NumberFormatException e){}
        if(occupied!=null)try{draft.occupied=Integer.parseInt(occupied.getValue());}catch(NumberFormatException e){}
    }
    private void updateBox(EditBox box,String value){if(box!=null&&!box.isFocused()&&!box.getValue().equals(value))box.setValue(value);}
    @Override protected void containerTick(){
        super.containerTick();if(frame==menu.liveFrame)return;capture();draft=RoadSystemDraft.merge(base,draft,menu.snapshot);base=menu.snapshot.copy();frame=menu.liveFrame;
        if(aggregate()){if(lastZoneCount!=menu.zones.size()){lastZoneCount=menu.zones.size();init();}else updateBox(name,draft.name);return;}
        updateBox(name,draft.name);updateBox(capacity,""+draft.capacity);updateBox(occupied,""+draft.occupied);updateBox(vehicleTypes,draft.vehicleTypes);
        if(capacity!=null)capacity.setEditable(control()&&draft.counting==0);if(occupied!=null)occupied.setEditable(control()&&draft.counting==0);
        if(modeButton!=null)modeButton.setMessage(mode());if(speedButton!=null)speedButton.setMessage(Component.literal(draft.speed+" km/h"));
        if(directionButton!=null)directionButton.setMessage(Component.literal(WayfindingData.arrow(draft.direction)));if(detectionButton!=null)detectionButton.setMessage(detection());
        if(countingButton!=null)countingButton.setMessage(tr("counting_"+draft.counting));if(autoButton!=null)autoButton.setMessage(tr("auto_display_"+draft.autoDisplay));
        if(routeButton!=null)routeButton.setMessage(route());
        if(heightButton!=null)heightButton.setMessage(tr("height_value",String.format(java.util.Locale.ROOT,"%.1f",draft.heightCm/100.0)));
    }
    @Override protected void renderBg(GuiGraphics g,float partial,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF0C111B);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFF8FF0D3);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,font.plainSubstrByWidth(title.getString(),aggregate()?imageWidth-28:imageWidth-140),14,12,0x8FF0D3,false);g.drawString(font,tr("name"),14,31,0xEEF3FB,false);
        if(aggregate()){
            g.drawString(font,tr("zones",menu.zones.size()),14,64,0xEEF3FB,false);
            for(int row=0;row<4;row++){int index=page*4+row;if(index>=menu.zones.size())break;var z=menu.zones.get(index);
                String label=z.name().isBlank()?z.pos().toShortString():z.name();
                String value=z.online()?tr("zone_counts",z.capacity()-z.occupied(),z.occupied(),z.capacity()).getString():"OFFLINE";
                g.drawString(font,font.plainSubstrByWidth(label,imageWidth-60),14,78+row*28,z.online()?0xEEF3FB:0xFF7777,false);
                g.drawString(font,font.plainSubstrByWidth(value,imageWidth-60),14,89+row*28,z.online()&&z.occupied()<z.capacity()?0x8FF0D3:0xFF7777,false);
            }
            String total=menu.online?tr("zone_counts",menu.snapshot.free(),menu.snapshot.occupied,menu.snapshot.capacity).getString():"OFFLINE";
            g.drawString(font,font.plainSubstrByWidth(total,imageWidth-114),100,193,menu.online?0x8FF0D3:0xFF7777,false);
        }else if(tab==0){
            g.drawString(font,tr("capacity"),14,119,0xEEF3FB,false);g.drawString(font,tr("occupied"),imageWidth-114,119,0xEEF3FB,false);g.drawString(font,tr("vehicle_types"),14,159,0xEEF3FB,false);
        }else{
            String hint=tr(control()?"spaces_connected":menu.role==RoadSystemBlock.Role.SPACE_SENSOR?"space_hint":menu.role==RoadSystemBlock.Role.BARRIER?"barrier_hint":menu.role==RoadSystemBlock.Role.HEIGHT_SIGN?"height_hint":"route_hint",menu.spaceCount).getString();
            int yHint=control()?146:114;
            for(var line:font.split(Component.literal(hint),imageWidth-28)){g.drawString(font,line,14,yHint,0xAAB7CC,false);yHint+=11;}
            if(menu.role!=RoadSystemBlock.Role.HEIGHT_SIGN)g.drawString(font,tr("zone_counts",menu.snapshot.free(),menu.snapshot.occupied,menu.snapshot.capacity),14,195,0x8FF0D3,false);
        }
        Component status=menu.role==RoadSystemBlock.Role.HEIGHT_SIGN?tr("height_local"):control()?tr("connections",menu.count):tr(menu.online?"online":"offline");
        g.drawString(font,font.plainSubstrByWidth(status.getString(),imageWidth-28),14,220,0xAAB7CC,false);
    }
    @Override public boolean keyPressed(int key,int scan,int mod){if(key==256||key==258)return super.keyPressed(key,scan,mod);if(getFocused() instanceof EditBox box){box.keyPressed(key,scan,mod);return true;}return super.keyPressed(key,scan,mod);}
    @Override public void render(GuiGraphics g,int x,int y,float partial){renderBackground(g,x,y,partial);super.render(g,x,y,partial);renderTooltip(g,x,y);}
}

