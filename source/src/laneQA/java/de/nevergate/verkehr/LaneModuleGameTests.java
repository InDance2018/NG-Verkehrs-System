package de.nevergate.verkehr;

import java.util.*;
import java.util.function.Consumer;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.*;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(value="ng_lanes",namespace=NgTraffic.ID)
public final class LaneModuleGameTests {
    static net.minecraft.world.entity.player.Player player(GameTestHelper h,BlockPos p,Direction d,String id,boolean creative){
        var x=h.makeMockPlayer();x.getAbilities().mayBuild=true;x.getAbilities().instabuild=creative;
        x.setPos(p.getX()+.5,p.getY()+3,p.getZ()+.5);x.setYRot(d.toYRot());
        x.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(NgTraffic.LANE_MODULES.get(id).get(),2));return x;
    }
    static InteractionResult use(net.minecraft.world.entity.player.Player p,BlockPos target){
        var hit=new BlockHitResult(Vec3.atCenterOf(target.below()).add(0,.5,0),Direction.UP,target.below(),false);
        return ForgeHooks.onPlaceItemIntoWorld(new UseOnContext(p,InteractionHand.MAIN_HAND,hit));
    }
    static void clear(GameTestHelper h,List<BlockPos> cells){for(var p:cells){h.getLevel().setBlock(p,Blocks.AIR.defaultBlockState(),3);h.getLevel().setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);}}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void allModulesAllDirectionsAndPersistentTiles(GameTestHelper h){
        var anchor=h.absolutePos(new BlockPos(4,4,4));
        for(var id:NgTraffic.LANE_MODULES.keySet())for(var d:Direction.Plane.HORIZONTAL){
            int length=id.equals("asphalt_lane_4")?1:4;var cells=LaneModuleItem.footprint(anchor,d,length);clear(h,cells);
            var p=player(h,anchor,d,id,false);h.assertTrue(use(p,anchor).consumesAction(),"Placement rejected "+id+" "+d);
            h.assertTrue(p.getMainHandItem().getCount()==1,"Wrong survival consumption");
            h.assertTrue(new HashSet<>(cells).size()==4*length,"Wrong footprint");
            for(int i=0;i<cells.size();i++){
                var s=h.getLevel().getBlockState(cells.get(i));
                h.assertTrue(length==1?s.is(NgTraffic.ASPHALT.get()):s.getBlock() instanceof LaneArrowBlock,"Wrong block");
                if(length==4){h.assertTrue(s.getValue(OrientedBlock.FACING)==d&&s.getValue(LaneArrowBlock.TILE)==LaneModuleItem.tile(i),"Wrong arrow orientation or tile");
                    var copy=net.minecraft.nbt.NbtUtils.readBlockState(h.getLevel().holderLookup(net.minecraft.core.registries.Registries.BLOCK),net.minecraft.nbt.NbtUtils.writeBlockState(s));h.assertTrue(copy.equals(s),"Tile state not persistent");}
                h.assertTrue(s.getCollisionShape(h.getLevel(),cells.get(i)).bounds().maxY==1,"Different road height");
            }
            clear(h,cells);
        }h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void obstructionAtEveryCellPreventsAllPlacement(GameTestHelper h){
        var anchor=h.absolutePos(new BlockPos(4,4,4));var cells=LaneModuleItem.footprint(anchor,Direction.NORTH,4);
        for(var blocked:cells){clear(h,cells);h.getLevel().setBlock(blocked,Blocks.DIAMOND_BLOCK.defaultBlockState(),3);var p=player(h,anchor,Direction.NORTH,"lane_arrow_left",false);
            var result=use(p,anchor);
            h.assertTrue(!result.consumesAction()&&p.getMainHandItem().getCount()==2,"Obstacle consumed item");
            for(var pos:cells)h.assertTrue(h.getLevel().getBlockState(pos).is(pos.equals(blocked)?Blocks.DIAMOND_BLOCK:Blocks.AIR),"Partial placement or overwrite");
        }h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void protectionCancellationRestoresEveryCellAndItem(GameTestHelper h){
        var anchor=h.absolutePos(new BlockPos(4,4,4));var cells=LaneModuleItem.footprint(anchor,Direction.EAST,4);clear(h,cells);int[] events={0};
        Consumer<BlockEvent.EntityMultiPlaceEvent> cancel=e->{if(e.getReplacedBlockSnapshots().stream().anyMatch(s->cells.contains(s.getPos()))){events[0]++;h.assertTrue(e.getReplacedBlockSnapshots().size()==16,"Protection did not receive whole footprint");e.setCanceled(true);}};
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {var p=player(h,anchor,Direction.EAST,"lane_arrow_right",false);h.assertTrue(use(p,anchor)==InteractionResult.FAIL,"Protection ignored");h.assertTrue(events[0]==1&&p.getMainHandItem().getCount()==2,"Protection event/count wrong");for(var pos:cells)h.assertTrue(h.getLevel().isEmptyBlock(pos),"Protection left a partial arrow");}
        finally {MinecraftForge.EVENT_BUS.unregister(cancel);}h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void creativeAndReadOnlyPlayers(GameTestHelper h){
        var anchor=h.absolutePos(new BlockPos(4,4,4));var cells=LaneModuleItem.footprint(anchor,Direction.SOUTH,1);clear(h,cells);var p=player(h,anchor,Direction.SOUTH,"asphalt_lane_4",true);
        h.assertTrue(use(p,anchor).consumesAction()&&p.getMainHandItem().getCount()==2,"Creative consumed pack");clear(h,cells);p.getAbilities().mayBuild=false;
        h.assertTrue(!use(p,anchor).consumesAction(),"Read-only placement allowed");for(var pos:cells)h.assertTrue(h.getLevel().isEmptyBlock(pos),"Read-only changed road");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void creativePickReturnsWholeArrowModule(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(2,3,2));
        for(String id:new String[]{"lane_arrow_left","lane_arrow_straight","lane_arrow_right"}){
            var block=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation(NgTraffic.ID,id));
            var state=block.defaultBlockState().setValue(LaneArrowBlock.TILE,15);
            var picked=block.getCloneItemStack(h.getLevel(),pos,state);
            h.assertTrue(picked.is(NgTraffic.LANE_MODULES.get(id).get())&&picked.getCount()==1,"Creative pick lost whole arrow");
        }h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void unloadedTargetDoesNotLoadChunks(GameTestHelper h){
        var anchor=new BlockPos(25000000,100,25000000);var level=h.getLevel();h.assertTrue(!level.hasChunkAt(anchor),"Fixture unexpectedly loaded");var p=player(h,anchor,Direction.NORTH,"lane_arrow_straight",false);
        // Direct context avoids the vanilla clicked-block lookup loading the distant test chunk.
        var hit=new BlockHitResult(Vec3.atCenterOf(anchor),Direction.UP,anchor,false);
        h.assertTrue(!p.getMainHandItem().getItem().useOn(new UseOnContext(p,InteractionHand.MAIN_HAND,hit)).consumesAction(),"Unloaded target accepted");
        h.assertTrue(!level.hasChunkAt(anchor)&&p.getMainHandItem().getCount()==2,"Unloaded chunk loaded or item lost");h.succeed();
    }
}
