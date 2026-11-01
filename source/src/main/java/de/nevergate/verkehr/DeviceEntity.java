package de.nevergate.verkehr;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public final class DeviceEntity extends BlockEntity {
    private final CrossingCycle cycle=new CrossingCycle();
    private boolean needsRecovery,needsPriorityRecovery,priorityRestart;
    private java.util.UUID trafficIdentity=java.util.UUID.randomUUID();
    private final PriorityCycle priority=new PriorityCycle();
    private int priorityGroup=-1;private boolean priorityUrgent;private long priorityExpires;
    public java.util.UUID trafficIdentity(){return trafficIdentity;}
    public int requestPriority(int group,boolean urgent){
        if(level==null||!isController()||!cycle.enabled()||blocked())return 0;
        group=group==1?1:0;if(program==1&&group!=0)return 0;
        if(priority.active()&&priority.group()==group)return 2;
        long now=level.getGameTime();
        if(priorityGroup<0||priorityExpires<now||urgent&&!priorityUrgent||priorityGroup==group&&urgent==priorityUrgent){
            priorityGroup=group;priorityUrgent=urgent;priorityExpires=now+40;return 1;
        }return 0;
    }
    private boolean priorityTick(long now){
        if(priorityGroup>=0&&priorityExpires<now)priorityGroup=-1;
        if(!cycle.enabled()||blocked()){priority.clear();priorityGroup=-1;return false;}
        if(priority.advance(now)){cycle.resumeAfterPriority(now);sync();}
        if(!priority.active()&&priorityGroup>=0&&cycle.priorityReady(now)){
            priority.begin(now,priorityGroup,priorityUrgent);priorityGroup=-1;sync();
        }
        return priority.active();
    }
    private int program,greenSeconds=30,turnSeconds=10,walkSeconds=6;
    private boolean parallel,manualNight,automaticNight,nightActive,restartSafety;
    private long restartAt=-1;
    private final int[][] presets=new int[3][];
    public int setting(int field) { return switch(field){case 0->program;case 1->greenSeconds;case 2->turnSeconds;case 3->walkSeconds;case 4->parallel?1:0;case 5->manualNight?1:0;case 6->automaticNight?1:0;default->0;}; }
    private void configureCycle() { cycle.configure(program,greenSeconds,turnSeconds,walkSeconds,parallel); }
    public void changeSetting(int field,int value) {
        if(field<0||field>6)return;
        setEnabled(false);
        switch(field){case 0->program=Math.max(0,Math.min(2,value));case 1->greenSeconds=Math.max(5,Math.min(120,value));case 2->turnSeconds=Math.max(5,Math.min(60,value));case 3->walkSeconds=Math.max(6,Math.min(60,value));case 4->parallel=value==1;case 5->manualNight=value==1;case 6->automaticNight=value==1;}
        configureCycle();sync();
    }
    public boolean presetPresent(int slot) { return slot>=0&&slot<3&&presets[slot]!=null; }
    public void savePreset(int slot) { if(slot<0||slot>2)return;presets[slot]=new int[7];for(int i=0;i<7;i++)presets[slot][i]=setting(i);sync(); }
    public void loadPreset(int slot) { if(!presetPresent(slot))return;int[] copy=presets[slot].clone();for(int i=0;i<7;i++)changeSetting(i,copy[i]); }
    public boolean nightActive() { tick();return cycle.enabled()&&nightActive; }
    private boolean blocked() { return nightActive||restartAt>=0; }
    public int phase() { tick();return !isEnabled()?0:priority.active()?8:nightActive?6:restartAt>=0?7:cycle.phase(level==null?0:level.getGameTime()); }
    public int nextPhase() { return !isEnabled()?0:nightActive?7:restartAt>=0?1:cycle.nextPhase(level==null?0:level.getGameTime()); }
    public int remaining() { return priority.active()&&level!=null?priority.remaining(level.getGameTime()):restartAt>=0&&level!=null?(int)Math.max(0,(restartAt-level.getGameTime()+19)/20):nightActive?0:cycle.secondsRemaining(level==null?0:level.getGameTime()); }
    public int signalLight(SignalEntity signal) {
        tick();if(level==null)return 0;
        if(priority.active()){int targetGroup=program==2?(signal.approach()<0?signal.group():signal.approach()<2?0:1):signal.group();return signal.movement()==0?priority.light(level.getGameTime(),targetGroup):0;}
        if(cycle.enabled()&&nightActive)return Math.floorMod(level.getGameTime(),20)<10?3:4;
        if(blocked())return 0;
        if(program==2){int group=signal.approach()<0?signal.group():signal.approach()<2?0:1;return cycle.light(level.getGameTime(),group).ordinal();}
        return (signal.movement()==0?cycle.light(level.getGameTime(),signal.group()):cycle.turnLight(level.getGameTime(),signal.approach(),signal.movement())).ordinal();
    }
    private BlockPos autoCenter,autoAnchor;
    private boolean autoEnabled,scanning,autoSeedAssigned;
    private static final Map<net.minecraft.world.level.Level,Set<BlockPos>> LOADED_CONTROLLERS=Collections.synchronizedMap(new WeakHashMap<>());
    private int autoRadius=32,autoUnclear;
    private long lastAutoScan=Long.MIN_VALUE;
    private String text="NG-Verkehr";
    private int capacity=100,occupied;
    private final Set<BlockPos> linkedDevices=new LinkedHashSet<>();
    private final Set<BlockPos> disconnectedDevices=new LinkedHashSet<>();
    private long connectionGeneration;
    public DeviceEntity(BlockPos pos,BlockState state) { super(NgTraffic.DEVICE.get(),pos,state); }
    public TrafficCycle.Light light(int group) {
        tick();
        return level==null||blocked()?TrafficCycle.Light.RED:priority.active()?TrafficCycle.Light.values()[priority.light(level.getGameTime(),group)]:cycle.light(level.getGameTime(),group);
    }
    @Override public void onLoad() { super.onLoad();if(level!=null&&!level.isClientSide&&isController())LOADED_CONTROLLERS.computeIfAbsent(level,l->new HashSet<>()).add(worldPosition); }
    @Override public void setRemoved() { if(level!=null){var known=LOADED_CONTROLLERS.get(level);if(known!=null)known.remove(worldPosition);}super.setRemoved(); }
    public void tick() {
        if(level==null || level.isClientSide || !isController()) return;
        LOADED_CONTROLLERS.computeIfAbsent(level,l->new HashSet<>()).add(worldPosition);
        if(autoEnabled&&autoCenter!=null&&autoAnchor!=null&&!scanning&&(lastAutoScan==Long.MIN_VALUE||level.getGameTime()-lastAutoScan>=40)) scanSignals();
        long now=level.getGameTime();
        if(needsPriorityRecovery){needsPriorityRecovery=false;priorityRestart=true;restartAt=now+40;priority.clear();priorityGroup=-1;sync();}
        boolean desiredNight=cycle.enabled()&&(manualNight||(automaticNight&&Math.floorMod(level.getDayTime(),24000)>=13000&&Math.floorMod(level.getDayTime(),24000)<23000));
        if(desiredNight!=nightActive){nightActive=desiredNight;cycle.setEnabled(cycle.enabled(),now);restartAt=desiredNight?-1:now+40;sync();}
        if(restartAt>=0&&now>=restartAt){restartAt=-1;if(priorityRestart){cycle.resumeAfterPriority(now);priorityRestart=false;}else cycle.setEnabled(cycle.enabled(),now);sync();}
        int turns=0,parallelMask=3;
        for(var pos:linkedDevices)if(level.hasChunkAt(pos)&&level.getBlockEntity(pos) instanceof SignalEntity signal&&signal.linkedTo(worldPosition)&&signal.movement()>0&&signal.approach()>=0)turns|=1<<CrossingCycle.routeIndex(signal.approach(),signal.movement());
        for(var pos:linkedDevices){
            if(!level.hasChunkAt(pos)){parallelMask=0;continue;}
            if(level.getBlockEntity(pos) instanceof SignalEntity signal&&signal.linkedTo(worldPosition)&&signal.kind()!=SignalKind.WARNING&&signal.movement()==0&&(signal.kind().movement<0||signal.approach()<0))parallelMask&=~(1<<signal.group());
        }
        cycle.setParallelPedMask(parallelMask);cycle.setTurnMask(turns);
        if(needsRecovery) { cycle.recover(level.getGameTime()); needsRecovery=false; setChanged(); }
        if(!priorityTick(now)&&!blocked()&&cycle.advance(level.getGameTime())) sync();
    }
    public static final int[] AUTO_RADII={16,24,32,48};
    public boolean autoEnabled() { return autoEnabled; }
    public int autoRadius() { return autoRadius; }
    public int autoUnclear() { return autoUnclear; }
    public BlockPos autoCenter() { return autoCenter; }
    public boolean hasAutoAnchor() { return autoAnchor!=null; }
    public void setAutoEnabled(boolean enabled) { autoEnabled=enabled;lastAutoScan=Long.MIN_VALUE;setEnabled(false); }
    public void setAutoRadius(int radius) {
        if(java.util.Arrays.stream(AUTO_RADII).noneMatch(r->r==radius))return;
        if(autoRadius!=radius){autoRadius=radius;autoUnclear=0;lastAutoScan=Long.MIN_VALUE;setEnabled(false);}
    }
    public void setAutoAnchor(BlockEntity signal) {
        if(autoAnchor==null&&(signal instanceof SignalEntity||signal instanceof PedestrianEntity)){autoAnchor=signal.getBlockPos().immutable();lastAutoScan=Long.MIN_VALUE;sync();}
    }
    public boolean setAutoCenter(BlockPos pos) {
        if(!isController()||pos.distSqr(worldPosition)>256*256)return false;
        autoCenter=pos.immutable();autoEnabled=true;lastAutoScan=Long.MIN_VALUE;autoUnclear=0;
        // Existing worlds can nominate any of their manually linked traffic devices.
        if(autoAnchor==null&&level!=null)for(var p:devices())if(level.hasChunkAt(p)&&(level.getBlockEntity(p) instanceof SignalEntity||level.getBlockEntity(p) instanceof PedestrianEntity)){autoAnchor=p;break;}
        setEnabled(false);return true;
    }
    private boolean inAutoArea(BlockPos pos) {
        if(autoCenter==null||Math.abs(pos.getY()-autoCenter.getY())>12)return false;
        long dx=(long)pos.getX()-autoCenter.getX(),dz=(long)pos.getZ()-autoCenter.getZ();
        return dx*dx+dz*dz<=(long)autoRadius*autoRadius;
    }
    private int autoApproach(SignalEntity signal) {
        if(!inAutoArea(signal.getBlockPos()))return -1;
        var facing=signal.getBlockState().getValue(OrientedBlock.FACING);
        long dx=(long)signal.getBlockPos().getX()-autoCenter.getX(),dz=(long)signal.getBlockPos().getZ()-autoCenter.getZ();
        long outward=dx*facing.getStepX()+dz*facing.getStepZ();
        long lateral=Math.abs(dx*facing.getStepZ()-dz*facing.getStepX());
        if(outward<2||lateral>outward+4)return -1;
        return switch(facing){case NORTH->0;case EAST->1;case SOUTH->2;case WEST->3;default->-1;};
    }
    private boolean canAutoRegister(BlockPos pos) { return devices().size()<256&&pos.distSqr(worldPosition)<=256*256; }
    private static int crossingAxis(PedestrianEntity entity) {
        return entity.getBlockState().getValue(OrientedBlock.FACING).getAxis()==net.minecraft.core.Direction.Axis.Z?0:1;
    }
    private int buttonGroup(PedestrianEntity button,List<BlockEntity> entities) {
        int fallback=crossingAxis(button),groups=0;long best=Long.MAX_VALUE;
        for(var be:entities)if(be instanceof PedestrianEntity lamp&&!lamp.isButton()&&lamp.linkedTo(worldPosition)){
            long dx=(long)lamp.getBlockPos().getX()-button.getBlockPos().getX(),dz=(long)lamp.getBlockPos().getZ()-button.getBlockPos().getZ();
            long dy=(long)lamp.getBlockPos().getY()-button.getBlockPos().getY();
            if(dx*dx+dz*dz>64||Math.abs(dy)>12)continue;
            long distance=4*(dx*dx+dz*dz)+dy*dy;
            if(distance<best){best=distance;groups=1<<lamp.group();}
            else if(distance==best)groups|=1<<lamp.group();
        }
        // Equally close different crossings are resolved by button orientation;
        // this also permits buttons to be built before their crossing lamps.
        return groups==1?0:groups==2?1:fallback;
    }
    public void scanSignals() {
        if(scanning||!autoEnabled||autoCenter==null||autoAnchor==null||!(level instanceof net.minecraft.server.level.ServerLevel server))return;
        scanning=true;lastAutoScan=level.getGameTime();int unclear=0;
        try {
            var entities=new ArrayList<BlockEntity>();
            int minX=(autoCenter.getX()-autoRadius)>>4,maxX=(autoCenter.getX()+autoRadius)>>4;
            int minZ=(autoCenter.getZ()-autoRadius)>>4,maxZ=(autoCenter.getZ()+autoRadius)>>4;
            for(int cx=minX;cx<=maxX;cx++)for(int cz=minZ;cz<=maxZ;cz++){
                var chunk=server.getChunkSource().getChunkNow(cx,cz);
                if(chunk!=null)entities.addAll(List.copyOf(chunk.getBlockEntities().values()));
            }
            var others=new ArrayList<DeviceEntity>();
            for(var pos:LOADED_CONTROLLERS.getOrDefault(level,Set.of()))if(level.hasChunkAt(pos)&&level.getBlockEntity(pos) instanceof DeviceEntity other&&other!=this&&other.isController()&&other.autoEnabled&&other.autoCenter!=null&&other.autoAnchor!=null)others.add(other);
            for(var be:entities)if(be instanceof SignalEntity signal&&!disconnectedDevices.contains(signal.getBlockPos())&&inAutoArea(signal.getBlockPos())){
                boolean seed=signal.getBlockPos().equals(autoAnchor)&&signal.linkedTo(worldPosition)&&!autoSeedAssigned;
                if(signal.hasController()&&(!signal.linkedTo(worldPosition)||(!signal.automatic()&&!seed)))continue;
                int approach=signal.kind()==SignalKind.WARNING?0:autoApproach(signal);
                boolean ambiguous=others.stream().anyMatch(other->other.inAutoArea(signal.getBlockPos()));
                if(approach<0||ambiguous){if(!signal.hasController())unclear++;continue;}
                if(signal.hasController()&&!seed&&signal.approach()==approach)continue;
                if(!signal.hasController()&&!canAutoRegister(signal.getBlockPos())){unclear++;continue;}
                setEnabled(false);signal.autoLink(worldPosition,approach);
                if(seed){autoSeedAssigned=true;sync();}
            }
            // Resolve the lamps before the buttons, independent of chunk/map
            // iteration order. A button can then use its actual local crossing.
            for(boolean buttons:new boolean[]{false,true})for(var be:entities)
                if(be instanceof PedestrianEntity pedestrian&&pedestrian.isButton()==buttons&&!disconnectedDevices.contains(pedestrian.getBlockPos())&&inAutoArea(pedestrian.getBlockPos())){
                    boolean seed=pedestrian.getBlockPos().equals(autoAnchor)&&pedestrian.linkedTo(worldPosition)&&!autoSeedAssigned;
                    if(pedestrian.hasController()&&(!pedestrian.linkedTo(worldPosition)||(!pedestrian.automatic()&&!seed)))continue;
                    if(others.stream().anyMatch(other->other.inAutoArea(pedestrian.getBlockPos()))){if(!pedestrian.hasController())unclear++;continue;}
                    int group=buttons?buttonGroup(pedestrian,entities):crossingAxis(pedestrian);
                    if(pedestrian.hasController()&&!seed&&pedestrian.group()==group)continue;
                    if(!pedestrian.hasController()&&!canAutoRegister(pedestrian.getBlockPos())){unclear++;continue;}
                    setEnabled(false);pedestrian.autoLink(worldPosition,group);
                    if(seed){autoSeedAssigned=true;sync();}
                }
            if(autoUnclear!=unclear){autoUnclear=unclear;sync();}
        } finally { scanning=false; }
    }
    public TrafficCycle.Light leftLight(int approach) { return routeLight(approach,1); }
    public TrafficCycle.Light routeLight(int approach,int movement) { tick();return level==null||blocked()?TrafficCycle.Light.RED:cycle.turnLight(level.getGameTime(),approach,movement); }
    public boolean requestPedestrians() { return requestPedestrians(0); }
    public boolean requestPedestrians(int group) { tick(); boolean accepted=!blocked()&&cycle.request(group); if(accepted) sync(); return accepted; }
    public boolean pedestrianGreen() { return pedestrianGreen(0); }
    public boolean pedestrianGreen(int group) { tick(); return level!=null&&!blocked()&&!priority.active()&&cycle.pedestrianGreen(level.getGameTime(),group); }
    public boolean pedestrianBusy() { tick(); return !blocked()&&cycle.busy(); }
    public boolean pedestrianBusy(int group) { tick(); return !blocked()&&cycle.busy(group); }
    public boolean isController() { return getBlockState().getBlock() instanceof DeviceBlock d && d.kind==DeviceBlock.Kind.CONTROLLER; }
    public boolean isEnabled() { return cycle.enabled(); }
    public long connectionGeneration() { return connectionGeneration; }
    public boolean validConnection(BlockPos pos,long generation) { return generation==connectionGeneration&&!disconnectedDevices.contains(pos); }
    public void allowConnection(BlockPos pos) { if(disconnectedDevices.remove(pos)){setEnabled(false);sync();} }
    public boolean disconnectDevice(BlockPos pos) {
        if(!devices().contains(pos))return false;
        setEnabled(false);disconnectedDevices.add(pos.immutable());linkedDevices.remove(pos);
        disconnectLoaded(pos);sync();return true;
    }
    private void disconnectLoaded(BlockPos pos) {
        if(level==null||!level.hasChunkAt(pos))return;
        var be=level.getBlockEntity(pos);
        if(be instanceof SignalEntity signal&&signal.linkedTo(worldPosition))signal.disconnect();
        else if(be instanceof PedestrianEntity pedestrian&&pedestrian.linkedTo(worldPosition))pedestrian.disconnect();
        else if(be instanceof ServiceEntity service&&service.linkedSignalTo(worldPosition))service.disconnectSignal();
    }
    public void disconnectAll() {
        var positions=devices();setEnabled(false);autoEnabled=false;
        connectionGeneration++;disconnectedDevices.addAll(positions);linkedDevices.clear();
        for(var pos:positions)disconnectLoaded(pos);
        // Keep midpoint/radius and the nominated layout reference. Recognition
        // only resumes after the user explicitly releases the disconnection list.
        autoSeedAssigned=true;autoUnclear=0;lastAutoScan=Long.MIN_VALUE;sync();
    }
    public void rediscoverDisconnected() {
        setEnabled(false);disconnectedDevices.clear();autoEnabled=true;lastAutoScan=Long.MIN_VALUE;sync();scanSignals();
    }
    public void registerDevice(BlockPos pos) {
        if(!disconnectedDevices.contains(pos)&&linkedDevices.size()<256 && pos.distSqr(worldPosition)<=256*256 && linkedDevices.add(pos.immutable())) setChanged();
    }
    public List<BlockPos> devices() {
        if(level!=null && linkedDevices.removeIf(pos->{
            if(!level.hasChunkAt(pos)) return false;
            var entity=level.getBlockEntity(pos);
            return !(entity instanceof SignalEntity s && s.linkedTo(worldPosition)) && !(entity instanceof PedestrianEntity p && p.linkedTo(worldPosition)) && !(entity instanceof ServiceEntity n && n.linkedSignalTo(worldPosition));
        })) setChanged();
        return List.copyOf(linkedDevices);
    }
    public void setEnabled(boolean value) { priority.clear();priorityGroup=-1;needsPriorityRecovery=priorityRestart=false; if(nightActive)restartSafety=true;nightActive=false;long now=level==null?0:level.getGameTime();restartAt=value&&restartSafety?now+40:-1;if(value)restartSafety=false;cycle.setEnabled(value,now); needsRecovery=false; sync(); }
    public void setText(String value) { text=value.substring(0,Math.min(64,value.length())); sync(); }
    public void setParking(int total,int used) {
        capacity=Math.max(0,Math.min(99999,total)); occupied=Math.max(0,Math.min(capacity,used)); sync();
    }
    public String displayText() {
        if(getBlockState().getBlock() instanceof DeviceBlock d && d.kind==DeviceBlock.Kind.PARKING)
            return capacity-occupied==0 ? "P BESETZT":"P "+(capacity-occupied)+" FREI";
        return text;
    }
    private void sync() { setChanged(); if(level!=null) level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3); }
    @Override protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);tag.putUUID("TrafficIdentity",trafficIdentity);tag.putBoolean("PriorityInterrupted",priority.active()||needsPriorityRecovery||priorityRestart);
        int[] settings=new int[7];for(int i=0;i<7;i++)settings[i]=setting(i);tag.putIntArray("ProgramSettings",settings);
        for(int i=0;i<3;i++)if(presets[i]!=null)tag.putIntArray("ProgramPreset"+i,presets[i]);
        tag.putInt("ActiveTurnBatch",cycle.activeTurns());tag.putBoolean("NightWasActive",nightActive||restartSafety||restartAt>=0);
        tag.putBoolean("AutoSeedAssigned",autoSeedAssigned);tag.putBoolean("AutoEnabled",autoEnabled);tag.putInt("AutoRadius",autoRadius);
        if(autoCenter!=null)tag.putLong("AutoCenter",autoCenter.asLong());if(autoAnchor!=null)tag.putLong("AutoAnchor",autoAnchor.asLong());
        tag.putLong("Epoch",cycle.epoch()); tag.putBoolean("Enabled",cycle.enabled());
        tag.putBoolean("PedPending",cycle.pending()); tag.putInt("PedPendingMask",cycle.pendingMask());
        tag.putLong("PedStart",cycle.start()); tag.putInt("PedResumeGroup",cycle.resumeGroup());
        tag.putInt("PedActiveGroup",cycle.activeGroup()); tag.putInt("PedFirstGroup",cycle.firstPending()); tag.putInt("PedServedMask",cycle.servedMask());
        tag.putInt("TurnMask",cycle.turnMask());tag.putInt("TurnQueue",cycle.turnQueue());tag.putInt("TurnActive",cycle.activeTurn());tag.putLong("TurnStart",cycle.turnStart());
        tag.putString("Text",text); tag.putInt("Capacity",capacity); tag.putInt("Occupied",occupied);
        tag.putLong("ConnectionGeneration",connectionGeneration);
        tag.putLongArray("DisconnectedDevices",disconnectedDevices.stream().mapToLong(BlockPos::asLong).toArray());
        tag.putLongArray("LinkedDevices",linkedDevices.stream().mapToLong(BlockPos::asLong).toArray());
    }
    @Override public void load(CompoundTag tag) {
        super.load(tag);trafficIdentity=tag.hasUUID("TrafficIdentity")?tag.getUUID("TrafficIdentity"):java.util.UUID.randomUUID();priority.clear();priorityGroup=-1;needsPriorityRecovery=tag.getBoolean("PriorityInterrupted");priorityRestart=false;
        int[] settings=tag.getIntArray("ProgramSettings");
        program=0;greenSeconds=30;turnSeconds=10;walkSeconds=6;parallel=false;manualNight=false;automaticNight=false;nightActive=false;restartAt=-1;
        if(settings.length==7){program=Math.max(0,Math.min(2,settings[0]));greenSeconds=Math.max(5,Math.min(120,settings[1]));turnSeconds=Math.max(5,Math.min(60,settings[2]));walkSeconds=Math.max(6,Math.min(60,settings[3]));parallel=settings[4]==1;manualNight=settings[5]==1;automaticNight=settings[6]==1;}
        restartSafety=tag.getBoolean("NightWasActive");if(restartSafety&&(!tag.contains("Enabled")||tag.getBoolean("Enabled")))restartAt=(level==null?0:level.getGameTime())+40;
        configureCycle();for(int i=0;i<3;i++){int[] saved=tag.getIntArray("ProgramPreset"+i);presets[i]=saved.length==7?saved:null;}
        autoSeedAssigned=tag.getBoolean("AutoSeedAssigned");autoEnabled=tag.getBoolean("AutoEnabled");autoRadius=tag.contains("AutoRadius")?tag.getInt("AutoRadius"):32;
        if(java.util.Arrays.stream(AUTO_RADII).noneMatch(r->r==autoRadius))autoRadius=32;
        autoCenter=tag.contains("AutoCenter")?BlockPos.of(tag.getLong("AutoCenter")):null;
        if(autoCenter!=null&&autoCenter.distSqr(worldPosition)>256*256)autoCenter=null;
        autoAnchor=tag.contains("AutoAnchor")?BlockPos.of(tag.getLong("AutoAnchor")):null;
        autoUnclear=0;lastAutoScan=Long.MIN_VALUE;scanning=false;
        int mask=tag.contains("PedPendingMask")?tag.getInt("PedPendingMask"):(tag.getBoolean("PedPending")?1:0);
        cycle.restore(tag.getLong("Epoch"),!tag.contains("Enabled")||tag.getBoolean("Enabled"),mask,
            tag.contains("PedStart")?Math.max(-1,tag.getLong("PedStart")):-1,tag.getInt("PedResumeGroup"),
            tag.getInt("PedActiveGroup"),tag.getInt("PedFirstGroup"),tag.getInt("PedServedMask"));
        cycle.restoreTurns(tag.getInt("TurnMask"),tag.getInt("TurnQueue"),tag.getInt("TurnActive"),tag.contains("TurnStart")?Math.max(-1,tag.getLong("TurnStart")):-1);
        cycle.restoreActiveTurns(tag.getInt("ActiveTurnBatch"));
        needsRecovery=cycle.start()>=0||cycle.turnStart()>=0; text=tag.contains("Text")?tag.getString("Text"):"NG-Verkehr";
        capacity=Math.max(0,Math.min(99999,tag.getInt("Capacity"))); occupied=Math.max(0,Math.min(capacity,tag.getInt("Occupied")));
        connectionGeneration=tag.getLong("ConnectionGeneration");disconnectedDevices.clear();
        for(long packed:tag.getLongArray("DisconnectedDevices")){var p=BlockPos.of(packed);if(p.distSqr(worldPosition)<=256*256)disconnectedDevices.add(p);}
        linkedDevices.clear(); for(long packed:tag.getLongArray("LinkedDevices")) { if(linkedDevices.size()>=256) break; var pos=BlockPos.of(packed); if(pos.distSqr(worldPosition)<=256*256) linkedDevices.add(pos); }
    }
    @Override public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
