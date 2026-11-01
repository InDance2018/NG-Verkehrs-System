package de.nevergate.verkehr.client;
import de.nevergate.verkehr.*;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
public final class OperationsRenderer implements BlockEntityRenderer<OperationsEntity> {
    public OperationsRenderer(BlockEntityRendererProvider.Context c){}
    @Override public void render(OperationsEntity e,float delta,PoseStack pose,MultiBufferSource buf,int light,int overlay){
        if(e.kind()==OperationsBlock.Kind.ROAD_BOLLARD||e.kind()==OperationsBlock.Kind.BUS_SENSOR)return;
        var font=Minecraft.getInstance().font;String text=e.detail().isBlank()?"BETRIEB PRÜFEN":e.detail();pose.pushPose();pose.translate(.5,.65,.5);pose.mulPose(Axis.YP.rotationDegrees(-e.getBlockState().getValue(OrientedBlock.FACING).toYRot()));pose.translate(0,0,.381);float scale=Math.min(.008f,.66f/Math.max(1,font.width(text)));pose.scale(scale,-scale,scale);font.drawInBatch(text,-font.width(text)/2f,0,e.alarm()?0xFFFF5555:e.open()?0xFF66FF66:0xFF66CCFF,false,pose.last().pose(),buf,Font.DisplayMode.NORMAL,0,0xF000F0);pose.popPose();
    }
}
