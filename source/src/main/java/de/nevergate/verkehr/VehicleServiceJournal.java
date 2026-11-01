package de.nevergate.verkehr;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
/** Write-ahead receipts for the narrowly versioned legacy vehicle bridge. */
public final class VehicleServiceJournal extends SavedData {
    public static final class Receipt {
        public final UUID transaction,vehicle,player;public final String operation;public final int requested;
        public boolean completed;public int delivered;
        Receipt(UUID transaction,UUID vehicle,UUID player,String operation,int requested){this.transaction=transaction;this.vehicle=vehicle;this.player=player;this.operation=operation;this.requested=requested;}
        public boolean matches(VehicleServiceEvent e){return vehicle.equals(e.vehicle.getUUID())&&player.equals(e.player.getUUID())&&operation.equals(e.operation.name())&&requested==e.requested;}
        public String proof(){return "ng-fahrzeuge-service/"+transaction;}
    }
    private final Map<UUID,Receipt> receipts=new LinkedHashMap<>();
    public static VehicleServiceJournal get(ServerLevel l){return l.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(VehicleServiceJournal::new,VehicleServiceJournal::load,DataFixTypes.LEVEL),"ng_verkehr_vehicle_receipts");}
    public Receipt lookup(UUID key){return receipts.get(key);}
    public Receipt begin(VehicleServiceEvent e){if(e.player==null||e.transaction==null)return null;var old=receipts.get(e.transaction);if(old!=null)return old.matches(e)?old:null;if(receipts.size()>=50000)return null;var r=new Receipt(e.transaction,e.vehicle.getUUID(),e.player.getUUID(),e.operation.name(),e.requested);receipts.put(r.transaction,r);setDirty();return r;}
    public void complete(Receipt r,int delivered){r.completed=true;r.delivered=delivered;setDirty();}
    public static VehicleServiceJournal load(CompoundTag t){var j=new VehicleServiceJournal();for(var tag:t.getList("Receipts",10)){var n=(CompoundTag)tag;if(j.receipts.size()>=50000)break;if(!n.hasUUID("Transaction")||!n.hasUUID("Vehicle")||!n.hasUUID("Player"))continue;var r=new Receipt(n.getUUID("Transaction"),n.getUUID("Vehicle"),n.getUUID("Player"),n.getString("Operation"),n.getInt("Requested"));r.completed=n.getBoolean("Completed");r.delivered=n.getInt("Delivered");j.receipts.put(r.transaction,r);}return j;}
    @Override public CompoundTag save(CompoundTag t){var list=new ListTag();for(var r:receipts.values()){var n=new CompoundTag();n.putUUID("Transaction",r.transaction);n.putUUID("Vehicle",r.vehicle);n.putUUID("Player",r.player);n.putString("Operation",r.operation);n.putInt("Requested",r.requested);n.putBoolean("Completed",r.completed);n.putInt("Delivered",r.delivered);list.add(n);}t.put("Receipts",list);return t;}
}
