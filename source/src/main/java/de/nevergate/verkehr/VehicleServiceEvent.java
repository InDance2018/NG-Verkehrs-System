package de.nevergate.verkehr;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/** Optional synchronous server-thread adapter, protocol 1. No money is moved here. */
public final class VehicleServiceEvent extends Event {
    public enum Operation { READ, FUEL, WASH, CHARGE }
    public record View(UUID vehicle, UUID owner, String plate, String kind, String fuel,
                       int fuelMilliLitres, int fuelCapacityMilliLitres, int dirtPercent,
                       boolean electric, int energyWh, int capacityWh, UUID driver, String busLine) {
        public View(UUID vehicle,UUID owner,String plate,String kind,String fuel,int fuelMilliLitres,int fuelCapacityMilliLitres,int dirtPercent,boolean electric,int energyWh,int capacityWh,UUID driver){this(vehicle,owner,plate,kind,fuel,fuelMilliLitres,fuelCapacityMilliLitres,dirtPercent,electric,energyWh,capacityWh,driver,"");}
        /** Keeps the dev10 constructor available for existing providers. */
        public View(UUID vehicle,UUID owner,String plate,String kind,String fuel,int fuelMilliLitres,int fuelCapacityMilliLitres,int dirtPercent,boolean electric,int energyWh,int capacityWh){this(vehicle,owner,plate,kind,fuel,fuelMilliLitres,fuelCapacityMilliLitres,dirtPercent,electric,energyWh,capacityWh,null,"");}
        public View {
            if(busLine==null||busLine.length()>12||vehicle==null||plate==null||kind==null||fuel==null||plate.length()>32||kind.length()>32||fuel.length()>32
                ||fuelMilliLitres<0||fuelCapacityMilliLitres<fuelMilliLitres||dirtPercent<0||dirtPercent>100
                ||energyWh<0||capacityWh<energyWh||!electric&&(energyWh!=0||capacityWh!=0))throw new IllegalArgumentException("Invalid vehicle snapshot");
        }
    }
    public final int protocol=1;
    public final Entity vehicle;
    public final ServerPlayer player;
    public final UUID transaction;
    public final Operation operation;
    /** FUEL: millilitres, CHARGE: Wh, WASH: program 1..3. */
    public final int requested;
    private View view;
    private boolean completed;
    private int delivered;
    private String receipt="";
    public VehicleServiceEvent(Entity vehicle,ServerPlayer player,UUID transaction,Operation operation,int requested){
        this.vehicle=vehicle;this.player=player;this.transaction=transaction;this.operation=operation;this.requested=requested;
        if(vehicle==null||operation==null||operation!=Operation.READ&&(player==null||transaction==null||requested<=0))throw new IllegalArgumentException();
    }
    public void describe(View value){if(view!=null||!vehicle.getUUID().equals(value.vehicle()))throw new IllegalStateException("Snapshot mismatch / second provider");view=value;}
    /** Only after durable, authorized, deduplicated execution. WASH returns 1, others actual units. */
    public void complete(int actual,String proof){
        if(operation==Operation.READ||completed||actual<=0||actual>(operation==Operation.WASH?1:requested)||proof==null||proof.isBlank()||proof.length()>160)throw new IllegalArgumentException("Invalid service receipt");
        completed=true;delivered=actual;receipt=proof;
    }
    public View view(){return view;}public boolean completed(){return completed;}public int delivered(){return delivered;}public String receipt(){return receipt;}
}
