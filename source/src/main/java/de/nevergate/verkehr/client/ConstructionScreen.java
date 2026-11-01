package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class ConstructionScreen extends AbstractContainerScreen<ConstructionMenu> {
    private final Map<Integer,Button> buttons=new LinkedHashMap<>();
    public ConstructionScreen(ConstructionMenu m,Inventory i,Component t){super(m,i,t);}
    private boolean powered(){return menu.kind==ConstructionBlock.Kind.TRAILER||menu.kind==ConstructionBlock.Kind.LAMP||menu.kind==ConstructionBlock.Kind.TIMED||menu.kind==ConstructionBlock.Kind.MAST||menu.kind==ConstructionBlock.Kind.ACCESS;}
    @Override protected void init(){
        imageWidth=Math.min(420,width-16);imageHeight=Math.min(238,height-8);super.init();buttons.clear();
        if(powered()){
            add(0,14,44,imageWidth-28);
            add(1,14,70,imageWidth-28);
            int bw=(imageWidth-34)/2;
            add(2,14,96,bw);add(3,20+bw,96,bw);
        }
        if(menu.kind==ConstructionBlock.Kind.LAMP){add(4,14,124,(imageWidth-34)/2);add(6,20+(imageWidth-34)/2,124,(imageWidth-34)/2);}
        if(menu.kind==ConstructionBlock.Kind.GATE)add(5,14,44,imageWidth-28);
        addRenderableWidget(Button.builder(Component.translatable("gui.done"),b->onClose()).bounds(leftPos+imageWidth-100,topPos+imageHeight-28,86,20).build());
        refresh();
    }
    private void add(int id,int x,int y,int w){buttons.put(id,addRenderableWidget(Button.builder(Component.empty(),b->{if(minecraft.gameMode!=null)minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id);}).bounds(leftPos+x,topPos+y,w,20).build()));}
    private Component label(int id){
        return switch(id){
            case 0->Component.translatable(menu.kind==ConstructionBlock.Kind.TRAILER?"label.ng_verkehr.work_arrow_"+menu.value(0):menu.kind==ConstructionBlock.Kind.LAMP?"label.ng_verkehr.work_lamp_"+menu.value(0):"label.ng_verkehr.work_power_"+(menu.value(0)==0?0:1));
            case 1->Component.translatable("label.ng_verkehr.work_schedule",Component.translatable(menu.value(4)==1?"options.on":"options.off"));
            case 2->Component.translatable("label.ng_verkehr.work_from",menu.value(1));
            case 3->Component.translatable("label.ng_verkehr.work_to",menu.value(2));
            case 4->Component.translatable("label.ng_verkehr.work_step",menu.value(3));
            case 5->Component.translatable("label.ng_verkehr.work_width",menu.value(5));
            default->Component.translatable("label.ng_verkehr.work_unlink");
        };
    }
    private void refresh(){buttons.forEach((id,b)->{b.setMessage(label(id));b.active=menu.value(9)==1;});}
    @Override protected void containerTick(){super.containerTick();refresh();}
    @Override protected void renderBg(GuiGraphics g,float p,int x,int y){g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF0C111B);g.fill(leftPos,topPos,leftPos+imageWidth,topPos+3,0xFFFFC947);}
    @Override protected void renderLabels(GuiGraphics g,int x,int y){
        g.drawString(font,title,14,12,0xFFFFC947,false);
        Component status=menu.kind==ConstructionBlock.Kind.LAMP?Component.translatable("label.ng_verkehr.work_state",Component.translatable(menu.value(7)==1?"options.on":"options.off"),menu.value(6)):
            ConstructionBlock.moving(menu.kind)?Component.translatable("label.ng_verkehr.work_gate_state",Component.translatable(menu.value(7)==1?"label.ng_verkehr.work_open":"label.ng_verkehr.work_closed")):
            menu.kind==ConstructionBlock.Kind.MAST||menu.kind==ConstructionBlock.Kind.TRAILER?Component.translatable("label.ng_verkehr.work_light",Component.translatable(menu.value(7)==1?"options.on":"options.off")):
            Component.translatable("label.ng_verkehr.work_modular");
        g.drawString(font,status,14,28,0xFFEEF3FB,false);
        String hint="label.ng_verkehr.work_hint_"+menu.kind.name().toLowerCase(java.util.Locale.ROOT);
        int hintY=powered()?154:76;
        g.drawWordWrap(font,Component.translatable(hint),14,hintY,imageWidth-28,0xFFAAB7CC);
        if(menu.kind==ConstructionBlock.Kind.ACCESS)g.drawString(font,Component.translatable("label.ng_verkehr.work_permits",menu.value(8)),14,imageHeight-46,0xFFEEF3FB,false);
    }
    @Override public void render(GuiGraphics g,int x,int y,float p){renderBackground(g,x,y,p);super.render(g,x,y,p);renderTooltip(g,x,y);}
}
