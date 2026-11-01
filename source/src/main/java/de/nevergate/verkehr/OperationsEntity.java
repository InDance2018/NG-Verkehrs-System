package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import static de.nevergate.verkehr.OperationsBlock.Kind.*;

public final class OperationsEntity extends BlockEntity {
    private UUID id=UUID.randomUUID(),owner,transaction,target,customer;
    private record Ref(BlockPos pos,UUID id){}
    private final List<Ref> links=new ArrayList<>();
    private final List<Ref> watched=new ArrayList<>();
    private final Map<UUID,UUID> cards=new LinkedHashMap<>();
    private final Set<String> plates=new LinkedHashSet<>();
    private record PlateGrant(UUID vehicle,UUID owner){}
    private final Map<String,PlateGrant> plateGrants=new LinkedHashMap<>();
    private int amount=10000,program=1,startHour=8,endHour=17,headway=10,speed=30,direction;
    private boolean journalRestored;
    private boolean enabled=true,freeService,manualVisibility,latched,online,alarm,open;
    private int progress,faults,activeUnits,phase;
    private String line="1",destination="Zentrum",detail="",receipt="";
    private long openUntil,arrivalUntil,sessionStart,nextServiceAt;
    // 0 idle, 1 running, 2 completed, 3 unknown (manual reconciliation only), 4 aborted.
    private int sessionState;
    public OperationsEntity(BlockPos p,BlockState s){super(NgTraffic.OPERATIONS_ENTITY.get(),p,s);}
    public OperationsBlock.Kind kind(){return ((OperationsBlock)getBlockState().getBlock()).kind;}
    public UUID identity(){return id;}public void setOwner(UUID value){owner=value;changed();}
    public boolean canEdit(Player p){return level!=null&&level.mayInteract(p,worldPosition)&&(p.hasPermissions(2)||p.isCreative()&&(owner==null||owner.equals(p.getUUID())));}
    public boolean canUse(Player p){restoreJournal();return level!=null&&!level.isClientSide&&level.mayInteract(p,worldPosition)&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
    public boolean online(){return online;}public boolean alarm(){return alarm;}public boolean open(){return open;}public String detail(){return detail;}
    public int progress(){return progress;}public int state(){return sessionState;}public int faults(){return faults;}public int activeUnits(){return activeUnits;}public int amount(){return amount;}public int program(){return program;}public int startHour(){return startHour;}public int endHour(){return endHour;}public int headway(){return headway;}public int speed(){return speed;}public int direction(){return direction;}public int linked(){return links.size()+watched.size();}public boolean enabled(){return enabled;}public boolean freeService(){return freeService;}public boolean visibility(){return manualVisibility;}public String line(){return line;}public String destination(){return destination;}public int phase(){return phase;}
    private OperationsEntity resolve(Ref r){
        if(!(level instanceof ServerLevel sl)||worldPosition.distSqr(r.pos)>128*128)return null;
        var c=sl.getChunkSource().getChunkNow(r.pos.getX()>>4,r.pos.getZ()>>4);if(c==null)return null;
        return c.getBlockEntity(r.pos) instanceof OperationsEntity e&&r.id.equals(e.id)?e:null;
    }
    public OperationsEntity linked(OperationsBlock.Kind k){for(var r:links){var e=resolve(r);if(e!=null&&e.kind()==k)return e;}return null;}
    public boolean connect(BlockEntity b,Player p){
        if(kind()==OPERATIONS_DESK&&b!=null&&!(b instanceof OperationsEntity)){
            var other=ServiceEntity.identity(b);if(!canEdit(p)||b.getLevel()!=level||!level.mayInteract(p,b.getBlockPos())||other==null||worldPosition.distSqr(b.getBlockPos())>128*128||watched.size()>=64)return false;
            if(watched.stream().noneMatch(r->r.id.equals(other)))watched.add(new Ref(b.getBlockPos().immutable(),other));changed();return true;
        }
        if(!(b instanceof OperationsEntity n)||n==this||n.level!=level||!canEdit(p)||!n.canEdit(p)||worldPosition.distSqr(n.worldPosition)>128*128)return false;
        boolean allow=switch(kind()){
            case CHARGE_CONTROL->n.kind()==CHARGER;
            case FUEL_CASHIER->n.kind()==FUEL_PUMP;
            case WASH_ENTRY->n.kind()==WASH_TUNNEL;
            case CARD_READER,PLATE_READER,DELIVERY_ZONE->n.kind()==ROAD_BOLLARD;
            case BUS_DISPLAY->n.kind()==BUS_ROUTE||n.kind()==BUS_SENSOR;
            case BUS_SENSOR->n.kind()==BUS_ROUTE;
            case ESCAPE_SIGN->n.kind()==TUNNEL_CALL;
            case OPERATIONS_DESK->true;
            default->false;
        };
        if(!allow)return false;
        if(links.stream().anyMatch(r->r.id.equals(n.id)))return true;
        if(links.size()>=64)return false;
        if(kind()!=OPERATIONS_DESK&&kind()!=CHARGE_CONTROL&&kind()!=FUEL_CASHIER)links.removeIf(r->{var old=resolve(r);return old==null||old.kind()==n.kind();});
        links.add(new Ref(n.worldPosition.immutable(),n.id));changed();return true;
    }
    public static String normalizePlate(String s){return s==null?"":s.strip().toUpperCase(Locale.ROOT).replaceAll("[\\s-]+","");}
    public boolean addPlate(String s){s=normalizePlate(s);if(!s.matches("[A-Z0-9ÄÖÜ]{1,16}")||plates.size()>=128)return false;plates.add(s);changed();return true;}
    public boolean registerPlate(String value){
        restoreJournal();String plate=normalizePlate(value);var e=selected();var v=view(e);
        if(e==null||v==null||v.owner()==null||!normalizePlate(v.plate()).equals(plate)||!addPlate(plate))return false;
        plateGrants.put(plate,new PlateGrant(e.getUUID(),v.owner()));durable();return true;
    }
    private void readPlateAuthority(CompoundTag t){
        plates.clear();for(var tag:t.getList("Plates",8)){String s=normalizePlate(tag.getAsString());if(s.matches("[A-Z0-9ÄÖÜ]{1,16}")&&plates.size()<128)plates.add(s);}
        plateGrants.clear();for(var tag:t.getList("PlateGrants",10)){var n=(CompoundTag)tag;String plate=normalizePlate(n.getString("Plate"));if(plates.contains(plate)&&n.hasUUID("Vehicle")&&n.hasUUID("Owner")&&plateGrants.size()<128)plateGrants.put(plate,new PlateGrant(n.getUUID("Vehicle"),n.getUUID("Owner")));}
    }
    public static boolean inWindow(long daytime,int start,int end){int h=(int)(Math.floorMod(daytime,24000)/1000+6)%24;return start==end||start<end&&h>=start&&h<end||start>end&&(h>=start||h<end);}
    public void schedule(int start,int end){startHour=Math.floorMod(start,24);endHour=Math.floorMod(end,24);changed();}
    public void route(String l,String d){line=l.strip().substring(0,Math.min(12,l.strip().length()));destination=d.strip().substring(0,Math.min(48,d.strip().length()));changed();}
    private void message(Player p,String s){p.displayClientMessage(Component.literal(s),true);}
    private boolean vehicle(Entity e){return e.isAlive()&&e.getVehicle()==null&&(RoadSystemEntity.vehicle(e)||e.getTags().contains("ng_vehicle"));}
    private List<Entity> nearby(){return level.getEntities((Entity)null,new AABB(worldPosition).inflate(4,2,4),this::vehicle);}
    private Entity selected(){var found=nearby();return found.size()==1?found.get(0):null;}
    private VehicleServiceEvent.View view(Entity e){
        if(e==null)return null;var ev=new VehicleServiceEvent(e,null,null,VehicleServiceEvent.Operation.READ,0);
        try{MinecraftForge.EVENT_BUS.post(ev);return ev.view();}catch(RuntimeException ex){return null;}
    }
    private Player driver(Entity e,VehicleServiceEvent.View v){if(e==null||v==null)return null;if(v.driver()!=null){for(var passenger:e.getPassengers())if(passenger instanceof Player p&&p.getUUID().equals(v.driver()))return p;return null;}return e.getControllingPassenger() instanceof Player p?p:null;}
    private boolean authorized(Entity e,VehicleServiceEvent.View v,Player p){return e!=null&&v!=null&&(p.getUUID().equals(v.owner())||driver(e,v)==p);}
    private boolean obstruction(){return !level.getEntities((Entity)null,new AABB(worldPosition).inflate(.05,0,.05),e->e.isAlive()&&!e.isSpectator()&&!(e instanceof net.minecraft.world.entity.item.ItemEntity)&&!(e instanceof net.minecraft.world.entity.ExperienceOrb)).isEmpty();}
    private void openLease(){openUntil=level.getGameTime()+100;changed();}
    private void durable(){changed();if(level instanceof ServerLevel sl){OperationsJournal.get(sl).checkpoint(id,saveWithoutMetadata());sl.getServer().overworld().getDataStorage().save();journalRestored=true;}}
    private void restoreJournal(){
        if(journalRestored||!(level instanceof ServerLevel sl))return;journalRestored=true;var saved=OperationsJournal.get(sl).lookup(id);if(saved==null)return;
        readPlateAuthority(saved);
        cards.clear();for(var tag:saved.getList("Cards",10)){var n=(CompoundTag)tag;if(n.hasUUID("Token")&&n.hasUUID("Holder")&&cards.size()<256)cards.put(n.getUUID("Token"),n.getUUID("Holder"));}
        sessionState=Math.max(0,Math.min(4,saved.getInt("SessionState")));if(sessionState==1)sessionState=4;
        transaction=saved.hasUUID("Transaction")?saved.getUUID("Transaction"):null;target=saved.hasUUID("Target")?saved.getUUID("Target"):null;customer=saved.hasUUID("Customer")?saved.getUUID("Customer"):null;receipt=saved.getString("Receipt");nextServiceAt=saved.getLong("NextServiceAt");progress=sessionState==2?100:0;
    }
    public boolean issueCard(Player p){
        if(kind()!=CARD_READER||!canEdit(p)||!canUse(p)||cards.size()>=256)return false;
        UUID token=UUID.randomUUID();cards.put(token,p.getUUID());var item=new ItemStack(NgTraffic.ACCESS_CARD.get());item.getOrCreateTag().putUUID("Card",token);item.getOrCreateTag().putUUID("Reader",id);durable();if(!p.getInventory().add(item))p.drop(item,false);message(p,"Persönliche Zufahrtskarte ausgestellt");return true;
    }
    public boolean grantCard(Player admin,Player holder){
        restoreJournal();
        if(kind()!=CARD_READER||!canEdit(admin)||cards.size()>=256)return false;
        UUID token=UUID.randomUUID();cards.put(token,holder.getUUID());var item=new ItemStack(NgTraffic.ACCESS_CARD.get());item.getOrCreateTag().putUUID("Card",token);item.getOrCreateTag().putUUID("Reader",id);durable();if(!holder.getInventory().add(item))holder.drop(item,false);return true;
    }
    private boolean readCard(Player p){
        var gate=linked(ROAD_BOLLARD);if(gate==null||!gate.enabled)return false;
        for(int i=0;i<p.getInventory().getContainerSize();i++){var s=p.getInventory().getItem(i);var t=s.getTag();if(s.is(NgTraffic.ACCESS_CARD.get())&&t!=null&&t.hasUUID("Card")&&t.hasUUID("Reader")&&id.equals(t.getUUID("Reader"))&&p.getUUID().equals(cards.get(t.getUUID("Card")))){gate.openLease();message(p,"Zufahrt freigegeben");return true;}}
        message(p,"Keine gültige persönliche Zufahrtskarte");return false;
    }
    private boolean service(ServerPlayer p){
        if(!enabled||sessionState==1||sessionState==3||level.getGameTime()<nextServiceAt)return false;
        if(!freeService){message(p,"Kostenpflichtige Leistung gesperrt: Zahlungs-/Leistungsprotokoll noch nicht angebunden");return false;}
        var e=selected();var v=view(e);if(!authorized(e,v,p)){message(p,"Genau ein berechtigtes Fahrzeug und Fahrzeugadapter erforderlich");return false;}
        if(Boolean.FALSE.equals(NgVehiclesCompat.canService(e,p))){message(p,"Motor aus, Handbremse an und Fahrzeugberechtigung erforderlich");return false;}
        if(kind()==CHARGER&&(!v.electric()||v.energyWh()>=v.capacityWh())){message(p,"Kein ladbarer Traktionsakku");return false;}
        if(kind()==FUEL_PUMP&&(v.fuel().isBlank()||v.fuelMilliLitres()>=v.fuelCapacityMilliLitres())){message(p,"Kein passender freier Kraftstofftank");return false;}
        if(kind()==WASH_TUNNEL&&v.dirtPercent()==0){message(p,"Fahrzeug ist bereits sauber");return false;}
        nextServiceAt=level.getGameTime()+40;transaction=UUID.randomUUID();target=e.getUUID();customer=p.getUUID();sessionStart=level.getGameTime();sessionState=1;progress=0;receipt="";durable();
        if(kind()!=WASH_TUNNEL)execute(p,e,v);return true;
    }
    private void execute(ServerPlayer p,Entity e,VehicleServiceEvent.View v){
        if(!authorized(e,v,p)||!target.equals(e.getUUID())||Boolean.FALSE.equals(NgVehiclesCompat.canService(e,p))){sessionState=4;detail="Vorgang abgebrochen – Fahrzeug/Berechtigung fehlt";durable();return;}
        var op=kind()==CHARGER?VehicleServiceEvent.Operation.CHARGE:kind()==FUEL_PUMP?VehicleServiceEvent.Operation.FUEL:VehicleServiceEvent.Operation.WASH;
        int requested=kind()==CHARGER?Math.min(amount,Math.max(0,v.capacityWh()-v.energyWh())):kind()==FUEL_PUMP?Math.min(amount,Math.max(0,v.fuelCapacityMilliLitres()-v.fuelMilliLitres())):program;
        if(requested<=0){sessionState=4;detail="Keine zulässige Menge";durable();return;}
        sessionState=3;durable(); // Crash or exception now requires reconciliation, never a fresh automatic transaction.
        var ev=new VehicleServiceEvent(e,p,transaction,op,requested);
        try{MinecraftForge.EVENT_BUS.post(ev);if(ev.completed()){sessionState=2;receipt=ev.receipt();progress=100;detail="Erbracht: "+ev.delivered()+(op==VehicleServiceEvent.Operation.FUEL?" ml":op==VehicleServiceEvent.Operation.CHARGE?" Wh":" Waschprogramm");}else detail="Unbestätigter Vorgang – Belegabgleich erforderlich";}
        catch(RuntimeException ex){detail="Unklarer Vorgang – Belegabgleich erforderlich";}
        durable();
    }
    public boolean action(Player p,int button){
        if(!canUse(p))return false;
        if(button==0){if(!enabled)return false;
            if(kind()==CARD_READER)return readCard(p);
            if(kind()==TUNNEL_CALL){latched=true;changed();message(p,"Notruf ausgelöst – Verkehrsleitstelle verständigen");return true;}
            if(kind()==WASH_ENTRY){var tunnel=linked(WASH_TUNNEL);return tunnel!=null&&p.distanceToSqr(tunnel.worldPosition.getX()+.5,tunnel.worldPosition.getY()+.5,tunnel.worldPosition.getZ()+.5)<=64&&p instanceof ServerPlayer sp&&tunnel.service(sp);}
            if((kind()==CHARGER||kind()==FUEL_PUMP||kind()==WASH_TUNNEL)&&p instanceof ServerPlayer sp)return service(sp);
            return false;
        }
        if(button==1){if(sessionState!=1||!p.getUUID().equals(customer)&&!canEdit(p))return false;sessionState=4;progress=0;durable();return true;}
        if(!canEdit(p))return false;
        switch(button){
            case 10->{enabled=!enabled;openUntil=0;if(!enabled&&sessionState==1){sessionState=4;progress=0;}}
            case 11->{amount=amount>=50000?1000:amount+1000;}
            case 12->{program=program%3+1;}
            case 13->{startHour=(startHour+1)%24;}
            case 14->{endHour=(endHour+1)%24;}
            case 15->{headway=headway>=60?1:headway+1;}
            case 16->{manualVisibility=!manualVisibility;}
            case 17->{if(kind()!=TUNNEL_CALL)return false;latched=false;}
            case 18->{direction=(direction+1)%3;}
            case 19->{links.clear();watched.clear();openUntil=0;}
            case 20->{if(kind()!=CARD_READER)return false;return issueCard(p);}
            case 21->{if(kind()!=CARD_READER)return false;cards.clear();durable();}
            case 22->{if(kind()!=PLATE_READER)return false;plates.clear();plateGrants.clear();durable();}
            case 23->{if(kind()!=CHARGER&&kind()!=FUEL_PUMP&&kind()!=WASH_TUNNEL)return false;freeService=!freeService;}
            case 24->{speed=speed>=80?20:speed+10;}
            default->{return false;}
        }changed();return true;
    }
    public void tick(){
        if(!(level instanceof ServerLevel sl)||level.getGameTime()%4!=0)return;restoreJournal();long now=level.getGameTime();online=enabled;alarm=false;open=false;faults=activeUnits=0;phase=0;
        if(!enabled){detail="Außer Betrieb";if(kind()==ROAD_BOLLARD&&getBlockState().getValue(OperationsBlock.STATUS)==1&&obstruction())open=true;finish(now);return;}
        switch(kind()){
            case CHARGER,FUEL_PUMP,WASH_TUNNEL->{
                var e=selected();var v=view(e);online=v!=null;
                if(sessionState==1&&kind()==WASH_TUNNEL){
                    var p=customer==null?null:sl.getServer().getPlayerList().getPlayer(customer);
                    if(e==null||!e.getUUID().equals(target)||p==null||p.level()!=level||!canUse(p)||!authorized(e,v,p)){sessionState=4;progress=0;detail="Waschen abgebrochen – Fahrzeug/Spieler entfernt";durable();}
                    else{long elapsed=now-sessionStart;progress=(int)Math.min(99,elapsed*100/(program*200));phase=(int)(elapsed/20%3)+1;detail="Waschprogramm "+program+": "+progress+" %";if(elapsed>=program*200)execute(p,e,v);}
                }else if(sessionState==3){alarm=true;detail="Unklarer Vorgang – Belegabgleich erforderlich";}
                else if(sessionState==4)detail="Vorgang abgebrochen";
                else if(sessionState==0)detail=v==null?"Fahrzeugadapter / eindeutiges Fahrzeug fehlt":!freeService?"Zahlungsanbindung fehlt":kind()==CHARGER&&!v.electric()?"Kein Elektroantrieb":"Kostenloser Betrieb bereit";
            }
            case CHARGE_CONTROL,FUEL_CASHIER,OPERATIONS_DESK->{
                for(var r:links){var n=resolve(r);if(n==null||!n.online||n.alarm)faults++;if(n!=null&&n.sessionState==1)activeUnits++;}
                for(var r:watched){var chunk=sl.getChunkSource().getChunkNow(r.pos.getX()>>4,r.pos.getZ()>>4);var b=chunk==null?null:chunk.getBlockEntity(r.pos);boolean bad=b==null||!r.id.equals(ServiceEntity.identity(b))||b instanceof ServiceEntity x&&(!x.online()||x.alarm())||b instanceof RoadSystemEntity x&&!x.online()||b instanceof TrafficNodeEntity x&&(!x.online()||x.alarm())||b instanceof DeviceEntity x&&!x.isEnabled();if(bad)faults++;}
                online=linked()>0&&enabled;alarm=faults>0;detail=linked()==0?"Keine Geräte verbunden":linked()+" Geräte · "+activeUnits+" aktiv · "+faults+" Störungen";
            }
            case WASH_ENTRY->{var n=linked(WASH_TUNNEL);online=n!=null&&n.online;open=online&&n.enabled&&n.sessionState!=1&&n.sessionState!=3;detail=!online?"Waschtunnel fehlt / nicht bereit":open?"Einfahrt frei – Programm am Gerät starten":"Bitte warten";}
            case ROAD_BOLLARD->{open=now<openUntil;if(!open&&getBlockState().getValue(OperationsBlock.STATUS)==1&&obstruction()){open=true;openUntil=now+20;}detail=open?"Poller abgesenkt":"Zufahrt gesperrt";}
            case CARD_READER->{var g=linked(ROAD_BOLLARD);online=g!=null&&g.enabled;detail=online?cards.size()+" persönliche Karten gültig":"Poller fehlt / gesperrt";}
            case PLATE_READER->{
                var gate=linked(ROAD_BOLLARD);online=gate!=null&&gate.enabled;var es=nearby();
                if(es.size()==1){var e=es.get(0);var v=view(e);var p=driver(e,v) instanceof ServerPlayer sp?sp:null;String plate=v==null?"":normalizePlate(v.plate());
                    var grant=plateGrants.get(plate);
                    if(online&&p!=null&&authorized(e,v,p)&&!plate.isEmpty()&&plates.contains(plate)&&grant!=null&&grant.vehicle.equals(e.getUUID())&&grant.owner.equals(v.owner())){gate.openLease();open=true;}
                    detail=v==null?"Kennzeichenadapter fehlt":open?"Kennzeichen freigegeben":"Keine berechtigte Zufahrt";
                }else detail=es.isEmpty()?"Kennzeichenleser bereit":"Mehrdeutige Fahrzeugerkennung – gesperrt";
            }
            case DELIVERY_ZONE->{var gate=linked(ROAD_BOLLARD);online=gate!=null&&gate.enabled;open=online&&inWindow(level.getDayTime(),startHour,endHour);if(open)gate.openLease();detail=open?"Lieferzeit – Zufahrt offen":"Außerhalb Lieferzeit / Poller fehlt";}
            case BUS_ROUTE->detail="Linie "+line+" → "+destination+" · Takt "+headway+" Spielminuten";
            case BUS_SENSOR->{var route=linked(BUS_ROUTE);online=route!=null&&route.enabled;boolean detected=false;for(var e:nearby()){var v=view(e);if(v!=null&&v.kind().equals("bus")?route!=null&&!v.busLine().isBlank()&&route.line.equals(v.busLine()):e.getTags().contains("ng_bus")){detected=true;break;}}if(online&&detected)arrivalUntil=now+80;open=online&&now<arrivalUntil;detail=open?"BUS ANGEKOMMEN":"Keine Busankunft";}
            case BUS_DISPLAY->{var route=linked(BUS_ROUTE);var sensor=linked(BUS_SENSOR);online=route!=null&&route.enabled&&sensor!=null&&sensor.online&&sensor.linked(BUS_ROUTE)==route;long period=(route==null?10:route.headway)*1000L/60;long eta=Math.floorMod(-level.getDayTime(),period);detail=!online?"Linie / Ankunftssensor fehlt":"Linie "+route.line+" → "+route.destination+(sensor.open?" · JETZT":" · Plan: "+((eta*60+999)/1000)+" Spielmin");}
            case SCHOOL_WARNING->{alarm=inWindow(level.getDayTime(),startHour,endHour);detail=alarm?"SCHULWEG – Tempo "+speed:"Schulweg-Warnzeit inaktiv";}
            case WORK_EXIT->{alarm=!nearby().isEmpty();detail=alarm?"ACHTUNG – Baustellenausfahrt":"Baustellenausfahrt überwacht";}
            case WEATHER_WARNING->{alarm=sl.isRainingAt(worldPosition.above())||sl.isThundering();detail=alarm?"NÄSSE / GEWITTER – Tempo "+speed:"Keine Wetterwarnung";}
            case VISIBILITY_CONTROL->{alarm=manualVisibility||sl.isThundering();detail=alarm?"SICHTWARNUNG – Tempo "+speed:"Sichtalarm aus";}
            case TUNNEL_CALL->{alarm=latched;detail=latched?"NOTRUF AKTIV – Hilfe erforderlich":"Notruf bereit";}
            case ESCAPE_SIGN->{var call=linked(TUNNEL_CALL);online=call!=null&&call.enabled;alarm=!online||call.latched;detail=(alarm?"FLUCHTWEG ":"NOTAUSGANG ")+(direction==0?"←":direction==1?"→":"↑");}
        }
        finish(now);
    }
    private void finish(long now){int status=kind()==ROAD_BOLLARD&&open?1:!online?3:alarm?2:open||sessionState==1?1:0;if(getBlockState().getValue(OperationsBlock.STATUS)!=status){level.setBlock(worldPosition,getBlockState().setValue(OperationsBlock.STATUS,status),3);level.updateNeighborsAt(worldPosition,getBlockState().getBlock());}if(now%20==0)changed();}
    public void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t){
        super.saveAdditional(t);t.putUUID("Identity",id);if(owner!=null)t.putUUID("Owner",owner);t.putBoolean("Enabled",enabled);t.putBoolean("FreeService",freeService);t.putBoolean("Visibility",manualVisibility);t.putBoolean("Latched",latched);t.putInt("Amount",amount);t.putInt("Program",program);t.putInt("Start",startHour);t.putInt("End",endHour);t.putInt("Headway",headway);t.putInt("Speed",speed);t.putInt("Direction",direction);t.putString("Line",line);t.putString("Destination",destination);t.putLong("NextServiceAt",nextServiceAt);t.putInt("SessionState",sessionState);t.putString("Receipt",receipt);if(transaction!=null)t.putUUID("Transaction",transaction);if(target!=null)t.putUUID("Target",target);if(customer!=null)t.putUUID("Customer",customer);
        var refs=new ListTag();for(var r:links){var n=new CompoundTag();n.putLong("Pos",r.pos.asLong());n.putUUID("Id",r.id);refs.add(n);}t.put("Links",refs);
        var ws=new ListTag();for(var r:watched){var n=new CompoundTag();n.putLong("Pos",r.pos.asLong());n.putUUID("Id",r.id);ws.add(n);}t.put("Watched",ws);
        var c=new ListTag();cards.forEach((token,holder)->{var n=new CompoundTag();n.putUUID("Token",token);n.putUUID("Holder",holder);c.add(n);});t.put("Cards",c);
        var ps=new ListTag();for(var plate:plates)ps.add(StringTag.valueOf(plate));t.put("Plates",ps);var grants=new ListTag();plateGrants.forEach((plate,g)->{var n=new CompoundTag();n.putString("Plate",plate);n.putUUID("Vehicle",g.vehicle);n.putUUID("Owner",g.owner);grants.add(n);});t.put("PlateGrants",grants);
        t.putBoolean("Online",online);t.putBoolean("Alarm",alarm);t.putBoolean("Open",open);t.putInt("Progress",progress);t.putInt("Faults",faults);t.putInt("Active",activeUnits);t.putInt("Phase",phase);t.putString("Detail",detail);
    }
    @Override public void load(CompoundTag t){
        super.load(t);journalRestored=false;id=t.hasUUID("Identity")?t.getUUID("Identity"):UUID.randomUUID();owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;enabled=t.getBoolean("Enabled");freeService=t.getBoolean("FreeService");manualVisibility=t.getBoolean("Visibility");latched=t.getBoolean("Latched");amount=Math.max(1000,Math.min(50000,t.getInt("Amount")));program=Math.max(1,Math.min(3,t.getInt("Program")));startHour=Math.floorMod(t.getInt("Start"),24);endHour=Math.floorMod(t.getInt("End"),24);headway=Math.max(1,Math.min(60,t.getInt("Headway")));speed=Math.max(20,Math.min(80,t.getInt("Speed")));direction=Math.floorMod(t.getInt("Direction"),3);line=t.getString("Line");if(line.length()>12)line=line.substring(0,12);destination=t.getString("Destination");if(destination.length()>48)destination=destination.substring(0,48);nextServiceAt=t.getLong("NextServiceAt");sessionState=Math.max(0,Math.min(4,t.getInt("SessionState")));receipt=t.getString("Receipt");if(receipt.length()>160)receipt=receipt.substring(0,160);transaction=t.hasUUID("Transaction")?t.getUUID("Transaction"):null;target=t.hasUUID("Target")?t.getUUID("Target"):null;customer=t.hasUUID("Customer")?t.getUUID("Customer"):null;
        links.clear();for(var tag:t.getList("Links",10)){var n=(CompoundTag)tag;if(n.hasUUID("Id")&&links.size()<64)links.add(new Ref(BlockPos.of(n.getLong("Pos")),n.getUUID("Id")));}
        watched.clear();for(var tag:t.getList("Watched",10)){var n=(CompoundTag)tag;if(n.hasUUID("Id")&&watched.size()<64)watched.add(new Ref(BlockPos.of(n.getLong("Pos")),n.getUUID("Id")));}
        cards.clear();for(var tag:t.getList("Cards",10)){var n=(CompoundTag)tag;if(n.hasUUID("Token")&&n.hasUUID("Holder")&&cards.size()<256)cards.put(n.getUUID("Token"),n.getUUID("Holder"));}
        readPlateAuthority(t);
        boolean client=level!=null&&level.isClientSide;if(!client&&sessionState==1)sessionState=4;online=client&&t.getBoolean("Online");alarm=client&&t.getBoolean("Alarm");open=client&&t.getBoolean("Open");progress=client?t.getInt("Progress"):sessionState==2?100:0;phase=client?t.getInt("Phase"):0;detail=t.getString("Detail");faults=t.getInt("Faults");activeUnits=t.getInt("Active");openUntil=arrivalUntil=0;
    }
    @Override public CompoundTag getUpdateTag(){var t=saveWithoutMetadata();for(var key:List.of("Owner","Cards","Plates","PlateGrants","Transaction","Target","Customer","Receipt"))t.remove(key);return t;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
