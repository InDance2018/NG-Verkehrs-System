package de.nevergate.verkehr.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.nevergate.verkehr.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public final class RoadSystemRenderer implements BlockEntityRenderer<RoadSystemEntity> {
    private final Font font;
    public RoadSystemRenderer(BlockEntityRendererProvider.Context context){font=context.getFont();}
    private void text(String value,float y,float width,int color,PoseStack pose,MultiBufferSource buffers,int light){pose.pushPose();pose.translate(0,y,0);float scale=Math.min(.014f,width/Math.max(1,font.width(value)));pose.scale(scale,-scale,scale);font.drawInBatch(value,-font.width(value)/2f,-4,color,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,light);pose.popPose();}
    @Override public void render(RoadSystemEntity node,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        if(node.controller()||node.sensor()||node.role()==RoadSystemBlock.Role.SPACE_SENSOR||node.role()==RoadSystemBlock.Role.BARRIER)return;
        var d=node.data();var role=node.role();pose.pushPose();pose.translate(.5,.5,.5);pose.mulPose(Axis.YP.rotationDegrees(-node.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));pose.translate(0,0,.071+(MountedBlock.supportId(node.getBlockState().getValue(MountedBlock.ATTACHMENT))==null?0:2.5/16));
        if(role==RoadSystemBlock.Role.HEIGHT_SIGN){
            text(Component.translatable("label.ng_verkehr.height").getString(),.23f,.80f,0xFFFFFFFF,pose,buffers,light);
            text(String.format(java.util.Locale.ROOT,"%.1f m",d.heightCm/100.0),-.02f,.74f,0xFFFFFFFF,pose,buffers,0xF000F0);
        }else if(role==RoadSystemBlock.Role.TRAFFIC_SIGN||role==RoadSystemBlock.Role.LANE_SIGN){
            int mode=node.online()?d.mode:0;var texture=new ResourceLocation(NgTraffic.ID,"textures/block/variable_mode_"+mode+".png");var v=buffers.getBuffer(RenderType.entityCutoutNoCull(texture));var matrix=pose.last().pose();
            v.vertex(matrix,-.43f,.43f,0).color(255,255,255,255).uv(0,0).overlayCoords(overlay).uv2(0xF000F0).normal(0,0,1).endVertex();v.vertex(matrix,-.43f,-.43f,0).color(255,255,255,255).uv(0,1).overlayCoords(overlay).uv2(0xF000F0).normal(0,0,1).endVertex();v.vertex(matrix,.43f,-.43f,0).color(255,255,255,255).uv(1,1).overlayCoords(overlay).uv2(0xF000F0).normal(0,0,1).endVertex();v.vertex(matrix,.43f,.43f,0).color(255,255,255,255).uv(1,0).overlayCoords(overlay).uv2(0xF000F0).normal(0,0,1).endVertex();
            if(mode==1){pose.translate(0,0,.003);pose.scale(2,2,1);text(Integer.toString(d.speed),0,.30f,0xFFFFFFFF,pose,buffers,0xF000F0);}
        }else{
            text(node.displayName(),.29f,.80f,0xFFFFFFFF,pose,buffers,light);
            text(node.online()?Component.translatable(d.free()>0?"label.ng_verkehr.free":"label.ng_verkehr.full",d.free()).getString():"OFFLINE",.08f,.82f,node.online()&&d.free()>0?0xFF8FF0D3:0xFFFF7777,pose,buffers,0xF000F0);
            text(node.online()?Component.translatable("label.ng_verkehr.board_occupied",d.occupied).getString():"—",-.10f,.78f,0xFFFFFFFF,pose,buffers,light);
            text(node.online()?Component.translatable("label.ng_verkehr.board_capacity",d.capacity).getString():"—",-.26f,.78f,0xFFFFFFFF,pose,buffers,light);
            if(role==RoadSystemBlock.Role.PARKING_GUIDE)text(node.displayArrow(),-.36f,.20f,0xFFFFFFFF,pose,buffers,light);
        }pose.popPose();
    }
}
