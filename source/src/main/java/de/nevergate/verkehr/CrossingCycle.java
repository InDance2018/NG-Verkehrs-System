package de.nevergate.verkehr;

/** Protected routes and pedestrian crossings inserted after vehicle yellow.
 * Bits 0..3 keep the dev11 left routes. Each further movement uses four bits.
 * Combination arrows have their own stage: no incompatible release is inferred.
 */
public final class CrossingCycle {
    private TrafficCycle VEHICLES=new TrafficCycle(600,60,40,20);
    private int green=600,walk=120,turnGreen=200,program,parallelPedMask,activeTurns;
    private boolean parallel;
    public void configure(int program,int greenSeconds,int turnSeconds,int walkSeconds,boolean parallel) {
        this.program=Math.max(0,Math.min(2,program));this.green=Math.max(5,Math.min(120,greenSeconds))*20;
        this.turnGreen=Math.max(5,Math.min(60,turnSeconds))*20;this.walk=Math.max(6,Math.min(60,walkSeconds))*20;
        this.parallel=parallel;VEHICLES=new TrafficCycle(green,60,40,20);
    }
    public void setParallelPedMask(int mask) { parallelPedMask=mask&3; }
    private int pedParallelGroup=-1;
    private int pedestrianExit() { return pedParallelGroup>=0?100:EXIT; }
    public int activeTurns() { return activeTurns; }
    public void restoreActiveTurns(int mask) { activeTurns=turnStart>=0?((mask|1<<activeTurn)&turnMask):0; }
    public static final int ENTRY=40, WALK=120, EXIT=80;
    public static final int TURN_ENTRY=40, TURN_RED_YELLOW=20, TURN_GREEN=200, TURN_YELLOW=60, TURN_EXIT=40;
    private long epoch, start=-1;
    private boolean enabled=true;
    private int resumeGroup, pendingMask, firstPending, activeGroup=-1, servedMask;
    private int turnMask,turnQueue,activeTurn=-1;
    private long turnStart=-1;
    public void setTurnMask(int mask) { turnMask=program==0?mask&0xFFFFFF:0; turnQueue&=turnMask;activeTurns&=turnMask; }
    public int turnMask() { return turnMask; }
    public int turnQueue() { return turnQueue; }
    public int activeTurn() { return activeTurn; }
    public long turnStart() { return turnStart; }
    private void beginTurn(long now) {
        activeTurn=Integer.numberOfTrailingZeros(turnQueue);activeTurns=1<<activeTurn;turnQueue&=~activeTurns;
        if(parallel)for(int route=0;route<24;route++)if((turnQueue&(1<<route))!=0){
            boolean safe=true;for(int other=0;other<24;other++)if((activeTurns&(1<<other))!=0&&!ConflictRules.compatible(route,other))safe=false;
            if(safe){activeTurns|=1<<route;turnQueue&=~(1<<route);}
        }
        turnStart=now;pedParallelGroup=-1;
        start=-1;activeGroup=-1;servedMask=0;
    }
    private void resumeVehicles(long now) {
        epoch=now-(resumeGroup==1?VEHICLES.period()/2:0);
        start=-1;activeGroup=-1;servedMask=0;turnStart=-1;activeTurn=-1;turnQueue=0;activeTurns=0;pedParallelGroup=-1;
    }
    public static int routeIndex(int approach,int movement) {
        return approach<0||approach>3||movement<1||movement>6 ? -1 : (movement-1)*4+approach;
    }
    public TrafficCycle.Light turnLight(long now,int approach) { return turnLight(now,approach,1); }
    public TrafficCycle.Light turnLight(long now,int approach,int movement) {
        int route=routeIndex(approach,movement);
        if(!enabled||route<0||(activeTurns&(1<<route))==0||turnStart<0||(turnMask&(1<<route))==0) return TrafficCycle.Light.RED;
        long elapsed=now-turnStart;
        if(elapsed<TURN_ENTRY)return TrafficCycle.Light.RED;
        if(elapsed<TURN_ENTRY+TURN_RED_YELLOW)return TrafficCycle.Light.RED_YELLOW;
        if(elapsed<TURN_ENTRY+TURN_RED_YELLOW+turnGreen)return TrafficCycle.Light.GREEN;
        if(elapsed<TURN_ENTRY+TURN_RED_YELLOW+turnGreen+TURN_YELLOW)return TrafficCycle.Light.YELLOW;
        return TrafficCycle.Light.RED;
    }
    public boolean request() {
        return request(0);
    }
    public boolean request(int group) {
        if(group<0 || group>1) throw new IllegalArgumentException("Pedestrian group must be 0 or 1");
        if(!enabled || activeGroup==group || (pendingMask&(1<<group))!=0) return false;
        if(pendingMask==0) firstPending=group;
        pendingMask|=1<<group; return true;
    }
    private void beginNext(long now) {
        activeGroup=firstPending; pendingMask&=~(1<<activeGroup);
        firstPending=1-activeGroup; servedMask|=1<<activeGroup; start=now;pedParallelGroup=parallel&&program==0&&(parallelPedMask&(1<<activeGroup))!=0?activeGroup:-1;
    }
    public boolean advance(long now) {
        if(!enabled) return false;
        if(turnStart>=0) {
            if(now-turnStart<TURN_ENTRY+TURN_RED_YELLOW+turnGreen+TURN_YELLOW+TURN_EXIT)return false;
            if(turnQueue!=0)beginTurn(now);else resumeVehicles(now);
            return true;
        }
        if(start>=0) {
            if(now-start<ENTRY+walk+pedestrianExit()) return false;
            // Serve both queued crossings in order, but at most once each per
            // batch so sustained requests cannot starve vehicle traffic.
            if(pendingMask!=0 && (servedMask&(1<<firstPending))==0) {
                beginNext(now); return true;
            }
            if(turnQueue!=0)beginTurn(now);else resumeVehicles(now);
            return true;
        }
        int half=VEHICLES.period()/2;
        int phase=(int)Math.floorMod(now-epoch,program==1?half:VEHICLES.period());
        int nextTurns=turnMask&(phase<half?0x555555:0xAAAAAA);
        if((pendingMask!=0||nextTurns!=0) && phase%half>=half-40) {
            servedMask=0;resumeGroup=program==1?0:phase<half?1:0;turnQueue=nextTurns;
            if(pendingMask!=0)beginNext(now);else beginTurn(now);
            return true;
        }
        return false;
    }
    public TrafficCycle.Light light(long now,int group) {
        if(!enabled||turnStart>=0)return TrafficCycle.Light.RED;
        if(start>=0){
            if(group!=pedParallelGroup||(parallelPedMask&(1<<group))==0)return TrafficCycle.Light.RED;
            long elapsed=now-start;
            if(elapsed<ENTRY-20)return TrafficCycle.Light.RED;
            if(elapsed<ENTRY)return TrafficCycle.Light.RED_YELLOW;
            if(elapsed<ENTRY+walk)return TrafficCycle.Light.GREEN;
            if(elapsed<ENTRY+walk+60)return TrafficCycle.Light.YELLOW;
            return TrafficCycle.Light.RED;
        }
        if(program==1)return group==0?VEHICLES.light(Math.floorMod(now-epoch,VEHICLES.period()/2),0):TrafficCycle.Light.RED;
        return VEHICLES.light(now-epoch,group);
    }
    public boolean pedestrianGreen(long now) { return pedestrianGreen(now,0); }
    public boolean pedestrianGreen(long now,int group) { return enabled && turnStart<0 && start>=0 && activeGroup==group && now-start>=ENTRY && now-start<ENTRY+walk; }
    public boolean busy() { return pendingMask!=0 || start>=0; }
    public boolean busy(int group) { return (pendingMask&(1<<group))!=0 || activeGroup==group; }
    public boolean enabled() { return enabled; }
    public long epoch() { return epoch; }
    public long start() { return start; }
    public boolean pending() { return pendingMask!=0; }
    public int pendingMask() { return pendingMask; }
    public int activeGroup() { return activeGroup; }
    public int firstPending() { return firstPending; }
    public int servedMask() { return servedMask; }
    public int resumeGroup() { return resumeGroup; }
    public void setEnabled(boolean value,long now) { enabled=value; epoch=now; start=-1; pendingMask=0; activeGroup=-1; servedMask=0; turnStart=-1;activeTurn=-1;turnQueue=0;activeTurns=0;pedParallelGroup=-1; }
    public void restore(long epoch,boolean enabled,boolean pending,long start,int resumeGroup) {
        restore(epoch,enabled,pending?1:0,start,resumeGroup,0,0,start>=0?1:0);
    }
    public void restore(long epoch,boolean enabled,int mask,long start,int resumeGroup,int active,int first,int served) {
        this.epoch=epoch; this.enabled=enabled; this.pendingMask=enabled?(mask&3):0;
        this.start=enabled?start:-1; this.resumeGroup=resumeGroup==1?1:0;
        this.activeGroup=this.start>=0?(active==1?1:0):-1;
        if(activeGroup>=0) pendingMask&=~(1<<activeGroup);
        this.firstPending=first==1?1:0;
        if((pendingMask&(1<<firstPending))==0) firstPending=1-firstPending;
        this.servedMask=activeGroup>=0?((served&3)|(1<<activeGroup)):0;
        turnMask=0;turnQueue=0;activeTurns=0;pedParallelGroup=-1;activeTurn=-1;turnStart=-1;
    }
    public void restoreTurns(int mask,int queue,int active,long start) {
        turnMask=mask&0xFFFFFF;turnQueue=enabled?(queue&turnMask):0;
        turnStart=enabled?start:-1;activeTurn=turnStart>=0?Math.max(0,Math.min(23,active)):-1;
        if(activeTurn>=0){activeTurns=1<<activeTurn;turnQueue&=~(1<<activeTurn);this.start=-1;activeGroup=-1;}
    }
    // Never resume a partially elapsed walk phase immediately after loading.
    public int phase(long now) {
        if(!enabled)return 0;if(start>=0)return 3+activeGroup;if(turnStart>=0)return 5;
        return 1+(program==1?0:Math.floorMod(now-epoch,VEHICLES.period())/(VEHICLES.period()/2));
    }
    public int nextPhase(long now) {
        if(!enabled)return 0;if(start>=0)return pendingMask!=0?3+firstPending:turnQueue!=0?5:1+resumeGroup;
        if(turnStart>=0)return turnQueue!=0?5:1+resumeGroup;
        return pendingMask!=0?3+firstPending:turnMask!=0?5:program==1?1:3-phase(now);
    }
    public int secondsRemaining(long now) {
        if(!enabled)return 0;long ticks=start>=0?ENTRY+walk+pedestrianExit()-(now-start):turnStart>=0?TURN_ENTRY+TURN_RED_YELLOW+turnGreen+TURN_YELLOW+TURN_EXIT-(now-turnStart):VEHICLES.period()/2-Math.floorMod(now-epoch,VEHICLES.period()/2);
        return (int)Math.max(0,(ticks+19)/20);
    }
    public boolean priorityReady(long now){return enabled&&start<0&&turnStart<0&&light(now,0)==TrafficCycle.Light.RED&&light(now,1)==TrafficCycle.Light.RED;}
    public void resumeAfterPriority(long now){resumeGroup=0;resumeVehicles(now);}
    public void recover(long now) { if(start>=0) start=now; if(turnStart>=0)turnStart=now; }
}
