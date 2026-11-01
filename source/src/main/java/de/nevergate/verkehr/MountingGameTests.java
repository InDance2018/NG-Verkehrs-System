package de.nevergate.verkehr;

import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(value="ng_mount",namespace=NgTraffic.ID)
public final class MountingGameTests {
    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void signsAndSignalsMountInsideEverySupport(GameTestHelper h){
        var level=h.getLevel();var p=h.absolutePos(new BlockPos(2,2,2));
        var player=ServiceGameTests.player(h,p.east(5),false);
        String[] ids={"sign_206","sign_274_50","work_sign_construction","town_sign","variable_sign","lane_board","parking_guide","parking_board","parking_height_sign","signal","signal_left","bicycle_signal","warning_signal","pedestrian_signal"};
        for(var attachment:MountedBlock.Attachment.values()){
            String support=MountedBlock.supportId(attachment);if(support==null)continue;
            for(var facing:Direction.Plane.HORIZONTAL)for(String id:ids){
                var s=NgTraffic.CONTENT.get(support).get().defaultBlockState();
                if(s.hasProperty(OrientedBlock.FACING))s=s.setValue(OrientedBlock.FACING,facing);
                level.setBlock(p,s,3);player.setYRot(facing.getOpposite().toYRot());
                var item=new ItemStack(NgTraffic.CONTENT.get(id).get(),2);player.setItemInHand(InteractionHand.MAIN_HAND,item);
                var result=item.useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(p),facing,p,false)));
                var placed=level.getBlockState(p);
                h.assertTrue(result.consumesAction()&&placed.is(NgTraffic.CONTENT.get(id).get()),"Mount failed: "+id+" on "+support+" "+facing);
                h.assertTrue(placed.getValue(MountedBlock.ATTACHMENT)==attachment,"Lost support "+support);
                h.assertTrue(placed.getValue(OrientedBlock.FACING)==facing,"Wrong mount facing");
                h.assertTrue(item.getCount()==1,"Wrong survival consumption");
                h.assertTrue(!placed.getShape(level,p).isEmpty(),"Missing mounted selection shape");
                var saved=net.minecraft.nbt.NbtUtils.writeBlockState(placed);
                h.assertTrue(net.minecraft.nbt.NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK),saved).equals(placed),"Mount lost on save/load");
                if(id.equals("signal"))h.assertTrue(level.getBlockEntity(p) instanceof SignalEntity,"Signal controller endpoint missing");
                if(id.equals("town_sign"))h.assertTrue(level.getBlockEntity(p) instanceof WayfindingEntity,"Sign text entity missing");
                level.removeBlock(p,false);
            }
        }
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void signalsAttachToSignalsOnBothSides(GameTestHelper h){
        var level=h.getLevel();var p=h.absolutePos(new BlockPos(3,2,3));var player=ServiceGameTests.player(h,p.east(5),false);
        for(var facing:Direction.Plane.HORIZONTAL)for(boolean right:new boolean[]{false,true}){
            var face=right?facing.getClockWise():facing.getCounterClockWise();var target=p.relative(face);
            level.removeBlock(target,false);level.setBlock(p,NgTraffic.SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,facing),3);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(NgTraffic.BICYCLE_SIGNAL.get()));
            var result=player.getMainHandItem().useOn(new UseOnContext(player,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(p),face,p,false)));
            var placed=level.getBlockState(target);
            h.assertTrue(result.consumesAction()&&placed.is(NgTraffic.BICYCLE_SIGNAL.get()),"Adjacent signal placement failed");
            h.assertTrue(placed.getValue(OrientedBlock.FACING)==facing,"Adjacent signal turned away");
            h.assertTrue(placed.getValue(MountedBlock.JOIN)==(right?MountedBlock.Join.LEFT:MountedBlock.Join.RIGHT),"Adjacent connector missing");
            var sourceBox=level.getBlockState(p).getShape(level,p).bounds().move(p.getX(),p.getY(),p.getZ());
            var targetBox=placed.getShape(level,target).bounds().move(target.getX(),target.getY(),target.getZ());
            double gap=face==Direction.EAST?targetBox.minX-sourceBox.maxX:face==Direction.WEST?sourceBox.minX-targetBox.maxX:face==Direction.SOUTH?targetBox.minZ-sourceBox.maxZ:sourceBox.minZ-targetBox.maxZ;
            h.assertTrue(Math.abs(gap)<.00001,"Signal housings still have a gap: "+gap);
            var box=placed.getShape(level,target).bounds();h.assertTrue(box.minX>=0&&box.minZ>=0&&box.maxX<=1&&box.maxZ<=1,"Joined head must remain inside its selectable cell");
            level.removeBlock(target,false);level.removeBlock(p,false);
        }h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void guardrailSupportsReflowBetweenEnds(GameTestHelper h){
        var level=h.getLevel();var p=h.absolutePos(new BlockPos(1,3,1));var rail=NgTraffic.CONTENT.get("guardrail").get();
        var end=NgTraffic.CONTENT.get("guardrail_end").get().defaultBlockState().setValue(OrientedBlock.FACING,Direction.NORTH);
        level.setBlock(p,end.setValue(OrientedBlock.FACING,Direction.SOUTH),3);
        level.setBlock(p.east(16),end,3);
        // Deliberately place backwards to prove that spacing is not placement-order dependent.
        for(int i=15;i>=1;i--)level.setBlock(p.east(i),rail.defaultBlockState(),3);
        h.runAfterDelay(8,()->{
            for(int i=1;i<=15;i++)h.assertTrue(level.getBlockState(p.east(i)).getValue(GuardrailBlock.SUPPORT)==(i%7==0),"Foot interval wrong at "+i);
            level.removeBlock(p,false);level.removeBlock(p.east(16),false);
        });
        h.runAfterDelay(18,()->{
            for(int i=1;i<=15;i++)h.assertTrue(level.getBlockState(p.east(i)).getValue(GuardrailBlock.SUPPORT)==((i-1)%7==0),"Foot spacing did not update after removing ends at "+i);
            for(int i=1;i<=15;i++)level.removeBlock(p.east(i),false);
            h.succeed();
        });
    }
}
