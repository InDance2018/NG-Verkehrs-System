package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ServiceEntity extends BlockEntity {
    private UUID id=UUID.randomUUID(),owner,reservedFor;
    private final Map<String,Ref> links=new LinkedHashMap<>();
    private final List<Ref> watched=new ArrayList<>();
    private record Ref(BlockPos pos,UUID id,String type){}
    private int rate=200,freeMinutes=10,cap=1500,duration=60,group,limit=30,height=220,interval=120,mode;
    private int free=-1,faults,effectiveSpeed=50,detectedHeight;
    private boolean online,alarm,green,waiting;
    private long openUntil,lastUse=-1,started=-1,nextRelease,greenUntil,signalGeneration;
    private String detail="";
    private final Set<UUID> bicycles=new HashSet<>();
    private final Map<UUID,Integer> rampSides=new HashMap<>();
    public void reserve(UUID player){reservedFor=player;changed();}
    public ServiceEntity(BlockPos p,BlockState s){super(NgTraffic.SERVICE_ENTITY.get(),p,s);}
    public ServiceBlock.Kind kind(){return ((ServiceBlock)getBlockState().getBlock()).kind;}
    public UUID identity(){return id;}
    public void owner(UUID o){owner=o;changed();}
    public boolean canEdit(Player p){return level!=null&&level.mayInteract(p,worldPosition)&&(p.isCreative()||p.hasPermissions(2))&&(owner==null||owner.equals(p.getUUID())||p.hasPermissions(2));}
    public boolean canUse(Player p){return level!=null&&!level.isClientSide&&level.mayInteract(p,worldPosition)&&p.distanceToSqr(worldPosition.getX()+.5,worldPosition.getY()+.5,worldPosition.getZ()+.5)<=64;}
    private BlockEntity loaded(BlockPos p){if(!(level instanceof ServerLevel l)||p==null)return null;var c=l.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4);return c==null?null:c.getBlockEntity(p);}
    static UUID identity(BlockEntity e){return e instanceof ServiceEntity s?s.id:e instanceof RoadSystemEntity r?r.identity():e instanceof TrafficNodeEntity n?n.identity():e instanceof DeviceEntity d?d.trafficIdentity():e instanceof ConstructionEntity c?c.identity():null;}
    static String type(BlockEntity e){return e instanceof ServiceEntity?"service":e instanceof RoadSystemEntity?"road":e instanceof TrafficNodeEntity?"traffic":e instanceof DeviceEntity?"signal":e instanceof ConstructionEntity?"construction":"";}
    private Ref ref(BlockEntity e){return new Ref(e.getBlockPos().immutable(),identity(e),type(e));}
    private BlockEntity resolve(Ref r){if(r==null||r.pos.distSqr(worldPosition)>128*128)return null;var e=loaded(r.pos);return e!=null&&r.type.equals(type(e))&&r.id.equals(identity(e))?e:null;}
    private BlockEntity link(String key){return resolve(links.get(key));}
    public ServiceEntity tariff(){return kind()==ServiceBlock.Kind.TARIFF?this:link("tariff") instanceof ServiceEntity s&&s.kind()==ServiceBlock.Kind.TARIFF?s:null;}
    public boolean linkedSignalTo(BlockPos p){var r=links.get("signal");return r!=null&&r.pos.equals(p);}
    public void disconnectSignal(){links.remove("signal");green=waiting=false;changed();}
    public DeviceEntity signal(){return link("signal") instanceof DeviceEntity d&&d.isController()&&d.validConnection(worldPosition,signalGeneration)?d:null;}
    public RoadSystemEntity road(){return link("road") instanceof RoadSystemEntity r?r:null;}
    public int rate(){var t=tariff();return t==null?rate:t.rate;}
    public int freeMinutes(){var t=tariff();return t==null?freeMinutes:t.freeMinutes;}
    public int cap(){var t=tariff();return t==null?cap:t.cap;}
    public int duration(){return duration;}public int group(){return group;}public int limit(){return limit;}public int height(){return height;}public int interval(){return interval;}public int mode(){return mode;}
    public int free(){return free;}public int faults(){return faults;}public boolean online(){return online;}public boolean alarm(){return alarm;}public boolean green(){return green;}public boolean waiting(){return waiting;}public int detectedHeight(){return detectedHeight;}public int effectiveSpeed(){return effectiveSpeed;}public int linked(){return links.size()+watched.size();}public String detail(){return detail;}
    public boolean connect(BlockEntity target,Player p){
        if(!canEdit(p)||target==this||target.getLevel()!=level||worldPosition.distSqr(target.getBlockPos())>128*128||!level.mayInteract(p,target.getBlockPos())||identity(target)==null)return false;
        if(target instanceof ServiceEntity s&&!s.canEdit(p))return false;
        if(kind()==ServiceBlock.Kind.FAULT_MONITOR){if(watched.stream().anyMatch(r->r.pos.equals(target.getBlockPos())))return true;if(watched.size()>=64)return false;watched.add(ref(target));}
        else if(target instanceof ServiceEntity s&&s.kind()==ServiceBlock.Kind.TARIFF)links.put("tariff",ref(target));
        else if(target instanceof DeviceEntity d&&d.isController()&&(kind()==ServiceBlock.Kind.PEDESTRIAN_CALL||kind()==ServiceBlock.Kind.ACOUSTIC_SIGNAL||kind()==ServiceBlock.Kind.BICYCLE_DETECTOR||kind()==ServiceBlock.Kind.BICYCLE_LIGHT)){
            // Registering a new demand device safely disables the controller until explicitly enabled.
            d.setEnabled(false);d.allowConnection(worldPosition);d.registerDevice(worldPosition);signalGeneration=d.connectionGeneration();links.put("signal",ref(target));
        }else if(target instanceof RoadSystemEntity r&&(r.controller()&&(kind()==ServiceBlock.Kind.FLOOR_BOARD||kind()==ServiceBlock.Kind.DYNAMIC_SPEED||kind()==ServiceBlock.Kind.HEIGHT_WARNING||kind()==ServiceBlock.Kind.TARIFF)||r.role()==RoadSystemBlock.Role.BARRIER&&kind()==ServiceBlock.Kind.EXIT_READER)){links.put("road",ref(target));if(r.role()==RoadSystemBlock.Role.BARRIER)r.ticketControlled();}
        else if(target instanceof ServiceEntity s&&s.kind()==ServiceBlock.Kind.RESERVATION_BOLLARD&&kind()==ServiceBlock.Kind.EXIT_READER)links.put("gate",ref(target));
        else if(kind()==ServiceBlock.Kind.DYNAMIC_SPEED&&(target instanceof TrafficNodeEntity n&&(n.kind()==TrafficNodeBlock.Kind.JAM_SENSOR||n.kind()==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER)||target instanceof ConstructionEntity))links.put("trigger",ref(target));
        else return false;
        changed();return true;
    }
    public boolean requestWalk(Player p){
        var c=signal();if(!canUse(p)||c==null)return false;boolean ok=c.requestPedestrians(group);waiting=ok||c.pedestrianBusy(group);say(p,ok?"Anforderung angenommen":"Keine Freigabe: Verbindung und Ampelprogramm prüfen");changed();return ok;
    }
    public void configure(int rate,int free,int cap,int duration,int group,int limit,int height,int interval){
        this.rate=Math.max(0,Math.min(100000,rate));freeMinutes=Math.max(0,Math.min(1440,free));this.cap=Math.max(0,Math.min(1000000,cap));this.duration=Math.max(15,Math.min(1440,duration));this.group=group==1?1:0;this.limit=Math.max(20,Math.min(130,limit/10*10));this.height=Math.max(150,Math.min(650,height/10*10));this.interval=Math.max(60,Math.min(1200,interval));changed();
    }
    private static void say(Player p,String s){p.displayClientMessage(Component.literal(s),true);}
    public UUID ticketId(ItemStack stack){var t=stack.getTag();return (stack.is(NgTraffic.PARK_TICKET.get())||stack.is(NgTraffic.PARK_PASS.get()))&&t!=null&&t.hasUUID("Ticket")?t.getUUID("Ticket"):null;}
    private ParkingLedger.Ticket selected(Player p,boolean passOnly){
        var root=tariff();if(root==null||!(level instanceof ServerLevel sl))return null;var ledger=ParkingLedger.get(sl);
        // Main-hand ticket wins; otherwise inspect the inventory. Never trust item-side prices/expiry.
        var held=ticketId(p.getMainHandItem());if(held!=null){var t=ledger.lookup(held);if(t!=null&&t.owner.equals(p.getUUID())&&t.zone.equals(root.id)&&!t.used&&(!passOnly||t.pass))return t;}
        for(int i=0;i<p.getInventory().getContainerSize();i++){UUID key=ticketId(p.getInventory().getItem(i));var t=key==null?null:ledger.lookup(key);if(t!=null&&t.owner.equals(p.getUUID())&&t.zone.equals(root.id)&&!t.used&&(!passOnly||t.pass))return t;}
        return null;
    }
    private void give(Player p,ParkingLedger.Ticket t){
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(t.id.equals(ticketId(p.getInventory().getItem(i))))return;
        var stack=new ItemStack(t.pass?NgTraffic.PARK_PASS.get():NgTraffic.PARK_TICKET.get());stack.getOrCreateTag().putUUID("Ticket",t.id);
        stack.setHoverName(Component.literal(t.pass?"Dauerparker-Ausweis":"Parkticket"));if(!p.getInventory().add(stack))p.drop(stack,false);
    }
    public boolean issue(Player p,boolean pass){
        if(!canUse(p)||!(level instanceof ServerLevel sl)||tariff()==null||pass&&!canEdit(p))return false;
        long now=ParkingLedger.now(sl);var ledger=ParkingLedger.get(sl);var t=ledger.existing(p.getUUID(),tariff().id,pass,now);
        if(t==null)t=ledger.issue(p.getUUID(),tariff().id,now,pass,pass?ParkingLedger.DAY*30:0);
        if(t==null){say(p,"Kein Ticket verfügbar. Verwaltung prüfen.");return false;}sl.getServer().overworld().getDataStorage().save();give(p,t);say(p,pass?"Dauerparker-Ausweis für 30 Tage ausgestellt":"Parkticket ausgegeben");return true;
    }
    public long quote(Player p){
        if(!(level instanceof ServerLevel sl)||tariff()==null)return -1;
        var t=selected(p,false);if(t!=null&&t.pass)return 0;
        long now=ParkingLedger.now(sl);if(t!=null&&t.paid&&t.validUntil>=now)return 0;
        long total=ParkingLedger.fee(kind()==ServiceBlock.Kind.PARK_METER?duration*1200L:t==null?0:Math.max(0,now-t.entered),rate(),freeMinutes(),cap());return kind()==ServiceBlock.Kind.PARK_METER?total:Math.max(0,total-(t==null?0:t.paidCents));
    }
    public boolean purchase(ServerPlayer p){
        if(!canUse(p)||tariff()==null)return false;var sl=p.serverLevel();var ledger=ParkingLedger.get(sl);long now=ParkingLedger.now(sl);
        var t=selected(p,false);if(kind()==ServiceBlock.Kind.PARK_METER&&t==null){t=ledger.existing(p.getUUID(),tariff().id,false,now);if(t==null)t=ledger.issue(p.getUUID(),tariff().id,now,false,0);}
        if(t==null||t.pass){say(p,"Zuerst ein passendes Einfahrtsticket ausstellen lassen");return false;}
        long amount=quote(p);if(amount<0)return false;
        if(!ledger.pay(p,t.id,tariff().id,amount,now+(kind()==ServiceBlock.Kind.PARK_METER?duration*1200L:15*1200L))){give(p,t);say(p,"Zahlung nicht bestätigt – NG-Wirtschaftssystem erforderlich");return false;}
        give(p,t);say(p,"Bezahlt: "+amount/100+","+String.format(java.util.Locale.ROOT,"%02d",amount%100)+" Euro");return true;
    }
    public boolean exit(Player p){
        if(!canUse(p)||!(level instanceof ServerLevel sl)||tariff()==null)return false;
        var t=selected(p,false);if(t==null||!ParkingLedger.get(sl).usable(t.id,p.getUUID(),tariff().id,ParkingLedger.now(sl))){say(p,"Kein gültiges bezahltes Ticket / Ausweis");return false;}
        var gate=link("gate");if(gate instanceof ServiceEntity b&&(b.tariff()==null||!b.tariff().id.equals(tariff().id))){say(p,"Schranke gehört zu einer anderen Parkzone");return false;}var road=road();if(!(gate instanceof ServiceEntity)||road!=null&&road.role()!=RoadSystemBlock.Role.BARRIER){if(road==null){say(p,"Ausfahrtsschranke fehlt");return false;}}
        if(!ParkingLedger.get(sl).consume(t.id,p.getUUID(),tariff().id,ParkingLedger.now(sl)))return false;
        sl.getServer().overworld().getDataStorage().save();
        if(gate instanceof ServiceEntity b)b.openUntil=level.getGameTime()+160;
        if(road!=null)road.ticketOpen(level.getGameTime()+160);say(p,"Ausfahrt freigegeben");changed();return true;
    }
    public boolean action(Player p,int button){
        if(!canUse(p))return false;
        if(button==0)return kind()==ServiceBlock.Kind.ENTRY_TICKET&&issue(p,false);
        if(button==1)return (kind()==ServiceBlock.Kind.CASHIER||kind()==ServiceBlock.Kind.PARK_METER)&&p instanceof ServerPlayer sp&&purchase(sp);
        if(button==2)return kind()==ServiceBlock.Kind.EXIT_READER&&exit(p);
        if(button==3)return (kind()==ServiceBlock.Kind.PEDESTRIAN_CALL||kind()==ServiceBlock.Kind.BICYCLE_DETECTOR)&&requestWalk(p);
        if(button==4&&kind()==ServiceBlock.Kind.PARK_METER){duration=duration>=240?15:duration+15;changed();return true;}
        if(!canEdit(p))return false;
        switch(button){
            case 10->{if(kind()!=ServiceBlock.Kind.TARIFF)return false;rate=rate>=2000?0:rate+50;}
            case 11->{if(kind()!=ServiceBlock.Kind.TARIFF)return false;freeMinutes=freeMinutes>=60?0:freeMinutes+5;}
            case 12->{if(kind()!=ServiceBlock.Kind.TARIFF)return false;cap=cap>=5000?0:cap+500;}
            case 13->group=1-group;
            case 14->height=height>=650?150:height+10;
            case 15->limit=limit>=130?20:limit+10;
            case 16->interval=interval>=400?60:interval+20;
            case 17->{return kind()==ServiceBlock.Kind.TARIFF&&issue(p,true);}
            case 18->{if(kind()!=ServiceBlock.Kind.TARIFF)return false;if(tariff()!=null&&level instanceof ServerLevel sl){ParkingLedger.get(sl).revokePasses(p.getUUID(),tariff().id);say(p,"Eigene Dauerberechtigung widerrufen");}}
            case 19->{releaseSpeed();links.clear();watched.clear();openUntil=greenUntil=0;}
            case 20->mode=1-mode;
            default->{return false;}
        }changed();return true;
    }
    private boolean vehicle(Entity e){return RoadSystemEntity.vehicle(e)||e.getVehicle()==null&&e.getTags().contains("ng_vehicle");}
    private boolean obstacle(){return !level.getEntities((Entity)null,new AABB(worldPosition).inflate(.05,0,.05),e->e.isAlive()&&!e.isSpectator()&&!(e instanceof net.minecraft.world.entity.item.ItemEntity)&&!(e instanceof net.minecraft.world.entity.ExperienceOrb)).isEmpty();}
    private void releaseSpeed(){var r=road();if(r!=null)r.releaseTraffic(id);}
    private void status(int state){if(getBlockState().getValue(ServiceBlock.STATUS)!=state){level.setBlock(worldPosition,getBlockState().setValue(ServiceBlock.STATUS,state),3);changed();}}
    public void tick(){
        if(!(level instanceof ServerLevel sl)||level.getGameTime()%4!=0)return;long now=level.getGameTime();if(started<0){started=now;nextRelease=now+60;greenUntil=0;}
        online=true;alarm=false;green=false;detail="";
        switch(kind()){
            case TARIFF->online=true;
            case PARK_METER,ENTRY_TICKET,CASHIER->online=tariff()!=null;
            case EXIT_READER->online=tariff()!=null&&(link("gate") instanceof ServiceEntity b&&b.tariff()!=null&&b.tariff().id.equals(tariff().id)||road()!=null);
            case RESERVATION_BOLLARD->{
                online=tariff()!=null;boolean authorized=false;
                if(online)for(var e:level.getEntities((Entity)null,new AABB(worldPosition).inflate(3,1,3),x->x.isAlive())){
                    var p=e instanceof Player pl?pl:e.getControllingPassenger() instanceof Player pl?pl:null;
                    if(p!=null&&(reservedFor==null||reservedFor.equals(p.getUUID()))){var t=selected(p,true);authorized|=t!=null&&ParkingLedger.get(sl).usable(t.id,p.getUUID(),tariff().id,ParkingLedger.now(sl));}
                }
                if(authorized)openUntil=now+40;green=now<openUntil;
                if(!green&&getBlockState().getValue(ServiceBlock.STATUS)==1&&obstacle()){openUntil=now+20;green=true;}
            }
            case FLOOR_BOARD->{var r=road();online=r!=null&&r.online();free=online?r.data().free():-1;detail=online?r.displayName()+": "+free+" frei":"Etage nicht erreichbar";}
            case HEIGHT_WARNING->{
                var r=road();online=r!=null&&r.online();detectedHeight=0;
                if(online)for(var e:level.getEntities((Entity)null,new AABB(worldPosition).inflate(8,4,8),this::vehicle)){
                    double top=e.getY()+e.getBbHeight();for(var rider:e.getIndirectPassengers())top=Math.max(top,rider.getY()+rider.getBbHeight());
                    detectedHeight=Math.max(detectedHeight,(int)Math.ceil((top-e.getY())*100));
                }int allowed=r==null?height:Math.min(height,r.data().heightCm);alarm=detectedHeight>allowed;detail=alarm?"ZU HOCH – Ausweichroute →":"Durchfahrt bis "+allowed/100.0+" m";
            }
            case PEDESTRIAN_CALL,ACOUSTIC_SIGNAL,BICYCLE_DETECTOR,BICYCLE_LIGHT->{
                var c=signal();online=c!=null&&c.validConnection(worldPosition,c.connectionGeneration())&&c.isEnabled();
                green=online&&c.pedestrianGreen(group);waiting=online&&c.pedestrianBusy(group)&&!green;
                if(kind()==ServiceBlock.Kind.BICYCLE_DETECTOR&&online){
                    var seen=new HashSet<UUID>();for(var e:level.getEntities((Entity)null,new AABB(worldPosition).inflate(3,1,3),x->vehicle(x)&&x.getTags().contains("ng_bicycle"))){seen.add(e.getUUID());if(!bicycles.contains(e.getUUID())&&(c.requestPedestrians(group)||c.pedestrianBusy(group)))bicycles.add(e.getUUID());}bicycles.retainAll(seen);
                }
                if(kind()==ServiceBlock.Kind.ACOUSTIC_SIGNAL&&online&&now%(green?8:40)==0)level.playSound(null,worldPosition,green?SoundEvents.NOTE_BLOCK_PLING.value():SoundEvents.NOTE_BLOCK_HAT.value(),SoundSource.BLOCKS,.25f,green?1.4f:.7f);
                detail=!online?"Keine sichere Freigabe":green?"GRÜN":waiting?"BITTE WARTEN":"ROT";
            }
            case DYNAMIC_SPEED->{
                var r=road();var trigger=link("trigger");online=r!=null&&r.online()&&trigger!=null;
                alarm=!online||trigger instanceof TrafficNodeEntity n&&n.alarm()||trigger instanceof ConstructionEntity c&&c.active();
                effectiveSpeed=alarm?limit:r==null?limit:r.data().speed;
                if(r!=null&&alarm)r.trafficOverride(id,2,limit,now+16);else releaseSpeed();detail="Tempo "+effectiveSpeed+" km/h";
            }
            case RAMP_METER->{
                online=mode==0;var vehicles=level.getEntities((Entity)null,new AABB(worldPosition).inflate(6,2,6),this::vehicle);boolean demand=!vehicles.isEmpty();
                if(online&&demand&&now>=nextRelease){greenUntil=now+20;nextRelease=now+interval;}green=online&&now<greenUntil;
                var seen=new HashSet<UUID>();var facing=getBlockState().getValue(OrientedBlock.FACING);
                for(var e:vehicles){seen.add(e.getUUID());double axis=(e.getX()-worldPosition.getX()-.5)*facing.getStepX()+(e.getZ()-worldPosition.getZ()-.5)*facing.getStepZ();int side=axis>.1?1:axis<-.1?-1:0;var old=rampSides.get(e.getUUID());if(green&&old!=null&&old!=0&&side!=0&&old!=side){greenUntil=0;green=false;}if(side!=0)rampSides.put(e.getUUID(),side);}
                rampSides.keySet().retainAll(seen);if(rampSides.size()>256)rampSides.clear();
                detail=green?"Ein Fahrzeug frei":"HALT";
            }
            case FAULT_MONITOR->{
                faults=0;var messages=new ArrayList<String>();for(var r:watched){var e=resolve(r);boolean bad=e==null||e instanceof ServiceEntity s&&!s.online||e instanceof RoadSystemEntity x&&!x.online()||e instanceof TrafficNodeEntity x&&!x.online()||e instanceof DeviceEntity x&&!x.isEnabled();if(bad){faults++;if(messages.size()<3)messages.add(r.pos.toShortString());}}
                online=!watched.isEmpty();alarm=faults>0;detail=alarm?"Störung: "+String.join("; ",messages):watched.isEmpty()?"Keine Geräte verbunden":"Alle Geräte erreichbar";
            }
        }
        boolean signalDevice=kind()==ServiceBlock.Kind.PEDESTRIAN_CALL||kind()==ServiceBlock.Kind.BICYCLE_LIGHT||kind()==ServiceBlock.Kind.RAMP_METER;status(!online?3:green?1:alarm||signalDevice?2:0);if(now%20==0){changed();if(kind()==ServiceBlock.Kind.TARIFF&&now%1200==0)ParkingLedger.get(sl).prune(ParkingLedger.now(sl));}
    }
    public void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t){
        super.saveAdditional(t);t.putLong("SignalGeneration",signalGeneration);t.putUUID("Identity",id);if(owner!=null)t.putUUID("Owner",owner);if(reservedFor!=null)t.putUUID("ReservedFor",reservedFor);t.putInt("Rate",rate);t.putInt("Free",freeMinutes);t.putInt("Cap",cap);t.putInt("Duration",duration);t.putInt("Group",group);t.putInt("Limit",limit);t.putInt("Height",height);t.putInt("Interval",interval);t.putInt("Mode",mode);
        var refs=new ListTag();links.forEach((key,r)->{var n=saveRef(r);n.putString("Key",key);refs.add(n);});t.put("Links",refs);var list=new ListTag();for(var r:watched)list.add(saveRef(r));t.put("Watched",list);
        t.putBoolean("Online",online);t.putBoolean("Alarm",alarm);t.putBoolean("Green",green);t.putBoolean("Waiting",waiting);t.putInt("FreeCount",free);t.putInt("Faults",faults);t.putInt("EffectiveSpeed",effectiveSpeed);t.putString("Detail",detail);
    }
    private CompoundTag saveRef(Ref r){var n=new CompoundTag();n.putLong("Pos",r.pos.asLong());n.putUUID("Id",r.id);n.putString("Type",r.type);return n;}
    private Ref readRef(CompoundTag n){return n.hasUUID("Id")?new Ref(BlockPos.of(n.getLong("Pos")),n.getUUID("Id"),n.getString("Type")):null;}
    @Override public void load(CompoundTag t){
        super.load(t);signalGeneration=t.getLong("SignalGeneration");id=t.hasUUID("Identity")?t.getUUID("Identity"):UUID.randomUUID();owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;reservedFor=t.hasUUID("ReservedFor")?t.getUUID("ReservedFor"):null;
        rate=Math.max(0,Math.min(100000,t.getInt("Rate")));freeMinutes=Math.max(0,Math.min(1440,t.getInt("Free")));cap=Math.max(0,Math.min(1000000,t.getInt("Cap")));duration=Math.max(15,Math.min(1440,t.getInt("Duration")));group=t.getInt("Group")==1?1:0;limit=Math.max(20,Math.min(130,t.getInt("Limit")));height=Math.max(150,Math.min(650,t.getInt("Height")));interval=Math.max(60,Math.min(1200,t.getInt("Interval")));mode=t.getInt("Mode")==1?1:0;
        links.clear();for(var e:t.getList("Links",10)){var n=(CompoundTag)e;var r=readRef(n);if(r!=null&&List.of("tariff","road","signal","gate","trigger").contains(n.getString("Key")))links.put(n.getString("Key"),r);}
        watched.clear();for(var e:t.getList("Watched",10)){var r=readRef((CompoundTag)e);if(r!=null&&watched.size()<64)watched.add(r);}
        boolean client=level!=null&&level.isClientSide;online=client&&t.getBoolean("Online");alarm=client&&t.getBoolean("Alarm");green=client&&t.getBoolean("Green");waiting=client&&t.getBoolean("Waiting");free=t.getInt("FreeCount");faults=t.getInt("Faults");effectiveSpeed=t.getInt("EffectiveSpeed");detail=t.getString("Detail");openUntil=greenUntil=0;started=-1;bicycles.clear();rampSides.clear();
    }
    @Override public CompoundTag getUpdateTag(){var t=saveWithoutMetadata();t.remove("Owner");return t;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
