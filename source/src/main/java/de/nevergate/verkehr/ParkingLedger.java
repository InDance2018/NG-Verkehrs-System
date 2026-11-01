package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraftforge.common.MinecraftForge;

/** Server-authoritative tickets; paper carries only an opaque lookup UUID. */
public final class ParkingLedger extends SavedData {
    public static final int MAX_RECORDS=50000;
    public static final long DAY=1728000L; // 24 real hours at 20 ticks/sec
    public static final class Ticket {
        public UUID id,owner,zone;
        public long entered,validUntil,paidCents;
        public boolean pass,paid,used,uncertain;
        public String receipt="";
        public UUID transaction;
        Ticket(UUID owner,UUID zone,long now,boolean pass){id=UUID.randomUUID();this.owner=owner;this.zone=zone;entered=now;this.pass=pass;}
    }
    private final Map<UUID,Ticket> records=new HashMap<>();
    private final Set<UUID> inFlight=new HashSet<>();
    public static ParkingLedger get(ServerLevel l){
        return l.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(ParkingLedger::new,ParkingLedger::load,DataFixTypes.LEVEL),"ng_verkehr_parking");
    }
    public static long now(ServerLevel l){return l.getServer().overworld().getGameTime();}
    public Ticket lookup(UUID id){return records.get(id);}
    public Ticket existing(UUID owner,UUID zone,boolean pass,long now){
        return records.values().stream().filter(t->t.owner.equals(owner)&&t.zone.equals(zone)&&t.pass==pass&&!t.used&&(!pass||t.validUntil>=now)).findFirst().orElse(null);
    }
    public Ticket issue(UUID owner,UUID zone,long now,boolean pass,long duration){
        if(pass&&duration<=0)return null;
        if(existing(owner,zone,pass,now)!=null||records.size()>=MAX_RECORDS)return null;
        var t=new Ticket(owner,zone,now,pass);if(pass){t.paid=true;t.validUntil=now+Math.min(DAY*365,duration);}
        records.put(t.id,t);setDirty();return t;
    }
    public static long fee(long ticks,int centsPerHour,int freeMinutes,int cap){
        ticks=Math.max(0,ticks);long free=Math.max(0,Math.min(1440,freeMinutes))*1200L;
        if(ticks<=free)return 0;
        long bill=ticks-free,full=bill/DAY,rest=bill%DAY;
        long daily=(24L*Math.max(0,centsPerHour));if(cap>0)daily=Math.min(daily,cap);
        long partial=((rest+71999)/72000)*Math.max(0,centsPerHour);if(cap>0)partial=Math.min(partial,cap);
        return Math.min(10000000,full*daily+partial);
    }
    public boolean usable(UUID id,UUID owner,UUID zone,long now){
        var t=records.get(id);return t!=null&&t.owner.equals(owner)&&t.zone.equals(zone)&&!t.used&&!t.uncertain&&t.paid&&t.validUntil>=now;
    }
    public boolean consume(UUID id,UUID owner,UUID zone,long now){
        if(!usable(id,owner,zone,now))return false;var t=records.get(id);if(!t.pass)t.used=true;setDirty();return true;
    }
    public boolean pay(ServerPlayer p,UUID id,UUID zone,long amount,long until){
        var t=records.get(id);long now=now(p.serverLevel());
        if(t==null||!t.owner.equals(p.getUUID())||!t.zone.equals(zone)||t.pass||t.used||t.uncertain||amount<0||until<now||!inFlight.add(id))return false;
        try{
            if(t.paid&&t.validUntil>=now)return true;
            t.uncertain=true;t.transaction=UUID.randomUUID();setDirty();p.serverLevel().getServer().overworld().getDataStorage().save(); // exceptions/pending responses cannot turn into a paid ticket
            if(amount>0){
                var event=new EconomyPaymentEvent(p,t.transaction,zone,amount,"ng_verkehr:parking");
                try{MinecraftForge.EVENT_BUS.post(event);}catch(RuntimeException ex){return false;}
                if(!event.confirmed()){t.uncertain=false;setDirty();return false;}
                t.receipt=event.receipt();
            }
            t.paid=true;t.paidCents=Math.min(10000000,t.paidCents+amount);t.validUntil=until;t.uncertain=false;setDirty();p.serverLevel().getServer().overworld().getDataStorage().save();return true;
        }finally{inFlight.remove(id);}
    }
    public int revokePasses(UUID owner,UUID zone){int count=0;for(var t:records.values())if(t.pass&&t.owner.equals(owner)&&t.zone.equals(zone)&&!t.used){t.used=true;count++;}if(count>0)setDirty();return count;}
    public void prune(long now){if(records.entrySet().removeIf(e->e.getValue().used&&now-e.getValue().entered>DAY*30||e.getValue().pass&&now-e.getValue().validUntil>DAY*30))setDirty();}
    @Override public CompoundTag save(CompoundTag root){
        var list=new ListTag();for(var t:records.values()){
            var n=new CompoundTag();n.putUUID("Id",t.id);if(t.transaction!=null)n.putUUID("Transaction",t.transaction);n.putUUID("Owner",t.owner);n.putUUID("Zone",t.zone);n.putLong("Entered",t.entered);n.putLong("Until",t.validUntil);n.putLong("PaidCents",t.paidCents);n.putBoolean("Pass",t.pass);n.putBoolean("Paid",t.paid);n.putBoolean("Used",t.used);n.putBoolean("Uncertain",t.uncertain);n.putString("Receipt",t.receipt);list.add(n);
        }root.put("Tickets",list);return root;
    }
    public static ParkingLedger load(CompoundTag root){
        var ledger=new ParkingLedger();for(var e:root.getList("Tickets",10)){
            var n=(CompoundTag)e;if(!n.hasUUID("Id")||!n.hasUUID("Owner")||!n.hasUUID("Zone")||ledger.records.size()>=MAX_RECORDS)continue;
            var t=new Ticket(n.getUUID("Owner"),n.getUUID("Zone"),Math.max(0,n.getLong("Entered")),n.getBoolean("Pass"));t.id=n.getUUID("Id");t.transaction=n.hasUUID("Transaction")?n.getUUID("Transaction"):null;t.validUntil=Math.max(0,n.getLong("Until"));t.paidCents=Math.max(0,Math.min(10000000,n.getLong("PaidCents")));t.paid=n.getBoolean("Paid");t.used=n.getBoolean("Used");t.uncertain=n.getBoolean("Uncertain");t.receipt=n.getString("Receipt");ledger.records.put(t.id,t);
        }return ledger;
    }
}
