package de.nevergate.verkehr;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(value=NgTraffic.ID,namespace=NgTraffic.ID)
public final class ParkingReliabilityGameTests {
    private static RoadSystemEntity node(GameTestHelper h,int x,int z,boolean aggregate){
        var p=h.absolutePos(new BlockPos(x,1,z));h.getLevel().setBlock(p,(aggregate?NgTraffic.PARKING_AGGREGATOR:NgTraffic.ROAD_CONTROLLER).get().defaultBlockState(),3);
        return (RoadSystemEntity)h.getLevel().getBlockEntity(p);
    }
    private static void configure(RoadSystemEntity n,int capacity,int occupied){var d=n.data();d.capacity=capacity;d.occupied=occupied;n.configure(d);}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void fullAndEmptyZonesStayWithinCapacity(GameTestHelper h){
        var c=node(h,1,1,false);configure(c,2,2);long now=h.getLevel().getGameTime();
        c.countPassage(UUID.randomUUID(),1,now);h.assertTrue(c.data().occupied==2&&c.data().free()==0,"Full zone overflowed");
        c.countPassage(UUID.randomUUID(),-1,now);h.assertTrue(c.data().occupied==1&&c.data().free()==1,"Exit did not reopen full zone");
        c.countPassage(UUID.randomUUID(),-1,now);c.countPassage(UUID.randomUUID(),-1,now);
        h.assertTrue(c.data().occupied==0&&c.data().free()==2,"Empty zone became negative");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void duplicateProtectionSurvivesControllerReload(GameTestHelper h){
        var c=node(h,1,1,false);long now=h.getLevel().getGameTime();var id=UUID.randomUUID();
        c.countPassage(id,1,now);var saved=c.saveWithoutMetadata();h.assertTrue(saved.contains("Passages")&&!c.getUpdateTag().contains("Passages"),"Passage state missing from save or leaked to display packet");c.load(saved);
        h.assertTrue(!c.countPassage(id,1,now+1)&&c.data().occupied==1,"Reload lost duplicate protection");
        h.assertTrue(c.countPassage(id,-1,now+2)&&c.data().occupied==0,"Valid exit was suppressed");
        h.assertTrue(c.countPassage(id,1,now+3)&&c.data().occupied==1,"Reentry after exit suppressed");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void expiredAndFuturePassagesDoNotBlockVehicles(GameTestHelper h){
        var c=node(h,1,1,false);long now=h.getLevel().getGameTime();var id=UUID.randomUUID();
        c.countPassage(id,1,now);var saved=c.saveWithoutMetadata();h.assertTrue(saved.contains("Passages")&&!c.getUpdateTag().contains("Passages"),"Passage state missing from save or leaked to display packet");c.load(saved);
        h.assertTrue(c.countPassage(id,1,now+100)&&c.data().occupied==2,"Expired duplicate protection did not expire");
        h.assertTrue(c.countPassage(id,1,now)&&c.data().occupied==3,"Future timestamp blocked legitimate crossing");
        saved.remove("Passages");c.load(saved);h.assertTrue(c.countPassage(id,1,now),"Legacy NBT failed");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=35)
    public static void overlappingSensorsCountOnceInTheirOwnZone(GameTestHelper h){
        var level=h.getLevel();var a=node(h,5,1,false);var b=node(h,5,2,false);configure(a,10,0);configure(b,10,0);
        var p=h.absolutePos(new BlockPos(2,1,2));var q=h.absolutePos(new BlockPos(3,1,2));
        level.setBlock(p.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(q.below(),Blocks.STONE.defaultBlockState(),3);
        level.setBlock(p,NgTraffic.ENTRY_SENSOR.get().defaultBlockState(),3);level.setBlock(q,NgTraffic.ENTRY_SENSOR.get().defaultBlockState(),3);
        var s1=(RoadSystemEntity)level.getBlockEntity(p);var s2=(RoadSystemEntity)level.getBlockEntity(q);a.attach(s1);a.attach(s2);
        var boat=new net.minecraft.world.entity.vehicle.Boat(level,p.getX()+1,p.getY()+.2,p.getZ()-.25);level.addFreshEntity(boat);
        h.runAfterDelay(2,()->{boat.setPos(p.getX()+1,p.getY()+.2,p.getZ()-.25);s1.tick();s2.tick();});
        h.runAfterDelay(3,()->{
            boat.setPos(p.getX()+1,p.getY()+.2,p.getZ()+1.25);s1.tick();s2.tick();
            h.assertTrue(a.data().occupied==1&&b.data().occupied==0,"Overlapping sensors doubled or leaked count");
            var id=boat.getUUID();b.countPassage(id,1,level.getGameTime());
            h.assertTrue(b.data().occupied==1,"Other zone suppressed same vehicle");boat.discard();h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void fullNetworkReloadRestoresCountsAndBoard(GameTestHelper h){
        var a=node(h,1,1,false);var b=node(h,2,1,false);var total=node(h,3,1,true);configure(a,7,7);configure(b,3,1);total.attachZone(a);total.attachZone(b);
        var p=h.absolutePos(new BlockPos(4,1,1));h.getLevel().setBlock(p,NgTraffic.PARKING_BOARD.get().defaultBlockState(),3);
        var board=(RoadSystemEntity)h.getLevel().getBlockEntity(p);total.attach(board);
        a.load(a.saveWithoutMetadata());b.load(b.saveWithoutMetadata());total.load(total.saveWithoutMetadata());board.load(board.saveWithoutMetadata());
        h.assertTrue(!total.online()&&!board.online(),"Saved online state was trusted");
        h.runAfterDelay(25,()->{
            h.assertTrue(a.data().occupied==7&&b.data().occupied==1&&total.online()&&board.online(),"Reload lost zone state or connections");
            h.assertTrue(board.data().capacity==10&&board.data().occupied==8&&board.data().free()==2,"Reload display values incorrect");h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void occupiedFallbackAndStrictPolicyRemainSeparate(GameTestHelper h){
        var level=h.getLevel();var stand=new net.minecraft.world.entity.decoration.ArmorStand(level,0,1,0);var player=h.makeMockPlayer();var rules=new RoadSystemData();
        h.assertTrue(!RoadSystemEntity.vehicle(stand,rules),"Empty miscellaneous entity auto-counted");
        player.startRiding(stand,true);h.assertTrue(RoadSystemEntity.vehicle(stand,rules)&&!RoadSystemEntity.vehicle(player,rules),"Occupied root or passenger handling failed");
        rules.detection=1;h.assertTrue(!RoadSystemEntity.vehicle(stand,rules),"Strict mode used automatic fallback");
        rules.vehicleTypes="minecraft:armor_stand";h.assertTrue(RoadSystemEntity.vehicle(stand,rules),"Explicit vehicle identifier ignored");
        player.stopRiding();h.assertTrue(RoadSystemEntity.vehicle(stand,rules),"Explicit empty vehicle ignored");h.succeed();
    }
}
