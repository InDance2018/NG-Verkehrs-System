package de.nevergate.verkehr;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=NgTraffic.ID,value=Dist.CLIENT)
public final class MountVisualScene {
 static boolean ready;static int ticks;
 @SubscribeEvent public static void server(TickEvent.ServerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||ready||e.getServer().getPlayerList().getPlayers().isEmpty())return;
  var p=e.getServer().getPlayerList().getPlayers().get(0);var l=p.serverLevel();
  l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,e.getServer());l.setDayTime(18000);l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,e.getServer());l.setWeatherParameters(100000,0,false,false);
  for(int x=-13;x<=13;x++)for(int z=-10;z<=10;z++){l.setBlockAndUpdate(new BlockPos(x,78,z),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(new BlockPos(x,79,z),NgTraffic.ASPHALT.get().defaultBlockState());}
  p.setGameMode(GameType.CREATIVE);

  String[] signs={"signal","signal","signal","pedestrian_signal","warning_signal"};
  var mounts=new MountedBlock.Attachment[]{MountedBlock.Attachment.SIGN_BRACKET,MountedBlock.Attachment.POST,MountedBlock.Attachment.SIGNAL_BRACKET,MountedBlock.Attachment.MAST,MountedBlock.Attachment.HANGER};
  for(int i=0;i<5;i++){
   var at=new BlockPos(-6+3*i,82,1);
   for(int y=80;y<82;y++)l.setBlockAndUpdate(new BlockPos(at.getX(),y,1),NgTraffic.CONTENT.get("post").get().defaultBlockState());
   var state=NgTraffic.CONTENT.get(signs[i]).get().defaultBlockState().setValue(OrientedBlock.FACING,Direction.NORTH).setValue(MountedBlock.ATTACHMENT,mounts[i]);
   if(state.hasProperty(SignalBlock.LIGHT))state=state.setValue(SignalBlock.LIGHT,i==1||i==4?3:i==2?2:0);
   if(state.hasProperty(PedestrianBlock.ACTIVE))state=state.setValue(PedestrianBlock.ACTIVE,true);
   l.setBlockAndUpdate(at,state);
   if(l.getBlockEntity(at)!=null)l.getBlockEntity(at).setRemoved();
  }
  l.setBlockAndUpdate(new BlockPos(1,82,1),NgTraffic.BICYCLE_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,Direction.NORTH).setValue(MountedBlock.ATTACHMENT,MountedBlock.Attachment.ALIGNED).setValue(SignalBlock.LIGHT,2));
  if(l.getBlockEntity(new BlockPos(1,82,1))!=null)l.getBlockEntity(new BlockPos(1,82,1)).setRemoved();
  for(int i=0;i<2;i++){
   var at=new BlockPos(-3+i*4,80,-1);
   l.setBlockAndUpdate(at,NgTraffic.WORK_TRAILER.get().defaultBlockState().setValue(OrientedBlock.FACING,Direction.NORTH).setValue(ConstructionBlock.ACTIVE,true).setValue(ConstructionBlock.MODE,i+1));
   if(l.getBlockEntity(at)!=null)l.getBlockEntity(at).setRemoved();
  }
  for(int x=-7;x<=7;x++)l.setBlockAndUpdate(new BlockPos(x,80,-3),NgTraffic.CONTENT.get("guardrail").get().defaultBlockState());
  l.setBlockAndUpdate(new BlockPos(-8,80,-3),NgTraffic.CONTENT.get("guardrail_end").get().defaultBlockState().setValue(OrientedBlock.FACING,Direction.SOUTH));
  l.setBlockAndUpdate(new BlockPos(8,80,-3),NgTraffic.CONTENT.get("guardrail_end").get().defaultBlockState().setValue(OrientedBlock.FACING,Direction.NORTH));
  p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setGameMode(GameType.SPECTATOR);p.teleportTo(l,9,86,-12,35,17);ready=true;System.out.println("MOUNT_VISUAL_SCENE_READY");
 }
 @SubscribeEvent public static void client(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;
  if(mc.player==null||!ready)return;ticks++;mc.options.renderDistance().set(3);mc.options.framerateLimit().set(30);mc.options.hideGui=true;
  if(ticks==80){

   int count=0;
   var random=net.minecraft.util.RandomSource.create(0);
   for(var b:net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValues()){
    if(!(b instanceof SignalBlock)&&b!=NgTraffic.PEDESTRIAN_SIGNAL.get())continue;
    for(var state:b.getStateDefinition().getPossibleStates()){
     var model=mc.getBlockRenderer().getBlockModel(state);
     if(model==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing lamp model "+state);
     var quads=new java.util.ArrayList<net.minecraft.client.renderer.block.model.BakedQuad>(model.getQuads(state,null,random));
     for(var d:Direction.values())quads.addAll(model.getQuads(state,d,random));
     for(var quad:quads){
      var path=quad.getSprite().contents().name().getPath();
      if(!path.endsWith("_1")||!(path.contains("lens_")||path.contains("signal_")||path.contains("/ped_")))continue;
      int[] v=quad.getVertices();int stride=v.length/4;
      for(int i=0;i<4;i++)if((v[i*stride+6]&0x00F000F0)!=0x00F000F0)throw new IllegalStateException("Dim active lens "+state+" "+path);
      count++;
     }
    }
   }
   if(count<1000)throw new IllegalStateException("No active lenses checked "+count);
   System.out.println("NIGHT_EMISSIVE_PASS "+count+" active lens quads at full brightness");
   Screenshot.grab(mc.gameDirectory,"night-signals.png",mc.getMainRenderTarget(),m->System.out.println(m.getString()));System.out.println("NIGHT_VISUAL_PASS");
  }
  if(ticks==110)mc.stop();
 }
}
