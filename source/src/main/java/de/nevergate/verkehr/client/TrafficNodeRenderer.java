package de.nevergate.verkehr.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.nevergate.verkehr.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.network.chat.Component;
public final class TrafficNodeRenderer implements BlockEntityRenderer<TrafficNodeEntity>{
    private final Font font;
    public TrafficNodeRenderer(BlockEntityRendererProvider.Context c){font=c.getFont();}
    private void text(String s,float y,float width,int color,PoseStack p,MultiBufferSource b){
        p.pushPose();p.translate(0,y,0);float scale=Math.min(.014f,width/Math.max(1,font.width(s)));p.scale(scale,-scale,scale);
        font.drawInBatch(s,-font.width(s)/2f,-4,color,false,p.last().pose(),b,Font.DisplayMode.NORMAL,0,0xF000F0);p.popPose();
    }
    @Override public void render(TrafficNodeEntity n,float partial,PoseStack p,MultiBufferSource b,int light,int overlay){
        if(n.kind()!=TrafficNodeBlock.Kind.SPEED_BOARD&&n.kind()!=TrafficNodeBlock.Kind.DETOUR_GUIDE&&n.kind()!=TrafficNodeBlock.Kind.TUNNEL_LANE)return;
        p.pushPose();p.translate(.5,.65,.5);p.mulPose(Axis.YP.rotationDegrees(-n.getBlockState().getValue(OrientedBlock.FACING).toYRot()));p.translate(0,0,.075);
        if(n.kind()==TrafficNodeBlock.Kind.SPEED_BOARD){
            text(n.online()&&n.speed()>=0?Integer.toString(n.speed()):"--",.09f,.76f,n.alarm()?0xFFFF5555:0xFF66FF88,p,b);
            text("km/h",-.12f,.65f,0xFFFFFFFF,p,b);text(Component.translatable("label.ng_verkehr.traffic_limit_short",n.limit()).getString(),-.28f,.75f,0xFFFFFFFF,p,b);
        }else if(n.kind()==TrafficNodeBlock.Kind.TUNNEL_LANE)text(n.status()==1?"↓":"X",-.02f,.6f,n.status()==1?0xFF66FF88:0xFFFF5555,p,b);
        else{
            text(n.online()&&n.alarm()?n.text():Component.translatable(n.online()?"label.ng_verkehr.traffic_inactive":"label.ng_verkehr.traffic_offline").getString(),.02f,.82f,n.online()&&n.alarm()?0xFFFFD65C:0xFF999999,p,b);
            text(n.online()&&n.alarm()?(n.direction()==0?"←":n.direction()==1?"↑":"→"):"--",-.24f,.6f,0xFFFFD65C,p,b);
        }p.popPose();
    }
}
