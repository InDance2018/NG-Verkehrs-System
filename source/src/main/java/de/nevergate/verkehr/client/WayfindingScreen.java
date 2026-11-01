package de.nevergate.verkehr.client;

import de.nevergate.verkehr.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.network.PacketDistributor;

public final class WayfindingScreen extends AbstractContainerScreen<WayfindingMenu> {
    private final EditBox[] targets=new EditBox[3],distances=new EditBox[3];
    private WayfindingData draft;
    private int rows;
    public WayfindingScreen(WayfindingMenu menu,Inventory inventory,Component title){super(menu,inventory,title);draft=menu.snapshot.copy();}
    @Override protected void init(){
        if(targets[0]!=null)capture();
        imageWidth=Math.min(440,width-16);imageHeight=230;super.init();
        rows=menu.kind==WayfindingBlock.Kind.MOTORWAY||menu.kind==WayfindingBlock.Kind.DISTANCE?3:2;
        boolean town=menu.kind==WayfindingBlock.Kind.TOWN||menu.kind==WayfindingBlock.Kind.TOWN_EXIT;
        for(int i=0;i<rows;i++){
            final int row=i;int y=topPos+49+i*36;
            int tw=town?imageWidth-28:imageWidth-152;
            targets[i]=new EditBox(font,leftPos+14,y,tw,19,Component.translatable("label.ng_verkehr.target"));targets[i].setMaxLength(WayfindingData.MAX_TEXT);targets[i].setValue(draft.targets[i]);addRenderableWidget(targets[i]);
            if(!town){
                distances[i]=new EditBox(font,leftPos+imageWidth-130,y,62,19,Component.translatable("label.ng_verkehr.distance"));distances[i].setMaxLength(WayfindingData.MAX_DISTANCE);distances[i].setValue(draft.distances[i]);addRenderableWidget(distances[i]);
                addRenderableWidget(Button.builder(Component.literal(WayfindingData.arrow(draft.directions[i])),button->{draft.directions[row]=(draft.directions[row]+1)%5;button.setMessage(Component.literal(WayfindingData.arrow(draft.directions[row])));}).bounds(leftPos+imageWidth-58,y,44,20).build());
            }
        }
        if(menu.kind==WayfindingBlock.Kind.MOTORWAY){
            addRenderableWidget(Button.builder(laneLabel(),button->{draft.laneCount=draft.laneCount%4+1;button.setMessage(laneLabel());}).bounds(leftPos+14,topPos+174,106,20).build());
            for(int i=0;i<4;i++){
                final int lane=i;int laneWidth=(imageWidth-144)/4;
                addRenderableWidget(Button.builder(Component.literal((i+1)+" "+WayfindingData.arrow(draft.lanes[i])),button->{draft.lanes[lane]=(draft.lanes[lane]+1)%5;button.setMessage(Component.literal((lane+1)+" "+WayfindingData.arrow(draft.lanes[lane])));}).bounds(leftPos+128+i*(laneWidth+3),topPos+174,laneWidth,20).build());
            }
        }
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"),button->onClose()).bounds(leftPos+14,topPos+203,100,20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),button->{capture();WayfindingNetwork.CHANNEL.send(new WayfindingNetwork.Save(menu.containerId,menu.revision,draft),PacketDistributor.SERVER.noArg());button.active=false;}).bounds(leftPos+imageWidth-114,topPos+203,100,20).build());
        setInitialFocus(targets[0]);
    }
    private Component laneLabel(){return Component.translatable("label.ng_verkehr.lanes",draft.laneCount);}
    private void capture(){for(int i=0;i<3;i++){if(targets[i]!=null)draft.targets[i]=targets[i].getValue();if(distances[i]!=null)draft.distances[i]=distances[i].getValue();}}
    @Override protected void renderBg(GuiGraphics g,float partial,int mouseX,int mouseY){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF0C111B);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFF8FF0D3);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,title,14,12,0x8FF0D3,false);
        boolean town=menu.kind==WayfindingBlock.Kind.TOWN||menu.kind==WayfindingBlock.Kind.TOWN_EXIT;
        for(int i=0;i<rows;i++){
            Component label=town?Component.translatable(i==0?"label.ng_verkehr.place":"label.ng_verkehr.district"):Component.translatable("label.ng_verkehr.target_number",i+1);
            g.drawString(font,label,14,37+i*36,0xEEF3FB,false);
            if(!town){g.drawString(font,Component.translatable("label.ng_verkehr.distance"),imageWidth-130,37+i*36,0xAAB7CC,false);}
        }
        if(menu.kind==WayfindingBlock.Kind.MOTORWAY)g.drawString(font,Component.translatable("label.ng_verkehr.lane_hint"),14,158,0xAAB7CC,false);
        else g.drawString(font,Component.translatable("label.ng_verkehr.sign_hint"),14,158,0xAAB7CC,false);
    }
    @Override public boolean keyPressed(int key,int scan,int modifiers){if(key==256||key==258)return super.keyPressed(key,scan,modifiers);if(getFocused() instanceof EditBox box){box.keyPressed(key,scan,modifiers);return true;}return super.keyPressed(key,scan,modifiers);}
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partial){renderBackground(g,mouseX,mouseY,partial);super.render(g,mouseX,mouseY,partial);renderTooltip(g,mouseX,mouseY);}
}
