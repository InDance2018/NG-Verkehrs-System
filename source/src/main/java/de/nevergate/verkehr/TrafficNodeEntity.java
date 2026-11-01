package de.nevergate.verkehr;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

public final class TrafficNodeEntity extends BlockEntity {
    public static final int MAX_COUNT=2000000000;
    private UUID identity=UUID.randomUUID(),parentId,roadId,signalId;
    private BlockPos parent,road,signal;
    private long signalGeneration;
    private int mode,limit=50,radius=16,threshold=3,delay=100,group,direction=1;
    private String text="Umleitung",types="";
    private int speed=-1,slow,forward,reverse;
    private boolean online=true,alarm;
    private long lastSample,slowSince=-1,trainLast=-1000,trainSince=-1,speedUntil,holdUntil,readyAt=-1,clearSince=-1;
    private int confirmed;
    private final Map<UUID,Observation> observations=new HashMap<>();
    private final Set<UUID> served=new HashSet<>();
    private final Deque<Long> passages=new ArrayDeque<>();
    private record Observation(double x,double z,int side,long tick){}
    public TrafficNodeEntity(BlockPos p,BlockState s){super(NgTraffic.TRAFFIC_NODE_ENTITY.get(),p,s);if(kind()==TrafficNodeBlock.Kind.CROSSING_CONTROLLER)radius=32;}
    public TrafficNodeBlock.Kind kind(){return ((TrafficNodeBlock)getBlockState().getBlock()).kind;}
    public UUID identity(){return identity;}
    public int mode(){return mode;}public int limit(){return limit;}public int radius(){return radius;}public int threshold(){return threshold;}public int delay(){return delay;}public int group(){return group;}
    public int direction(){return direction;}public int speed(){return speed;}public int slow(){return slow;}public int forward(){return forward;}public int reverse(){return reverse;}
    public boolean online(){return online;}public boolean alarm(){return alarm;}public String text(){return text;}public int status(){return getBlockState().getValue(TrafficNodeBlock.STATUS);}
    public int hourly(){return Math.min(999999,passages.size()*60);}
    public int connectionCount(){return (parent!=null?1:0)+(road!=null?1:0)+(signal!=null?1:0);}
    private BlockEntity loaded(BlockPos p){
        if(!(level instanceof ServerLevel s)||p==null)return null;
        var chunk=s.getChunkSource().getChunkNow(p.getX()>>4,p.getZ()>>4);return chunk==null?null:chunk.getBlockEntity(p);
    }
    public TrafficNodeEntity source(){return loaded(parent) instanceof TrafficNodeEntity n&&n.identity.equals(parentId)&&compatible(n.kind(),kind())&&worldPosition.distSqr(n.worldPosition)<=128*128?n:null;}
    public boolean canEdit(Player p){return level!=null&&!level.isClientSide&&p.getAbilities().mayBuild&&level.mayInteract(p,worldPosition);}
    public static boolean compatible(TrafficNodeBlock.Kind from,TrafficNodeBlock.Kind to){
        return from==TrafficNodeBlock.Kind.SPEED_SENSOR&&to==TrafficNodeBlock.Kind.SPEED_BOARD||
            from==TrafficNodeBlock.Kind.CROSSING_CONTROLLER&&(to==TrafficNodeBlock.Kind.CROSSBUCK||to==TrafficNodeBlock.Kind.CROSSING_LIGHT||to==TrafficNodeBlock.Kind.CROSSING_BARRIER)||
            from==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER&&(to==TrafficNodeBlock.Kind.TUNNEL_BARRIER||to==TrafficNodeBlock.Kind.TUNNEL_LIGHT||to==TrafficNodeBlock.Kind.TUNNEL_LANE||to==TrafficNodeBlock.Kind.DETOUR_CONTROLLER)||
            from==TrafficNodeBlock.Kind.DETOUR_CONTROLLER&&to==TrafficNodeBlock.Kind.DETOUR_GUIDE||
            from==TrafficNodeBlock.Kind.JAM_SENSOR&&(to==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER||to==TrafficNodeBlock.Kind.DETOUR_CONTROLLER);
    }
    public boolean link(TrafficNodeEntity target,Player player){
        if(target==this||level!=target.level||!compatible(kind(),target.kind())||worldPosition.distSqr(target.worldPosition)>128*128||!canEdit(player)||!target.canEdit(player))return false;
        for(var n=this;n!=null;n=n.source())if(n==target)return false;
        target.parent=worldPosition.immutable();target.parentId=identity;target.changed();return true;
    }
    public boolean linkRoad(RoadSystemEntity target,Player p){
        if(!canEdit(p)||!p.level().mayInteract(p,target.getBlockPos())||!target.controller()||target.aggregator()||worldPosition.distSqr(target.getBlockPos())>128*128||
            !(kind()==TrafficNodeBlock.Kind.JAM_SENSOR||kind()==TrafficNodeBlock.Kind.TUNNEL_CONTROLLER||kind()==TrafficNodeBlock.Kind.DETOUR_CONTROLLER))return false;
        clearRoad();road=target.getBlockPos().immutable();roadId=target.identity();changed();return true;
    }
    public boolean linkSignal(DeviceEntity target,Player p){
        if(!canEdit(p)||!p.level().mayInteract(p,target.getBlockPos())||!target.isController()||worldPosition.distSqr(target.getBlockPos())>128*128||
            !(kind()==TrafficNodeBlock.Kind.BUS_SENSOR||kind()==TrafficNodeBlock.Kind.EMERGENCY_SENSOR))return false;
        signal=target.getBlockPos().immutable();signalGeneration=target.connectionGeneration();signalId=target.trafficIdentity();target.setChanged();changed();return true;
    }
    private void clearRoad(){if(loaded(road) instanceof RoadSystemEntity n&&n.identity().equals(roadId))n.releaseTraffic(identity);}
    public void disconnect(){clearRoad();parent=null;parentId=null;road=null;roadId=null;signal=null;served.clear();changed();}
    public void settings(int newMode,int newLimit,int newRadius,int newThreshold,int newDelay,int newGroup,int newDirection){
        mode=Math.max(0,Math.min(2,newMode));limit=Math.max(5,Math.min(130,newLimit));radius=Math.max(8,Math.min(32,newRadius));threshold=Math.max(2,Math.min(8,newThreshold));delay=Math.max(20,Math.min(200,newDelay));group=newGroup==1?1:0;direction=Math.max(0,Math.min(2,newDirection));changed();
    }
    public void text(String value){text=WayfindingData.clean(value,48);changed();}
    public void types(String value){types=RoadVehicleRules.clean(value);changed();}
    public String types(){return types;}
    public boolean action(Player p,int button){
        if(!canEdit(p))return false;
        switch(button){
            case 0->mode=(mode+1)%3;
            case 1->limit=limit>=130?5:limit+5;
            case 2->radius=radius==8?16:radius==16?32:8;
            case 3->threshold=threshold>=8?2:threshold+1;
            case 4->delay=delay>=200?20:delay+20;
            case 5->group=1-group;
            case 6->direction=(direction+1)%3;
            case 7->{disconnect();return true;}
            default->{return false;}
        }changed();return true;
    }
    public static double measuredKph(double x,double z,double oldX,double oldZ,long ticks){
        return ticks<=0?0:Math.min(999,Math.hypot(x-oldX,z-oldZ)*72.0/ticks);
    }
    private boolean marked(Entity e,String tag){return e.getTags().contains(tag)||RoadVehicleRules.contains(types,BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());}
    private boolean vehicle(Entity e){return RoadSystemEntity.vehicle(e)||e.getVehicle()==null&&marked(e,"ng_vehicle");}
    public void scan(){
        if(level==null||level.isClientSide)return;
        long now=level.getGameTime();if(readyAt<0)readyAt=now+(kind()==TrafficNodeBlock.Kind.CROSSING_CONTROLLER?60:delay+4);var d=getBlockState().getValue(OrientedBlock.FACING);
        var area=new AABB(worldPosition).inflate(radius,3,radius);
        var seen=new HashSet<UUID>();int stopped=0;boolean train=false;int newestSpeed=-1;
        for(var e:level.getEntities((Entity)null,area,x->x.isAlive()&&x.getVehicle()==null)){
            if(e.getY()<worldPosition.getY()-.5||e.getY()>worldPosition.getY()+4)continue;
            if(kind()==TrafficNodeBlock.Kind.CROSSING_CONTROLLER){
                if(e instanceof AbstractMinecart||marked(e,"ng_train"))train=true;
                continue;
            }
            if(!vehicle(e))continue;
            double dx=e.getX()-worldPosition.getX()-.5,dz=e.getZ()-worldPosition.getZ()-.5;
            double across=dx*d.getStepZ()-dz*d.getStepX(),along=dx*d.getStepX()+dz*d.getStepZ();
            if(Math.abs(across)>1.5||Math.abs(along)>radius)continue;
            seen.add(e.getUUID());int side=along>.1?1:along<-.1?-1:0;
            var before=observations.get(e.getUUID());if(before!=null&&before.tick==now)continue;double kmh=before==null?-1:measuredKph(e.getX(),e.getZ(),before.x,before.z,now-before.tick);
            if(before!=null&&now>before.tick&&now-before.tick<=10){
                if(kmh<=5)stopped++;
                newestSpeed=Math.max(newestSpeed,(int)Math.round(kmh));
                if(kind()==TrafficNodeBlock.Kind.COUNTER&&side!=0&&before.side!=0&&side!=before.side&&now-before.tick<=10){
                    if(side<0)forward=Math.min(MAX_COUNT,forward+1);else reverse=Math.min(MAX_COUNT,reverse+1);passages.addLast(now);setChanged();
                }
            }
            // Keep the previous signed side while a vehicle straddles the detector plane.
            observations.put(e.getUUID(),new Observation(e.getX(),e.getZ(),side==0&&before!=null?before.side:side,now));
            if(kind()==TrafficNodeBlock.Kind.BUS_SENSOR||kind()==TrafficNodeBlock.Kind.EMERGENCY_SENSOR){
                boolean urgent=kind()==TrafficNodeBlock.Kind.EMERGENCY_SENSOR;
                if(marked(e,urgent?"ng_emergency_active":"ng_bus")&&!served.contains(e.getUUID())&&loaded(signal) instanceof DeviceEntity c&&c.connectionGeneration()==signalGeneration&&c.trafficIdentity().equals(signalId)){
                    int result=c.requestPriority(group,urgent);if(result==2)served.add(e.getUUID());
                }
            }
        }
        served.retainAll(seen);
        observations.entrySet().removeIf(e->now-e.getValue().tick>20||!seen.contains(e.getKey()));
        if(observations.size()>256)observations.clear();
        while(!passages.isEmpty()&&now-passages.peekFirst()>1200)passages.removeFirst();
        if(kind()==TrafficNodeBlock.Kind.SPEED_SENSOR){if(newestSpeed>=0){speed=newestSpeed;speedUntil=now+40;}else if(now>speedUntil)speed=-1;alarm=speed>limit;}
        if(kind()==TrafficNodeBlock.Kind.JAM_SENSOR){
            slow=stopped;
            if(stopped>=threshold){clearSince=-1;if(slowSince<0)slowSince=now;if(now-slowSince>=delay)alarm=true;}
            else{slowSince=-1;if(clearSince<0)clearSince=now;if(now-clearSince>=40)alarm=false;}
        }
        if(kind()==TrafficNodeBlock.Kind.CROSSING_CONTROLLER){
            if(train){trainLast=now;if(trainSince<0)trainSince=now;}
            if(now-trainLast>60)trainSince=-1;
            alarm=trainSince>=0||now<readyAt;
            confirmed=now<readyAt?1:trainSince>=0&&now-trainSince>=60?1:0;
        }
        changed();
    }
    private void state(int value){var old=getBlockState();if(old.getValue(TrafficNodeBlock.STATUS)!=value)level.setBlock(worldPosition,old.setValue(TrafficNodeBlock.STATUS,value),3);}
    private boolean parentRequired(){return kind()==TrafficNodeBlock.Kind.SPEED_BOARD||kind()==TrafficNodeBlock.Kind.CROSSBUCK||kind()==TrafficNodeBlock.Kind.CROSSING_LIGHT||kind()==TrafficNodeBlock.Kind.CROSSING_BARRIER||kind()==TrafficNodeBlock.Kind.TUNNEL_BARRIER||kind()==TrafficNodeBlock.Kind.TUNNEL_LIGHT||kind()==TrafficNodeBlock.Kind.TUNNEL_LANE||kind()==TrafficNodeBlock.Kind.DETOUR_GUIDE;}
    public void tick(){
        if(level==null||level.isClientSide||level.getGameTime()%2!=0)return;long now=level.getGameTime();
        if(TrafficNodeBlock.sensor(kind())||kind()==TrafficNodeBlock.Kind.CROSSING_CONTROLLER)scan();
        var src=source();online=parent==null?!parentRequired():src!=null&&src.online;
        if(kind()==TrafficNodeBlock.Kind.JAM_SENSOR)online=readyAt>=0&&now>=readyAt;
        if(kind()==TrafficNodeBlock.Kind.DETOUR_CONTROLLER&&mode!=0)online=true;
        int next=0;
        switch(kind()){
            case SPEED_SENSOR->next=alarm?2:speed>=0?1:0;
            case SPEED_BOARD->{speed=src==null?-1:src.speed;limit=src==null?limit:src.limit;alarm=speed>limit;next=!online?3:alarm?2:1;}
            case JAM_SENSOR->next=alarm?2:1;
            case CROSSING_CONTROLLER->next=alarm?confirmed==1?2:3:1;
            case CROSSBUCK->next=online?1:3;
            case CROSSING_LIGHT->{alarm=!online||src.alarm;next=alarm?(now%20<10?2:3):1;}
            case CROSSING_BARRIER->{
                alarm=!online||src.confirmed==1;next=alarm?2:1;
            }
            case BUS_SENSOR,EMERGENCY_SENSOR->{online=loaded(signal) instanceof DeviceEntity c&&c.connectionGeneration()==signalGeneration&&c.trafficIdentity().equals(signalId);next=online?1:3;}
            case TUNNEL_CONTROLLER->{alarm=mode==1||mode==2||parent!=null&&(!online||src.alarm);next=alarm?mode==2?3:2:1;}
            case TUNNEL_BARRIER,TUNNEL_LANE->{alarm=!online||src.alarm;next=alarm?2:1;}
            case TUNNEL_LIGHT->{alarm=!online||src.alarm;next=alarm||src.group==0?1:0;}
            case DETOUR_CONTROLLER->{alarm=mode==1||mode==0&&parent!=null&&(!online||src.alarm);next=!online?3:alarm?2:1;}
            case DETOUR_GUIDE->{alarm=src!=null&&src.alarm;next=!online?3:alarm?2:0;direction=src==null?direction:src.direction;text=src==null?text:src.text;}
            case COUNTER->next=1;
        }
        if(TrafficNodeBlock.barrier(kind())){
            boolean wantOpen=next==1;if(wantOpen)holdUntil=now+20;
            if(!wantOpen&&(status()==1||now<holdUntil)){
                var shape=getBlockState().setValue(TrafficNodeBlock.STATUS,2).getCollisionShape(level,worldPosition).bounds().move(worldPosition);
                boolean obstacle=!level.getEntities((Entity)null,shape,e->e.isAlive()&&!(e instanceof net.minecraft.world.entity.item.ItemEntity)&&!(e instanceof net.minecraft.world.entity.ExperienceOrb)).isEmpty();
                if(obstacle||now<holdUntil)next=1;
            }
        }
        state(next);
        if(loaded(road) instanceof RoadSystemEntity c&&c.identity().equals(roadId)){
            if(!online||alarm)c.trafficOverride(identity,!online?3:kind()==TrafficNodeBlock.Kind.JAM_SENSOR?2:3,limit,now+12);else c.releaseTraffic(identity);
        }
        if(now%10==0)changed();
    }
    private void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag t){
        super.saveAdditional(t);t.putUUID("Identity",identity);
        if(parent!=null&&parentId!=null){t.putLong("Parent",parent.asLong());t.putUUID("ParentId",parentId);}
        if(road!=null&&roadId!=null){t.putLong("Road",road.asLong());t.putUUID("RoadId",roadId);}
        if(signal!=null&&signalId!=null){t.putUUID("SignalId",signalId);t.putLong("Signal",signal.asLong());t.putLong("Generation",signalGeneration);}
        t.putInt("Mode",mode);t.putInt("Limit",limit);t.putInt("Radius",radius);t.putInt("Threshold",threshold);t.putInt("Delay",delay);t.putInt("Group",group);t.putInt("Direction",direction);t.putString("Text",text);t.putString("Types",types);t.putInt("Forward",forward);t.putInt("Reverse",reverse);
        t.putInt("Speed",speed);t.putInt("Slow",slow);t.putBoolean("Online",online);t.putBoolean("Alarm",alarm);
    }
    @Override public void load(CompoundTag t){
        super.load(t);identity=t.hasUUID("Identity")?t.getUUID("Identity"):UUID.randomUUID();
        parent=t.contains("Parent")&&t.hasUUID("ParentId")?BlockPos.of(t.getLong("Parent")):null;parentId=parent==null?null:t.getUUID("ParentId");
        road=t.contains("Road")&&t.hasUUID("RoadId")?BlockPos.of(t.getLong("Road")):null;roadId=road==null?null:t.getUUID("RoadId");
        signal=t.contains("Signal")&&t.hasUUID("SignalId")?BlockPos.of(t.getLong("Signal")):null;signalId=signal==null?null:t.getUUID("SignalId");signalGeneration=t.getLong("Generation");
        mode=Math.max(0,Math.min(2,t.getInt("Mode")));limit=Math.max(5,Math.min(130,t.getInt("Limit")));radius=Math.max(8,Math.min(32,t.getInt("Radius")));threshold=Math.max(2,Math.min(8,t.getInt("Threshold")));delay=Math.max(20,Math.min(200,t.getInt("Delay")));group=t.getInt("Group")==1?1:0;direction=Math.max(0,Math.min(2,t.getInt("Direction")));
        text=WayfindingData.clean(t.getString("Text"),48);types=RoadVehicleRules.clean(t.getString("Types"));forward=Math.max(0,Math.min(MAX_COUNT,t.getInt("Forward")));reverse=Math.max(0,Math.min(MAX_COUNT,t.getInt("Reverse")));
        speed=t.getInt("Speed");slow=t.getInt("Slow");online=level!=null&&level.isClientSide&&t.getBoolean("Online");alarm=level!=null&&level.isClientSide&&t.getBoolean("Alarm");
        observations.clear();served.clear();passages.clear();slowSince=trainSince=readyAt=clearSince=-1;trainLast=-1000;holdUntil=speedUntil=0;
    }
    @Override public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
