package de.nevergate.verkehr.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.nevergate.verkehr.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

public final class WayfindingRenderer implements BlockEntityRenderer<WayfindingEntity> {
    private final Font font;
    public WayfindingRenderer(BlockEntityRendererProvider.Context context){font=context.getFont();}
    private void text(String value,float x,float y,float maxWidth,int color,PoseStack pose,MultiBufferSource buffers,int light){
        if(value.isEmpty())return;
        pose.pushPose();pose.translate(x,y,0);float scale=Math.min(.012f,maxWidth/Math.max(1,font.width(value)));pose.scale(scale,-scale,scale);
        font.drawInBatch(value,-font.width(value)/2f,-4,color,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,light);pose.popPose();
    }
    @Override public void render(WayfindingEntity sign,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay){
        var kind=sign.kind();var data=sign.data();boolean highway=kind==WayfindingBlock.Kind.MOTORWAY||kind==WayfindingBlock.Kind.DISTANCE;
        pose.pushPose();pose.translate(.5,.5,.5);pose.mulPose(Axis.YP.rotationDegrees(-sign.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));pose.translate(0,0,.070+(MountedBlock.supportId(sign.getBlockState().getValue(MountedBlock.ATTACHMENT))==null?0:2.5/16));
        int color=highway||kind==WayfindingBlock.Kind.EXIT?0xFFFFFFFF:0xFF111111;
        if(kind==WayfindingBlock.Kind.TOWN||kind==WayfindingBlock.Kind.TOWN_EXIT){text(data.targets[0],0,.10f,1.15f,color,pose,buffers,light);text(data.targets[1],0,-.15f,1.15f,color,pose,buffers,light);}
        else{
            int count=highway?3:2;float width=highway?2.65f:1.65f;
            for(int i=0;i<count;i++){
                float y=(highway?.61f:.22f)-i*(highway?.37f:.42f);
                text(data.targets[i],-.15f,y,width-.75f,color,pose,buffers,light);
                text(data.distances[i],width/2-.28f,y,.48f,color,pose,buffers,light);
                if(!data.targets[i].isEmpty())text(WayfindingData.arrow(data.directions[i]),-width/2,y,.22f,color,pose,buffers,light);
            }
            if(kind==WayfindingBlock.Kind.MOTORWAY)for(int i=0;i<data.laneCount;i++)text(WayfindingData.arrow(data.lanes[i]),(i-(data.laneCount-1)/2f)*.58f,-.64f,.30f,color,pose,buffers,light);
        }
        pose.popPose();
    }
    @Override public boolean shouldRenderOffScreen(WayfindingEntity sign){return true;}
    @Override public int getViewDistance(){return 128;}
}
