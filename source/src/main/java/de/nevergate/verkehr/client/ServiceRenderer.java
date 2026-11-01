package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
public final class ServiceRenderer implements BlockEntityRenderer<ServiceEntity> {
    public ServiceRenderer(BlockEntityRendererProvider.Context c){}
    @Override public void render(ServiceEntity e,float delta,PoseStack pose,MultiBufferSource buf,int light,int overlay){
        if(e.kind()==ServiceBlock.Kind.BICYCLE_DETECTOR||e.kind()==ServiceBlock.Kind.RESERVATION_BOLLARD)return;
        String text=e.detail().isBlank()?e.online()?"BEREIT":"VERBINDUNG?":e.detail();
        var font=Minecraft.getInstance().font;pose.pushPose();pose.translate(.5,.65,.5);pose.mulPose(Axis.YP.rotationDegrees(-e.getBlockState().getValue(OrientedBlock.FACING).toYRot()));pose.translate(0,0,.381);float scale=Math.min(.008f,.66f/Math.max(1,font.width(text)));pose.scale(scale,-scale,scale);font.drawInBatch(text,-font.width(text)/2f,0,e.alarm()?0xFFFF5555:e.green()?0xFF66FF66:0xFF66CCFF,false,pose.last().pose(),buf,Font.DisplayMode.NORMAL,0,0xF000F0);pose.popPose();
    }
}
