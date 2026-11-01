package de.nevergate.verkehr;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.gametest.GameTestHolder;
@GameTestHolder(value=NgTraffic.ID,namespace=NgTraffic.ID)
public final class TrafficOperationsGameTests {
    private static TrafficNodeEntity put(GameTestHelper h,BlockPos p,TrafficNodeBlock.Kind k){h.getLevel().setBlock(p,NgTraffic.TRAFFIC_NODES.get(k).get().defaultBlockState(),3);return (TrafficNodeEntity)h.getLevel().getBlockEntity(p);}
    private static net.minecraft.world.entity.player.Player player(GameTestHelper h,BlockPos p){var x=h.makeMockPlayer();x.getAbilities().mayBuild=true;x.setPos(p.getX()+.5,p.getY()+.5,p.getZ()+.5);return x;}
    private static Boat boat(GameTestHelper h,BlockPos p,double z){var b=new Boat(h.getLevel(),p.getX()+.5,p.getY()+.3,p.getZ()+z);b.setNoGravity(true);b.noPhysics=true;h.getLevel().addFreshEntity(b);return b;}
    private static RoadSystemEntity road(GameTestHelper h,BlockPos p){h.getLevel().setBlock(p,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);return (RoadSystemEntity)h.getLevel().getBlockEntity(p);}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void actualSpeedConversionAndVerticalMotionIgnored(GameTestHelper h){
        h.assertTrue(TrafficNodeEntity.measuredKph(1,0,0,0,1)==72,"Wrong metres per tick to km/h");
        h.assertTrue(TrafficNodeEntity.measuredKph(2,0,0,0,2)==72&&TrafficNodeEntity.measuredKph(0,0,0,0,2)==0,"Invalid stationary or sampled speed");
        h.assertTrue(TrafficNodeEntity.measuredKph(1000,0,0,0,1)==999&&TrafficNodeEntity.measuredKph(1,1,0,0,0)==0,"Invalid limits");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=35)
    public static void movingVehicleFeedsSpeedDisplayAndWarning(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var sensor=put(h,p,TrafficNodeBlock.Kind.SPEED_SENSOR);sensor.settings(0,5,16,3,20,0,1);var board=put(h,p.east(2),TrafficNodeBlock.Kind.SPEED_BOARD);sensor.link(board,player(h,p));var b=boat(h,p,-1);
        h.onEachTick(()->b.setPos(p.getX()+.5,p.getY()+.3,b.getZ()+.2));
        h.runAfterDelay(14,()->{h.assertTrue(sensor.speed()>5&&board.speed()>5&&board.alarm()&&board.online(),"Real movement did not reach live display");b.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=35)
    public static void directionalCounterIgnoresInitialAndStationaryVehicles(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var n=put(h,p,TrafficNodeBlock.Kind.COUNTER);var b=boat(h,p,-.5);
        h.runAfterDelay(4,()->{h.assertTrue(n.forward()==0&&n.reverse()==0,"Stationary vehicle counted");b.setPos(p.getX()+.5,p.getY()+.3,p.getZ()+1.5);});
        h.runAfterDelay(10,()->{h.assertTrue(n.forward()==1&&n.reverse()==0,"Forward crossing missing or duplicated");b.setPos(p.getX()+.5,p.getY()+.3,p.getZ()-.5);});
        h.runAfterDelay(16,()->{h.assertTrue(n.forward()==1&&n.reverse()==1&&n.hourly()==120,"Reverse crossing or hourly estimate wrong");b.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=110)
    public static void queueTriggersExistingWarningAndRestoresManualMode(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var n=put(h,p,TrafficNodeBlock.Kind.JAM_SENSOR);n.settings(0,50,8,3,20,0,1);var c=road(h,p.east(2));var d=c.data();d.mode=1;c.configure(d);
        h.getLevel().setBlock(p.east(3),NgTraffic.VARIABLE_SIGN.get().defaultBlockState(),3);var sign=(RoadSystemEntity)h.getLevel().getBlockEntity(p.east(3));c.attach(sign);n.linkRoad(c,player(h,p));
        var boats=List.of(boat(h,p,2),boat(h,p,4),boat(h,p,6));
        h.runAfterDelay(45,()->{h.assertTrue(n.alarm()&&n.slow()==3&&sign.data().mode==2&&c.data().mode==1,"Queue override changed base or warning missing");boats.forEach(Entity::discard);});
        h.runAfterDelay(100,()->{h.assertTrue(!n.alarm()&&sign.data().mode==1,"Queue did not clear and restore configured mode");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=70)
    public static void expiredExternalOverrideCannotLeavePermanentClosure(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var c=road(h,p);var d=c.data();d.mode=1;c.configure(d);h.getLevel().setBlock(p.east(),NgTraffic.LANE_BOARD.get().defaultBlockState(),3);var s=(RoadSystemEntity)h.getLevel().getBlockEntity(p.east());c.attach(s);c.trafficOverride(UUID.randomUUID(),3,30,h.getLevel().getGameTime()+30);
        h.runAfterDelay(16,()->h.assertTrue(s.data().mode==3&&c.data().mode==1,"External closure not applied independently"));
        h.runAfterDelay(55,()->{h.assertTrue(s.data().mode==1,"Expired owner left a permanent override");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=90)
    public static void independentOverridesDoNotCancelEachOther(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var c=road(h,p);var d=c.data();d.mode=1;c.configure(d);h.getLevel().setBlock(p.east(),NgTraffic.VARIABLE_SIGN.get().defaultBlockState(),3);var s=(RoadSystemEntity)h.getLevel().getBlockEntity(p.east());c.attach(s);var a=UUID.randomUUID();var b=UUID.randomUUID();long now=h.getLevel().getGameTime();c.trafficOverride(a,2,30,now+100);c.trafficOverride(b,3,30,now+100);
        h.runAfterDelay(15,()->{h.assertTrue(s.data().mode==3,"Closure lost to queue");c.releaseTraffic(b);});
        h.runAfterDelay(35,()->{h.assertTrue(s.data().mode==2,"Removing one source removed another");c.releaseTraffic(a);});
        h.runAfterDelay(55,()->{h.assertTrue(s.data().mode==1,"Configured mode not restored");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=260)
    public static void railwayWarningClosingAndDelayedReopening(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var n=put(h,p,TrafficNodeBlock.Kind.CROSSING_CONTROLLER);var gate=put(h,p.east(2),TrafficNodeBlock.Kind.CROSSING_BARRIER);var light=put(h,p.east(3),TrafficNodeBlock.Kind.CROSSING_LIGHT);var pl=player(h,p);n.link(gate,pl);n.link(light,pl);
        var train=new Minecart(h.getLevel(),p.getX()+.5,p.getY()+.3,p.getZ()+6);train.setNoGravity(true);train.noPhysics=true;
        h.runAfterDelay(80,()->{h.assertTrue(gate.status()==1,"Clear crossing did not open after startup");h.getLevel().addFreshEntity(train);});
        h.runAfterDelay(95,()->h.assertTrue(n.alarm()&&gate.status()==1&&light.status()>=2,"No advance warning before closure"));
        h.runAfterDelay(170,()->{h.assertTrue(gate.status()==2,"Detected train did not close barrier");train.discard();});
        h.runAfterDelay(248,()->{h.assertTrue(!n.alarm()&&gate.status()==1,"Crossing did not reopen after train cleared");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void unlinkedBarriersFailClosedAndTunnelLampStaysOn(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var a=put(h,p,TrafficNodeBlock.Kind.CROSSING_BARRIER);var b=put(h,p.east(),TrafficNodeBlock.Kind.TUNNEL_BARRIER);var light=put(h,p.east(2),TrafficNodeBlock.Kind.TUNNEL_LIGHT);var lane=put(h,p.east(3),TrafficNodeBlock.Kind.TUNNEL_LANE);
        h.runAfterDelay(6,()->{h.assertTrue(a.status()!=1&&b.status()!=1&&lane.status()==2,"Unlinked crossing or tunnel falsely open");h.assertTrue(light.getBlockState().getLightEmission(h.getLevel(),light.getBlockPos())==15,"Lost connection disabled tunnel safety lighting");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=70)
    public static void tunnelDetourPipelineClosesTogetherAndReopens(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var t=put(h,p,TrafficNodeBlock.Kind.TUNNEL_CONTROLLER);var g=put(h,p.east(2),TrafficNodeBlock.Kind.TUNNEL_BARRIER);var lane=put(h,p.east(3),TrafficNodeBlock.Kind.TUNNEL_LANE);var light=put(h,p.east(4),TrafficNodeBlock.Kind.TUNNEL_LIGHT);var detour=put(h,p.south(2),TrafficNodeBlock.Kind.DETOUR_CONTROLLER);var guide=put(h,p.south(3),TrafficNodeBlock.Kind.DETOUR_GUIDE);var pl=player(h,p);
        h.assertTrue(t.link(g,pl)&&t.link(lane,pl)&&t.link(light,pl)&&t.link(detour,pl)&&detour.link(guide,pl),"Tunnel pipeline could not link");
        detour.text("Moers");detour.settings(0,50,16,3,100,0,2);t.settings(1,50,16,3,100,1,1);
        h.runAfterDelay(12,()->{h.assertTrue(g.status()==2&&lane.status()==2&&light.status()==1&&guide.alarm()&&guide.text().equals("Moers")&&guide.direction()==2,"Closure and detour not synchronized");t.settings(0,50,16,3,100,1,1);});
        h.runAfterDelay(45,()->{h.assertTrue(g.status()==1&&lane.status()==1&&light.status()==0&&!guide.alarm(),"Tunnel did not recover or detour stayed active");detour.settings(1,50,16,3,100,0,2);h.getLevel().removeBlock(p,false);});
        h.runAfterDelay(55,()->{h.assertTrue(guide.online()&&guide.alarm(),"Forced detour depended on missing trigger");detour.settings(2,50,16,3,100,0,2);});
        h.runAfterDelay(65,()->{h.assertTrue(guide.online()&&!guide.alarm(),"Manual detour off retained alarm");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void replacementControllerDoesNotAdoptOldEndpoints(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var t=put(h,p,TrafficNodeBlock.Kind.TUNNEL_CONTROLLER);var g=put(h,p.east(2),TrafficNodeBlock.Kind.TUNNEL_BARRIER);t.link(g,player(h,p));var old=t.identity();
        h.getLevel().removeBlock(p,false);var replacement=put(h,p,TrafficNodeBlock.Kind.TUNNEL_CONTROLLER);
        h.runAfterDelay(8,()->{h.assertTrue(!old.equals(replacement.identity())&&g.source()==null&&!g.online()&&g.status()!=1,"Replacement adopted endpoint");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void signalReplacementIdentityInvalidatesPrioritySensor(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var n=put(h,p,TrafficNodeBlock.Kind.BUS_SENSOR);var cp=p.east(2);h.getLevel().setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var old=(DeviceEntity)h.getLevel().getBlockEntity(cp);n.linkSignal(old,player(h,p));
        h.getLevel().removeBlock(cp,false);h.getLevel().setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        h.runAfterDelay(8,()->{h.assertTrue(!n.online(),"Replacement signal controller adopted old bus request source");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void trafficMenuRejectsDistantStaleAndReadOnlyChanges(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var n=put(h,p,TrafficNodeBlock.Kind.SPEED_SENSOR);var pl=player(h,p);var menu=new TrafficNodeMenu(1,pl.getInventory(),n);
        h.assertTrue(menu.clickMenuButton(pl,1)&&n.limit()==55,"Valid edit failed");pl.getAbilities().mayBuild=false;h.assertTrue(!menu.clickMenuButton(pl,1),"Read-only edit accepted");pl.getAbilities().mayBuild=true;pl.setPos(p.getX()+20,p.getY(),p.getZ());h.assertTrue(!menu.clickMenuButton(pl,1),"Distant edit accepted");pl.setPos(p.getX()+.5,p.getY(),p.getZ()+.5);h.getLevel().removeBlock(p,false);put(h,p,TrafficNodeBlock.Kind.SPEED_SENSOR);h.assertTrue(!menu.clickMenuButton(pl,1),"Stale editor changed replacement");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void nodeSettingsConnectionsAndHighCountersPersist(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var root=put(h,p,TrafficNodeBlock.Kind.DETOUR_CONTROLLER);var guide=put(h,p.east(),TrafficNodeBlock.Kind.DETOUR_GUIDE);root.link(guide,player(h,p));root.settings(1,80,32,6,160,1,2);root.text("Düsseldorf / Moers");root.types("minecraft:boat");var id=root.identity();root.load(root.saveWithoutMetadata());guide.load(guide.saveWithoutMetadata());h.assertTrue(root.identity().equals(id)&&guide.source()==root&&root.radius()==32&&root.delay()==160&&root.text().contains("Moers")&&root.types().equals("minecraft:boat"),"Persisted configuration lost");
        var counter=put(h,p.south(2),TrafficNodeBlock.Kind.COUNTER);var tag=counter.saveWithoutMetadata();tag.putInt("Forward",2000000000);tag.putInt("Reverse",65537);counter.load(tag);var m=new TrafficNodeMenu(2,player(h,p).getInventory(),counter);h.assertTrue(m.count(10)==2000000000&&m.count(12)==65537,"High counters truncated");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void nodeLinksCheckTypesBuildPermissionAndSelfLink(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var a=put(h,p,TrafficNodeBlock.Kind.SPEED_SENSOR);var board=put(h,p.east(),TrafficNodeBlock.Kind.SPEED_BOARD);var tunnel=put(h,p.south(),TrafficNodeBlock.Kind.TUNNEL_CONTROLLER);var pl=player(h,p);
        h.assertTrue(!a.link(a,pl)&&!a.link(tunnel,pl),"Incompatible link accepted");pl.getAbilities().mayBuild=false;h.assertTrue(!a.link(board,pl),"Read-only link accepted");pl.getAbilities().mayBuild=true;h.assertTrue(a.link(board,pl),"Valid link rejected");board.disconnect();h.assertTrue(board.source()==null&&board.connectionCount()==0,"Disconnect failed");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void railEntityExplicitMarkIsRecognized(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var n=put(h,p,TrafficNodeBlock.Kind.CROSSING_CONTROLLER);var b=boat(h,p,4);b.addTag("ng_train");
        h.runAfterDelay(10,()->{h.assertTrue(n.alarm(),"Explicitly marked train not detected");b.discard();h.succeed();});
    }
    private static void priorityScenario(GameTestHelper h,boolean emergency){priorityScenario(h,emergency,false);}
    private static void priorityScenario(GameTestHelper h,boolean emergency,boolean construction){
        var p=h.absolutePos(new BlockPos(2,1,2));var cp=p.east(2);h.getLevel().setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var c=(DeviceEntity)h.getLevel().getBlockEntity(cp);
        var ap=p.south(2);var bp=p.south(3);h.getLevel().setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);h.getLevel().setBlock(bp,NgTraffic.SIGNAL.get().defaultBlockState(),3);var a=(SignalEntity)h.getLevel().getBlockEntity(ap);var b=(SignalEntity)h.getLevel().getBlockEntity(bp);a.link(cp,0);b.link(cp,1);
        if(construction){a.autoLink(cp,0);b.autoLink(cp,2);c.changeSetting(0,2);}
        c.changeSetting(1,5);c.setEnabled(true);
        var sensor=put(h,p,emergency?TrafficNodeBlock.Kind.EMERGENCY_SENSOR:TrafficNodeBlock.Kind.BUS_SENSOR);sensor.settings(0,50,16,3,100,construction?0:1,1);sensor.linkSignal(c,player(h,p));var vehicle=boat(h,p,5);vehicle.addTag(emergency?"ng_emergency_active":"ng_bus");
        h.onEachTick(()->{a.tick();b.tick();h.assertTrue(!(a.getBlockState().getValue(SignalBlock.LIGHT)==2&&b.getBlockState().getValue(SignalBlock.LIGHT)==2),"Conflicting priority greens");});
        h.runAfterDelay(270,()->{h.assertTrue((construction?a:b).getBlockState().getValue(SignalBlock.LIGHT)==2&&(construction?b:a).getBlockState().getValue(SignalBlock.LIGHT)==0,"Marked priority request did not receive safe green");vehicle.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=320)
    public static void markedBusReceivesPriorityAfterClearance(GameTestHelper h){priorityScenario(h,false);}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=320)
    public static void markedEmergencyReceivesPriorityAfterClearance(GameTestHelper h){priorityScenario(h,true);}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void trafficNodeStatesRotateAndMenuSnapshotRoundtrips(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));
        for(var kind:TrafficNodeBlock.Kind.values())for(var d:Direction.Plane.HORIZONTAL){
            var s=NgTraffic.TRAFFIC_NODES.get(kind).get().defaultBlockState().setValue(OrientedBlock.FACING,d);
            h.assertTrue(!s.getShape(h.getLevel(),p).isEmpty()&&s.rotate(Rotation.CLOCKWISE_90).getValue(OrientedBlock.FACING)==d.getClockWise(),"Device orientation/shape invalid");
        }
        var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());b.writeBlockPos(p);b.writeEnum(TrafficNodeBlock.Kind.COUNTER);var m=new TrafficNodeMenu(1,player(h,p).getInventory(),b);b.release();h.assertTrue(m.pos.equals(p)&&m.kind==TrafficNodeBlock.Kind.COUNTER,"Menu snapshot failed");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=380)
    public static void interruptedPriorityRestartsWithFullAllRed(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));var cp=p.east(2);h.getLevel().setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var c=(DeviceEntity)h.getLevel().getBlockEntity(cp);
        var ap=p.south(2);var bp=p.south(3);h.getLevel().setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);h.getLevel().setBlock(bp,NgTraffic.SIGNAL.get().defaultBlockState(),3);var a=(SignalEntity)h.getLevel().getBlockEntity(ap);var b=(SignalEntity)h.getLevel().getBlockEntity(bp);a.link(cp,0);b.link(cp,1);c.changeSetting(1,5);c.setEnabled(true);
        final boolean[] requesting={true};h.onEachTick(()->{if(requesting[0])c.requestPriority(1,true);});
        h.runAfterDelay(270,()->{
            a.tick();b.tick();h.assertTrue(b.getBlockState().getValue(SignalBlock.LIGHT)==2,"No active priority to interrupt");
            c.requestPedestrians(0);var saved=c.saveWithoutMetadata();h.assertTrue(saved.getBoolean("PriorityInterrupted"),"Active priority not marked for safe restart");requesting[0]=false;c.load(saved);a.tick();b.tick();
            h.assertTrue(a.getBlockState().getValue(SignalBlock.LIGHT)==0&&b.getBlockState().getValue(SignalBlock.LIGHT)==0,"Restart resumed live green");
        });
        h.runAfterDelay(300,()->{a.tick();b.tick();h.assertTrue(a.getBlockState().getValue(SignalBlock.LIGHT)==0&&b.getBlockState().getValue(SignalBlock.LIGHT)==0,"Restart clearance shorter than 2s");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=320)
    public static void emergencyRequestWinsOverPendingBusRequest(GameTestHelper h){
        var p=h.absolutePos(new BlockPos(2,1,2));h.getLevel().setBlock(p,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var c=(DeviceEntity)h.getLevel().getBlockEntity(p);c.changeSetting(1,5);c.setEnabled(true);
        h.onEachTick(()->{c.requestPriority(0,false);c.requestPriority(1,true);});
        h.runAfterDelay(270,()->{h.assertTrue(c.light(1)==TrafficCycle.Light.GREEN&&c.light(0)==TrafficCycle.Light.RED,"Pending bus request blocked emergency priority");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=320)
    public static void priorityRespectsOpposingConstructionGroups(GameTestHelper h){priorityScenario(h,false,true);}
}
