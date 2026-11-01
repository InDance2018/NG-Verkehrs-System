package de.nevergate.verkehr;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.gametest.GameTestHolder;
@GameTestHolder(value="ng_night",namespace=NgTraffic.ID)
public final class NightSignalGameTests {
 @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
 public static void activeLensesEmitMaximumLightAndOffStaysDark(GameTestHelper h){
  var p=h.absolutePos(new BlockPos(2,2,2));int checked=0;
  for(var entry:NgTraffic.CONTENT.values()){
   var block=entry.get();
   if(!(block instanceof SignalBlock signal))continue;
   for(var facing:Direction.Plane.HORIZONTAL)for(var mount:MountedBlock.Attachment.values())for(int phase=0;phase<5;phase++){
    var s=block.defaultBlockState().setValue(OrientedBlock.FACING,facing).setValue(MountedBlock.ATTACHMENT,mount).setValue(SignalBlock.LIGHT,phase);
    if(block instanceof MobileSignalBlock)s=s.setValue(MobileSignalBlock.HALF,DoubleBlockHalf.UPPER);
    int expected=signal.kind==SignalKind.WARNING?(phase==3?15:0):(phase==4?0:15);
    h.assertTrue(s.getLightEmission(h.getLevel(),p)==expected,"Wrong light for "+s);
    if(block instanceof MobileSignalBlock)h.assertTrue(s.setValue(MobileSignalBlock.HALF,DoubleBlockHalf.LOWER).getLightEmission(h.getLevel(),p)==0,"Mobile base lit");
    checked++;
   }
  }
  h.assertTrue(checked==SignalKind.values().length*4*MountedBlock.Attachment.values().length*5,"State coverage: "+checked);h.succeed();
 }
 @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
 public static void pedestrianRedAndGreenAreBrightAndButtonUnchanged(GameTestHelper h){
  var p=h.absolutePos(new BlockPos(2,2,2));
  for(boolean green:new boolean[]{false,true}){
   var s=NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState().setValue(PedestrianBlock.ACTIVE,green);
   h.assertTrue(s.getLightEmission(h.getLevel(),p)==15,"Pedestrian lamp dark");
   var b=NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState().setValue(PedestrianBlock.ACTIVE,green);
   h.assertTrue(b.getLightEmission(h.getLevel(),p)==(green?5:0),"Button light changed");
  }h.succeed();
 }

 @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
 public static void equipmentOutlinesFollowVisibleGeometry(GameTestHelper h){
  var pos=h.absolutePos(new BlockPos(2,2,2));int checked=0;
  for(var entry:NgTraffic.CONTENT.values()){
   var b=entry.get();if(!(b instanceof ConstructionBlock)&&!(b instanceof WorksiteBlock))continue;
   for(var state:b.getStateDefinition().getPossibleStates()){
    h.assertTrue(!state.getShape(h.getLevel(),pos).isEmpty(),"Empty equipment shape: "+state);checked++;
   }
  }
  var trailer=NgTraffic.WORK_TRAILER.get().defaultBlockState().getShape(h.getLevel(),pos);
  h.assertTrue(!net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(trailer,net.minecraft.world.level.block.Block.box(0,0,12,1,1,13),net.minecraft.world.phys.shapes.BooleanOp.AND),"Invisible trailer corner is still selectable");
  h.assertTrue(net.minecraft.world.phys.shapes.Shapes.joinIsNotEmpty(trailer,net.minecraft.world.level.block.Block.box(7,2.5,0.5,9,3.5,1.5),net.minecraft.world.phys.shapes.BooleanOp.AND),"Visible trailer hitch missing from outline");
  h.assertTrue(checked>200,"Equipment orientation coverage");h.succeed();
 }
}