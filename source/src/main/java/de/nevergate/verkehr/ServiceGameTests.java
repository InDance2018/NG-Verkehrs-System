package de.nevergate.verkehr;
import java.util.*;
import java.util.function.Consumer;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.gametest.GameTestHolder;
@GameTestHolder(value=NgTraffic.ID,namespace=NgTraffic.ID)
public final class ServiceGameTests {
    static ServiceEntity put(GameTestHelper h,BlockPos p,ServiceBlock.Kind k){h.getLevel().setBlock(p,NgTraffic.SERVICES.get(k).get().defaultBlockState(),3);return (ServiceEntity)h.getLevel().getBlockEntity(p);}
    static ServerPlayer player(GameTestHelper h,BlockPos p,boolean creative){var x=new ServerPlayer(h.getLevel().getServer(),h.getLevel(),new GameProfile(UUID.randomUUID(),"SvcTest"),ClientInformation.createDefault()){ @Override public void displayClientMessage(Component m,boolean a){} @Override public boolean isCreative(){return creative;} };x.getAbilities().instabuild=creative;x.getAbilities().mayBuild=true;x.setPos(p.getX()+.5,p.getY()+.5,p.getZ()+.5);return x;}
    static BlockPos origin(GameTestHelper h){return h.absolutePos(new BlockPos(2,1,2));}
    static void free(ServiceEntity t){t.configure(0,0,0,60,0,30,220,120);}
    static ParkingLedger.Ticket token(GameTestHelper h,ServerPlayer p,ServiceEntity tariff,boolean pass){var l=ParkingLedger.get(h.getLevel());var t=l.issue(p.getUUID(),tariff.identity(),ParkingLedger.now(h.getLevel()),pass,pass?ParkingLedger.DAY:0);var s=new ItemStack(pass?NgTraffic.PARK_PASS.get():NgTraffic.PARK_TICKET.get());s.getOrCreateTag().putUUID("Ticket",t.id);p.getInventory().add(s);return t;}
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void tariffBoundariesAndDailyCap(GameTestHelper h){
        h.assertTrue(ParkingLedger.fee(12000,200,10,1500)==0,"Free boundary charged");
        h.assertTrue(ParkingLedger.fee(12001,200,10,1500)==200,"First started hour missing");
        h.assertTrue(ParkingLedger.fee(ParkingLedger.DAY,200,0,1500)==1500,"Daily cap missing");
        h.assertTrue(ParkingLedger.fee(ParkingLedger.DAY+1,200,0,1500)==1700,"Next day not billed");
        h.assertTrue(ParkingLedger.fee(-1,200,0,0)==0,"Negative time charged");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void ledgerRejectsForgeryAndConsumesOnlyOnce(GameTestHelper h){
        var l=new ParkingLedger();var owner=UUID.randomUUID();var zone=UUID.randomUUID();var t=l.issue(owner,zone,100,false,0);t.paid=true;t.validUntil=200;
        h.assertTrue(!l.usable(t.id,UUID.randomUUID(),zone,101)&&!l.usable(t.id,owner,UUID.randomUUID(),101)&&!l.usable(UUID.randomUUID(),owner,zone,101),"Foreign ticket accepted");
        h.assertTrue(l.consume(t.id,owner,zone,101)&&!l.consume(t.id,owner,zone,101),"Ticket reused");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void persistedLedgerKeepsOwnershipExpiryAndConsumption(GameTestHelper h){
        var l=new ParkingLedger();var owner=UUID.randomUUID();var zone=UUID.randomUUID();var t=l.issue(owner,zone,100,false,0);t.paid=true;t.validUntil=200;
        var copy=ParkingLedger.load(l.save(new CompoundTag()));h.assertTrue(copy.usable(t.id,owner,zone,150)&&!copy.usable(t.id,owner,zone,201),"Saved validity wrong");copy.consume(t.id,owner,zone,150);h.assertTrue(!ParkingLedger.load(copy.save(new CompoundTag())).usable(t.id,owner,zone,150),"Consumed ticket resurrected");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void passRepeatedUseRevocationAndZoneRestriction(GameTestHelper h){
        var l=new ParkingLedger();var owner=UUID.randomUUID();var zone=UUID.randomUUID();var t=l.issue(owner,zone,100,true,1000);
        h.assertTrue(l.consume(t.id,owner,zone,101)&&l.consume(t.id,owner,zone,102),"Pass consumed like ticket");h.assertTrue(l.revokePasses(owner,zone)==1&&!l.usable(t.id,owner,zone,103),"Pass revocation missing");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void missingEconomyCannotMarkPaid(GameTestHelper h){
        var pos=origin(h);var p=player(h,pos,false);var l=ParkingLedger.get(h.getLevel());var z=UUID.randomUUID();var t=l.issue(p.getUUID(),z,ParkingLedger.now(h.getLevel()),false,0);
        h.assertTrue(!l.pay(p,t.id,z,200,ParkingLedger.now(h.getLevel())+1200)&&!t.paid,"Missing provider created paid ticket");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void confirmedPaymentIsNotDebitedTwice(GameTestHelper h){
        var pos=origin(h);var p=player(h,pos,false);var l=ParkingLedger.get(h.getLevel());var z=UUID.randomUUID();var t=l.issue(p.getUUID(),z,ParkingLedger.now(h.getLevel()),false,0);int[] charged={0};
        Consumer<EconomyPaymentEvent> provider=e->{if(e.player==p){charged[0]++;e.confirm("test-receipt");}};MinecraftForge.EVENT_BUS.addListener(provider);
        try{long until=ParkingLedger.now(h.getLevel())+1200;h.assertTrue(l.pay(p,t.id,z,200,until)&&l.pay(p,t.id,z,200,until)&&charged[0]==1,"Repeated payment duplicated debit");}finally{MinecraftForge.EVENT_BUS.unregister(provider);}h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void zeroTariffWorksWithoutEconomy(GameTestHelper h){
        var pos=origin(h);var p=player(h,pos,false);var l=ParkingLedger.get(h.getLevel());var z=UUID.randomUUID();var t=l.issue(p.getUUID(),z,ParkingLedger.now(h.getLevel()),false,0);
        h.assertTrue(l.pay(p,t.id,z,0,ParkingLedger.now(h.getLevel())+1200)&&t.paid,"Free tariff required payment provider");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void staleLinkCannotAdoptReplacementTariff(GameTestHelper h){
        var pos=origin(h);var t=put(h,pos,ServiceBlock.Kind.TARIFF);var meter=put(h,pos.east(2),ServiceBlock.Kind.PARK_METER);var p=player(h,pos,true);h.assertTrue(meter.connect(t,p)&&meter.tariff()==t,"Tariff link missing");h.getLevel().removeBlock(pos,false);put(h,pos,ServiceBlock.Kind.TARIFF);h.assertTrue(meter.tariff()==null,"Replacement tariff inherited tickets");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void survivalCannotChangeTariffOrGrantPass(GameTestHelper h){
        var pos=origin(h);var t=put(h,pos,ServiceBlock.Kind.TARIFF);var p=player(h,pos,false);h.assertTrue(!t.action(p,10)&&!t.action(p,17)&&!t.action(p,19),"Survival user changed admin settings");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void entryAndFreeCashierIssueAuthoritativeTicket(GameTestHelper h){
        var pos=origin(h);var tariff=put(h,pos,ServiceBlock.Kind.TARIFF);free(tariff);var entry=put(h,pos.east(),ServiceBlock.Kind.ENTRY_TICKET);var cashier=put(h,pos.east(2),ServiceBlock.Kind.CASHIER);var admin=player(h,pos,true);var p=player(h,pos,false);entry.connect(tariff,admin);cashier.connect(tariff,admin);
        h.assertTrue(entry.action(p,0)&&cashier.purchase(p),"Free entry/cashier flow failed");h.assertTrue(!entry.action(p,17),"Forged menu button issued pass");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void paidTicketOpensGateAndCopyCannotReuse(GameTestHelper h){
        var pos=origin(h);var tariff=put(h,pos,ServiceBlock.Kind.TARIFF);var gate=put(h,pos.east(2),ServiceBlock.Kind.RESERVATION_BOLLARD);var reader=put(h,pos.east(),ServiceBlock.Kind.EXIT_READER);var admin=player(h,pos,true);gate.connect(tariff,admin);reader.connect(tariff,admin);reader.connect(gate,admin);var p=player(h,pos,false);var t=token(h,p,tariff,false);ParkingLedger.get(h.getLevel()).pay(p,t.id,tariff.identity(),0,ParkingLedger.now(h.getLevel())+1200);
        h.assertTrue(reader.exit(p)&&!reader.exit(p),"Exit ticket reused / failed");h.runAfterDelay(8,()->{h.assertTrue(gate.green(),"Paid exit failed to open gate");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void readerDoesNotConsumeWhenGateMissing(GameTestHelper h){
        var pos=origin(h);var tariff=put(h,pos,ServiceBlock.Kind.TARIFF);var reader=put(h,pos.east(),ServiceBlock.Kind.EXIT_READER);reader.connect(tariff,player(h,pos,true));var p=player(h,pos,false);var t=token(h,p,tariff,false);var l=ParkingLedger.get(h.getLevel());l.pay(p,t.id,tariff.identity(),0,ParkingLedger.now(h.getLevel())+1200);
        h.assertTrue(!reader.exit(p)&&!t.used,"Missing gate consumed ticket");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=20)
    public static void ticketControlledRoadBarrierFailsClosed(GameTestHelper h){
        var pos=origin(h);h.getLevel().setBlock(pos,NgTraffic.PARKING_BARRIER.get().defaultBlockState(),3);var b=(RoadSystemEntity)h.getLevel().getBlockEntity(pos);b.ticketControlled();b.ticketOpen(h.getLevel().getGameTime()+100);var saved=b.saveWithoutMetadata();b.load(saved);b.tick();h.assertTrue(!b.barrierOpen(),"Reload retained ticket-open lease");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void unlinkedSignalsAndBollardFailClosed(GameTestHelper h){
        var pos=origin(h);var gate=put(h,pos,ServiceBlock.Kind.RESERVATION_BOLLARD);var bike=put(h,pos.east(),ServiceBlock.Kind.BICYCLE_LIGHT);var sound=put(h,pos.east(2),ServiceBlock.Kind.ACOUSTIC_SIGNAL);
        h.runAfterDelay(8,()->{h.assertTrue(!gate.green()&&!bike.green()&&!sound.green()&&!bike.online(),"Unlinked safety device showed green");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void floorDisplayShowsZoneCountAndFaultMonitorSeesRemoval(GameTestHelper h){
        var pos=origin(h);h.getLevel().setBlock(pos,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);var road=(RoadSystemEntity)h.getLevel().getBlockEntity(pos);var data=road.data();data.capacity=100;data.occupied=36;road.configure(data);var board=put(h,pos.east(),ServiceBlock.Kind.FLOOR_BOARD);var monitor=put(h,pos.east(2),ServiceBlock.Kind.FAULT_MONITOR);var admin=player(h,pos,true);board.connect(road,admin);monitor.connect(board,admin);
        h.runAfterDelay(8,()->{h.assertTrue(board.free()==64&&monitor.faults()==0,"Floor free count wrong");h.getLevel().removeBlock(pos.east(),false);});h.runAfterDelay(20,()->{h.assertTrue(monitor.faults()==1&&monitor.alarm(),"Missing board not reported");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=30)
    public static void heightWarningDetectsVehicleAndRiderHeight(GameTestHelper h){
        var pos=origin(h);h.getLevel().setBlock(pos,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);var r=(RoadSystemEntity)h.getLevel().getBlockEntity(pos);var warning=put(h,pos.east(),ServiceBlock.Kind.HEIGHT_WARNING);warning.configure(200,0,0,60,0,30,150,120);warning.connect(r,player(h,pos,true));var b=new Boat(h.getLevel(),pos.getX()+.5,pos.getY(),pos.getZ()+2.5);b.setNoGravity(true);h.getLevel().addFreshEntity(b);var rider=h.makeMockPlayer();rider.setPos(b.getX(),b.getY()+1,b.getZ());rider.startRiding(b,true);
        h.runAfterDelay(8,()->{h.assertTrue(warning.detectedHeight()>0,"Vehicle not scanned");b.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void dynamicSpeedFailsToRestrictiveLimitWhenTriggerMissing(GameTestHelper h){
        var pos=origin(h);h.getLevel().setBlock(pos,NgTraffic.ROAD_CONTROLLER.get().defaultBlockState(),3);var road=(RoadSystemEntity)h.getLevel().getBlockEntity(pos);var node=put(h,pos.east(),ServiceBlock.Kind.DYNAMIC_SPEED);node.connect(road,player(h,pos,true));
        h.runAfterDelay(8,()->{h.assertTrue(node.effectiveSpeed()==30&&node.alarm()&&!node.online(),"Missing trigger removed restriction");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void serviceSettingsAndLinksSurviveNbt(GameTestHelper h){
        var pos=origin(h);var tariff=put(h,pos,ServiceBlock.Kind.TARIFF);tariff.configure(450,25,2300,90,1,40,340,200);var reader=put(h,pos.east(),ServiceBlock.Kind.CASHIER);reader.connect(tariff,player(h,pos,true));var tag=reader.saveWithoutMetadata();reader.load(tag);h.assertTrue(reader.tariff()==tariff&&reader.rate()==450&&reader.cap()==2300,"Service links/settings lost");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void replacementSignalIsRejectedAndNeverGreen(GameTestHelper h){
        var pos=origin(h);h.getLevel().setBlock(pos,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var c=(DeviceEntity)h.getLevel().getBlockEntity(pos);var bike=put(h,pos.east(),ServiceBlock.Kind.BICYCLE_LIGHT);bike.connect(c,player(h,pos,true));h.getLevel().removeBlock(pos,false);h.getLevel().setBlock(pos,NgTraffic.CONTROLLER.get().defaultBlockState(),3);
        h.runAfterDelay(8,()->{h.assertTrue(bike.signal()==null&&!bike.green(),"Replaced controller accepted stale link");h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=90)
    public static void rampMeterStartsRedAndManualClosureStopsRelease(GameTestHelper h){
        var pos=origin(h);var ramp=put(h,pos,ServiceBlock.Kind.RAMP_METER);var b=new Boat(h.getLevel(),pos.getX()+.5,pos.getY()+.2,pos.getZ()+3);b.setNoGravity(true);h.getLevel().addFreshEntity(b);var admin=player(h,pos,true);
        h.runAfterDelay(8,()->h.assertTrue(!ramp.green(),"Ramp started immediately green"));h.runAfterDelay(72,()->{h.assertTrue(ramp.green(),"Ramp demand never released");ramp.action(admin,20);});h.runAfterDelay(80,()->{h.assertTrue(!ramp.green(),"Manual ramp closure ignored");b.discard();h.succeed();});
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=25)
    public static void serviceSignalLinksAreManagedByController(GameTestHelper h){
        var pos=origin(h);h.getLevel().setBlock(pos,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var c=(DeviceEntity)h.getLevel().getBlockEntity(pos);c.setEnabled(true);var b=put(h,pos.east(),ServiceBlock.Kind.BICYCLE_LIGHT);h.assertTrue(b.connect(c,player(h,pos,true))&&!c.isEnabled()&&c.devices().contains(pos.east()),"New signal connection not safely registered");c.disconnectAll();h.assertTrue(b.signal()==null&&!b.green(),"Disconnect-all left bicycle green-capable");h.succeed();
    }
    @GameTest(template="ng_verkehr:empty",timeoutTicks=160)
    public static void bicycleDemandRetriesAfterNightRecovery(GameTestHelper h){
        var pos=origin(h);h.getLevel().setBlock(pos,NgTraffic.CONTROLLER.get().defaultBlockState(),3);var c=(DeviceEntity)h.getLevel().getBlockEntity(pos);var sensor=put(h,pos.east(),ServiceBlock.Kind.BICYCLE_DETECTOR);sensor.connect(c,player(h,pos,true));c.changeSetting(5,1);c.setEnabled(true);
        var b=new Boat(h.getLevel(),pos.getX()+2.5,pos.getY()+.2,pos.getZ()+.5);b.addTag("ng_bicycle");b.setNoGravity(true);b.noPhysics=true;h.getLevel().addFreshEntity(b);
        h.runAfterDelay(12,()->{c.changeSetting(5,0);c.setEnabled(true);});h.runAfterDelay(125,()->{h.assertTrue(c.pedestrianBusy(0)||sensor.green(),"Waiting bicycle lost demand through recovery");b.discard();h.succeed();});
    }
}