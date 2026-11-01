package de.nevergate.verkehr.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.nevergate.verkehr.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;

@Mod.EventBusSubscriber(modid=NgTraffic.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class TrafficClient {
    public static void receiveSystem(WayfindingNetwork.LiveSystem message){
        var player=Minecraft.getInstance().player;if(player!=null&&player.containerMenu instanceof RoadSystemMenu menu&&menu.containerId==message.menuId())menu.applyLive(message.data());
    }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) { event.enqueueWork(()->{MenuScreens.register(NgTraffic.OPERATIONS_MENU.get(),OperationsScreen::new);MenuScreens.register(NgTraffic.SERVICE_MENU.get(),ServiceScreen::new);MenuScreens.register(NgTraffic.TRAFFIC_NODE_MENU.get(),TrafficNodeScreen::new);MenuScreens.register(NgTraffic.CONSTRUCTION_MENU.get(),ConstructionScreen::new);MenuScreens.register(NgTraffic.CONTROLLER_MENU.get(),ControllerScreen::new);MenuScreens.register(NgTraffic.WAYFINDING_MENU.get(),WayfindingScreen::new);MenuScreens.register(NgTraffic.ROAD_SYSTEM_MENU.get(),RoadSystemScreen::new);}); }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) { event.registerBlockEntityRenderer(NgTraffic.OPERATIONS_ENTITY.get(),OperationsRenderer::new);event.registerBlockEntityRenderer(NgTraffic.SERVICE_ENTITY.get(),ServiceRenderer::new);event.registerBlockEntityRenderer(NgTraffic.TRAFFIC_NODE_ENTITY.get(),TrafficNodeRenderer::new);event.registerBlockEntityRenderer(NgTraffic.DEVICE.get(),DisplayRenderer::new);event.registerBlockEntityRenderer(NgTraffic.WAYFINDING_ENTITY.get(),WayfindingRenderer::new);event.registerBlockEntityRenderer(NgTraffic.ROAD_SYSTEM_ENTITY.get(),RoadSystemRenderer::new); }
    public static final class DisplayRenderer implements BlockEntityRenderer<DeviceEntity> {
        public DisplayRenderer(BlockEntityRendererProvider.Context context) {}
        @Override public void render(DeviceEntity entity,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
            if(entity.isController()) return;
            Font font=Minecraft.getInstance().font; String text=entity.displayText();
            pose.pushPose(); pose.translate(.5,.55,.5);
            pose.mulPose(Axis.YP.rotationDegrees(-entity.getBlockState().getValue(HorizontalDirectionalBlock.FACING).toYRot()));
            pose.translate(0,0,.069); float scale=Math.min(.008f,.80f/Math.max(1,font.width(text)));
            pose.scale(scale,-scale,scale);
            font.drawInBatch(text,-font.width(text)/2f,0,0xFF66FF66,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,0xF000F0);
            pose.popPose();
        }
    }
}

