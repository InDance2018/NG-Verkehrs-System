package de.nevergate.verkehr;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import io.netty.buffer.Unpooled;

@GameTestHolder(value=NgTraffic.ID,namespace=NgTraffic.ID)
public final class ParkingExpansionGameTests{
    private static RoadSystemEntity node(GameTestHelper h,int x,int z,net.minecraft.world.level.block.Block block){
        var p=h.absolutePos(new BlockPos(x,1,z));h.getLevel().setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);h.getLevel().setBlock(p,block.defaultBlockState(),3);
        return (RoadSystemEntity)h.getLevel().getBlockEntity(p);
    }
    private static RoadSystemEntity zone(GameTestHelper h,int x,int z,int capacity,int occupied){
        var n=node(h,x,z,NgTraffic.ROAD_CONTROLLER.get());var d=n.data();d.capacity=capacity;d.occupied=occupied;n.configure(d);return n;
    }
    private static byte[] snapshot(RoadSystemEntity n){var b=new FriendlyByteBuf(Unpooled.buffer());try{RoadSystemMenu.writeSnapshot(b,n);var bytes=new byte[b.readableBytes()];b.readBytes(bytes);return bytes;}finally{b.release();}}
    private static RoadSystemMenu client(GameTestHelper h,RoadSystemEntity n){var b=new FriendlyByteBuf(Unpooled.wrappedBuffer(snapshot(n)));try{return new RoadSystemMenu(1,h.makeMockPlayer().getInventory(),b);}finally{b.release();}}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=45)
    public static void individualSpacesCountOccupancyAndRecover(GameTestHelper h){
        var c=zone(h,5,1,20,4);var a=node(h,1,2,NgTraffic.SPACE_SENSOR.get());var b=node(h,2,2,NgTraffic.SPACE_SENSOR.get());c.attach(a);c.attach(b);
        var d=c.data();d.counting=1;c.configure(d);var p=a.getBlockPos();var boat=new net.minecraft.world.entity.vehicle.Boat(h.getLevel(),p.getX()+.5,p.getY()+.2,p.getZ()+.5);h.getLevel().addFreshEntity(boat);
        h.runAfterDelay(12,()->{
            h.assertTrue(c.online()&&c.data().capacity==2&&c.data().occupied==1&&a.getBlockState().getValue(RoadSystemBlock.OPEN)&&!b.getBlockState().getValue(RoadSystemBlock.OPEN),"Space counts or indicators incorrect");
            h.assertTrue(!c.countPassage(UUID.randomUUID(),1,h.getLevel().getGameTime()),"Passage counter modified sensor mode");boat.discard();
        });
        h.runAfterDelay(28,()->{h.assertTrue(c.data().occupied==0&&c.data().free()==2&&!a.getBlockState().getValue(RoadSystemBlock.OPEN),"Departed vehicle not released");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void emptySpaceModeOfflineAndReplacementIdentityChecked(GameTestHelper h){
        var c=zone(h,1,1,10,0);var d=c.data();d.counting=1;c.configure(d);h.assertTrue(!c.online(),"No sensors reported an available parking zone");
        var a=node(h,2,1,NgTraffic.SPACE_SENSOR.get());c.attach(a);
        h.runAfterDelay(8,()->{
            h.assertTrue(c.online()&&c.data().capacity==1,"Linked space did not activate");
            var saved=c.saveWithoutMetadata();h.getLevel().setBlock(a.getBlockPos(),Blocks.AIR.defaultBlockState(),3);
            h.getLevel().setBlock(a.getBlockPos(),NgTraffic.SPACE_SENSOR.get().defaultBlockState(),3);c.load(saved);
        });
        h.runAfterDelay(22,()->{h.assertTrue(!c.online(),"Replacement sensor silently adopted previous identity");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=80)
    public static void barrierOpensClosesAndBlocksAtFullCapacity(GameTestHelper h){
        var c=zone(h,5,1,2,0);var gate=node(h,2,2,NgTraffic.PARKING_BARRIER.get());c.attach(gate);var p=gate.getBlockPos();
        var boat=new net.minecraft.world.entity.vehicle.Boat(h.getLevel(),p.getX()+.5,p.getY()+.2,p.getZ()-1.1);boat.setNoGravity(true);h.getLevel().addFreshEntity(boat);
        h.runAfterDelay(5,()->{h.assertTrue(gate.barrierOpen()&&gate.getBlockState().getCollisionShape(h.getLevel(),p).isEmpty(),"Gate failed to open or kept collision");boat.discard();});
        h.runAfterDelay(32,()->{h.assertTrue(!gate.barrierOpen()&&!gate.getBlockState().getCollisionShape(h.getLevel(),p).isEmpty(),"Gate did not close");
            var d=c.data();d.occupied=2;c.configure(d);
            var fullBoat=new net.minecraft.world.entity.vehicle.Boat(h.getLevel(),p.getX()+.5,p.getY()+.2,p.getZ()-.6);fullBoat.setNoGravity(true);h.getLevel().addFreshEntity(fullBoat);
            h.runAfterDelay(5,()->{h.assertTrue(!gate.barrierOpen(),"Full parking zone opened entrance");fullBoat.discard();h.succeed();});
        });
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=45)
    public static void barrierObstructionOverridesClosingAndOfflineIsClosed(GameTestHelper h){
        var c=zone(h,5,1,1,1);var gate=node(h,2,2,NgTraffic.PARKING_BARRIER.get());c.attach(gate);var p=gate.getBlockPos();
        var stand=new net.minecraft.world.entity.decoration.ArmorStand(h.getLevel(),p.getX()+.5,p.getY(),p.getZ()+.5);h.getLevel().addFreshEntity(stand);
        h.runAfterDelay(4,()->{h.assertTrue(gate.barrierOpen(),"Gate closed through obstruction");stand.discard();gate.detach();});
        h.runAfterDelay(30,()->{h.assertTrue(!gate.barrierOpen()&&!gate.online(),"Detached unobstructed barrier stayed open");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void oversizedExplicitVehicleCannotOpenBarrier(GameTestHelper h){
        var c=zone(h,5,1,2,0);var d=c.data();d.detection=1;d.vehicleTypes="minecraft:armor_stand";c.configure(d);
        var gate=node(h,2,2,NgTraffic.PARKING_BARRIER.get());c.attach(gate);var limit=gate.data();limit.heightCm=150;gate.configure(limit);var p=gate.getBlockPos();
        var stand=new net.minecraft.world.entity.decoration.ArmorStand(h.getLevel(),p.getX()+.5,p.getY(),p.getZ()-1.2);stand.setNoGravity(true);h.getLevel().addFreshEntity(stand);
        h.runAfterDelay(6,()->{h.assertTrue(!gate.barrierOpen(),"Oversized vehicle opened gate");var next=gate.data();next.heightCm=250;gate.configure(next);});
        h.runAfterDelay(15,()->{h.assertTrue(gate.barrierOpen(),"Suitable adjusted height ignored");stand.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=55)
    public static void automaticSignsSwitchWithParkingAvailability(GameTestHelper h){
        var c=zone(h,1,1,1,0);var d=c.data();d.autoDisplay=1;c.configure(d);
        var sign=node(h,2,1,NgTraffic.VARIABLE_SIGN.get());var lane=node(h,3,1,NgTraffic.LANE_BOARD.get());c.attach(sign);c.attach(lane);
        h.runAfterDelay(12,()->{h.assertTrue(sign.data().mode==1&&lane.data().mode==4,"Free zone automatic state wrong");c.countPassage(UUID.randomUUID(),1,h.getLevel().getGameTime());});
        h.runAfterDelay(27,()->{h.assertTrue(sign.data().mode==3&&lane.data().mode==3,"Full zone not closed");c.countPassage(UUID.randomUUID(),-1,h.getLevel().getGameTime());});
        h.runAfterDelay(42,()->{h.assertTrue(sign.data().mode==1&&lane.data().mode==4,"Reopened zone not restored");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=60)
    public static void automaticAndFixedRoutingChooseTheirOwnZone(GameTestHelper h){
        var a=zone(h,1,1,10,10);var b=zone(h,4,1,20,3);var ad=a.data();ad.name="Deck A";a.configure(ad);var bd=b.data();bd.name="Deck B";b.configure(bd);
        var total=node(h,6,1,NgTraffic.PARKING_AGGREGATOR.get());total.attachZone(a);total.attachZone(b);
        var guide=node(h,2,3,NgTraffic.PARKING_GUIDE.get());total.attach(guide);var d=guide.data();d.routeMode=2;guide.configure(d);
        h.runAfterDelay(15,()->{h.assertTrue(guide.online()&&guide.displayName().equals("Deck B")&&guide.data().free()==17,"Automatic route did not choose available zone");var fixed=guide.data();fixed.routeMode=1;fixed.routeTarget=a.getBlockPos().asLong();guide.configure(fixed);});
        h.runAfterDelay(32,()->{h.assertTrue(guide.online()&&guide.displayName().equals("Deck A")&&guide.data().free()==0,"Fixed route ignored selected full zone");total.removeZone(0);});
        h.runAfterDelay(48,()->{h.assertTrue(!guide.online(),"Removed fixed destination remained available");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void twoMenuViewersReceiveIndependentAtomicSnapshots(GameTestHelper h){
        var c=zone(h,1,1,23,4);var first=client(h,c);var second=client(h,c);c.countPassage(UUID.randomUUID(),1,h.getLevel().getGameTime());
        first.applyLive(snapshot(c));h.assertTrue(first.snapshot.occupied==5&&second.snapshot.occupied==4,"Snapshots shared mutable state");
        second.applyLive(snapshot(c));h.assertTrue(second.snapshot.occupied==5&&second.revision==c.revision(),"Second viewer did not update");
        var d=c.data();d.heightCm=330;d.autoDisplay=1;d.counting=0;c.configure(d);first.applyLive(snapshot(c));h.assertTrue(first.snapshot.heightCm==330&&first.snapshot.autoDisplay==1,"New settings lost in real packet encoding");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void localEditsSurviveLiveCounterUpdates(GameTestHelper h){
        var base=new RoadSystemData();var local=base.copy();local.name="Unsaved name";local.heightCm=310;var remote=base.copy();remote.occupied=8;remote.speed=80;
        var merged=RoadSystemDraft.merge(base,local,remote);
        h.assertTrue(merged.name.equals("Unsaved name")&&merged.heightCm==310&&merged.occupied==8&&merged.speed==80,"Three-way merge lost edit or update");
        h.assertTrue(base.occupied==0&&local.occupied==0,"Draft merge mutated input");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void newSettingsAndSpaceLinksPersistWithoutTrustingOnline(GameTestHelper h){
        var c=zone(h,1,1,10,0);var space=node(h,2,1,NgTraffic.SPACE_SENSOR.get());c.attach(space);var d=c.data();d.counting=1;d.autoDisplay=1;c.configure(d);var saved=c.saveWithoutMetadata();c.load(saved);
        h.assertTrue(c.connectedSpaceCount()==1&&c.data().counting==1&&c.data().autoDisplay==1&&!c.online(),"Space links/settings not restored safely");
        var sign=node(h,3,1,NgTraffic.HEIGHT_SIGN.get());d=sign.data();d.heightCm=650;sign.configure(d);sign.load(sign.saveWithoutMetadata());h.assertTrue(sign.data().heightCm==650,"Height setting lost");
        saved.remove("Counting");saved.remove("HeightCm");saved.remove("RouteMode");saved.remove("AutoDisplay");saved.remove("Spaces");c.load(saved);
        h.assertTrue(c.data().counting==0&&c.data().heightCm==220&&c.connectedSpaceCount()==0,"Legacy data migration incorrect");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void routingBehindTheSignDoesNotPointAhead(GameTestHelper h){
        var a=zone(h,4,1,5,0);var total=node(h,6,4,NgTraffic.PARKING_AGGREGATOR.get());total.attachZone(a);
        var guide=node(h,4,4,NgTraffic.PARKING_GUIDE.get());total.attach(guide);var d=guide.data();d.routeMode=2;guide.configure(d);
        h.runAfterDelay(15,()->{h.assertTrue(guide.online()&&guide.displayArrow().equals("↓"),"Destination behind sign incorrectly pointed ahead");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void liveAggregateRowsAndTotalUseTheSameRevision(GameTestHelper h){
        var a=zone(h,1,1,17,7);var total=node(h,2,1,NgTraffic.PARKING_AGGREGATOR.get());total.attachZone(a);
        a.countPassage(java.util.UUID.randomUUID(),1,h.getLevel().getGameTime());var view=client(h,total);
        h.assertTrue(view.snapshot.occupied==8&&view.zones.get(0).occupied()==8&&view.snapshot.free()==9,"Live aggregate mixed old total with new zone row");h.succeed();
    }
}
