package de.nevergate.verkehr;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.gametest.GameTestHolder;
import static de.nevergate.verkehr.OperationsBlock.Kind.*;
@GameTestHolder(value="ng_compat",namespace=NgTraffic.ID)
public final class NgVehiclesIntegrationGameTests {
    static Object field(Object o,String name){try{return o.getClass().getField(name).get(o);}catch(Exception ex){throw new RuntimeException(ex);}}
    static Object call(Object o,String name,Class<?>[] types,Object...args){try{return o.getClass().getMethod(name,types).invoke(o,args);}catch(Exception ex){throw new RuntimeException(ex);}}
    static Object call(Object o,String name){return call(o,name,new Class<?>[0]);}
    static void state(Entity e,float fuel,float dirt){var systems=field(e,"systems");var tag=new CompoundTag();call(systems,"save",new Class[]{CompoundTag.class},tag);tag.putFloat("Fuel",fuel);tag.putFloat("Dirt",dirt);call(systems,"load",new Class[]{CompoundTag.class},tag);}
    static ServerPlayer player(GameTestHelper h,BlockPos pos,boolean creative){var p=ServiceGameTests.player(h,pos,creative);p.connection=new net.minecraft.server.network.ServerGamePacketListenerImpl(h.getLevel().getServer(),VehicleCompatProbe.connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile()));return p;}
    static Entity car(GameTestHelper h,BlockPos pos,Player owner){
        var type=ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("ng_fahrzeuge","ng_city"));h.assertTrue(type!=null,"Real vehicle mod not loaded");var e=type.create(h.getLevel());h.assertTrue(e!=null&&e.getClass().getName().equals("de.nevergate.fahrzeuge.NgCityEntity"),"Real vehicle class unavailable");e.setPos(pos.getX()+.5,pos.getY()+.2,pos.getZ()+2.5);e.setNoGravity(true);e.noPhysics=true;call(field(e,"equipment"),"claim",new Class[]{Player.class},owner);h.getLevel().addFreshEntity(e);return e;
    }
    static VehicleServiceEvent execute(Entity e,ServerPlayer p,UUID tx,VehicleServiceEvent.Operation op,int amount){var event=new VehicleServiceEvent(e,p,tx,op,amount);MinecraftForge.EVENT_BUS.post(event);return event;}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void realVehicleReadUsesLitresAndExistingPlate(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);state(e,12.5F,30);call(field(field(e,"systems"),"comfort"),"setPlate",new Class[]{String.class},"NG-42");var v=NgVehiclesCompat.inspect(e);h.assertTrue(v!=null&&v.owner().equals(p.getUUID())&&v.plate().equals("NG-42")&&v.fuelMilliLitres()==12500&&v.fuelCapacityMilliLitres()==40000&&v.dirtPercent()==30,"Existing vehicle state misread");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void realDriverReportedWhenVanillaControllerIsNull(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);h.assertTrue(p.startRiding(e,true),"Cannot sit in actual vehicle");var v=NgVehiclesCompat.inspect(e);h.assertTrue(e.getControllingPassenger()==null&&p.getUUID().equals(v.driver()),"Vehicle-specific driver not read");p.stopRiding();e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void realVehiclePlateReaderOpensLinkedBollard(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);call(field(field(e,"systems"),"comfort"),"setPlate",new Class[]{String.class},"NG-42");p.startRiding(e,true);var reader=OperationsGameTests.put(h,pos,PLATE_READER);var gate=OperationsGameTests.put(h,pos.east(),ROAD_BOLLARD);h.assertTrue(reader.registerPlate("ng 42"),"Plate registration failed");reader.connect(gate,p);h.runAfterDelay(16,()->{h.assertTrue(reader.open()&&gate.open(),"Actual NG-Fahrzeuge driver/plate did not open gate");p.stopRiding();e.discard();h.succeed();});}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void realTankRefillsPartialCapacityOnlyOnce(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);state(e,38.5F,0);var tx=UUID.randomUUID();var first=execute(e,p,tx,VehicleServiceEvent.Operation.FUEL,10000);h.assertTrue(first.completed()&&first.delivered()==1500&&NgVehiclesCompat.inspect(e).fuelMilliLitres()==40000,"Actual capacity/quantity not respected");var repeat=execute(e,p,tx,VehicleServiceEvent.Operation.FUEL,10000);h.assertTrue(repeat.completed()&&repeat.receipt().equals(first.receipt())&&repeat.delivered()==1500,"Repeated UUID did not return same receipt");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void transactionCannotBeReusedWithChangedAmount(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);state(e,10,0);var tx=UUID.randomUUID();h.assertTrue(execute(e,p,tx,VehicleServiceEvent.Operation.FUEL,1000).completed(),"Initial service failed");h.assertTrue(!execute(e,p,tx,VehicleServiceEvent.Operation.FUEL,2000).completed()&&NgVehiclesCompat.inspect(e).fuelMilliLitres()==11000,"Changed payload reused transaction");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void realWashUsesExistingDirtAndReplaysReceipt(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);state(e,20,75);var tx=UUID.randomUUID();var first=execute(e,p,tx,VehicleServiceEvent.Operation.WASH,2);h.assertTrue(first.completed()&&first.delivered()==1&&NgVehiclesCompat.inspect(e).dirtPercent()==0,"Existing dirt not cleaned");h.assertTrue(execute(e,p,tx,VehicleServiceEvent.Operation.WASH,2).completed(),"Wash receipt not replayed");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void unparkedVehicleRejectedBeforeStartingDevice(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);state(e,10,10);var tag=new CompoundTag();call(e,"saveEquipment",new Class[]{CompoundTag.class},tag);tag.putBoolean("ParkingBrake",false);call(e,"loadEquipment",new Class[]{CompoundTag.class},tag);var n=OperationsGameTests.put(h,pos,FUEL_PUMP);n.action(p,23);h.assertTrue(!n.action(p,0)&&n.state()==0&&NgVehiclesCompat.inspect(e).fuelMilliLitres()==10000,"Unparked car started service");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void foreignOwnerCannotModifyActualVehicle(GameTestHelper h){var pos=OperationsGameTests.origin(h);var owner=player(h,pos,true);var stranger=player(h,pos,false);var e=car(h,pos,owner);state(e,10,75);h.assertTrue(!execute(e,stranger,UUID.randomUUID(),VehicleServiceEvent.Operation.FUEL,1000).completed()&&!execute(e,stranger,UUID.randomUUID(),VehicleServiceEvent.Operation.WASH,1).completed()&&NgVehiclesCompat.inspect(e).dirtPercent()==75,"Foreign player modified vehicle");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void actualStarterBatteryNeverAppearsAsTractionBattery(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);var v=NgVehiclesCompat.inspect(e);h.assertTrue(v!=null&&!v.electric()&&v.energyWh()==0&&v.capacityWh()==0&&!execute(e,p,UUID.randomUUID(),VehicleServiceEvent.Operation.CHARGE,1000).completed(),"Starter battery treated as electric vehicle");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void uncertainSavedReceiptDoesNotExecuteAgain(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var e=car(h,pos,p);state(e,10,0);var tx=UUID.randomUUID();var event=new VehicleServiceEvent(e,p,tx,VehicleServiceEvent.Operation.FUEL,1000);var j=VehicleServiceJournal.get(h.getLevel());j.begin(event);h.getLevel().getServer().overworld().getDataStorage().save();var copy=VehicleServiceJournal.load(j.save(new CompoundTag()));h.assertTrue(copy.lookup(tx)!=null&&!copy.lookup(tx).completed,"Uncertain receipt lost on serialize");h.assertTrue(!execute(e,p,tx,VehicleServiceEvent.Operation.FUEL,1000).completed()&&NgVehiclesCompat.inspect(e).fuelMilliLitres()==10000,"Uncertain service retried");e.discard();h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void unknownVersionsAreNotGuessed(GameTestHelper h){h.assertTrue(!NgVehiclesCompat.supportedVersion("0.1.0-dev10")&&NgVehiclesCompat.supportedVersion("0.1.0-dev11")&&!NgVehiclesCompat.supportedVersion("0.1.0-dev13")&&!NgVehiclesCompat.supportedVersion("unknown"),"Unknown version guessed compatible");h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void oldProviderSnapshotConstructorRemainsUsable(GameTestHelper h){var key=UUID.randomUUID();var v=new VehicleServiceEvent.View(key,null,"","car","",0,0,0,false,0,0);h.assertTrue(v.vehicle().equals(key)&&v.driver()==null,"Old provider constructor broken");h.succeed();}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void actualBusArrivalMatchesConfiguredVehicleLine(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var type=ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("ng_fahrzeuge","ng_bus"));h.assertTrue(type!=null,"Latest bus missing");var bus=type.create(h.getLevel());bus.setNoGravity(true);bus.noPhysics=true;bus.setPos(pos.getX()+.5,pos.getY()+.2,pos.getZ()+3);call(field(bus,"equipment"),"claim",new Class[]{Player.class},p);h.getLevel().addFreshEntity(bus);call(field(field(bus,"systems"),"expansion"),"setRoute",new Class[]{Player.class,String.class,String.class},p,"42","Bahnhof");var route=OperationsGameTests.put(h,pos,BUS_ROUTE);route.route("42","Bahnhof");var sensor=OperationsGameTests.put(h,pos.east(),BUS_SENSOR);sensor.connect(route,p);h.runAfterDelay(12,()->{h.assertTrue(sensor.open()&&NgVehiclesCompat.inspect(bus).busLine().equals("42"),"Matching bus line not detected");bus.discard();h.succeed();});}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void differentActualBusLineCannotClaimArrival(GameTestHelper h){var pos=OperationsGameTests.origin(h);var p=player(h,pos,true);var type=ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation("ng_fahrzeuge","ng_bus"));h.assertTrue(type!=null,"Latest bus missing");var bus=type.create(h.getLevel());bus.setNoGravity(true);bus.noPhysics=true;bus.setPos(pos.getX()+.5,pos.getY()+.2,pos.getZ()+3);call(field(bus,"equipment"),"claim",new Class[]{Player.class},p);h.getLevel().addFreshEntity(bus);call(field(field(bus,"systems"),"expansion"),"setRoute",new Class[]{Player.class,String.class,String.class},p,"43","Zentrum");var route=OperationsGameTests.put(h,pos,BUS_ROUTE);route.route("42","Bahnhof");var sensor=OperationsGameTests.put(h,pos.east(),BUS_SENSOR);sensor.connect(route,p);h.runAfterDelay(12,()->{h.assertTrue(!sensor.open(),"Wrong bus line claimed arrival");bus.discard();h.succeed();});}

    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void copiedLicensePlateCannotInheritAnotherVehiclesAccess(GameTestHelper h){var pos=OperationsGameTests.origin(h);var admin=player(h,pos,true);var permitted=car(h,pos,admin);call(field(field(permitted,"systems"),"comfort"),"setPlate",new Class[]{String.class},"NG-42");var reader=OperationsGameTests.put(h,pos,PLATE_READER);var gate=OperationsGameTests.put(h,pos.east(),ROAD_BOLLARD);reader.connect(gate,admin);h.assertTrue(reader.registerPlate("NG42"),"Actual vehicle grant missing");permitted.discard();var stranger=player(h,pos,true);var copy=car(h,pos,stranger);call(field(field(copy,"systems"),"comfort"),"setPlate",new Class[]{String.class},"NG-42");stranger.startRiding(copy,true);h.runAfterDelay(12,()->{h.assertTrue(!reader.open()&&!gate.open(),"Copied plate inherited another vehicle's access");stranger.stopRiding();permitted.discard();copy.discard();h.succeed();});}


    @GameTest(template="ng_verkehr:empty",timeoutTicks=40)
    public static void dev12AllVehicleTypesExposeExistingServiceState(GameTestHelper h){
        h.assertTrue(NgVehiclesCompat.supportedVersion("0.1.0-dev11")&&NgVehiclesCompat.supportedVersion("0.1.0-dev12")&&!NgVehiclesCompat.supportedVersion("0.1.0-dev13"),"Version gate incorrect");
        var pos=OperationsGameTests.origin(h);var owner=player(h,pos,true);
        int count=0;
        for(var type:ForgeRegistries.ENTITY_TYPES.getValues()){
            var id=ForgeRegistries.ENTITY_TYPES.getKey(type);
            if(id==null||!id.getNamespace().equals("ng_fahrzeuge"))continue;
            var e=type.create(h.getLevel());
            if(e==null||!e.getClass().getName().equals("de.nevergate.fahrzeuge.NgCityEntity"))continue;
            e.setPos(pos.getX()+.5,pos.getY()+.2,pos.getZ()+2.5);e.setNoGravity(true);e.noPhysics=true;
            call(field(e,"equipment"),"claim",new Class[]{Player.class},owner);h.getLevel().addFreshEntity(e);
            var view=NgVehiclesCompat.inspect(e);
            h.assertTrue(view!=null&&owner.getUUID().equals(view.owner())&&view.fuelCapacityMilliLitres()>0,"Dev12 vehicle unreadable: "+id);
            h.assertTrue(e.getBbWidth()>0&&e.getBbHeight()>0,"Invalid bounds "+id);
            e.discard();count++;
        }
        h.assertTrue(count==11,"Expected eleven Dev12 vehicle types, got "+count);h.succeed();
    }
}
