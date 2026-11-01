package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Separate from the existing traffic-light controller; no chunk forcing. */
public final class RoadSystemEntity extends BlockEntity {
    public static final TagKey<EntityType<?>> VEHICLES=TagKey.create(Registries.ENTITY_TYPE,new ResourceLocation(NgTraffic.ID,"parking_vehicles"));
    private RoadSystemData data=new RoadSystemData();
    private record TrafficLease(int mode,int speed,long expires){}
    private final Map<UUID,TrafficLease> trafficLeases=new HashMap<>();
    public UUID identity(){return identity;}
    public void trafficOverride(UUID source,int mode,int speed,long expires){
        if(!controller()||aggregator()||level==null||mode<2||mode>3)return;
        if(!trafficLeases.containsKey(source)&&trafficLeases.size()>=64)return;
        trafficLeases.put(source,new TrafficLease(mode,Math.max(5,Math.min(130,speed)),expires));
    }
    public void releaseTraffic(UUID source){trafficLeases.remove(source);}
    private RoadSystemData effectiveData(){
        if(level==null)return data;
        long now=level.getGameTime();trafficLeases.entrySet().removeIf(e->e.getValue().expires<now);
        var result=data.copy();
        for(var lease:trafficLeases.values())if(result.mode!=3){result.mode=Math.max(result.mode==2?2:0,lease.mode);result.speed=Math.min(result.speed,lease.speed);result.autoDisplay=0;}
        return result;
    }
    private UUID identity=UUID.randomUUID(),parentIdentity;
    private BlockPos parent;
    private int revision;
    private boolean online;
    private final Set<BlockPos> devices=new LinkedHashSet<>();
    private final Map<BlockPos,UUID> zones=new LinkedHashMap<>();
    private final Map<BlockPos,UUID> spaces=new LinkedHashMap<>();
    private String routeLabel="";private int routeArrow=1;private long barrierHoldUntil;
    public String displayName(){return routeLabel.isBlank()?data.name:routeLabel;}
    public int displayDirection(){return data.routeMode==0?data.direction:routeArrow;}
    public String displayArrow(){return data.routeMode!=0&&routeArrow==5?"↓":WayfindingData.arrow(displayDirection());}
    public boolean barrierOpen(){return role()==RoadSystemBlock.Role.BARRIER&&getBlockState().getValue(RoadSystemBlock.OPEN);}
    public int connectedSpaceCount(){return spaces.size();}
    public List<ZoneInfo> availableZones(){var source=parentNode();return aggregator()?zones():source!=null&&source.aggregator()?source.zones():List.of();}
    public record ZoneInfo(BlockPos pos,String name,boolean online,int capacity,int occupied){}
    private record Seen(int side,long tick){}
    private record Passage(int delta,long tick){}
    private final Map<UUID,Seen> seen=new HashMap<>();
    private final Map<UUID,Passage> passages=new HashMap<>();
    public RoadSystemEntity(BlockPos pos,BlockState state){super(NgTraffic.ROAD_SYSTEM_ENTITY.get(),pos,state);}
    public RoadSystemBlock.Role role(){return ((RoadSystemBlock)getBlockState().getBlock()).role;}
    public boolean aggregator(){return role()==RoadSystemBlock.Role.AGGREGATOR;}
    public boolean controller(){return role()==RoadSystemBlock.Role.CONTROLLER||aggregator();}
    public boolean sensor(){return role()==RoadSystemBlock.Role.ENTRY_SENSOR||role()==RoadSystemBlock.Role.EXIT_SENSOR;}
    public void prepareMenuSnapshot(){if(aggregator())refreshZones();}
    public RoadSystemData data(){return data.copy();}
    public RoadSystemData vehicleRules(){var source=parentNode();return source==null?data():source.data();}
    public int revision(){return revision;}
    public int deviceCount(){return devices.size();}
    public boolean online(){return controller()&&!aggregator()?(data.counting==0||online):online;}
    public BlockPos parent(){return parent;}
    public void configure(RoadSystemData value){
        var next=value.copy();
        if(aggregator()){next.capacity=data.capacity;next.occupied=data.occupied;next.mode=0;}
        else if(controller()){next.capacity=Math.min(100000,next.capacity);next.occupied=Math.min(next.capacity,next.occupied);if(next.counting==1){next.capacity=data.capacity;next.occupied=data.occupied;}}
        if(role()!=RoadSystemBlock.Role.PARKING_GUIDE){next.routeMode=0;next.routeTarget=0;}
        data=next;if(controller()&&!aggregator())refreshSpaces();revision++;publish();
    }
    private RoadSystemEntity loadedNode(BlockPos pos){
        if(!(level instanceof net.minecraft.server.level.ServerLevel server))return null;
        // getBlockEntity via Level.getChunkAt can renew UNKNOWN tickets. Never request a chunk here.
        var chunk=server.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4);
        return chunk!=null&&chunk.getBlockEntity(pos) instanceof RoadSystemEntity node?node:null;
    }
    private RoadSystemEntity zoneNode(BlockPos pos,UUID id){
        var node=loadedNode(pos);return node!=null&&node.controller()&&!node.aggregator()&&node.identity.equals(id)?node:null;
    }
    public List<ZoneInfo> zones(){
        var result=new ArrayList<ZoneInfo>();
        for(var entry:zones.entrySet()){
            var node=zoneNode(entry.getKey(),entry.getValue());
            result.add(new ZoneInfo(entry.getKey(),node==null?"":node.data.name,node!=null&&node.online(),node==null?0:node.data.capacity,node==null?0:node.data.occupied));
        }
        return List.copyOf(result);
    }
    public boolean attachZone(RoadSystemEntity zone){
        if(!aggregator()||!zone.controller()||zone.aggregator()||level==null||level!=zone.level||worldPosition.distSqr(zone.worldPosition)>256*256)return false;
        if(!zones.containsKey(zone.worldPosition)&&zones.size()>=16)return false;
        zones.put(zone.worldPosition.immutable(),zone.identity);revision++;refreshZones();publish();return true;
    }
    public boolean removeZone(int index){
        if(!aggregator()||index<0||index>=zones.size())return false;
        var pos=new ArrayList<>(zones.keySet()).get(index);zones.remove(pos);revision++;refreshZones();publish();return true;
    }
    private void refreshZones(){
        if(!aggregator())return;
        int capacity=0,occupied=0;boolean available=!zones.isEmpty();
        for(var entry:zones.entrySet()){
            var zone=zoneNode(entry.getKey(),entry.getValue());
            if(zone==null||!zone.online()){available=false;continue;}
            capacity+=zone.data.capacity;occupied+=zone.data.occupied;
        }
        var next=data.copy();
        if(available){next.capacity=capacity;next.occupied=occupied;}
        if(online!=available||!data.same(next)){online=available;data=next;if(controller()&&!aggregator())refreshSpaces();revision++;publish();}
    }
    private void publish(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    public boolean attach(RoadSystemEntity target){
        if(!controller()||target==this||target.controller()||level!=target.level||worldPosition.distSqr(target.worldPosition)>256*256)return false;
        if(aggregator()&&target.role()!=RoadSystemBlock.Role.PARKING_GUIDE&&target.role()!=RoadSystemBlock.Role.PARKING_BOARD)return false;
        if(!devices.contains(target.worldPosition)&&devices.size()>=256)return false;
        target.detach();devices.add(target.worldPosition.immutable());target.parent=worldPosition.immutable();target.parentIdentity=identity;target.seen.clear();if(target.role()==RoadSystemBlock.Role.SPACE_SENSOR)spaces.put(target.worldPosition.immutable(),target.identity);
        revision++;setChanged();target.sync();return true;
    }
    public void detach(){
        var old=parent==null?null:loadedNode(parent);if(old!=null&&Objects.equals(parentIdentity,old.identity)){old.devices.remove(worldPosition);old.spaces.remove(worldPosition);old.revision++;old.setChanged();}
        parent=null;parentIdentity=null;online=false;seen.clear();revision++;publish();
    }
    private RoadSystemEntity parentNode(){
        if(parent==null)return null;var node=loadedNode(parent);return node!=null&&node.controller()&&Objects.equals(node.identity,parentIdentity)&&node.devices.contains(worldPosition)?node:null;
    }
    private void sync(){
        var source=parentNode();boolean nextOnline=source!=null&&source.online();var next=data.copy();
        String nextLabel="";int nextArrow=data.direction;
        if(source!=null){
            var shared=source.effectiveData();
            if(role()!=RoadSystemBlock.Role.SPACE_SENSOR){
                next.mode=shared.mode;next.speed=shared.speed;next.capacity=shared.capacity;next.occupied=shared.occupied;
                if((role()==RoadSystemBlock.Role.TRAFFIC_SIGN||role()==RoadSystemBlock.Role.LANE_SIGN)&&shared.autoDisplay==1)
                    next.mode=shared.free()==0?3:role()==RoadSystemBlock.Role.LANE_SIGN?4:1;
            }
            if(role()==RoadSystemBlock.Role.PARKING_GUIDE&&data.routeMode!=0){
                RoadSystemEntity selected=null;
                if(source.aggregator()){
                    if(data.routeMode==1){var id=source.zones.get(BlockPos.of(data.routeTarget));if(id!=null)selected=source.zoneNode(BlockPos.of(data.routeTarget),id);}
                    else for(var entry:source.zones.entrySet()){var zone=source.zoneNode(entry.getKey(),entry.getValue());if(zone!=null&&zone.online()&&(selected==null||selected.data.free()==0&&zone.data.free()>0))selected=zone;}
                }
                if(selected==null||!selected.online()||!source.online())nextOnline=false;
                else{
                    next.capacity=selected.data.capacity;next.occupied=selected.data.occupied;nextLabel=selected.data.name.isBlank()?selected.worldPosition.toShortString():selected.data.name;
                    var facing=getBlockState().getValue(OrientedBlock.FACING);int dx=selected.worldPosition.getX()-worldPosition.getX(),dz=selected.worldPosition.getZ()-worldPosition.getZ();
                    int across=dx*facing.getStepZ()-dz*facing.getStepX();int forward=-dx*facing.getStepX()-dz*facing.getStepZ();
                    nextArrow=Math.abs(across)>Math.abs(forward)?(across>0?2:0):(forward<0?5:1);
                }
            }
        }
        boolean routeChanged=!routeLabel.equals(nextLabel)||routeArrow!=nextArrow;routeLabel=nextLabel;routeArrow=nextArrow;
        if(routeChanged||online!=nextOnline||!data.same(next)){online=nextOnline;data=next;if(controller()&&!aggregator())refreshSpaces();revision++;publish();}
    }
    public boolean countPassage(UUID vehicle,int delta,long tick){
        if(!controller()||aggregator()||data.counting==1||(delta!=1&&delta!=-1))return false;
        var last=passages.get(vehicle);if(last!=null&&last.delta==delta&&tick>=last.tick&&tick-last.tick<100)return false;
        passages.put(vehicle,new Passage(delta,tick));passages.entrySet().removeIf(e->tick<e.getValue().tick||tick-e.getValue().tick>200);
        int before=data.occupied;data.occupied=Math.max(0,Math.min(data.capacity,before+delta));
        if(before!=data.occupied){revision++;publish();}return true;
    }
    public static boolean vehicle(Entity entity){return vehicle(entity,new RoadSystemData());}
    public static boolean vehicle(Entity entity,RoadSystemData rules){
        if(entity instanceof Player||entity.getRootVehicle()!=entity)return false;
        var key=net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if(entity.getType().is(VEHICLES)||RoadVehicleRules.contains(rules.vehicleTypes,key))return true;
        if(rules.detection==1)return false;
        if(entity instanceof Boat||entity instanceof AbstractMinecart)return true;
        if(rules.detection==2)return false;
        if(entity.getType().getCategory()!=MobCategory.MISC)return false;
        for(Entity passenger:entity.getIndirectPassengers())if(passenger instanceof Player)return true;
        return false;
    }
    private void detect(){
        var controller=parentNode();if(controller==null){seen.clear();return;}
        long time=level.getGameTime();var facing=getBlockState().getValue(OrientedBlock.FACING);
        for(var entity:level.getEntities((Entity)null,new AABB(worldPosition).inflate(2,3,2),entity->vehicle(entity,controller.data))){
            if(entity.getY()<worldPosition.getY()||entity.getY()>worldPosition.getY()+3)continue;
            double dx=entity.getX()-worldPosition.getX()-.5,dz=entity.getZ()-worldPosition.getZ()-.5;
            double across=dx*facing.getStepZ()-dz*facing.getStepX();if(Math.abs(across)>1.5)continue;
            double along=dx*facing.getStepX()+dz*facing.getStepZ();int side=along>.05?1:along<-.05?-1:0;
            if(side==0)continue;
            var previous=seen.put(entity.getUUID(),new Seen(side,time));
            if(previous!=null&&time-previous.tick<20&&previous.side==1&&side==-1)controller.countPassage(entity.getUUID(),role()==RoadSystemBlock.Role.ENTRY_SENSOR?1:-1,time);
        }
        seen.entrySet().removeIf(e->time-e.getValue().tick>40);
    }
    private void refreshSpaces(){
        if(!controller()||aggregator()||data.counting==0)return;
        int occupied=0;boolean available=!spaces.isEmpty();
        for(var entry:spaces.entrySet()){
            var space=loadedNode(entry.getKey());
            if(space==null||space.role()!=RoadSystemBlock.Role.SPACE_SENSOR||!space.identity.equals(entry.getValue())||space.parentNode()!=this){available=false;continue;}
            occupied+=space.data.occupied>0?1:0;
        }
        var next=data.copy();if(available){next.capacity=spaces.size();next.occupied=occupied;}
        if(online!=available||!data.same(next)){online=available;data=next;revision++;publish();}
    }
    private void detectSpace(){
        var source=parentNode();boolean nextOnline=source!=null;boolean occupied=false;
        if(source!=null)for(var entity:level.getEntities((Entity)null,new AABB(worldPosition).expandTowards(0,3,0),e->vehicle(e,source.data))){
            // One floor block owns a root position; adjacent stall sensors cannot claim it twice.
            if(entity.getX()>=worldPosition.getX()&&entity.getX()<worldPosition.getX()+1&&entity.getZ()>=worldPosition.getZ()&&entity.getZ()<worldPosition.getZ()+1&&entity.getY()>=worldPosition.getY()&&entity.getY()<worldPosition.getY()+4){occupied=true;break;}
        }
        int count=occupied?1:0;
        if(getBlockState().getValue(RoadSystemBlock.OPEN)!=occupied)level.setBlock(worldPosition,getBlockState().setValue(RoadSystemBlock.OPEN,occupied),3);
        if(data.capacity!=1||data.occupied!=count||online!=nextOnline){data.capacity=1;data.occupied=count;online=nextOnline;revision++;publish();}
    }
    private boolean ticketControlled;
    private long ticketUntil;
    public void ticketControlled(){ticketControlled=true;setChanged();}
    public void ticketOpen(long until){if(level!=null&&ticketControlled)ticketUntil=Math.min(level.getGameTime()+200,Math.max(ticketUntil,until));}
    private void updateBarrier(){
        var source=parentNode();var area=new AABB(worldPosition).inflate(2,1,2);long time=level.getGameTime();
        boolean approach=false;
        if(!ticketControlled&&source!=null&&source.online()&&source.data.free()>0){
            for(var entity:level.getEntities((Entity)null,area,e->vehicle(e,source.data))){
                double low=entity.getY(),high=low+entity.getBbHeight();
                for(var rider:entity.getIndirectPassengers())high=Math.max(high,rider.getY()+rider.getBbHeight());
                if(high-low<=data.heightCm/100.0){approach=true;break;}
            }
        }
        var obstruction=new AABB(worldPosition.getX(),worldPosition.getY()+.3,worldPosition.getZ(),worldPosition.getX()+1,worldPosition.getY()+1.5,worldPosition.getZ()+1);
        if(!barrierOpen())obstruction=getBlockState().setValue(RoadSystemBlock.OPEN,false).getCollisionShape(level,worldPosition).bounds().move(worldPosition);
        boolean blocked=!level.getEntities((Entity)null,obstruction,e->!e.isSpectator()&&!e.isRemoved()).isEmpty();
        if(approach||blocked)barrierHoldUntil=time+20;
        boolean open=approach||blocked||time<barrierHoldUntil||ticketControlled&&time<ticketUntil;
        if(open!=barrierOpen())level.setBlock(worldPosition,getBlockState().setValue(RoadSystemBlock.OPEN,open),3);
    }
    public void tick(){
        if(aggregator()){if(level.getGameTime()%10==0)refreshZones();return;}
        if(controller()){if(level.getGameTime()%5==0)refreshSpaces();return;}
        if(level.getGameTime()%10==0)sync();
        if(sensor())detect();if(role()==RoadSystemBlock.Role.SPACE_SENSOR)detectSpace();if(role()==RoadSystemBlock.Role.BARRIER)updateBarrier();
    }
    @Override protected void saveAdditional(CompoundTag tag){
        super.saveAdditional(tag);tag.putBoolean("TicketControlled",ticketControlled);tag.putUUID("Identity",identity);tag.putInt("Revision",revision);tag.putString("Name",data.name);tag.putInt("Mode",data.mode);tag.putInt("Speed",data.speed);tag.putInt("Capacity",data.capacity);tag.putInt("Occupied",data.occupied);tag.putInt("Direction",data.direction);tag.putBoolean("Online",online);
        if(parent!=null){tag.putLong("Parent",parent.asLong());tag.putUUID("ParentIdentity",parentIdentity);}
        tag.putLongArray("Devices",devices.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putInt("Counting",data.counting);tag.putInt("HeightCm",data.heightCm);tag.putInt("RouteMode",data.routeMode);tag.putLong("RouteTarget",data.routeTarget);tag.putInt("AutoDisplay",data.autoDisplay);
        tag.putString("RouteLabel",routeLabel);tag.putInt("RouteArrow",routeArrow);
        var savedSpaces=new ListTag();for(var entry:spaces.entrySet()){var saved=new CompoundTag();saved.putLong("Pos",entry.getKey().asLong());saved.putUUID("Identity",entry.getValue());savedSpaces.add(saved);}tag.put("Spaces",savedSpaces);
        tag.putString("VehicleTypes",data.vehicleTypes);tag.putInt("Detection",data.detection);
        // Retain recent controller-wide deduplication across saves/chunk reloads.
        long now=level==null?Long.MAX_VALUE:level.getGameTime();var savedPassages=new ListTag();
        for(var entry:passages.entrySet()){
            var passage=entry.getValue();if(passage.tick>now||now-passage.tick>200)continue;
            if(savedPassages.size()>=4096)break;
            var saved=new CompoundTag();saved.putUUID("Vehicle",entry.getKey());saved.putInt("Delta",passage.delta);saved.putLong("Tick",passage.tick);savedPassages.add(saved);
        }
        tag.put("Passages",savedPassages);
        var savedZones=new ListTag();for(var entry:zones.entrySet()){var saved=new CompoundTag();saved.putLong("Pos",entry.getKey().asLong());saved.putUUID("Identity",entry.getValue());savedZones.add(saved);}tag.put("Zones",savedZones);
    }
    @Override public void load(CompoundTag tag){
        super.load(tag);ticketControlled=tag.getBoolean("TicketControlled");ticketUntil=0;trafficLeases.clear();if(tag.hasUUID("Identity"))identity=tag.getUUID("Identity");revision=tag.getInt("Revision");var next=new RoadSystemData();next.name=tag.getString("Name");next.mode=tag.getInt("Mode");if(tag.contains("Speed"))next.speed=tag.getInt("Speed");if(tag.contains("Capacity"))next.capacity=tag.getInt("Capacity");next.occupied=tag.getInt("Occupied");if(tag.contains("Direction"))next.direction=tag.getInt("Direction");data=next.copy();online=tag.getBoolean("Online");parent=tag.contains("Parent")&&tag.hasUUID("ParentIdentity")?BlockPos.of(tag.getLong("Parent")):null;parentIdentity=parent==null?null:tag.getUUID("ParentIdentity");devices.clear();long[] positions=tag.getLongArray("Devices");for(int i=0;i<Math.min(256,positions.length);i++)devices.add(BlockPos.of(positions[i]));seen.clear();passages.clear();
        data.counting=Math.max(0,Math.min(1,tag.getInt("Counting")));data.heightCm=tag.contains("HeightCm")?Math.max(150,Math.min(650,tag.getInt("HeightCm"))):220;data.routeMode=Math.max(0,Math.min(2,tag.getInt("RouteMode")));data.routeTarget=tag.getLong("RouteTarget");data.autoDisplay=Math.max(0,Math.min(1,tag.getInt("AutoDisplay")));
        routeLabel=WayfindingData.clean(tag.getString("RouteLabel"),96);routeArrow=Math.max(0,Math.min(5,tag.getInt("RouteArrow")));
        spaces.clear();var savedSpaces=tag.getList("Spaces",Tag.TAG_COMPOUND);for(int i=0;i<Math.min(256,savedSpaces.size());i++){var entry=savedSpaces.getCompound(i);var pos=BlockPos.of(entry.getLong("Pos"));if(entry.hasUUID("Identity")&&devices.contains(pos)&&worldPosition.distSqr(pos)<=256*256)spaces.put(pos,entry.getUUID("Identity"));}
        data.vehicleTypes=RoadVehicleRules.clean(tag.getString("VehicleTypes"));data.detection=Math.max(0,Math.min(2,tag.getInt("Detection")));
        if(controller()&&!aggregator()){data.capacity=Math.min(100000,data.capacity);data.occupied=Math.min(data.capacity,data.occupied);}
        if(level==null||!level.isClientSide)online=false;
        if(controller()&&!aggregator()){
            var savedPassages=tag.getList("Passages",Tag.TAG_COMPOUND);
            for(int i=0;i<Math.min(4096,savedPassages.size());i++){
                var saved=savedPassages.getCompound(i);int delta=saved.getInt("Delta");long tick=saved.getLong("Tick");
                if(saved.hasUUID("Vehicle")&&(delta==1||delta==-1)&&tick>=0)passages.put(saved.getUUID("Vehicle"),new Passage(delta,tick));
            }
        }
        zones.clear();var savedZones=tag.getList("Zones",Tag.TAG_COMPOUND);
        for(int i=0;i<Math.min(16,savedZones.size());i++){var entry=savedZones.getCompound(i);if(entry.hasUUID("Identity")){var pos=BlockPos.of(entry.getLong("Pos"));if(!pos.equals(worldPosition)&&worldPosition.distSqr(pos)<=256*256)zones.put(pos,entry.getUUID("Identity"));}}
    }
    @Override public CompoundTag getUpdateTag(){var tag=saveWithoutMetadata();tag.remove("Passages");return tag;}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
