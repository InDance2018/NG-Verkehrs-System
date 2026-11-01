package de.nevergate.verkehr;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;

/** Optional versioned bridge, inspected against current NG-Fahrzeuge dev11. Never guesses unknown APIs. */
public final class NgVehiclesCompat {
    public static final Set<String> SUPPORTED=Set.of("0.1.0-dev11","0.1.0-dev12","0.1.0-beta1");
    private static boolean registered;
    private record Binding(Field equipment,Field systems,Field comfort,Field upgrades,Method owner,Method driver,
                           Method plate,Method fuel,Method capacity,Method dirt,Method allowed,Method refuel,Method wash){}
    private static final Map<Class<?>,Binding> bindings=new HashMap<>();
    public static boolean supportedVersion(String value){return value!=null&&SUPPORTED.contains(value);}
    public static boolean enabled(){return ModList.get().getModContainerById("ng_fahrzeuge").map(m->supportedVersion(m.getModInfo().getVersion().toString())).orElse(false);}
    public static void register(){if(registered)return;registered=true;MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST,NgVehiclesCompat::event);}
    private static Binding binding(Entity e)throws ReflectiveOperationException{
        if(!enabled()||!e.getClass().getName().equals("de.nevergate.fahrzeuge.NgCityEntity"))return null;
        var old=bindings.get(e.getClass());if(old!=null)return old;
        var c=e.getClass();var eq=c.getField("equipment");var sy=c.getField("systems");var comfort=sy.getType().getField("comfort");var upgrades=sy.getType().getField("upgrades");
        var b=new Binding(eq,sy,comfort,upgrades,eq.getType().getMethod("owner"),c.getMethod("getDriver"),comfort.getType().getMethod("plate"),sy.getType().getMethod("fuel"),sy.getType().getMethod("tankCapacity"),upgrades.getType().getMethod("dirt"),sy.getType().getMethod("serviceAllowed",Player.class),sy.getType().getMethod("refuel",float.class),upgrades.getType().getMethod("wash",Player.class));bindings.put(c,b);return b;
    }
    private static double number(Method m,Object target)throws ReflectiveOperationException{double n=((Number)m.invoke(target)).doubleValue();if(!Double.isFinite(n)||n<0)throw new IllegalStateException("Invalid vehicle quantity");return n;}
    public static VehicleServiceEvent.View inspect(Entity e){
        if(!(e.level() instanceof ServerLevel sl)||!sl.getServer().isSameThread())return null;
        try{var b=binding(e);if(b==null)return null;var systems=b.systems.get(e);var equipment=b.equipment.get(e);var comfort=b.comfort.get(systems);var upgrades=b.upgrades.get(systems);boolean modern=enabled();var driver=modern?null:b.driver.invoke(e);UUID driverId=driver instanceof Player p&&e.hasPassenger(p)?p.getUUID():null;
            UUID owner=(UUID)b.owner.invoke(equipment);String kind="car",busLine="";
            if(modern){
                var snapshotClass=Class.forName("de.nevergate.fahrzeuge.VehicleSnapshot",true,e.getClass().getClassLoader());
                var optional=(Optional<?>)snapshotClass.getMethod("of",e.getClass()).invoke(null,e);if(optional.isEmpty())return null;var snapshot=optional.get();
                driverId=(UUID)snapshotClass.getMethod("driver").invoke(snapshot);owner=(UUID)snapshotClass.getMethod("owner").invoke(snapshot);
                String type=(String)snapshotClass.getMethod("type").invoke(snapshot);kind=type.equals("ng_bus")?"bus":type.equals("ng_motorcycle")?"motorcycle":"car";
                if(kind.equals("bus")){var expansion=systems.getClass().getField("expansion").get(systems);busLine=(String)expansion.getClass().getMethod("route").invoke(expansion);}
            }
            int capacity=(int)Math.round(number(b.capacity,systems)*1000);int fuel=(int)Math.floor(number(b.fuel,systems)*1000+.0001);int dirt=(int)Math.ceil(number(b.dirt,upgrades));
            // The legacy mod has one unspecified fuel and no electric drivetrain. Do not invent petrol/diesel or traction energy.
            return new VehicleServiceEvent.View(e.getUUID(),owner,(String)b.plate.invoke(comfort),kind,"ng_fahrzeuge:standard",fuel,capacity,Math.min(100,dirt),false,0,0,driverId,busLine);
        }catch(ReflectiveOperationException|RuntimeException ex){return null;}
    }
    public static Boolean canService(Entity e,Player p){try{var b=binding(e);return b==null?null:(Boolean)b.allowed.invoke(b.systems.get(e),p);}catch(ReflectiveOperationException|RuntimeException ex){return false;}}
    private static void flush(ServerLevel sl){sl.getServer().overworld().getDataStorage().save();}
    public static void event(VehicleServiceEvent e){
        if(!(e.vehicle.level() instanceof ServerLevel sl)||!sl.getServer().isSameThread()||!e.vehicle.isAlive())return;
        if(e.operation==VehicleServiceEvent.Operation.READ){if(e.view()==null){var v=inspect(e.vehicle);if(v!=null)e.describe(v);}return;}
        if(e.completed()||e.operation==VehicleServiceEvent.Operation.CHARGE||e.player==null||e.player.level()!=sl||e.player.isSpectator()||!e.player.isAlive()||e.player.distanceToSqr(e.vehicle)>64)return;
        try{
            var b=binding(e.vehicle);if(b==null)return;var journal=VehicleServiceJournal.get(sl);var old=journal.lookup(e.transaction);
            if(old!=null){if(old.matches(e)&&old.completed)e.complete(old.delivered,old.proof());return;}
            if(!Boolean.TRUE.equals(canService(e.vehicle,e.player)))return;
            if(e.operation==VehicleServiceEvent.Operation.FUEL&&(e.requested<=0||e.requested>50000)||e.operation==VehicleServiceEvent.Operation.WASH&&(e.requested<1||e.requested>3))return;
            var receipt=journal.begin(e);if(receipt==null)return;flush(sl); // Durable uncertain record BEFORE changing the vehicle.
            var systems=b.systems.get(e.vehicle);int actual;
            if(e.operation==VehicleServiceEvent.Operation.FUEL){double used=((Number)b.refuel.invoke(systems,e.requested/1000F)).doubleValue();if(!Double.isFinite(used)||used<0)return;actual=Math.min(e.requested,(int)Math.floor(used*1000+.0001));}
            else actual=Boolean.TRUE.equals(b.wash.invoke(b.upgrades.get(systems),e.player))?1:0;
            if(actual<=0)return;
            // The legacy API has no transactional save operation: wait for its real entity/chunk disk save before confirming.
            sl.save(null,true,false);
            journal.complete(receipt,actual);flush(sl);e.complete(actual,receipt.proof());
        }catch(ReflectiveOperationException ex){throw new IllegalStateException("NG-Fahrzeuge service uncertain; reconcile saved receipt",ex);}
    }
}
