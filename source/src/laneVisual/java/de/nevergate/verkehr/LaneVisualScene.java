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
public final class LaneVisualScene {
 static boolean ready;static int ticks;
 @SubscribeEvent public static void server(TickEvent.ServerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||ready||e.getServer().getPlayerList().getPlayers().isEmpty())return;
  var p=e.getServer().getPlayerList().getPlayers().get(0);var l=p.serverLevel();
  l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,e.getServer());l.setDayTime(6000);l.setWeatherParameters(100000,0,false,false);
  for(int x=-13;x<=13;x++)for(int z=-10;z<=10;z++){l.setBlockAndUpdate(new BlockPos(x,78,z),Blocks.STONE.defaultBlockState());l.setBlockAndUpdate(new BlockPos(x,79,z),NgTraffic.ASPHALT.get().defaultBlockState());}
  p.setGameMode(GameType.CREATIVE);
  for(int i=0;i<3;i++){
   var anchor=new BlockPos(-8+6*i,79,2);var id=new String[]{"lane_arrow_left","lane_arrow_straight","lane_arrow_right"}[i];
   for(var pos:LaneModuleItem.footprint(anchor,Direction.NORTH,4))l.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
   p.setYRot(180);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(NgTraffic.LANE_MODULES.get(id).get()));
   var hit=new BlockHitResult(Vec3.atCenterOf(anchor.below()).add(0,.5,0),Direction.UP,anchor.below(),false);
   if(!ForgeHooks.onPlaceItemIntoWorld(new UseOnContext(p,InteractionHand.MAIN_HAND,hit)).consumesAction())throw new IllegalStateException("Visual placement failed");
  }
  p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);p.setGameMode(GameType.SPECTATOR);p.teleportTo(l,0,95,10,180,62);ready=true;System.out.println("LANE_VISUAL_SCENE_READY");
 }
 @SubscribeEvent public static void client(TickEvent.ClientTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();mc.options.pauseOnLostFocus=false;
  if(mc.player==null||!ready)return;ticks++;mc.options.renderDistance().set(3);mc.options.framerateLimit().set(30);mc.options.hideGui=true;
  if(ticks==80){
   for(var id:new String[]{"lane_arrow_left","lane_arrow_straight","lane_arrow_right"}){
    var b=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation(NgTraffic.ID,id));
    for(var s:b.getStateDefinition().getPossibleStates())if(mc.getBlockRenderer().getBlockModel(s)==mc.getModelManager().getMissingModel())throw new IllegalStateException("Missing arrow model "+s);
   }
   Screenshot.grab(mc.gameDirectory,"lane-modules.png",mc.getMainRenderTarget(),m->System.out.println(m.getString()));System.out.println("LANE_VISUAL_PASS 192 block models and three placed continuous arrows");
  }
  if(ticks==110)mc.stop();
 }
}
