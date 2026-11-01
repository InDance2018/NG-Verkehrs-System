package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;

@GameTestHolder(value=NgTraffic.ID,namespace=NgTraffic.ID)
public final class TrafficGameTests {
    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void expandedSignsHaveItemsAndAllHorizontalStates(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,1,1));
        for(String id:SignExpansion.IDS){
            var block=NgTraffic.CONTENT.get(id).get();
            h.assertTrue(block.asItem()!=net.minecraft.world.item.Items.AIR,"Missing sign item: "+id);
            for(var facing:net.minecraft.core.Direction.Plane.HORIZONTAL){
                var state=block.defaultBlockState().setValue(OrientedBlock.FACING,facing);
                level.setBlock(pos,state,3);
                var stored=net.minecraft.nbt.NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK),net.minecraft.nbt.NbtUtils.writeBlockState(state));
                h.assertTrue(stored.equals(state),"State roundtrip lost orientation: "+id);
                h.assertTrue(state.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90).getValue(OrientedBlock.FACING)==facing.getClockWise(),"Sign rotation failed: "+id);
                h.assertTrue(!state.getShape(level,pos).isEmpty(),"Sign has no selection shape: "+id);
            }
        }
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void roadSystemSharedModesAndOfflineFallback(GameTestHelper h){
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));var sp=cp.east();var pp=cp.east(2);level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);level.setBlock(sp,NgTraffic.VARIABLE_SIGN.get().defaultBlockState(),3);level.setBlock(pp,NgTraffic.PARKING_BOARD.get().defaultBlockState(),3);
        var controller=(RoadSystemEntity)level.getBlockEntity(cp);var sign=(RoadSystemEntity)level.getBlockEntity(sp);var parking=(RoadSystemEntity)level.getBlockEntity(pp);h.assertTrue(controller.attach(sign)&&controller.attach(parking),"Shared display linking failed");var d=controller.data();d.mode=1;d.speed=80;d.capacity=120;d.occupied=30;controller.configure(d);
        h.runAfterDelay(12,()->{h.assertTrue(sign.online()&&sign.data().mode==1&&sign.data().speed==80&&parking.data().free()==90,"Displays did not receive shared state");level.setBlock(cp,Blocks.AIR.defaultBlockState(),3);level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);});
        h.runAfterDelay(25,()->{h.assertTrue(!sign.online()&&!parking.online(),"Old endpoints adopted a replacement controller");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void parkingCountersClampDeduplicateAndPersist(GameTestHelper h){
        var cp=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);var c=(RoadSystemEntity)level.getBlockEntity(cp);var d=c.data();d.capacity=2;c.configure(d);var vehicle=java.util.UUID.randomUUID();
        h.assertTrue(c.countPassage(vehicle,1,100),"Entry rejected");h.assertTrue(!c.countPassage(vehicle,1,101)&&c.data().occupied==1,"Duplicate entry counted");c.countPassage(java.util.UUID.randomUUID(),1,102);c.countPassage(java.util.UUID.randomUUID(),1,103);h.assertTrue(c.data().occupied==2,"Capacity exceeded");c.countPassage(vehicle,-1,104);h.assertTrue(c.data().occupied==1,"Exit did not decrement");
        var restored=new RoadSystemEntity(cp,c.getBlockState());restored.load(c.saveWithoutMetadata());h.assertTrue(restored.data().capacity==2&&restored.data().occupied==1,"Parking count not persisted");c.countPassage(java.util.UUID.randomUUID(),-1,105);c.countPassage(java.util.UUID.randomUUID(),-1,106);h.assertTrue(c.data().occupied==0,"Negative occupancy");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void parkingSensorRealBoatForwardCrossingOnly(GameTestHelper h){
        var level=h.getLevel();var gate=h.absolutePos(new BlockPos(2,1,2));var cp=gate.east(2);level.setBlock(gate.below(),Blocks.STONE.defaultBlockState(),3);level.setBlock(gate,NgTraffic.ENTRY_SENSOR.get().defaultBlockState(),3);level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);var c=(RoadSystemEntity)level.getBlockEntity(cp);var sensor=(RoadSystemEntity)level.getBlockEntity(gate);c.attach(sensor);
        var boat=new net.minecraft.world.entity.vehicle.Boat(level,gate.getX()+.5,gate.getY()+.2,gate.getZ()-.25);level.addFreshEntity(boat);
        h.runAfterDelay(2,()->{boat.setPos(gate.getX()+.5,gate.getY()+.2,gate.getZ()-.25);sensor.tick();h.assertTrue(c.data().occupied==0,"Stationary newly observed boat counted");});
        h.runAfterDelay(3,()->{boat.setPos(gate.getX()+.5,gate.getY()+.2,gate.getZ()+1.25);sensor.tick();h.assertTrue(c.data().occupied==1,"Actual forward boat crossing not counted");});
        h.runAfterDelay(4,()->{boat.setPos(gate.getX()+.5,gate.getY()+.2,gate.getZ()-.25);sensor.tick();h.assertTrue(c.data().occupied==1,"Reverse crossing counted");boat.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void parkingVehicleFilterAndManualCorrection(GameTestHelper h){
        var level=h.getLevel();var player=h.makeMockPlayer();var boat=new net.minecraft.world.entity.vehicle.Boat(level,0,1,0);h.assertTrue(!RoadSystemEntity.vehicle(player)&&RoadSystemEntity.vehicle(boat),"Foot players included or boats excluded");
        var cp=h.absolutePos(new BlockPos(1,1,1));level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);var c=(RoadSystemEntity)level.getBlockEntity(cp);var data=c.data();data.capacity=500;data.occupied=250;c.configure(data);h.assertTrue(c.data().free()==250,"Manual correction failed");data.occupied=9999;c.configure(data);h.assertTrue(c.data().occupied==500,"Manual correction overflow");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void roadSystemMenuPermissionsAndStaleEdit(GameTestHelper h){
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);var c=(RoadSystemEntity)level.getBlockEntity(cp);var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);var first=new RoadSystemMenu(1,player.getInventory(),c);var second=new RoadSystemMenu(2,player.getInventory(),c);var d=c.data();d.mode=3;h.assertTrue(first.save(player,0,d),"Valid controller edit rejected");d.mode=4;h.assertTrue(!second.save(player,0,d)&&c.data().mode==3,"Concurrent edit overwrote state");var next=new RoadSystemMenu(3,player.getInventory(),c);player.getAbilities().mayBuild=false;h.assertTrue(!next.save(player,c.revision(),d),"No-build player edited controller");player.getAbilities().mayBuild=true;player.setPos(cp.getX()+20,cp.getY(),cp.getZ());h.assertTrue(!next.save(player,c.revision(),d),"Distant controller edit permitted");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void roadSystemEndpointCannotForgeSharedOccupancy(GameTestHelper h){
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));var pp=cp.east();level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);level.setBlock(pp,NgTraffic.PARKING_GUIDE.get().defaultBlockState(),3);var c=(RoadSystemEntity)level.getBlockEntity(cp);var p=(RoadSystemEntity)level.getBlockEntity(pp);c.attach(p);var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(pp.getX()+.5,pp.getY()+.5,pp.getZ()+.5);var menu=new RoadSystemMenu(1,player.getInventory(),p);var data=p.data();data.name="Parkhaus";data.capacity=90000;data.occupied=89999;data.mode=4;h.assertTrue(menu.save(player,p.revision(),data),"Endpoint name save failed");h.assertTrue(p.data().name.equals("Parkhaus")&&p.data().capacity==100&&p.data().occupied==0&&c.data().occupied==0,"Endpoint forged central state");p.detach();h.assertTrue(c.deviceCount()==0&&!p.online(),"Detach left controller link");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void roadSystemLinksPersistAndPacketRoundtrip(GameTestHelper h){
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));var sp=cp.east();level.setBlock(cp,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);level.setBlock(sp,NgTraffic.LANE_BOARD.get().defaultBlockState(),3);var c=(RoadSystemEntity)level.getBlockEntity(cp);var s=(RoadSystemEntity)level.getBlockEntity(sp);c.attach(s);var ct=c.saveWithoutMetadata();var st=s.saveWithoutMetadata();c.load(ct);s.load(st);h.assertTrue(c.deviceCount()==1&&s.parent().equals(cp),"Saved controller connections lost");
        var d=new RoadSystemData();d.name="Parkhaus Düsseldorf";d.capacity=400;d.occupied=153;d.mode=3;var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());RoadSystemMenu.writeData(d,b);var read=RoadSystemMenu.readData(b);b.release();h.assertTrue(read.same(d),"System packet lost text or counters");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void gantryAndAdditionalWayfindingRegisteredAndRotatable(GameTestHelper h){
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,1,1));
        for(String id:java.util.List.of("bridge_support","bridge_crossbeam","bridge_hanger")){var block=NgTraffic.CONTENT.get(id).get();var state=block.defaultBlockState();h.assertTrue(!state.getShape(level,pos).isEmpty()&&state.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90).getValue(OrientedBlock.FACING)==net.minecraft.core.Direction.EAST,"Gantry shape/rotation invalid");}
        for(var block:java.util.List.of(NgTraffic.EXIT_GUIDE.get(),NgTraffic.DISTANCE_GUIDE.get(),NgTraffic.DETOUR_GUIDE.get())){level.setBlock(pos,block.defaultBlockState(),3);h.assertTrue(level.getBlockEntity(pos) instanceof WayfindingEntity,"Additional guide missing text entity");}h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void wayfindingDataPersistenceAndUpdateTag(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();level.setBlock(pos,NgTraffic.MOTORWAY_GUIDE.get().defaultBlockState(),3);
        var sign=(WayfindingEntity)level.getBlockEntity(pos);var data=new WayfindingData();data.targets[0]="Düsseldorf";data.targets[1]="Moers";data.targets[2]="Duisburg";data.distances[0]="25 km";data.directions[0]=2;data.laneCount=4;data.lanes[3]=4;sign.update(data);
        var restored=new WayfindingEntity(pos,sign.getBlockState());restored.load(sign.saveWithoutMetadata());
        h.assertTrue(restored.data().targets[0].equals("Düsseldorf")&&restored.data().targets[2].equals("Duisburg")&&restored.data().distances[0].equals("25 km")&&restored.data().laneCount==4&&restored.data().lanes[3]==4&&restored.revision()==1,"Sign NBT lost content / lanes / revision");
        restored.load(sign.getUpdateTag());h.assertTrue(restored.data().directions[0]==2,"Tracking update lost destination direction");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void wayfindingPacketUnicodeAndBounds(GameTestHelper h){
        var data=new WayfindingData();data.targets[0]="Köln / Düsseldorf";data.targets[1]="😀".repeat(48);data.laneCount=255;data.lanes[0]=-5;data.distances[2]="999999999999999";
        var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());WayfindingNetwork.writeData(data,b);var restored=WayfindingNetwork.readData(b);b.release();
        h.assertTrue(restored.targets[0].equals(data.targets[0])&&restored.targets[1].equals(data.targets[1])&&restored.laneCount==4&&restored.lanes[0]==0&&restored.distances[2].length()==12,"Packet roundtrip or input limits failed");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void wayfindingEditorStaleSaveAndPermissions(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();level.setBlock(pos,NgTraffic.ROAD_GUIDE.get().defaultBlockState(),3);var sign=(WayfindingEntity)level.getBlockEntity(pos);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);
        var first=new WayfindingMenu(1,player.getInventory(),sign);var stale=new WayfindingMenu(2,player.getInventory(),sign);var data=new WayfindingData();data.targets[0]="Moers";
        h.assertTrue(first.save(player,0,data),"Valid editor save rejected");data.targets[0]="Overwrite";h.assertTrue(!stale.save(player,0,data)&&sign.data().targets[0].equals("Moers"),"Stale editor overwrote newer content");
        var current=new WayfindingMenu(3,player.getInventory(),sign);player.getAbilities().mayBuild=false;h.assertTrue(!current.save(player,1,data),"Read-only player changed sign");player.getAbilities().mayBuild=true;player.setPos(pos.getX()+20,pos.getY(),pos.getZ());h.assertTrue(!current.save(player,1,data),"Remote sign edit allowed");
        player.setPos(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5);level.setBlock(pos,NgTraffic.TOWN_SIGN.get().defaultBlockState(),3);h.assertTrue(!current.save(player,1,data),"Replaced sign accepted old editor");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void wayfindingTypesOrientationDropsAndMenuSnapshot(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(1,1,1));var level=h.getLevel();var player=h.makeMockPlayer();
        for(var block:java.util.List.of(NgTraffic.TOWN_SIGN.get(),NgTraffic.TOWN_EXIT_SIGN.get(),NgTraffic.ROAD_GUIDE.get(),NgTraffic.MOTORWAY_GUIDE.get())){
            level.setBlock(pos,block.defaultBlockState(),3);var sign=(WayfindingEntity)level.getBlockEntity(pos);var data=new WayfindingData();data.targets[0]="Testort";sign.update(data);
            var b=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());WayfindingMenu.writeSnapshot(b,sign);var menu=new WayfindingMenu(7,player.getInventory(),b);b.release();h.assertTrue(menu.pos.equals(pos)&&menu.kind==sign.kind()&&menu.revision==1&&menu.snapshot.targets[0].equals("Testort"),"Menu snapshot incorrect");
            var rotated=block.defaultBlockState().rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);h.assertTrue(rotated.getValue(OrientedBlock.FACING)==net.minecraft.core.Direction.EAST,"Sign rotation failed");
            var drops=net.minecraft.world.level.block.Block.getDrops(level.getBlockState(pos),level,pos,sign);h.assertTrue(drops.size()==1&&drops.get(0).is(block.asItem()),"Sign loot missing or duplicated");
        }h.succeed();
    }
    private static boolean selectMenu(ControllerMenu menu,net.minecraft.world.entity.player.Player player,int index) {
        int revision=menu.revision();int[] actions={25,(revision>>>21)&127,(revision>>>14)&127,(revision>>>7)&127,revision&127,8+index/ControllerMenu.PAGE_SIZE,ControllerMenu.ROW_ACTION+index%ControllerMenu.PAGE_SIZE};
        for(int action:actions){
            var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(menu.containerId,action).write(buffer);
            var packet=new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(buffer);buffer.release();
            if(!menu.clickMenuButton(player,packet.getButtonId()))return false;
        }
        return true;
    }
    private static void deliverMenu(ControllerMenu server,ControllerMenu client) {
        for(int i=0;i<=ControllerMenu.COMMIT;i++)client.setData(i,(short)server.value(i));
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=100)
    public static void worksiteLightsModesPermissionsAndScheduledBlink(GameTestHelper h) {
        var level=h.getLevel();var lampPos=h.absolutePos(new BlockPos(1,1,1));var arrowPos=lampPos.east(2);var steadyPos=lampPos.south(2);
        var lamp=(WorksiteBlock)NgTraffic.WORK_LAMP.get();var board=(WorksiteBlock)NgTraffic.WORK_ARROW.get();
        level.setBlock(lampPos,lamp.defaultBlockState(),3);level.setBlock(arrowPos,board.defaultBlockState(),3);
        level.setBlock(steadyPos,lamp.defaultBlockState().setValue(WorksiteBlock.MODE,1),3);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
        var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(arrowPos),net.minecraft.core.Direction.NORTH,arrowPos,false);
        board.use(level.getBlockState(arrowPos),level,arrowPos,player,hand,hit);h.assertTrue(level.getBlockState(arrowPos).getValue(WorksiteBlock.MODE)==2,"Arrow did not select right");
        board.use(level.getBlockState(arrowPos),level,arrowPos,player,hand,hit);h.assertTrue(level.getBlockState(arrowPos).getValue(WorksiteBlock.MODE)==0&&!level.getBlockState(arrowPos).getValue(WorksiteBlock.LIT),"Arrow off failed");
        player.getAbilities().mayBuild=false;board.use(level.getBlockState(arrowPos),level,arrowPos,player,hand,hit);h.assertTrue(level.getBlockState(arrowPos).getValue(WorksiteBlock.MODE)==0,"Read-only player changed board");
        player.getAbilities().mayBuild=true;board.use(level.getBlockState(arrowPos),level,arrowPos,player,hand,hit);
        var saved=net.minecraft.nbt.NbtUtils.writeBlockState(level.getBlockState(arrowPos));var restored=net.minecraft.nbt.NbtUtils.readBlockState(level.holderLookup(net.minecraft.core.registries.Registries.BLOCK),saved);
        h.assertTrue(restored.getValue(WorksiteBlock.MODE)==1&&restored.getValue(OrientedBlock.FACING)==net.minecraft.core.Direction.NORTH,"Board blockstate persistence failed");
        boolean[] seen=new boolean[4];
        for(int delay=5;delay<=35;delay+=5)h.runAfterDelay(delay,()->{
            seen[level.getBlockState(lampPos).getValue(WorksiteBlock.LIT)?0:1]=true;seen[level.getBlockState(arrowPos).getValue(WorksiteBlock.LIT)?2:3]=true;
            h.assertTrue(level.getBlockState(steadyPos).getValue(WorksiteBlock.LIT),"Steady light blinked");
        });
        h.runAfterDelay(36,()->{for(boolean observed:seen)h.assertTrue(observed,"Scheduled lamp/arrow did not alternate");
            lamp.use(level.getBlockState(lampPos),level,lampPos,player,hand,hit);h.assertTrue(!level.getBlockState(lampPos).getValue(WorksiteBlock.LIT),"Lamp off failed");
        });
        h.runAfterDelay(45,()->{h.assertTrue(!level.getBlockState(lampPos).getValue(WorksiteBlock.LIT),"Pending tick restarted switched-off lamp");h.succeed();});
    }
    @GameTest(template="ng_verkehr:auto_test",timeoutTicks=100)
    public static void mobileSignalHeadLinkingCycleAndSingleEndpoint(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.east(2);var np=center.north(8);var sp=center.south(8);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var d=(DeviceEntity)level.getBlockEntity(cp);
        level.setBlock(np,NgTraffic.MOBILE_SIGNAL.get().defaultBlockState(),3);level.setBlock(sp,NgTraffic.MOBILE_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.SOUTH),3);
        h.assertTrue(level.getBlockState(np.above()).getBlock() instanceof MobileSignalBlock&&level.getBlockEntity(np.above())==null,"Mobile head missing or duplicate endpoint created");
        var lowerDrops=net.minecraft.world.level.block.Block.getDrops(level.getBlockState(np),level,np,level.getBlockEntity(np));
        var upperDrops=net.minecraft.world.level.block.Block.getDrops(level.getBlockState(np.above()),level,np.above(),null);
        h.assertTrue(lowerDrops.size()==1&&lowerDrops.get(0).is(NgTraffic.MOBILE_SIGNAL.get().asItem())&&upperDrops.isEmpty(),"Mobile signal drops duplicate / missing item");
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;var hand=net.minecraft.world.InteractionHand.MAIN_HAND;player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(NgTraffic.SIGNAL_LINKER.get()));
        for(var pos:java.util.List.of(cp,np.above())){
            var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.NORTH,pos,false);
            h.assertTrue(NgTraffic.SIGNAL_LINKER.get().useOn(new net.minecraft.world.item.context.UseOnContext(player,hand,hit)).consumesAction(),"Linking by upper signal head failed");
        }
        d.setAutoCenter(center);d.scanSignals();h.assertTrue(d.devices().size()==2,"Head registered as a second device / opposing mobile not recognized");
        var north=(SignalEntity)level.getBlockEntity(np);var south=(SignalEntity)level.getBlockEntity(sp);
        h.assertTrue(north.approach()==0&&south.approach()==2&&north.automatic()&&south.automatic(),"Mobile auto orientation failed");
        d.setAutoEnabled(false);d.changeSetting(0,2);d.setEnabled(true);north.tick();south.tick();
        h.assertTrue(level.getBlockState(np.above()).getValue(SignalBlock.LIGHT)==1&&level.getBlockState(sp.above()).getValue(SignalBlock.LIGHT)==0,"Worksite groups/head mirroring failed");
        h.runAfterDelay(21,()->{north.tick();south.tick();h.assertTrue(level.getBlockState(np.above()).getValue(SignalBlock.LIGHT)==2&&level.getBlockState(sp.above()).getValue(SignalBlock.LIGHT)==0,"Mobile green or conflict exclusion failed");
            d.disconnectDevice(np);north.tick();h.assertTrue(level.getBlockState(np.above()).getValue(SignalBlock.LIGHT)==0,"Disconnected mobile head stayed lit");
            level.destroyBlock(np.above(),false);h.assertTrue(level.getBlockState(np).isAir(),"Breaking head left base / connection behind");d.setEnabled(false);h.succeed();});
    }
    @GameTest(template="ng_verkehr:auto_test")
    public static void overheadRecognitionAndParallelPedestrianFallback(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.offset(2,0,0);var sp=center.offset(0,9,-8);var pp=center.offset(-3,1,-7);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        level.setBlock(sp,NgTraffic.SIGNAL_STRAIGHT.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.NORTH),3);
        level.setBlock(sp.above(),NgTraffic.CONTENT.get("signal_boom_end").get().defaultBlockState(),3);
        level.setBlock(pp,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);
        var d=(DeviceEntity)level.getBlockEntity(cp);var signal=(SignalEntity)level.getBlockEntity(sp);signal.link(cp,0);d.setAutoAnchor(signal);d.setAutoCenter(center);d.scanSignals();
        h.assertTrue(signal.automatic()&&signal.approach()==0&&((PedestrianEntity)level.getBlockEntity(pp)).linkedTo(cp),"Overhead signal / crossing not recognized");
        d.setAutoEnabled(false);d.changeSetting(1,5);d.changeSetting(4,1);d.setEnabled(true);d.requestPedestrians(0);
        // Place the running cycle at the beginning of its requested walk phase.
        var state=d.saveWithoutMetadata();long now=level.getGameTime();state.putLong("Epoch",now-180);d.load(state);d.tick();
        h.runAfterDelay(40,()->{
            signal.tick();h.assertTrue(d.pedestrianGreen(0)&&d.signalLight(signal)==2,"Compatible straight / walk did not run together");
            var unknown=center.offset(1,1,-8);level.setBlock(unknown,NgTraffic.SIGNAL.get().defaultBlockState(),3);((SignalEntity)level.getBlockEntity(unknown)).link(cp,0);
            signal.tick();h.assertTrue(d.signalLight(signal)==0,"Unknown circle signal failed to cancel parallel walk");
            d.setEnabled(false);h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=200)
    public static void programsSettingsPresetsNightAndSafeRestart(GameTestHelper h) {
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));var sp=cp.offset(2,0,0);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);level.setBlock(sp,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        var d=(DeviceEntity)level.getBlockEntity(cp);var signal=(SignalEntity)level.getBlockEntity(sp);signal.link(cp,0);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var menu=new ControllerMenu(71,player.getInventory(),d,d.devices());
        for(int a:new int[]{26,1,15,26,2,7,26,3,12,26,4,1,26,7,0})h.assertTrue(menu.clickMenuButton(player,a),"Settings packet rejected");
        d.changeSetting(1,25);d.loadPreset(0);h.assertTrue(d.setting(1)==15&&d.setting(2)==7&&d.setting(3)==12&&d.setting(4)==1&&!d.isEnabled(),"Preset restore failed");
        var saved=d.saveWithoutMetadata();d.load(saved);h.assertTrue(d.setting(1)==15&&d.presetPresent(0),"Program settings lost on save/load");
        d.changeSetting(5,1);d.setEnabled(true);signal.tick();
        h.assertTrue(d.nightActive()&&(signal.getBlockState().getValue(SignalBlock.LIGHT)==3||signal.getBlockState().getValue(SignalBlock.LIGHT)==4)&&!d.requestPedestrians(0),"Night mode accepted walk/failed blink");
        d.changeSetting(5,0);d.changeSetting(6,1);level.setDayTime(14000);d.setEnabled(true);signal.tick();h.assertTrue(d.nightActive(),"Automatic night did not start");
        level.setDayTime(1000);signal.tick();h.assertTrue(d.phase()==7&&signal.getBlockState().getValue(SignalBlock.LIGHT)==0,"Night return skipped all-red");
        h.runAfterDelay(40,()->{signal.tick();h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==1,"Night return skipped red-yellow");
            d.changeSetting(6,0);d.changeSetting(0,2);signal.assignRoute(2,0);d.setEnabled(true);signal.tick();
            h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==0,"Opposite construction end released as group A");
            signal.assignRoute(0,0);signal.tick();h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==1,"Construction group A failed");
            d.setEnabled(false);h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty")
    public static void roadPartGeometryRotationAndRailConnection(GameTestHelper h) {
        var level=h.getLevel();var pos=h.absolutePos(new BlockPos(1,1,1));
        for(var entry:NgTraffic.CONTENT.entrySet())if(entry.getValue().get() instanceof RoadPartBlock part){
            for(var facing:net.minecraft.core.Direction.Plane.HORIZONTAL){var state=part.defaultBlockState().setValue(OrientedBlock.FACING,facing);var shape=state.getShape(level,pos);
                h.assertTrue(!shape.isEmpty()&&shape.bounds().getYsize()>0,"Missing physical road geometry: "+entry.getKey());}
        }
        var rail=NgTraffic.CONTENT.get("guardrail").get();var end=NgTraffic.CONTENT.get("guardrail_end").get();
        level.setBlock(pos,rail.defaultBlockState(),3);level.setBlock(pos.east(),end.defaultBlockState(),3);
        h.assertTrue(level.getBlockState(pos).getValue(net.minecraft.world.level.block.FenceBlock.EAST),"Rail did not connect to end piece");
        var boom=NgTraffic.CONTENT.get("signal_boom").get();var north=boom.defaultBlockState().getShape(level,pos).bounds();var east=boom.defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST).getShape(level,pos).bounds();
        h.assertTrue(north.getZsize()>north.getXsize()&&east.getXsize()>east.getZsize(),"Boom rotation differs from model");h.succeed();
    }
    @GameTest(template="ng_verkehr:auto_test")
    public static void liveMenuUpdatesWithoutReopeningAndKeepsDeviceIdentity(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.offset(2,0,0);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var controller=(DeviceEntity)level.getBlockEntity(cp);
        var north=center.offset(0,2,-8);var east=center.offset(8,2,0);var south=center.offset(0,2,8);
        var positions=java.util.List.of(north,east,south);var facings=new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH};
        for(int i=0;i<3;i++)level.setBlock(positions.get(i),NgTraffic.SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,facings[i]),3);
        var seed=(SignalEntity)level.getBlockEntity(north);seed.link(cp,0);controller.setAutoAnchor(seed);controller.setAutoCenter(center);controller.scanSignals();
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var menu=new ControllerMenu(60,player.getInventory(),controller,controller.devices());
        var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());ControllerMenu.writeSnapshot(buffer,controller,menu.positions);var client=new ControllerMenu(60,player.getInventory(),buffer);buffer.release();deliverMenu(menu,client);
        selectMenu(menu,player,menu.positions.indexOf(east));int oldRevision=menu.revision();
        controller.disconnectDevice(north);menu.broadcastChanges();
        h.assertTrue(menu.positions.size()==2&&menu.positions.get(menu.value(1)).equals(east),"Deleting an earlier row changed the selected device");
        // The old rendered row/revision cannot select the shifted neighbour.
        for(int action:new int[]{25,(oldRevision>>>21)&127,(oldRevision>>>14)&127,(oldRevision>>>7)&127,oldRevision&127})menu.clickMenuButton(player,action);
        h.assertTrue(!menu.clickMenuButton(player,8)&&!menu.clickMenuButton(player,ControllerMenu.ROW_ACTION),"Stale row selection accepted");
        h.assertTrue(menu.positions.get(menu.value(1)).equals(east),"Rejected stale click changed selection");
        h.assertTrue(menu.clickMenuButton(player,20)&&menu.positions.size()==1&&menu.value(1)==-1,"Disconnect did not remove row/clear selection immediately");
        int southApproach=((SignalEntity)level.getBlockEntity(south)).approach();h.assertTrue(!menu.clickMenuButton(player,11)&&((SignalEntity)level.getBlockEntity(south)).approach()==southApproach,"Old detail action modified the next device");
        for(int i=0;i<ControllerMenu.COMMIT;i++)client.setData(i,(short)menu.value(i));
        h.assertTrue(client.positions.size()==3,"Client published a partial list transaction");
        client.setData(ControllerMenu.COMMIT,(short)menu.value(ControllerMenu.COMMIT));h.assertTrue(client.positions.equals(menu.positions)&&client.value(1)==-1&&client.revision()==menu.revision(),"Atomic client snapshot failed");
        menu.clickMenuButton(player,24);deliverMenu(menu,client);h.assertTrue(menu.positions.size()==3&&client.positions.equals(menu.positions),"Rediscovery required reopening");
        var footPos=center.offset(-5,2,5);var buttonPos=footPos.below(2);var warningPos=center.offset(1,2,1);
        level.setBlock(footPos,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);level.setBlock(buttonPos,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);level.setBlock(warningPos,NgTraffic.WARNING_SIGNAL.get().defaultBlockState(),3);controller.scanSignals();menu.broadcastChanges();deliverMenu(menu,client);
        h.assertTrue(client.positions.size()==6&&client.deviceType(client.positions.indexOf(buttonPos))==2&&client.deviceType(client.positions.indexOf(warningPos))==13,"New device types did not appear in open menu");
        menu.clickMenuButton(player,21);menu.clickMenuButton(player,22);deliverMenu(menu,client);h.assertTrue(client.positions.isEmpty()&&client.value(1)==-1,"Full reset left stale rows");
        menu.clickMenuButton(player,24);deliverMenu(menu,client);h.assertTrue(client.positions.size()==6,"Repopulation from empty menu failed");
        // Interleaved discovery must consume the complete selection frame,
        // never interpret remaining revision digits as reset/start commands.
        menu.clickMenuButton(player,25);menu.clickMenuButton(player,0);controller.disconnectDevice(north);menu.broadcastChanges();
        menu.clickMenuButton(player,21);menu.clickMenuButton(player,22);menu.clickMenuButton(player,0);
        h.assertTrue(!menu.clickMenuButton(player,8)&&menu.value(14)==0&&!controller.isEnabled()&&menu.positions.size()==5,"Interleaved selection frame executed an unrelated action");
        controller.setAutoEnabled(false);h.succeed();
    }
    @GameTest(template="ng_verkehr:empty")
    public static void liveMenuCoordinateTransportPreservesFullSignedPositions(GameTestHelper h) {
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var controller=(DeviceEntity)level.getBlockEntity(cp);var player=h.makeMockPlayer();
        var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());ControllerMenu.writeSnapshot(buffer,controller,java.util.List.of());var client=new ControllerMenu(61,player.getInventory(),buffer);buffer.release();
        var expected=new BlockPos(-29999999,-64,29999999);int[] xyz={expected.getX(),expected.getY(),expected.getZ()};
        for(int axis=0;axis<3;axis++){client.setData(ControllerMenu.HEADER+2+axis*2,(short)xyz[axis]);client.setData(ControllerMenu.HEADER+3+axis*2,(short)(xyz[axis]>>>16));}
        client.setData(ControllerMenu.COUNT,1);client.setData(ControllerMenu.REV_LOW,(short)50000);client.setData(ControllerMenu.REV_HIGH,123);
        h.assertTrue(client.positions.isEmpty(),"Uncommitted coordinates visible");client.setData(ControllerMenu.COMMIT,1);
        h.assertTrue(client.positions.equals(java.util.List.of(expected))&&client.revision()==((123<<16)|50000),"Signed-short transport truncated position/revision");
        h.succeed();
    }
    @GameTest(template="ng_verkehr:auto_test")
    public static void disconnectEveryKindPersistsExclusionsAndAllowsExplicitRelink(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.offset(2,0,0);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var controller=(DeviceEntity)level.getBlockEntity(cp);
        var positions=new java.util.ArrayList<BlockPos>();int index=0;
        for(var kind:SignalKind.values()){var pos=center.offset(index++*2-10,2,-12);level.setBlock(pos,NgTraffic.CONTENT.get(kind.id).get().defaultBlockState(),3);positions.add(pos);}
        var pp=center.offset(-4,2,4);var tp=pp.below(2);level.setBlock(pp,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);level.setBlock(tp,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);positions.add(pp);positions.add(tp);
        var seed=(SignalEntity)level.getBlockEntity(positions.get(0));seed.link(cp,0);controller.setAutoAnchor(seed);controller.setAutoCenter(center);controller.scanSignals();
        h.assertTrue(controller.devices().size()==SignalKind.values().length+2,"Expected every signal kind and both pedestrian types");
        var oldSignal=seed.saveWithoutMetadata();
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var menu=new ControllerMenu(50,player.getInventory(),controller,controller.devices());
        for(int i=0;i<positions.size();i++){
            var pos=positions.get(i);controller.setEnabled(true);int row=menu.positions.indexOf(pos);
            h.assertTrue(selectMenu(menu,player,row)&&menu.clickMenuButton(player,20),"Single disconnect failed: "+i);
            var be=level.getBlockEntity(pos);boolean connected=be instanceof SignalEntity signal?signal.hasController():((PedestrianEntity)be).hasController();
            h.assertTrue(!connected&&!controller.isEnabled(),"Disconnect must clear endpoint and stop program");
            h.assertTrue(be instanceof SignalEntity?level.getBlockState(pos).getValue(SignalBlock.LIGHT)==0:!level.getBlockState(pos).getValue(PedestrianBlock.ACTIVE),"Disconnected device not safe");
            controller.scanSignals();h.assertTrue(!controller.devices().contains(pos)&&controller.devices().size()==positions.size()-1-i,"Automatic scan reattached disconnected device");
        }
        var saved=controller.saveWithoutMetadata();controller.load(saved);seed.load(oldSignal);seed.tick();
        h.assertTrue(!seed.hasController()&&controller.devices().isEmpty(),"Persisted single-device revocation failed on stale endpoint load");
        controller.scanSignals();h.assertTrue(controller.devices().isEmpty(),"Saved disconnection exclusions lost");
        var op=center.offset(3,0,0);level.setBlock(op,NgTraffic.CONTROLLER.get().defaultBlockState(),3);seed.link(op,0);
        selectMenu(menu,player,menu.positions.indexOf(positions.get(0)));
        h.assertTrue(!menu.clickMenuButton(player,20)&&seed.linkedTo(op),"Stale menu disconnected another controller's signal");
        var right=(SignalEntity)level.getBlockEntity(positions.get(2));right.link(cp,0);
        h.assertTrue(right.linkedTo(cp)&&controller.devices().size()==1,"Explicit manual relink did not release exclusion");
        controller.rediscoverDisconnected();h.assertTrue(controller.devices().size()==positions.size()-1&&seed.linkedTo(op)&&!controller.isEnabled(),"Explicit rediscovery lost safe state or stole foreign signal");
        player.getAbilities().mayBuild=false;h.assertTrue(!menu.clickMenuButton(player,20)&&!menu.clickMenuButton(player,21)&&!menu.clickMenuButton(player,24),"Disconnect controls bypassed permissions");
        controller.setAutoEnabled(false);h.succeed();
    }
    @GameTest(template="ng_verkehr:auto_test",timeoutTicks=150)
    public static void resetRequiresConfirmationAndInvalidatesLateLoadedLinks(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.offset(2,0,0);var ap=center.offset(0,2,-8);var wp=center.offset(0,2,0);var pp=center.offset(8,2,0);var tp=pp.below(2);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);level.setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);level.setBlock(wp,NgTraffic.WARNING_SIGNAL.get().defaultBlockState(),3);level.setBlock(pp,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);level.setBlock(tp,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);
        var controller=(DeviceEntity)level.getBlockEntity(cp);var signal=(SignalEntity)level.getBlockEntity(ap);var pedestrian=(PedestrianEntity)level.getBlockEntity(pp);var button=(PedestrianEntity)level.getBlockEntity(tp);
        signal.link(cp,0);controller.setAutoAnchor(signal);controller.setAutoCenter(center);controller.scanSignals();
        var oldSignal=signal.saveWithoutMetadata();var oldPedestrian=pedestrian.saveWithoutMetadata();var oldButton=button.saveWithoutMetadata();long oldGeneration=controller.connectionGeneration();
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var menu=new ControllerMenu(51,player.getInventory(),controller,controller.devices());
        h.assertTrue(!menu.clickMenuButton(player,22)&&controller.devices().size()==4,"Unconfirmed reset accepted");
        menu.clickMenuButton(player,21);h.assertTrue(menu.value(14)==1,"Confirmation not armed");menu.clickMenuButton(player,23);h.assertTrue(!menu.clickMenuButton(player,22)&&controller.devices().size()==4,"Cancel failed");
        menu.clickMenuButton(player,21);
        h.runAfterDelay(101,()->{
            h.assertTrue(!menu.clickMenuButton(player,22)&&controller.devices().size()==4,"Expired confirmation accepted");
            controller.setEnabled(true);controller.requestPedestrians(1);int chunks=level.getChunkSource().getLoadedChunksCount();
            menu.clickMenuButton(player,21);h.assertTrue(menu.clickMenuButton(player,22),"Confirmed reset failed");
            h.assertTrue(!menu.clickMenuButton(player,22),"Same confirmation reused");
            h.assertTrue(controller.devices().isEmpty()&&!controller.autoEnabled()&&!controller.isEnabled()&&!controller.pedestrianBusy()&&controller.connectionGeneration()!=oldGeneration,"Reset did not clear connections, queues or discovery");
            h.assertTrue(center.equals(controller.autoCenter())&&controller.autoRadius()==32&&level.getChunkSource().getLoadedChunksCount()==chunks,"Reset changed centre/radius or loaded chunks");
            for(var pos:java.util.List.of(ap,wp))h.assertTrue(level.getBlockState(pos).getValue(SignalBlock.LIGHT)==0,"Reset vehicle/warning not safe");
            for(var pos:java.util.List.of(pp,tp))h.assertTrue(!level.getBlockState(pos).getValue(PedestrianBlock.ACTIVE),"Reset pedestrian/button not safe");
            var saved=controller.saveWithoutMetadata();controller.load(saved);
            signal.load(oldSignal);signal.tick();pedestrian.load(oldPedestrian);pedestrian.tick();button.load(oldButton);
            h.assertTrue(button.controller()==null&&!button.hasController()&&!signal.hasController()&&!pedestrian.hasController()&&controller.devices().isEmpty(),"Old unloaded endpoint reattached after reset/load");
            // Simulate an old endpoint absent from the controller's registered
            // list. Generation invalidation must cover these legacy links too.
            var ghostPos=center.offset(5,2,-7);level.setBlock(ghostPos,NgTraffic.SIGNAL.get().defaultBlockState(),3);var ghost=(SignalEntity)level.getBlockEntity(ghostPos);ghost.load(oldSignal);ghost.tick();
            h.assertTrue(!ghost.hasController()&&!controller.validConnection(ghostPos,oldGeneration),"Unknown old generation survived reset");
            controller.setAutoEnabled(true);controller.scanSignals();h.assertTrue(!signal.hasController()&&!pedestrian.hasController(),"Auto toggle ignored per-device disconnection list");
            controller.rediscoverDisconnected();h.assertTrue(controller.devices().size()==5&&!controller.isEnabled(),"Conscious rediscovery failed after full reset");
            controller.setAutoEnabled(false);h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:auto_test",timeoutTicks=650)
    public static void completeAutomaticSetupLinksEveryKindAndServesBothCrossings(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.offset(2,0,0);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var controller=(DeviceEntity)level.getBlockEntity(cp);
        var signals=new java.util.ArrayList<SignalEntity>();int index=0;
        for(var kind:SignalKind.values()){
            var pos=kind==SignalKind.WARNING?center.offset(0,2,0):center.offset(index*2-10,3,-16);index++;
            level.setBlock(pos,NgTraffic.CONTENT.get(kind.id).get().defaultBlockState(),3);signals.add((SignalEntity)level.getBlockEntity(pos));
        }
        var a1=center.offset(-6,3,-5);var a2=center.offset(-6,3,5);var b1=center.offset(6,3,-5);var b2=center.offset(12,3,-5);
        var lamps=new java.util.ArrayList<PedestrianEntity>();var facings=new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.WEST};
        var lampPositions=java.util.List.of(a1,a2,b1,b2);
        for(int i=0;i<4;i++){var pos=lampPositions.get(i);level.setBlock(pos,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,facings[i]),3);lamps.add((PedestrianEntity)level.getBlockEntity(pos));}
        var ta=center.offset(-6,0,-5);var taSide=center.offset(-5,0,-5);var tb=center.offset(6,0,-5);
        // Button face deliberately differs from its local pedestrian signal.
        for(var pos:java.util.List.of(ta,taSide))level.setBlock(pos,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST).setValue(PedestrianBlock.MOUNT,PedestrianBlock.Mount.MAST),3);
        level.setBlock(tb,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.NORTH).setValue(PedestrianBlock.MOUNT,PedestrianBlock.Mount.MAST),3);
        var buttonA=(PedestrianEntity)level.getBlockEntity(ta);var sideButton=(PedestrianEntity)level.getBlockEntity(taSide);var buttonB=(PedestrianEntity)level.getBlockEntity(tb);
        var seed=signals.get(0);seed.link(cp,0);controller.setAutoAnchor(seed);controller.setAutoCenter(center);controller.scanSignals();
        h.assertTrue(controller.devices().size()==SignalKind.values().length+7,"Automatic list must contain all signal kinds, 4 foot lamps and 3 buttons");
        for(var signal:signals)h.assertTrue(signal.linkedTo(cp)&&signal.automatic(),"Automatic link missing: "+signal.kind());
        for(int i=0;i<lamps.size();i++)h.assertTrue(lamps.get(i).linkedTo(cp)&&lamps.get(i).automatic()&&lamps.get(i).group()==(i<2?0:1),"Pedestrian facing axis wrong");
        h.assertTrue(buttonA.linkedTo(cp)&&sideButton.linkedTo(cp)&&buttonB.linkedTo(cp)&&buttonA.group()==0&&sideButton.group()==0&&buttonB.group()==1,"Mast buttons did not follow nearby crossing independently of mount side");
        var restored=new PedestrianEntity(tb,level.getBlockState(tb));restored.load(buttonB.saveWithoutMetadata());h.assertTrue(restored.linkedTo(cp)&&restored.group()==1&&restored.automatic(),"Automatic button NBT lost");
        controller.setEnabled(true);var saved=controller.saveWithoutMetadata();saved.putLong("Epoch",level.getGameTime()-679);controller.load(saved);
        var player=h.makeMockPlayer();var hand=net.minecraft.world.InteractionHand.MAIN_HAND;player.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
        for(var pos:java.util.List.of(ta,tb)){var state=level.getBlockState(pos);state.getBlock().use(state,level,pos,player,hand,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos),net.minecraft.core.Direction.NORTH,pos,false));}
        h.assertTrue(controller.pedestrianBusy(0)&&controller.pedestrianBusy(1),"Automatically linked buttons did not request both crossings");
        for(int delay:new int[]{45,285})h.runAfterDelay(delay,()->{
            int green=delay==45?0:1;
            for(var lamp:lamps){lamp.tick();h.assertTrue(lamp.getBlockState().getValue(PedestrianBlock.ACTIVE)==(lamp.group()==green),"Wrong automatic pedestrian crossing green");}
            for(var signal:signals){signal.tick();int light=signal.getBlockState().getValue(SignalBlock.LIGHT);
                h.assertTrue(light==(signal.kind()==SignalKind.WARNING?(Math.floorMod(level.getGameTime(),20)<10?3:0):0),"Vehicles must be red and warning must blink during crossing");}
        });
        h.runAfterDelay(545,()->{
            var left=signals.stream().filter(x->x.kind()==SignalKind.LEFT).findFirst().orElseThrow();left.tick();h.assertTrue(left.getBlockState().getValue(SignalBlock.LIGHT)==2,"Protected route did not resume after automatic foot requests");
            buttonA.assignGroup(1);controller.scanSignals();h.assertTrue(buttonA.group()==1&&!buttonA.automatic(),"Manual button override overwritten");
            controller.setEnabled(false);for(var lamp:lamps){lamp.tick();h.assertTrue(!lamp.getBlockState().getValue(PedestrianBlock.ACTIVE),"Stopped pedestrian lamp not red");}
            for(var button:java.util.List.of(buttonA,sideButton,buttonB)){button.tick();h.assertTrue(!button.getBlockState().getValue(PedestrianBlock.ACTIVE),"Stopped button still pending");}
            for(var signal:signals){signal.tick();h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==0,"Stop failed on automatic signal: "+signal.kind());}
            controller.setAutoEnabled(false);h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:auto_test")
    public static void automaticFootButtonsAndWarningsRespectOwnershipAndBuildOrder(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.offset(2,0,0);var op=center.offset(3,0,0);
        for(var pos:java.util.List.of(cp,op))level.setBlock(pos,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        var controller=(DeviceEntity)level.getBlockEntity(cp);var other=(DeviceEntity)level.getBlockEntity(op);
        var seedPos=center.offset(-6,2,-5);level.setBlock(seedPos,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);var seed=(PedestrianEntity)level.getBlockEntity(seedPos);seed.link(cp,1);controller.setAutoAnchor(seed);controller.setAutoCenter(center);
        var foreignWarning=center.offset(5,2,5);level.setBlock(foreignWarning,NgTraffic.WARNING_SIGNAL.get().defaultBlockState(),3);var warning=(SignalEntity)level.getBlockEntity(foreignWarning);warning.link(op,0);other.setAutoAnchor(warning);other.setAutoCenter(center.offset(4,0,0));other.tick();
        var footPos=center.offset(8,2,0);var buttonPos=center.offset(8,0,0);var warnPos=center.offset(1,2,1);
        level.setBlock(footPos,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);level.setBlock(buttonPos,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);level.setBlock(warnPos,NgTraffic.WARNING_SIGNAL.get().defaultBlockState(),3);
        var foot=(PedestrianEntity)level.getBlockEntity(footPos);var button=(PedestrianEntity)level.getBlockEntity(buttonPos);var freeWarning=(SignalEntity)level.getBlockEntity(warnPos);
        controller.scanSignals();other.scanSignals();h.assertTrue(!foot.hasController()&&!button.hasController()&&!freeWarning.hasController(),"Overlapping areas claimed a foot device/warning");
        other.setAutoEnabled(false);controller.scanSignals();h.assertTrue(seed.automatic()&&seed.group()==0,"A pedestrian lamp must work as the first automatic device");
        h.assertTrue(foot.linkedTo(cp)&&button.linkedTo(cp)&&button.group()==1&&freeWarning.linkedTo(cp)&&warning.linkedTo(op),"Universal automatic ownership incorrect");
        // Foreign pedestrian devices are never stolen, just like vehicle lamps.
        foot.link(op,0);button.link(op,0);controller.scanSignals();h.assertTrue(foot.linkedTo(op)&&button.linkedTo(op),"Foreign foot device stolen");
        var earlyPos=center.offset(-12,0,1);level.setBlock(earlyPos,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);var early=(PedestrianEntity)level.getBlockEntity(earlyPos);controller.scanSignals();h.assertTrue(early.linkedTo(cp)&&early.group()==0,"Button built before lamp must use its axis");
        var latePos=earlyPos.above(3);level.setBlock(latePos,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);controller.setEnabled(true);controller.scanSignals();h.assertTrue(early.group()==1&&!controller.isEnabled(),"Automatic button did not adapt to later local foot lamp safely");
        early.assignGroup(0);controller.scanSignals();h.assertTrue(early.group()==0&&!early.automatic(),"Manual crossing correction not preserved");
        // Equidistant A/B lamps use button orientation, never map iteration order.
        var tie=center.offset(0,0,14);var tieA=tie.offset(-1,3,0);var tieB=tie.offset(1,3,0);
        level.setBlock(tieA,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);level.setBlock(tieB,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);level.setBlock(tie,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);controller.scanSignals();
        h.assertTrue(((PedestrianEntity)level.getBlockEntity(tie)).group()==1,"Tied crossings must use button orientation");
        controller.setAutoEnabled(false);h.succeed();
    }
    @GameTest(template="ng_verkehr:auto_test",timeoutTicks=100)
    public static void automaticDiscoveryUsesRadiusGeometryAndPreservesManualLinks(GameTestHelper h) {
        var level=h.getLevel();var center=h.absolutePos(new BlockPos(64,1,64));var cp=center.offset(2,0,0);
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var controller=(DeviceEntity)level.getBlockEntity(cp);
        var north=center.offset(0,2,-8);var south=center.offset(0,2,8);var east=center.offset(8,2,0);var west=center.offset(-8,2,0);
        var positions=java.util.List.of(north,east,south,west);var facing=java.util.List.of(net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.WEST);
        for(int i=0;i<4;i++)level.setBlock(positions.get(i),NgTraffic.SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,facing.get(i)),3);
        var seed=(SignalEntity)level.getBlockEntity(north);seed.link(cp,1);controller.setAutoAnchor(seed);
        h.assertTrue(!controller.autoEnabled()&&controller.autoRadius()==32,"Legacy controller must stay manual; default radius 32");
        controller.setAutoCenter(center);controller.scanSignals();
        for(int i=0;i<4;i++){
            var signal=(SignalEntity)level.getBlockEntity(positions.get(i));
            h.assertTrue(signal.linkedTo(cp)&&signal.automatic()&&signal.approach()==i&&signal.group()==i%2,"Automatic approach wrong: "+i);
        }
        h.assertTrue(!controller.isEnabled(),"Discovery must stop controller");
        var rightPos=center.offset(6,3,5);level.setBlock(rightPos,NgTraffic.SIGNAL_RIGHT.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);
        var right=(SignalEntity)level.getBlockEntity(rightPos);controller.scanSignals();h.assertTrue(right.linkedTo(cp)&&right.approach()==1&&right.movement()==2,"Arrow role lost during discovery");
        var reversed=center.offset(-9,2,0);level.setBlock(reversed,NgTraffic.SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);
        controller.scanSignals();h.assertTrue(!((SignalEntity)level.getBlockEntity(reversed)).hasController()&&controller.autoUnclear()>0,"Inward-facing uncertain lamp must remain unlinked");
        var manual=(SignalEntity)level.getBlockEntity(west);manual.assignRoute(1,2);controller.scanSignals();h.assertTrue(manual.approach()==1&&manual.movement()==2&&!manual.automatic(),"Manual correction overwritten");
        var otherPos=center.offset(3,0,0);level.setBlock(otherPos,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var other=(DeviceEntity)level.getBlockEntity(otherPos);
        var foreignPos=center.offset(4,2,-9);level.setBlock(foreignPos,NgTraffic.SIGNAL.get().defaultBlockState(),3);var foreign=(SignalEntity)level.getBlockEntity(foreignPos);foreign.link(otherPos,1);
        controller.scanSignals();h.assertTrue(foreign.linkedTo(otherPos),"Discovery stole another controller's lamp");
        var pedPos=center.offset(0,2,7);level.setBlock(pedPos,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);controller.scanSignals();h.assertTrue(((PedestrianEntity)level.getBlockEntity(pedPos)).linkedTo(cp),"Foot signal must now join automatic discovery");
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var menu=new ControllerMenu(40,player.getInventory(),controller,controller.devices());
        for(int i=0;i<4;i++){h.assertTrue(menu.clickMenuButton(player,16+i)&&controller.autoRadius()==DeviceEntity.AUTO_RADII[i]&&menu.value(9)==DeviceEntity.AUTO_RADII[i],"Radius button wrong");}
        controller.setAutoRadius(31);h.assertTrue(controller.autoRadius()==48,"Unsupported radius accepted");
        var saved=controller.saveWithoutMetadata();var restored=new DeviceEntity(cp,level.getBlockState(cp));restored.load(saved);
        h.assertTrue(restored.autoEnabled()&&restored.autoRadius()==48&&center.equals(restored.autoCenter())&&restored.hasAutoAnchor(),"Discovery settings did not persist");
        var restoredSignal=new SignalEntity(south,level.getBlockState(south));restoredSignal.load(((SignalEntity)level.getBlockEntity(south)).saveWithoutMetadata());h.assertTrue(restoredSignal.automatic(),"Automatic signal ownership did not persist");
        // Radius is horizontal, with an independent height limit. Exact edge accepted.
        controller.setAutoRadius(16);var edge=center.offset(16,2,0);var outside=center.offset(17,2,0);var high=center.offset(0,13,-7);
        for(var p:java.util.List.of(edge,outside))level.setBlock(p,NgTraffic.SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.EAST),3);
        level.setBlock(high,NgTraffic.SIGNAL.get().defaultBlockState(),3);controller.scanSignals();
        h.assertTrue(((SignalEntity)level.getBlockEntity(edge)).linkedTo(cp)&&!((SignalEntity)level.getBlockEntity(outside)).hasController()&&!((SignalEntity)level.getBlockEntity(high)).hasController(),"Radius/height boundary failed");
        int chunks=level.getChunkSource().getLoadedChunksCount();controller.setAutoRadius(48);controller.scanSignals();h.assertTrue(chunks==level.getChunkSource().getLoadedChunksCount(),"Discovery force-loaded chunks");
        // Two automatic areas must not claim an as-yet unlinked lamp.
        other.setAutoAnchor(foreign);other.setAutoCenter(center.offset(4,0,0));other.tick();
        var overlap=center.offset(2,2,-12);level.setBlock(overlap,NgTraffic.SIGNAL.get().defaultBlockState(),3);controller.scanSignals();other.scanSignals();
        h.assertTrue(!((SignalEntity)level.getBlockEntity(overlap)).hasController(),"Overlapping controllers claimed an ambiguous lamp");
        other.setAutoEnabled(false);controller.setAutoEnabled(false);
        var later=center.offset(-10,2,1);level.setBlock(later,NgTraffic.SIGNAL.get().defaultBlockState().setValue(OrientedBlock.FACING,net.minecraft.core.Direction.WEST),3);
        controller.scanSignals();h.assertTrue(!((SignalEntity)level.getBlockEntity(later)).hasController(),"Disabled discovery linked a lamp");
        controller.setAutoEnabled(true);controller.setEnabled(true);
        // No explicit scan: the controller ticker must notice newly placed signals.
        h.runAfterDelay(45,()->{
            controller.tick();h.assertTrue(((SignalEntity)level.getBlockEntity(later)).linkedTo(cp)&&!controller.isEnabled(),"New placement not discovered through periodic tick");
            player.getAbilities().mayBuild=false;h.assertTrue(!menu.clickMenuButton(player,19)&&!menu.clickMenuButton(player,1),"Discovery edits bypassed permissions");
            controller.setAutoEnabled(false);h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty")
    public static void linkerSetsCenterOnlyWithDeliberateSneakAndValidController(GameTestHelper h) {
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));var ap=h.absolutePos(new BlockPos(2,1,1));var floor=h.absolutePos(new BlockPos(3,0,3));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);level.setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);level.setBlock(floor,Blocks.STONE.defaultBlockState(),3);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;var hand=net.minecraft.world.InteractionHand.MAIN_HAND;var stack=new net.minecraft.world.item.ItemStack(NgTraffic.SIGNAL_LINKER.get());player.setItemInHand(hand,stack);
        var item=NgTraffic.SIGNAL_LINKER.get();java.util.function.Function<BlockPos,net.minecraft.world.item.context.UseOnContext> ctx=p->new net.minecraft.world.item.context.UseOnContext(player,hand,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(p),net.minecraft.core.Direction.UP,p,false));
        player.setShiftKeyDown(true);h.assertTrue(!item.useOn(ctx.apply(floor)).consumesAction(),"Center set with no selected controller");
        player.setShiftKeyDown(false);item.useOn(ctx.apply(cp));item.useOn(ctx.apply(ap));
        var controller=(DeviceEntity)level.getBlockEntity(cp);h.assertTrue(controller.hasAutoAnchor()&&!controller.autoEnabled(),"First lamp did not nominate anchor, or manual mode was unexpectedly enabled");
        h.assertTrue(!item.useOn(ctx.apply(floor)).consumesAction()&&controller.autoCenter()==null,"Ordinary click changed center");
        player.setShiftKeyDown(true);h.assertTrue(item.useOn(ctx.apply(floor)).consumesAction()&&floor.equals(controller.autoCenter())&&controller.autoEnabled()&&!controller.isEnabled(),"Sneak ground click did not set center safely");
        var previous=controller.autoCenter();stack.getOrCreateTag().putString("ControllerDimension","ng_verkehr:other");h.assertTrue(!item.useOn(ctx.apply(floor.offset(1,0,0))).consumesAction()&&previous.equals(controller.autoCenter()),"Cross-dimension center change accepted");
        player.getAbilities().mayBuild=false;h.assertTrue(!item.useOn(ctx.apply(floor)).consumesAction(),"No-build player changed center");controller.setAutoEnabled(false);h.succeed();
    }
    @GameTest(template="ng_verkehr:auto_test")
    public static void compactMenuSelectsAll256DevicesAndUsesDirectAssignments(GameTestHelper h) {
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));var ap=h.absolutePos(new BlockPos(2,1,1));var pp=h.absolutePos(new BlockPos(3,1,1));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);level.setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);level.setBlock(pp,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);
        var controller=(DeviceEntity)level.getBlockEntity(cp);var signal=(SignalEntity)level.getBlockEntity(ap);var button=(PedestrianEntity)level.getBlockEntity(pp);signal.link(cp,0);button.link(cp,0);
        var positions=new java.util.ArrayList<BlockPos>();for(int i=0;i<256;i++)positions.add(cp.offset(i%16,0,2+i/16));positions.set(255,ap);positions.set(254,pp);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        for(int i=0;i<254;i++){var pos=positions.get(i);level.setBlock(pos,NgTraffic.SIGNAL.get().defaultBlockState(),3);((SignalEntity)level.getBlockEntity(pos)).link(cp,0);}
        var menu=new ControllerMenu(32,player.getInventory(),controller,controller.devices());
        for(int i=0;i<256;i++){
            int bank=8+i/ControllerMenu.PAGE_SIZE,row=ControllerMenu.ROW_ACTION+i%ControllerMenu.PAGE_SIZE;
            for(int action:new int[]{25,(menu.revision()>>>21)&127,(menu.revision()>>>14)&127,(menu.revision()>>>7)&127,menu.revision()&127,bank,row}){
                var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(32,action).write(buffer);
                var packet=new net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket(buffer);buffer.release();
                h.assertTrue(packet.getButtonId()==action&&menu.clickMenuButton(player,packet.getButtonId()),"Selection action lost on wire: "+i);
            }
            h.assertTrue(menu.value(1)==i,"Wrong snapshot device selected: "+i);
        }
        selectMenu(menu,player,menu.positions.indexOf(ap));
        h.assertTrue(menu.clickMenuButton(player,14)&&signal.approach()==3&&!controller.isEnabled(),"Direct west assignment must stop controller");
        menu.clickMenuButton(player,0);menu.clickMenuButton(player,14);h.assertTrue(controller.isEnabled(),"Selecting existing direction should not stop controller");
        menu.clickMenuButton(player,11);h.assertTrue(signal.approach()==0&&!controller.isEnabled(),"Direct north assignment failed");
        menu.clickMenuButton(player,15);h.assertTrue(signal.approach()==-1,"Legacy group assignment failed");
        selectMenu(menu,player,menu.positions.indexOf(pp));menu.clickMenuButton(player,7);h.assertTrue(button.group()==1,"Direct crossing B failed");menu.clickMenuButton(player,6);h.assertTrue(button.group()==0,"Direct crossing A failed");
        h.assertTrue(!menu.clickMenuButton(player,127),"Out-of-range last bank row accepted");
        player.getAbilities().mayBuild=false;h.assertTrue(!menu.clickMenuButton(player,8)&&!menu.clickMenuButton(player,11),"Read-only player can change menu");
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=2250)
    public static void allSignalVariantsRegisterPersistAndReceiveProtectedPhases(GameTestHelper h) {
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        var controller=(DeviceEntity)level.getBlockEntity(cp);
        var signals=new java.util.ArrayList<SignalEntity>();int index=0;
        for(var kind:SignalKind.values()) {
            var p=h.absolutePos(new BlockPos(2+index%5,1,1+index/5));index++;
            var block=NgTraffic.CONTENT.get(kind.id).get();level.setBlock(p,block.defaultBlockState(),3);
            h.assertTrue(level.getBlockEntity(p) instanceof SignalEntity,"Block entity missing: "+kind);
            var signal=(SignalEntity)level.getBlockEntity(p);signal.link(cp,0);signals.add(signal);
            if(signal.fixedRole()){
                signal.assignRoute(0,2);
                h.assertTrue(signal.movement()==kind.movement&&signal.approach()==0,"Fixed symbol role lost: "+kind);
            }
            var restored=new SignalEntity(p,level.getBlockState(p));restored.load(signal.saveWithoutMetadata());
            h.assertTrue(restored.kind()==kind&&restored.movement()==signal.movement()&&restored.linkedTo(cp),"Variant persistence failed: "+kind);
        }
        var pp=h.absolutePos(new BlockPos(7,1,1));level.setBlock(pp,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);
        var ped=(PedestrianEntity)level.getBlockEntity(pp);ped.link(cp,0);
        controller.setEnabled(true);for(var signal:signals)signal.tick();
        for(var signal:signals)if(signal.movement()==0&&signal.kind()!=SignalKind.WARNING)
            h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==1,"Straight/bicycle must follow group A");
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var menu=new ControllerMenu(30,player.getInventory(),controller,controller.devices());
        for(var signal:signals){int row=menu.positions.indexOf(signal.getBlockPos());h.assertTrue(row>=0&&menu.deviceType(row)==signal.kind().menuType,"Menu variant type missing: "+signal.kind());}
        var saved=controller.saveWithoutMetadata();saved.putLong("Epoch",level.getGameTime()-679);controller.load(saved);controller.tick();
        for(int movement=1;movement<=6;movement++) {
            final int current=movement;
            h.runAfterDelay(65+(movement-1)*360,()->{
                controller.tick();ped.tick();
                for(var signal:signals){signal.tick();if(signal.kind()==SignalKind.WARNING)continue;
                    h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==(signal.movement()==current?2:0),"Conflicting or missing phase: "+current+" / "+signal.kind());
                }
                h.assertTrue(!ped.getBlockState().getValue(PedestrianBlock.ACTIVE),"Pedestrian green during protected route");
            });
        }
        h.runAfterDelay(2165,()->{
            controller.tick();for(var signal:signals)signal.tick();
            h.assertTrue(controller.light(1)==TrafficCycle.Light.RED_YELLOW,"Program did not return to group B");
            controller.setEnabled(false);for(var signal:signals){signal.tick();h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==0,"Stop failed: "+signal.kind());}
            level.setBlock(cp,Blocks.AIR.defaultBlockState(),3);for(var signal:signals){signal.tick();h.assertTrue(signal.getBlockState().getValue(SignalBlock.LIGHT)==0,"Missing controller not safe: "+signal.kind());}
            h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void warningBlinkAndFixedRoleMenuActions(GameTestHelper h) {
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));var wp=h.absolutePos(new BlockPos(2,1,1));var rp=h.absolutePos(new BlockPos(3,1,1));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);level.setBlock(wp,NgTraffic.WARNING_SIGNAL.get().defaultBlockState(),3);level.setBlock(rp,NgTraffic.SIGNAL_STRAIGHT_RIGHT.get().defaultBlockState(),3);
        var controller=(DeviceEntity)level.getBlockEntity(cp);var warning=(SignalEntity)level.getBlockEntity(wp);var route=(SignalEntity)level.getBlockEntity(rp);
        warning.tick();h.assertTrue(warning.getBlockState().getValue(SignalBlock.LIGHT)==0,"Unlinked warning must be off");
        warning.link(cp,0);route.link(cp,0);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var menu=new ControllerMenu(31,player.getInventory(),controller,controller.devices());
        selectMenu(menu,player,menu.positions.indexOf(rp));
        h.assertTrue(!menu.clickMenuButton(player,4)&&route.movement()==4,"Fixed combination can be reassigned");
        route.assignRoute(3,4);menu.clickMenuButton(player,3);
        h.assertTrue(route.approach()==0&&route.movement()==4&&!controller.isEnabled(),"Fixed approach must wrap W to N and stop");
        selectMenu(menu,player,menu.positions.indexOf(wp));h.assertTrue(!menu.clickMenuButton(player,3),"Warning must not expose route assignment");
        controller.setEnabled(true);
        for(int delay=1;delay<=25;delay++)h.runAfterDelay(delay,()->{
            warning.tick();int expected=Math.floorMod(level.getGameTime(),20)<10?3:0;
            h.assertTrue(warning.getBlockState().getValue(SignalBlock.LIGHT)==expected,"Warning blink period/aspect wrong");
        });
        h.runAfterDelay(26,()->{controller.setEnabled(false);warning.tick();h.assertTrue(warning.getBlockState().getValue(SignalBlock.LIGHT)==0,"Stopped warning not dark");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=1100)
    public static void protectedLeftArrowsYieldToPedestriansAndServeOneApproach(GameTestHelper h) {
        var level=h.getLevel();var cp=h.absolutePos(new BlockPos(1,1,1));
        var va=h.absolutePos(new BlockPos(2,1,1));var vb=h.absolutePos(new BlockPos(3,1,1));
        var ln=h.absolutePos(new BlockPos(1,1,2));var ls=h.absolutePos(new BlockPos(2,1,2));var pp=h.absolutePos(new BlockPos(3,1,2));var tp=h.absolutePos(new BlockPos(1,1,3));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        for(var p:java.util.List.of(va,vb))level.setBlock(p,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        for(var p:java.util.List.of(ln,ls))level.setBlock(p,NgTraffic.SIGNAL_LEFT.get().defaultBlockState(),3);
        level.setBlock(pp,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);level.setBlock(tp,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);
        var a=(SignalEntity)level.getBlockEntity(va);var b=(SignalEntity)level.getBlockEntity(vb);a.link(cp,0);b.link(cp,1);
        var north=(SignalEntity)level.getBlockEntity(ln);var south=(SignalEntity)level.getBlockEntity(ls);north.link(cp,0);south.link(cp,0);south.assignRoute(2,1);
        north.assignRoute(-1,0);h.assertTrue(north.approach()==0&&north.movement()==1,"Left-arrow role must not become straight");
        var ped=(PedestrianEntity)level.getBlockEntity(pp);var button=(PedestrianEntity)level.getBlockEntity(tp);ped.link(cp);button.link(cp);
        var controller=(DeviceEntity)level.getBlockEntity(cp);var saved=controller.saveWithoutMetadata();saved.putLong("Epoch",level.getGameTime()-679);controller.load(saved);
        controller.requestPedestrians(0);controller.tick();
        h.runAfterDelay(45,()->{
            ped.tick();north.tick();south.tick();
            h.assertTrue(level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Pedestrian demand must be served before left queue");
            h.assertTrue(level.getBlockState(ln).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(ls).getValue(SignalBlock.LIGHT)==0,"Left arrows conflict with pedestrian walk");
        });
        h.runAfterDelay(305,()->{
            north.tick();south.tick();a.tick();b.tick();ped.tick();
            h.assertTrue(level.getBlockState(ln).getValue(SignalBlock.LIGHT)==2&&level.getBlockState(ls).getValue(SignalBlock.LIGHT)==0,"Only north left arrow may be green");
            h.assertTrue(level.getBlockState(va).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(vb).getValue(SignalBlock.LIGHT)==0&&!level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Other traffic must remain red during north left");
        });
        h.runAfterDelay(665,()->{
            north.tick();south.tick();a.tick();b.tick();ped.tick();
            h.assertTrue(level.getBlockState(ls).getValue(SignalBlock.LIGHT)==2&&level.getBlockState(ln).getValue(SignalBlock.LIGHT)==0,"South must receive separate left phase");
            h.assertTrue(level.getBlockState(va).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(vb).getValue(SignalBlock.LIGHT)==0&&!level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Other traffic must remain red during south left");
        });
        h.runAfterDelay(965,()->{
            north.tick();south.tick();b.tick();
            h.assertTrue(level.getBlockState(vb).getValue(SignalBlock.LIGHT)==1,"Vehicle B must resume after left queue");
            var recovery=controller.saveWithoutMetadata();recovery.putInt("TurnMask",5);recovery.putInt("TurnQueue",4);recovery.putInt("TurnActive",0);recovery.putLong("TurnStart",level.getGameTime()-60);controller.load(recovery);controller.tick();north.tick();
            h.assertTrue(level.getBlockState(ln).getValue(SignalBlock.LIGHT)==0&&controller.saveWithoutMetadata().getLong("TurnStart")==level.getGameTime(),"Loaded turn must restart through red clearance");
            controller.setEnabled(false);north.tick();south.tick();h.assertTrue(level.getBlockState(ln).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(ls).getValue(SignalBlock.LIGHT)==0,"Stop must force all left arrows red");
            level.setBlock(cp,Blocks.AIR.defaultBlockState(),3);north.tick();h.assertTrue(level.getBlockState(ln).getValue(SignalBlock.LIGHT)==0,"Removed controller must leave left arrow red");
            h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty")
    public static void controllerMenuRoutesAndSecurity(GameTestHelper h) {
        var level=h.getLevel();var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;
        var cp=h.absolutePos(new BlockPos(1,1,1));var ap=h.absolutePos(new BlockPos(2,1,1));var pp=h.absolutePos(new BlockPos(3,1,1));var tp=h.absolutePos(new BlockPos(4,1,1));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);level.setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        level.setBlock(pp,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);level.setBlock(tp,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);
        var controller=(DeviceEntity)level.getBlockEntity(cp);var signal=(SignalEntity)level.getBlockEntity(ap);var pedestrian=(PedestrianEntity)level.getBlockEntity(pp);var button=(PedestrianEntity)level.getBlockEntity(tp);
        signal.link(cp,0);pedestrian.link(cp,0);button.link(cp,0);
        player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var snapshot=controller.devices();h.assertTrue(snapshot.size()==3,"Linked devices must be discovered without scanning the world");
        var menu=new ControllerMenu(17,player.getInventory(),controller,snapshot);
        h.assertTrue(controller.isEnabled(),"Opening a menu must not toggle the controller");
        h.assertTrue(selectMenu(menu,player,snapshot.indexOf(ap)),"Signal row selection failed");
        h.assertTrue(menu.clickMenuButton(player,3)&&signal.approach()==0&&!controller.isEnabled(),"Assigning north must stop controller");
        h.assertTrue(menu.clickMenuButton(player,4)&&signal.movement()==1,"Left turn assignment failed");
        menu.clickMenuButton(player,0);signal.tick();
        h.assertTrue(level.getBlockState(ap).getValue(SignalBlock.LIGHT)==0,"Prepared turn must stay red without protected program");
        menu.clickMenuButton(player,4);menu.clickMenuButton(player,4);
        h.assertTrue(signal.movement()==0,"Straight assignment failed");
        menu.clickMenuButton(player,3);h.assertTrue(signal.approach()==1&&signal.group()==1,"East must map to vehicle group B");
        var restored=new SignalEntity(ap,level.getBlockState(ap));restored.load(signal.saveWithoutMetadata());
        h.assertTrue(restored.approach()==1&&restored.movement()==0,"Routing must persist");
        selectMenu(menu,player,snapshot.indexOf(pp));menu.clickMenuButton(player,5);
        h.assertTrue(pedestrian.group()==1&&!controller.isEnabled(),"Pedestrian crossing edit failed");
        selectMenu(menu,player,snapshot.indexOf(tp));menu.clickMenuButton(player,5);
        h.assertTrue(button.group()==1,"Button crossing edit failed");
        player.getAbilities().mayBuild=false;h.assertTrue(!menu.clickMenuButton(player,0),"No-build player must not operate menu");player.getAbilities().mayBuild=true;
        player.setPos(cp.getX()+20,cp.getY(),cp.getZ());h.assertTrue(!menu.clickMenuButton(player,0),"Distant menu must reject actions");player.setPos(cp.getX()+.5,cp.getY()+.5,cp.getZ()+.5);
        var other=h.absolutePos(new BlockPos(1,1,3));level.setBlock(other,NgTraffic.CONTROLLER.get().defaultBlockState(),3);signal.link(other,0);
        selectMenu(menu,player,snapshot.indexOf(ap));h.assertTrue(!menu.clickMenuButton(player,3),"Stale menu must not modify another controller's device");
        h.assertTrue(!controller.devices().contains(ap),"Reassigned devices must leave the old list");
        for(int i=0;i<7;i++){var p=h.absolutePos(new BlockPos(i+2,1,3));level.setBlock(p,NgTraffic.SIGNAL.get().defaultBlockState(),3);((SignalEntity)level.getBlockEntity(p)).link(cp,0);}
        var pages=new ControllerMenu(18,player.getInventory(),controller,controller.devices());
        h.assertTrue(pages.value(8)==0,"Initial selection bank failed");
        h.assertTrue(selectMenu(pages,player,1)&&pages.value(1)==1,"Table row selection failed");
        var buffer=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        ControllerMenu.writeSnapshot(buffer,controller,controller.devices());var clientCopy=new ControllerMenu(18,player.getInventory(),buffer);
        h.assertTrue(clientCopy.positions.equals(pages.positions)&&clientCopy.controllerPos.equals(cp),"Opening snapshot must round-trip");buffer.release();
        level.setBlock(cp,Blocks.AIR.defaultBlockState(),3);h.assertTrue(!pages.stillValid(player)&&!pages.clickMenuButton(player,0),"Removed controller must invalidate menu");
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty")
    public static void smallerButtonsMountOnMastsWithoutLosingParts(GameTestHelper h) {
        var level=h.getLevel();var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;player.getAbilities().instabuild=false;
        var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
        var post=NgTraffic.CONTENT.get("post").get();int index=0;
        BlockPos mounted=null;
        for(var direction:java.util.List.of(net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.WEST)) {
            var p=h.absolutePos(new BlockPos(++index,1,1));mounted=p;
            level.setBlock(p,post.defaultBlockState(),3);
            level.setBlock(p.above(),NgTraffic.SIGNAL.get().defaultBlockState(),3);
            var stack=new net.minecraft.world.item.ItemStack(NgTraffic.PEDESTRIAN_BUTTON.get(),2);player.setItemInHand(hand,stack);
            var hit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(p).add(direction.getStepX()*.0625,0,direction.getStepZ()*.0625),direction,p,false);
            var result=stack.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(player,hand,hit));
            h.assertTrue(result.consumesAction(),"Mount placement must succeed on "+direction);
            var state=level.getBlockState(p);
            h.assertTrue(state.is(NgTraffic.PEDESTRIAN_BUTTON.get())&&state.getValue(PedestrianBlock.MOUNT)==PedestrianBlock.Mount.MAST,"Button must mount in the mast cell");
            h.assertTrue(state.getValue(PedestrianBlock.FACING)==direction,"Button must face the clicked mast side");
            h.assertTrue(level.getBlockState(p.above()).is(NgTraffic.SIGNAL.get()),"Signal above mast must remain");
            h.assertTrue(stack.getCount()==1,"Mounting must consume exactly one button");
            var drops=net.minecraft.world.level.block.Block.getDrops(state,level,p,level.getBlockEntity(p),player,net.minecraft.world.item.ItemStack.EMPTY);
            int buttons=0,posts=0;for(var drop:drops){if(drop.is(NgTraffic.PEDESTRIAN_BUTTON.get().asItem()))buttons+=drop.getCount();if(drop.is(post.asItem()))posts+=drop.getCount();}
            h.assertTrue(buttons==1&&posts==1,"Breaking must return one button and one mast");
        }
        var cp=h.absolutePos(new BlockPos(1,1,3));level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        var entity=(PedestrianEntity)level.getBlockEntity(mounted);entity.link(cp,1);
        player.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
        var state=level.getBlockState(mounted);
        state.getBlock().use(state,level,mounted,player,hand,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(mounted),net.minecraft.core.Direction.NORTH,mounted,false));
        var controller=(DeviceEntity)level.getBlockEntity(cp);
        h.assertTrue(controller.pedestrianBusy(1)&&!controller.pedestrianBusy(0),"Mounted button must still request its own crossing");
        var wall=h.absolutePos(new BlockPos(1,1,4));level.setBlock(wall,Blocks.STONE.defaultBlockState(),3);
        var wallStack=new net.minecraft.world.item.ItemStack(NgTraffic.PEDESTRIAN_BUTTON.get());player.setItemInHand(hand,wallStack);
        var wallHit=new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(wall).add(.5,0,0),net.minecraft.core.Direction.EAST,wall,false);
        h.assertTrue(wallStack.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(player,hand,wallHit)).consumesAction(),"Wall mounting must work");
        var wallState=level.getBlockState(wall.east());h.assertTrue(wallState.is(NgTraffic.PEDESTRIAN_BUTTON.get())&&wallState.getValue(PedestrianBlock.MOUNT)==PedestrianBlock.Mount.WALL,"Wall mount must sit against the wall");
        var freeDrops=net.minecraft.world.level.block.Block.getDrops(wallState,level,wall.east(),level.getBlockEntity(wall.east()),player,net.minecraft.world.item.ItemStack.EMPTY);
        h.assertTrue(freeDrops.stream().noneMatch(s->s.is(post.asItem())),"Wall button must not duplicate masts");
        var blocked=h.absolutePos(new BlockPos(4,1,4));level.setBlock(blocked,post.defaultBlockState(),3);player.getAbilities().mayBuild=false;
        var denied=new net.minecraft.world.item.ItemStack(NgTraffic.PEDESTRIAN_BUTTON.get());player.setItemInHand(hand,denied);
        h.assertTrue(!denied.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(player,hand,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(blocked),net.minecraft.core.Direction.NORTH,blocked,false))).consumesAction(),"No-build players must not replace masts");
        h.assertTrue(level.getBlockState(blocked).is(post),"Denied placement must preserve mast");
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=600)
    public static void separatePedestrianCrossingsQueueAndPersist(GameTestHelper h) {
        var level=h.getLevel();
        var cp=h.absolutePos(new BlockPos(1,1,1));var va=h.absolutePos(new BlockPos(2,1,1));var vb=h.absolutePos(new BlockPos(3,1,1));
        var pa=h.absolutePos(new BlockPos(1,1,2));var pb=h.absolutePos(new BlockPos(2,1,2));
        var ta=h.absolutePos(new BlockPos(1,1,3));var tb=h.absolutePos(new BlockPos(2,1,3));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        for(var p:java.util.List.of(va,vb))level.setBlock(p,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        for(var p:java.util.List.of(pa,pb))level.setBlock(p,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);
        for(var p:java.util.List.of(ta,tb))level.setBlock(p,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;
        var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
        player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(NgTraffic.SIGNAL_LINKER.get()));
        java.util.function.Function<BlockPos,net.minecraft.world.phys.BlockHitResult> hit=p->new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(p),net.minecraft.core.Direction.UP,p,false);
        var linker=NgTraffic.SIGNAL_LINKER.get();
        for(var p:java.util.List.of(cp,pa,ta))h.assertTrue(linker.useOn(new net.minecraft.world.item.context.UseOnContext(player,hand,hit.apply(p))).consumesAction(),"A selection/link failed");
        player.setShiftKeyDown(true);
        for(var p:java.util.List.of(pb,tb))h.assertTrue(linker.useOn(new net.minecraft.world.item.context.UseOnContext(player,hand,hit.apply(p))).consumesAction(),"B link failed");
        player.setShiftKeyDown(false);
        var a=(PedestrianEntity)level.getBlockEntity(pa);var b=(PedestrianEntity)level.getBlockEntity(pb);
        h.assertTrue(a.group()==0&&b.group()==1,"Shift-click must select crossing B");
        var restored=new PedestrianEntity(pb,level.getBlockState(pb));restored.load(b.saveWithoutMetadata());
        h.assertTrue(restored.group()==1,"Crossing B assignment must persist");
        var legacy=b.saveWithoutMetadata();legacy.remove("Group");restored.load(legacy);
        h.assertTrue(restored.group()==0,"Old pedestrian links must migrate to crossing A");
        var vehicleA=(SignalEntity)level.getBlockEntity(va);var vehicleB=(SignalEntity)level.getBlockEntity(vb);vehicleA.link(cp,0);vehicleB.link(cp,1);
        var controller=(DeviceEntity)level.getBlockEntity(cp);
        var initial=controller.saveWithoutMetadata();initial.putLong("Epoch",level.getGameTime()-680);controller.load(initial);
        player.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
        var stateA=level.getBlockState(ta);stateA.getBlock().use(stateA,level,ta,player,hand,hit.apply(ta));
        var buttonB=(PedestrianEntity)level.getBlockEntity(tb);buttonB.tick();
        h.assertTrue(!level.getBlockState(tb).getValue(PedestrianBlock.ACTIVE),"B button must not inherit A demand");
        var stateB=level.getBlockState(tb);stateB.getBlock().use(stateB,level,tb,player,hand,hit.apply(tb));
        var saved=controller.saveWithoutMetadata();
        h.assertTrue(saved.getInt("PedActiveGroup")==0&&saved.getInt("PedPendingMask")==2,"B must queue behind active A");
        controller.load(saved);controller.tick();
        h.assertTrue(controller.saveWithoutMetadata().getInt("PedPendingMask")==2,"Queued B lost on load");
        h.runAfterDelay(45,()->{
            a.tick();b.tick();vehicleA.tick();vehicleB.tick();
            h.assertTrue(level.getBlockState(pa).getValue(PedestrianBlock.ACTIVE)&&!level.getBlockState(pb).getValue(PedestrianBlock.ACTIVE),"Only requested crossing A may walk first");
            h.assertTrue(level.getBlockState(va).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(vb).getValue(SignalBlock.LIGHT)==0,"Vehicles must remain red");
        });
        h.runAfterDelay(245,()->{
            a.tick();b.tick();vehicleA.tick();vehicleB.tick();
            h.assertTrue(!level.getBlockState(pa).getValue(PedestrianBlock.ACTIVE)&&!level.getBlockState(pb).getValue(PedestrianBlock.ACTIVE),"Both crossings must be red between walks");
            h.assertTrue(level.getBlockState(va).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(vb).getValue(SignalBlock.LIGHT)==0,"No vehicle release between queued crossings");
        });
        h.runAfterDelay(285,()->{
            a.tick();b.tick();vehicleA.tick();vehicleB.tick();
            h.assertTrue(!level.getBlockState(pa).getValue(PedestrianBlock.ACTIVE)&&level.getBlockState(pb).getValue(PedestrianBlock.ACTIVE),"Queued crossing B must walk second");
            h.assertTrue(level.getBlockState(va).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(vb).getValue(SignalBlock.LIGHT)==0,"Vehicles conflict with B");
        });
        h.runAfterDelay(485,()->{
            a.tick();b.tick();vehicleA.tick();vehicleB.tick();
            for(var p:java.util.List.of(ta,tb))((PedestrianEntity)level.getBlockEntity(p)).tick();
            h.assertTrue(!level.getBlockState(pa).getValue(PedestrianBlock.ACTIVE)&&!level.getBlockState(pb).getValue(PedestrianBlock.ACTIVE),"Both crossings must finish red");
            h.assertTrue(!level.getBlockState(ta).getValue(PedestrianBlock.ACTIVE)&&!level.getBlockState(tb).getValue(PedestrianBlock.ACTIVE),"Both button indicators must reset");
            h.assertTrue(level.getBlockState(vb).getValue(SignalBlock.LIGHT)==1,"Vehicle B must resume after both crossings");
            level.setBlock(cp,Blocks.AIR.defaultBlockState(),3);a.tick();b.tick();
            h.assertTrue(!level.getBlockState(pa).getValue(PedestrianBlock.ACTIVE)&&!level.getBlockState(pb).getValue(PedestrianBlock.ACTIVE),"Removed controller must force both crossings red");
            h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=350)
    public static void pedestrianRequestWaitsForSafeVehicleStop(GameTestHelper h) {
        var level=h.getLevel();
        var cp=h.absolutePos(new BlockPos(1,1,1));var ap=h.absolutePos(new BlockPos(2,1,1));var bp=h.absolutePos(new BlockPos(3,1,1));
        var pp=h.absolutePos(new BlockPos(1,1,2));var tp=h.absolutePos(new BlockPos(2,1,2));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        level.setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);level.setBlock(bp,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        level.setBlock(pp,NgTraffic.PEDESTRIAN_SIGNAL.get().defaultBlockState(),3);level.setBlock(tp,NgTraffic.PEDESTRIAN_BUTTON.get().defaultBlockState(),3);
        var player=h.makeMockPlayer();player.getAbilities().mayBuild=true;
        var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
        player.setItemInHand(hand,new net.minecraft.world.item.ItemStack(NgTraffic.SIGNAL_LINKER.get()));
        java.util.function.Function<BlockPos,net.minecraft.world.phys.BlockHitResult> hit=p->new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(p),net.minecraft.core.Direction.UP,p,false);
        var linker=NgTraffic.SIGNAL_LINKER.get();
        for(var p:java.util.List.of(cp,pp,tp)) h.assertTrue(linker.useOn(new net.minecraft.world.item.context.UseOnContext(player,hand,hit.apply(p))).consumesAction(),"Click must link pedestrian devices");
        var pedestrian=(PedestrianEntity)level.getBlockEntity(pp);var button=(PedestrianEntity)level.getBlockEntity(tp);
        var restored=new PedestrianEntity(pp,level.getBlockState(pp));restored.load(pedestrian.saveWithoutMetadata());
        h.assertTrue(restored.saveWithoutMetadata().getLong("Controller")==cp.asLong(),"Pedestrian link must persist");
        var a=(SignalEntity)level.getBlockEntity(ap);var b=(SignalEntity)level.getBlockEntity(bp);a.link(cp,0);b.link(cp,1);
        var controller=(DeviceEntity)level.getBlockEntity(cp);
        var saved=controller.saveWithoutMetadata();saved.putLong("Epoch",level.getGameTime()-680);controller.load(saved);
        player.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
        var state=level.getBlockState(tp);
        state.getBlock().use(state,level,tp,player,hand,hit.apply(tp));
        var requestState=controller.saveWithoutMetadata();
        h.assertTrue(requestState.getBoolean("PedPending")||requestState.getLong("PedStart")>=0,"Button click must persist a queued or already accepted request");
        controller.tick();a.tick();b.tick();pedestrian.tick();button.tick();
        h.assertTrue(!level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Walk must wait for entry clearance");
        h.assertTrue(level.getBlockState(tp).getValue(PedestrianBlock.ACTIVE),"Button must show accepted request");
        h.runAfterDelay(45,()->{
            pedestrian.tick();a.tick();b.tick();
            h.assertTrue(level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Pedestrian green must be reached");
            h.assertTrue(level.getBlockState(ap).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(bp).getValue(SignalBlock.LIGHT)==0,"Both vehicle groups must be red while pedestrians walk");
            h.assertTrue(!controller.requestPedestrians(),"Repeated request must not extend walk");
        });
        h.runAfterDelay(165,()->{
            pedestrian.tick();a.tick();b.tick();
            h.assertTrue(!level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Walk must end before vehicles restart");
            h.assertTrue(level.getBlockState(ap).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(bp).getValue(SignalBlock.LIGHT)==0,"Vehicle clearance must remain red");
        });
        h.runAfterDelay(245,()->{
            a.tick();b.tick();button.tick();
            h.assertTrue(level.getBlockState(bp).getValue(SignalBlock.LIGHT)==1,"Next vehicle group must restart red-yellow");
            h.assertTrue(!level.getBlockState(tp).getValue(PedestrianBlock.ACTIVE),"Button indicator must reset");
            controller.setEnabled(false);pedestrian.tick();
            h.assertTrue(!controller.requestPedestrians()&&!level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Stopped controller must reject walk requests");
            level.setBlock(cp,Blocks.AIR.defaultBlockState(),3);pedestrian.tick();
            h.assertTrue(!level.getBlockState(pp).getValue(PedestrianBlock.ACTIVE),"Missing controller must force pedestrian red");
            h.succeed();
        });
    }
    @GameTest(template="ng_verkehr:empty")
    public static void clickLinkerAndControllerWork(GameTestHelper h) {
        var level=h.getLevel();
        var cp=h.absolutePos(new BlockPos(1,1,1));
        var ap=h.absolutePos(new BlockPos(2,1,1));
        var bp=h.absolutePos(new BlockPos(3,1,1));
        level.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        level.setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        level.setBlock(bp,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        var player=h.makeMockPlayer();
        player.getAbilities().mayBuild=true;
        player.setShiftKeyDown(false);
        var hand=net.minecraft.world.InteractionHand.MAIN_HAND;
        var stack=new net.minecraft.world.item.ItemStack(NgTraffic.SIGNAL_LINKER.get());
        player.setItemInHand(hand,stack);
        java.util.function.Function<BlockPos,net.minecraft.world.item.context.UseOnContext> context=p->new net.minecraft.world.item.context.UseOnContext(player,hand,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(p),net.minecraft.core.Direction.UP,p,false));
        var item=NgTraffic.SIGNAL_LINKER.get();
        h.assertTrue(!item.useOn(context.apply(ap)).consumesAction(),"Must select a controller first");
        h.assertTrue(item.useOn(context.apply(cp)).consumesAction(),"Controller selection must work");
        stack=stack.copy(); player.setItemInHand(hand,stack);
        h.assertTrue(item.useOn(context.apply(ap)).consumesAction(),"Selection must survive item copy");
        var a=(SignalEntity)level.getBlockEntity(ap);
        h.assertTrue(a.saveWithoutMetadata().getLong("Controller")==cp.asLong(),"Click must link the selected controller");
        h.assertTrue(a.saveWithoutMetadata().getInt("Group")==0,"Normal click selects group A");
        player.setShiftKeyDown(true);
        h.assertTrue(item.useOn(context.apply(bp)).consumesAction(),"Sneak click must connect group B");
        var b=(SignalEntity)level.getBlockEntity(bp);
        h.assertTrue(b.saveWithoutMetadata().getInt("Group")==1,"Sneak click selects group B");
        player.setShiftKeyDown(false);
        stack.getOrCreateTag().putString("ControllerDimension","ng_verkehr:other");
        h.assertTrue(!item.useOn(context.apply(ap)).consumesAction(),"Cross-dimension selection must be rejected");
        item.useOn(context.apply(cp));
        stack.getOrCreateTag().putLong("SelectedController",cp.offset(300,0,0).asLong());
        h.assertTrue(!item.useOn(context.apply(ap)).consumesAction(),"Remote controller must be rejected");
        item.useOn(context.apply(cp));
        var controller=(DeviceEntity)level.getBlockEntity(cp);
        player.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
        var state=level.getBlockState(cp);
        player.setShiftKeyDown(true);
        state.getBlock().use(state,level,cp,player,hand,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(cp),net.minecraft.core.Direction.UP,cp,false));
        h.assertTrue(!controller.isEnabled(),"Empty-hand click stops the controller");
        a.tick(); b.tick();
        h.assertTrue(level.getBlockState(ap).getValue(SignalBlock.LIGHT)==0&&level.getBlockState(bp).getValue(SignalBlock.LIGHT)==0,"Stopping forces both groups red");
        state.getBlock().use(state,level,cp,player,hand,new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(cp),net.minecraft.core.Direction.UP,cp,false));
        h.assertTrue(controller.isEnabled(),"Second click starts the controller");
        player.setShiftKeyDown(false);
        a.tick(); b.tick();
        h.assertTrue(level.getBlockState(ap).getValue(SignalBlock.LIGHT)==1&&level.getBlockState(bp).getValue(SignalBlock.LIGHT)==0,"Restart begins with A red-yellow and B red");
        player.setItemInHand(hand,stack);
        level.setBlock(cp,Blocks.AIR.defaultBlockState(),3);
        h.assertTrue(!item.useOn(context.apply(ap)).consumesAction(),"Removed controller must be rejected");
        player.setItemInHand(hand,net.minecraft.world.item.ItemStack.EMPTY);
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty")
    public static void controllerFailureTurnsSignalsRed(GameTestHelper h) {
        BlockPos cp=h.absolutePos(new BlockPos(1,1,1));
        BlockPos ap=h.absolutePos(new BlockPos(2,1,1));
        BlockPos bp=h.absolutePos(new BlockPos(3,1,1));
        var world=h.getLevel();
        world.setBlock(cp,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        world.setBlock(ap,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        world.setBlock(bp,NgTraffic.SIGNAL.get().defaultBlockState(),3);
        var controller=(DeviceEntity)world.getBlockEntity(cp);
        var a=(SignalEntity)world.getBlockEntity(ap);
        var b=(SignalEntity)world.getBlockEntity(bp);
        controller.setEnabled(true); a.link(cp,0); b.link(cp,1);
        h.assertTrue(world.getBlockState(ap).getValue(SignalBlock.LIGHT)==1,"Group 0 must start red-yellow");
        h.assertTrue(world.getBlockState(bp).getValue(SignalBlock.LIGHT)==0,"Group 1 must remain red");
        controller.setEnabled(false); a.tick(); b.tick();
        h.assertTrue(world.getBlockState(ap).getValue(SignalBlock.LIGHT)==0,"Disabled controller must force red");
        controller.setEnabled(true); a.tick();
        world.setBlock(cp,Blocks.AIR.defaultBlockState(),3); a.tick(); b.tick();
        h.assertTrue(world.getBlockState(ap).getValue(SignalBlock.LIGHT)==0&&world.getBlockState(bp).getValue(SignalBlock.LIGHT)==0,"Missing controller must force both red");
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty")
    public static void parkingValuesPersistAndClamp(GameTestHelper h) {
        BlockPos p=h.absolutePos(new BlockPos(1,1,1));
        var state=NgTraffic.PARKING.get().defaultBlockState();
        h.getLevel().setBlock(p,state,3);
        var device=(DeviceEntity)h.getLevel().getBlockEntity(p);
        device.setParking(100,20);
        h.assertTrue(device.displayText().equals("P 80 FREI"),"Free spaces must be calculated");
        var restored=new DeviceEntity(p,state); restored.load(device.saveWithoutMetadata());
        h.assertTrue(restored.displayText().equals("P 80 FREI"),"Parking values must survive NBT round trip");
        device.setParking(10,99999);
        h.assertTrue(device.displayText().equals("P BESETZT"),"Occupancy must be clamped to capacity");
        device.setParking(-10,-20);
        h.assertTrue(device.displayText().equals("P BESETZT"),"Zero capacity must show occupied");
        h.succeed();
    }
    @GameTest(template="ng_verkehr:empty")
    public static void displayTextPersists(GameTestHelper h) {
        BlockPos p=h.absolutePos(new BlockPos(1,1,1));
        var state=NgTraffic.DISPLAY.get().defaultBlockState();
        h.getLevel().setBlock(p,state,3);
        var device=(DeviceEntity)h.getLevel().getBlockEntity(p);
        device.setText("Ausfahrt NeverGate");
        var restored=new DeviceEntity(p,state); restored.load(device.saveWithoutMetadata());
        h.assertTrue(restored.displayText().equals("Ausfahrt NeverGate"),"Text must survive NBT round trip");
        device.setText("x".repeat(100));
        h.assertTrue(device.displayText().length()==64,"Display length must be bounded");
        h.succeed();
    }
}
