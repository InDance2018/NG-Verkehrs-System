package de.nevergate.verkehr;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(value=NgTraffic.ID,namespace=NgTraffic.ID)
public final class ConstructionGameTests {
    private static ConstructionEntity put(GameTestHelper h,BlockPos p,Block b){h.getLevel().setBlock(p,b.defaultBlockState(),3);return (ConstructionEntity)h.getLevel().getBlockEntity(p);}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void extendingGateOccupiesRealCellsAndOnlyDropsOneItem(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));var c=put(h,p,NgTraffic.WORK_GATE.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;
        h.assertTrue(c.resize(player,4),"Gate did not extend");
        for(int i=1;i<4;i++)h.assertTrue(l.getBlockEntity(p.east(i)) instanceof ConstructionEntity e&&e.control()==c&&!l.getBlockState(p.east(i)).getCollisionShape(l,p.east(i)).isEmpty(),"Missing physical owned segment");
        l.destroyBlock(p,true);
        for(int i=1;i<4;i++)h.assertTrue(l.getBlockState(p.east(i)).isAir(),"Segment survived root removal");
        int count=l.getEntitiesOfClass(ItemEntity.class,new AABB(p).inflate(5)).stream().filter(e->e.getItem().is(NgTraffic.WORK_GATE.get().asItem())).mapToInt(e->e.getItem().getCount()).sum();
        h.assertTrue(count==1,"Root did not drop exactly one gate");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void gateRefusesOccupiedCellsWithoutPartialChanges(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));var c=put(h,p,NgTraffic.WORK_GATE.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;
        l.setBlock(p.east(2),Blocks.DIAMOND_BLOCK.defaultBlockState(),3);
        h.assertTrue(!c.resize(player,4)&&c.width()==1&&l.getBlockState(p.east()).isAir()&&l.getBlockState(p.east(2)).is(Blocks.DIAMOND_BLOCK),"Resize overwrote obstacle or partially extended");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void breakingExtensionRetractsOtherExtensions(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));var c=put(h,p,NgTraffic.WORK_GATE.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;c.resize(player,4);
        l.destroyBlock(p.east(2),true);
        h.assertTrue(c.width()==1&&l.getBlockState(p.east()).isAir()&&l.getBlockState(p.east(3)).isAir()&&l.getBlockState(p).is(NgTraffic.WORK_GATE.get()),"Segment break left remnants");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void extendingGateRefusesEntitiesAndNoBuildPlayer(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));var c=put(h,p,NgTraffic.WORK_GATE.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;
        var mob=new ArmorStand(l,p.getX()+1.5,p.getY(),p.getZ()+.5);mob.setNoGravity(true);l.addFreshEntity(mob);
        h.assertTrue(!c.resize(player,2)&&c.width()==1,"Gate extended through entity");mob.discard();
        player.getAbilities().mayBuild=false;h.assertTrue(!c.resize(player,2),"Read-only player extended gate");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=24)
    public static void runningLampsSequenceAndPersistence(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(1,1,1));var root=put(h,p,NgTraffic.WORK_RUNNING_LAMP.get());var b=put(h,p.east(),NgTraffic.WORK_RUNNING_LAMP.get());var c=put(h,p.east(2),NgTraffic.WORK_RUNNING_LAMP.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;
        h.assertTrue(root.attach(b,player)&&root.attach(c,player)&&!root.attach(b,player)&&!b.attach(root,player),"Sequence link validation failed");
        root.load(root.saveWithoutMetadata());b.load(b.saveWithoutMetadata());c.load(c.saveWithoutMetadata());
        h.assertTrue(root.linked()==3&&b.control()==root&&c.control()==root,"Sequence did not persist");
        h.runAfterDelay(4,()->{int n=(root.active()?1:0)+(b.active()?1:0)+(c.active()?1:0);h.assertTrue(n==1,"Running light did not select exactly one lamp");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=24)
    public static void deletedLeaderDoesNotAdoptOldLamps(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(1,1,1));var root=put(h,p,NgTraffic.WORK_RUNNING_LAMP.get());var child=put(h,p.east(),NgTraffic.WORK_RUNNING_LAMP.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;root.attach(child,player);
        var oldId=root.identity();h.getLevel().removeBlock(p,false);var replacement=put(h,p,NgTraffic.WORK_RUNNING_LAMP.get());
        h.runAfterDelay(4,()->{h.assertTrue(!replacement.identity().equals(oldId)&&child.control()==null&&!child.active(),"Replacement adopted stale child");child.disconnect(player);h.assertTrue(child.control()==child,"Orphan could not be manually disconnected");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void constructionMenuChecksDistanceBuildAndReplacement(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(1,1,1));var n=put(h,p,NgTraffic.WORK_MAST.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(p.getX()+.5,p.getY()+.5,p.getZ()+.5);
        var m=new ConstructionMenu(1,player.getInventory(),n);h.assertTrue(m.clickMenuButton(player,0)&&n.mode()==0,"Valid menu edit failed");
        player.getAbilities().mayBuild=false;h.assertTrue(!m.clickMenuButton(player,0),"No-build edit accepted");
        player.getAbilities().mayBuild=true;player.setPos(p.getX()+20,p.getY(),p.getZ());h.assertTrue(!m.clickMenuButton(player,0),"Distant edit accepted");
        player.setPos(p.getX()+.5,p.getY()+.5,p.getZ()+.5);put(h,p,NgTraffic.WORK_ACCESS.get());h.assertTrue(!m.clickMenuButton(player,0),"Stale menu changed replacement");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void accessOwnerPlayerVehiclePermissionsPersistAndRevoke(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(1,1,1));var n=put(h,p,NgTraffic.WORK_ACCESS.get());var owner=h.makeMockPlayer();var boat=new Boat(h.getLevel(),p.getX()+1,p.getY(),p.getZ());n.setOwner(owner.getUUID());
        h.assertTrue(n.authorized(owner)&&!n.authorized(boat),"Default access wrong");
        n.permit(boat.getUUID(),true,true);n.timing(22000,2000,true);n.load(n.saveWithoutMetadata());
        h.assertTrue(n.authorized(owner)&&n.authorized(boat)&&n.schedule()&&n.start()==22000&&n.end()==2000,"Access or timing not saved");
        n.permit(boat.getUUID(),true,false);h.assertTrue(!n.authorized(boat),"Revoked vehicle retained access");
        n.setOwner(UUID.randomUUID());h.assertTrue(!n.authorized(owner),"Owner replacement did not revoke");
        n.permit(owner.getUUID(),false,true);h.assertTrue(n.authorized(owner),"Player grant failed");
        n.permit(owner.getUUID(),false,false);h.assertTrue(!n.authorized(owner),"Player revocation failed");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void accessListsBoundedAndOwnerProtectsConfiguration(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(1,1,1));var n=put(h,p,NgTraffic.WORK_ACCESS.get());var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;n.setOwner(UUID.randomUUID());
        h.assertTrue(!n.canEdit(player),"Nonowner edited access gate");
        for(int i=0;i<64;i++)h.assertTrue(n.permit(UUID.randomUUID(),true,true),"Could not add valid permission");
        h.assertTrue(!n.permit(UUID.randomUUID(),false,true)&&n.permitted()==64,"Access list exceeded bound");
        n.load(n.saveWithoutMetadata());h.assertTrue(n.permitted()==64,"Permission list did not persist");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=26)
    public static void scheduledBarrierDoesNotCloseOnObstacle(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));var n=put(h,p,NgTraffic.WORK_TIMED.get());n.mode(0);
        h.runAfterDelay(4,()->{
            h.assertTrue(n.active(),"Disabled closure did not open");
            var mob=new ArmorStand(l,p.getX()+.5,p.getY(),p.getZ()+.5);mob.setNoGravity(true);l.addFreshEntity(mob);n.mode(1);n.timing(0,0,true);
            h.runAfterDelay(4,()->{h.assertTrue(n.active(),"Barrier closed on entity");mob.discard();});
        });
        h.runAfterDelay(24,()->{h.assertTrue(!n.active(),"Barrier stayed open after obstacle and hold cleared");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void lightMastActuallyEmitsLight(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));var n=put(h,p,NgTraffic.WORK_MAST.get());
        h.runAfterDelay(4,()->{h.assertTrue(n.active()&&l.getBlockState(p).getLightEmission(l,p)==15,"Enabled mast does not emit level 15");n.mode(0);});
        h.runAfterDelay(8,()->{h.assertTrue(!n.active()&&l.getBlockState(p).getLightEmission(l,p)==0,"Disabled mast still emits light");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void bridgesHaveWalkableDeckAndLowCableCrossings(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));
        for(var block:List.of(NgTraffic.WORK_CABLE.get(),NgTraffic.WORK_HOSE.get())){
            var s=block.defaultBlockState();h.assertTrue(s.getCollisionShape(l,p).bounds().maxY<=3.0/16,"Crossing is too tall");
        }
        var s=NgTraffic.WORK_BRIDGE.get().defaultBlockState();
        h.assertTrue(!s.getCollisionShape(l,p).isEmpty()&&s.getCollisionShape(l,p).bounds().maxY==1,"Bridge deck or rails missing");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void constructionTimingSurvivesNegativeAndOvernightTime(GameTestHelper h){
        h.assertTrue(ConstructionRules.scheduled(23000,22000,2000)&&ConstructionRules.scheduled(1000,22000,2000)&&!ConstructionRules.scheduled(12000,22000,2000),"Overnight schedule wrong");
        h.assertTrue(ConstructionRules.scheduled(-1,22000,2000)&&ConstructionRules.scheduled(24000,0,1000)&&!ConstructionRules.scheduled(1000,0,1000),"Boundary wrap wrong");
        h.assertTrue(ConstructionRules.scheduled(12000,0,0),"Equal time must cover all day");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void allConstructionOrientationsAndNetworkSnapshot(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));
        for(var block:List.of(NgTraffic.WORK_TRAILER.get(),NgTraffic.WORK_RUNNING_LAMP.get(),NgTraffic.WORK_TIMED.get(),NgTraffic.WORK_GATE.get(),NgTraffic.WORK_CABLE.get(),NgTraffic.WORK_HOSE.get(),NgTraffic.WORK_BRIDGE.get(),NgTraffic.WORK_MAST.get(),NgTraffic.WORK_ACCESS.get())){
            for(var d:Direction.Plane.HORIZONTAL){var s=block.defaultBlockState().setValue(OrientedBlock.FACING,d);h.assertTrue(!s.getShape(l,p).isEmpty(),"Empty shape");h.assertTrue(s.rotate(Rotation.CLOCKWISE_90).getValue(OrientedBlock.FACING)==d.getClockWise(),"Rotation mismatch");}
        }
        var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());b.writeBlockPos(p);b.writeEnum(ConstructionBlock.Kind.LAMP);var menu=new ConstructionMenu(2,h.makeMockPlayer().getInventory(),b);b.release();
        h.assertTrue(menu.pos.equals(p)&&menu.kind==ConstructionBlock.Kind.LAMP,"Menu snapshot lost position/kind");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=35)
    public static void mobileTrafficCycleWorksAlongsideNewWorksiteEquipment(GameTestHelper h){
        var l=h.getLevel();var center=h.absolutePos(new BlockPos(2,1,2));var cp=center.east(2);var np=center.north();var sp=center.south();
        l.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var controller=(DeviceEntity)l.getBlockEntity(cp);
        l.setBlock(np,NgTraffic.MOBILE_SIGNAL.get().defaultBlockState(),3);l.setBlock(sp,NgTraffic.MOBILE_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,Direction.SOUTH),3);
        var north=(SignalEntity)l.getBlockEntity(np);var south=(SignalEntity)l.getBlockEntity(sp);north.link(cp,0);south.link(cp,1);
        var lamp=put(h,cp.north(2),NgTraffic.WORK_RUNNING_LAMP.get());lamp.mode(2);
        var trailer=put(h,cp.south(2),NgTraffic.WORK_TRAILER.get());
        l.setBlock(cp.south(3),NgTraffic.WORK_LAMP.get().defaultBlockState(),3);
        controller.changeSetting(0,2);controller.setEnabled(true);north.tick();south.tick();
        h.runAfterDelay(21,()->{
            north.tick();south.tick();
            h.assertTrue(l.getBlockState(np.above()).getValue(SignalBlock.LIGHT)==2&&l.getBlockState(sp.above()).getValue(SignalBlock.LIGHT)==0,"Warning equipment interfered with one-way signal cycle");
            h.assertTrue(lamp.active(),"Signal cycle disabled independent warning light");controller.setEnabled(false);north.tick();south.tick();
            h.assertTrue(l.getBlockState(np.above()).getValue(SignalBlock.LIGHT)==0&&l.getBlockState(sp.above()).getValue(SignalBlock.LIGHT)==0,"Stopping did not set both signals red");h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void openGatePostsRotateWithVisibleModel(GameTestHelper h){
        var l=h.getLevel();var p=h.absolutePos(new BlockPos(1,1,1));
        for(var block:List.of(NgTraffic.WORK_TIMED.get(),NgTraffic.WORK_ACCESS.get())){
            var base=block.defaultBlockState().setValue(ConstructionBlock.ACTIVE,true);
            var n=base.setValue(OrientedBlock.FACING,Direction.NORTH).getCollisionShape(l,p).bounds();
            var e=base.setValue(OrientedBlock.FACING,Direction.EAST).getCollisionShape(l,p).bounds();
            var so=base.setValue(OrientedBlock.FACING,Direction.SOUTH).getCollisionShape(l,p).bounds();
            var w=base.setValue(OrientedBlock.FACING,Direction.WEST).getCollisionShape(l,p).bounds();
            h.assertTrue(n.minX==0&&so.minX==13.0/16&&e.minZ==0&&w.minZ==13.0/16,"Open gate collision does not follow model rotation");
        }h.succeed();
    }
}
