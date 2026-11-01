package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(value=NgTraffic.ID,namespace=NgTraffic.ID)
public final class ParkingZoneGameTests {
    private static RoadSystemEntity node(GameTestHelper h,int x,int z,boolean aggregate){
        var p=h.absolutePos(new BlockPos(x,1,z));h.getLevel().setBlock(p,(aggregate?NgTraffic.PARKING_AGGREGATOR:NgTraffic.ROAD_CONTROLLER).get().defaultBlockState(),3);
        return (RoadSystemEntity)h.getLevel().getBlockEntity(p);
    }
    private static void configure(RoadSystemEntity n,int capacity,int occupied){var d=n.data();d.capacity=capacity;d.occupied=occupied;n.configure(d);}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=50)
    public static void independentZonesPropagateExactTotals(GameTestHelper h){
        var a=node(h,1,1,false);var b=node(h,2,1,false);var total=node(h,3,1,true);configure(a,100,10);configure(b,50,50);
        h.assertTrue(total.attachZone(a)&&total.attachZone(b),"Zones not linked");
        var p=h.absolutePos(new BlockPos(4,1,1));h.getLevel().setBlock(p,NgTraffic.PARKING_BOARD.get().defaultBlockState(),3);var board=(RoadSystemEntity)h.getLevel().getBlockEntity(p);total.attach(board);
        h.runAfterDelay(15,()->{h.assertTrue(total.online()&&total.data().capacity==150&&total.data().occupied==60&&board.data().free()==90,"Initial totals incorrect");a.countPassage(java.util.UUID.randomUUID(),1,h.getLevel().getGameTime());});
        h.runAfterDelay(38,()->{h.assertTrue(a.data().occupied==11&&b.data().occupied==50&&board.online()&&board.data().free()==89,"Zone count leaked or aggregate stale");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void zoneLimitsDedupAndNoNestedAggregation(GameTestHelper h){
        var total=node(h,1,1,true);RoadSystemEntity first=null;
        for(int i=0;i<16;i++){var zone=node(h,2+i%4,2+i/4,false);configure(zone,100000,90000);h.assertTrue(total.attachZone(zone),"Valid zone rejected");if(first==null)first=zone;}
        h.assertTrue(total.attachZone(first)&&total.zones().size()==16,"Duplicate zone counted twice");
        h.assertTrue(total.data().capacity==1600000&&total.data().occupied==1440000,"Large total clamped or overflowed");
        h.assertTrue(!total.attachZone(node(h,6,2,false))&&!total.attachZone(node(h,6,3,true)),"Zone limit or nested aggregation accepted");
        var p=h.absolutePos(new BlockPos(6,1,4));h.getLevel().setBlock(p,NgTraffic.ENTRY_SENSOR.get().defaultBlockState(),3);
        h.assertTrue(!total.attach((RoadSystemEntity)h.getLevel().getBlockEntity(p))&&!total.countPassage(java.util.UUID.randomUUID(),1,1),"Aggregate accepted sensor or direct count");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=45)
    public static void offlineZoneCannotBeReplacedSilently(GameTestHelper h){
        var zone=node(h,1,1,false);var total=node(h,2,1,true);configure(zone,100,20);total.attachZone(zone);
        var p=h.absolutePos(new BlockPos(3,1,1));h.getLevel().setBlock(p,NgTraffic.PARKING_BOARD.get().defaultBlockState(),3);var board=(RoadSystemEntity)h.getLevel().getBlockEntity(p);total.attach(board);
        h.runAfterDelay(12,()->{h.getLevel().setBlock(zone.getBlockPos(),Blocks.AIR.defaultBlockState(),3);h.getLevel().setBlock(zone.getBlockPos(),NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);});
        h.runAfterDelay(35,()->{h.assertTrue(!total.online()&&!board.online()&&!total.zones().get(0).online(),"Replacement adopted old zone identity");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void zoneLinksPersistAndRemoveWithoutChangingCounts(GameTestHelper h){
        var zone=node(h,1,1,false);var total=node(h,2,1,true);configure(zone,71,23);total.attachZone(zone);var saved=total.saveWithoutMetadata();total.load(saved);
        h.assertTrue(total.zones().size()==1&&!total.online(),"Saved zone lost or online trusted before validation");
        h.runAfterDelay(15,()->{h.assertTrue(total.online()&&total.data().free()==48,"Persisted zone not revalidated");h.assertTrue(total.removeZone(0)&&!total.online()&&zone.data().occupied==23&&!total.removeZone(0),"Removing zone modified occupancy or accepted invalid index");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void vehiclePoliciesUseExactIdsAndAlwaysExcludePlayers(GameTestHelper h){
        var level=h.getLevel();var rules=new RoadSystemData();rules.detection=1;
        var boat=new net.minecraft.world.entity.vehicle.Boat(level,0,1,0);var player=h.makeMockPlayer();
        h.assertTrue(!RoadSystemEntity.vehicle(boat,rules),"Strict mode accepted unlisted boat");rules.vehicleTypes="minecraft:boat,minecraft:player";
        h.assertTrue(RoadSystemEntity.vehicle(boat,rules)&&!RoadSystemEntity.vehicle(player,rules),"Exact allowlist failed or pedestrian counted");
        var stand=new net.minecraft.world.entity.decoration.ArmorStand(level,0,1,0);rules.vehicleTypes="minecraft:armor_stand";
        h.assertTrue(RoadSystemEntity.vehicle(stand,rules),"Explicit non-default entity type ignored");
        rules.vehicleTypes="minecraft:armor_stand_extra";h.assertTrue(!RoadSystemEntity.vehicle(stand,rules),"Prefix entity ID accepted");
        rules.detection=2;h.assertTrue(RoadSystemEntity.vehicle(boat,rules),"Vanilla-only fallback lost boats");player.startRiding(boat,true);h.assertTrue(!RoadSystemEntity.vehicle(player,rules),"Passenger counted separately");player.stopRiding();h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void strictVehicleListDrivesActualSensorCrossing(GameTestHelper h){
        var level=h.getLevel();var c=node(h,5,2,false);var d=c.data();d.detection=1;d.vehicleTypes="minecraft:boat";c.configure(d);
        var gate=h.absolutePos(new BlockPos(2,1,2));level.setBlock(gate.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(gate,NgTraffic.ENTRY_SENSOR.get().defaultBlockState(),3);var sensor=(RoadSystemEntity)level.getBlockEntity(gate);c.attach(sensor);
        var boat=new net.minecraft.world.entity.vehicle.Boat(level,gate.getX()+.5,gate.getY()+.2,gate.getZ()-.25);level.addFreshEntity(boat);
        h.runAfterDelay(2,()->{boat.setPos(gate.getX()+.5,gate.getY()+.2,gate.getZ()-.25);sensor.tick();});
        h.runAfterDelay(3,()->{boat.setPos(gate.getX()+.5,gate.getY()+.2,gate.getZ()+1.25);sensor.tick();h.assertTrue(c.data().occupied==1,"Strict list not used by sensor");boat.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void aggregateMenuProtectsTotalsAndZoneRemoval(GameTestHelper h){
        var zone=node(h,1,1,false);var total=node(h,2,1,true);configure(zone,90,33);total.attachZone(zone);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;var p=total.getBlockPos();player.setPos(p.getX()+.5,p.getY()+.5,p.getZ()+.5);
        var first=new RoadSystemMenu(1,player.getInventory(),total);var stale=new RoadSystemMenu(2,player.getInventory(),total);var d=total.data();d.name="Alle Parkzonen";d.capacity=1;d.occupied=0;
        h.assertTrue(first.save(player,first.revision,d)&&total.data().capacity==90&&total.data().occupied==33,"Aggregate menu forged totals");
        h.assertTrue(!stale.removeZone(player,stale.revision,0),"Stale zone removal accepted");var current=new RoadSystemMenu(3,player.getInventory(),total);player.getAbilities().mayBuild=false;h.assertTrue(!current.removeZone(player,current.revision,0),"No-build player removed zone");player.getAbilities().mayBuild=true;h.assertTrue(!current.removeZone(player,current.revision,99)&&current.removeZone(player,current.revision,0)&&zone.data().occupied==33,"Zone removal permissions or index handling failed");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void zoneMenuSnapshotAndLegacyNbtRoundtrip(GameTestHelper h){
        var zone=node(h,1,1,false);var total=node(h,2,1,true);var d=zone.data();d.name="Parkdeck Düsseldorf";d.vehicleTypes="demo:car,demo:truck";d.detection=1;d.capacity=123;d.occupied=12;zone.configure(d);total.attachZone(zone);
        var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());RoadSystemMenu.writeSnapshot(b,total);var read=new RoadSystemMenu(1,h.makeMockPlayer().getInventory(),b);b.release();h.assertTrue(read.zones.size()==1&&read.zones.get(0).name().equals(d.name)&&read.snapshot.capacity==123,"Zone snapshot lost list or total");
        var saved=zone.saveWithoutMetadata();zone.load(saved);h.assertTrue(zone.data().vehicleTypes.equals(d.vehicleTypes)&&zone.data().detection==1,"Vehicle config not persisted");
        saved.remove("VehicleTypes");saved.remove("Detection");saved.remove("Zones");zone.load(saved);h.assertTrue(zone.data().detection==0&&zone.data().vehicleTypes.isEmpty()&&zone.data().capacity==123&&zone.data().occupied==12,"Legacy NBT migration lost data");h.succeed();
    }
}
