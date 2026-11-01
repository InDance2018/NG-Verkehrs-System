import de.nevergate.verkehr.CrossingCycle;
import de.nevergate.verkehr.TrafficCycle;

public final class CrossingCycleTest {
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        var c=new CrossingCycle();
        long lastVehicle=-10000,lastWalk=-10000; int walkTicks=0,completed=0;
        for(int tick=0;tick<100000;tick++) {
            if(tick%231==10) c.request();
            c.advance(tick); boolean green=c.pedestrianGreen(tick);
            boolean vehicle=false;
            for(int group=0;group<2;group++) vehicle|=c.light(tick,group)!=TrafficCycle.Light.RED;
            check(!(green&&vehicle),"Conflicting pedestrian/vehicle signal at "+tick);
            if(green) {
                check(tick-lastVehicle>=CrossingCycle.ENTRY,"Entry clearance too short");
                walkTicks++;lastWalk=tick;
                check(!c.request(),"Repeated request during walk must not extend it");
            } else if(walkTicks>0) { check(walkTicks==CrossingCycle.WALK,"Walk duration changed");walkTicks=0;completed++; }
            if(vehicle) { check(tick-lastWalk>CrossingCycle.EXIT,"Exit clearance too short");lastVehicle=tick; }
            check(!c.advance(tick),"Repeated update at same tick must be idempotent");
        }
        check(completed>100,"Not enough crossing cycles exercised");
        c.setEnabled(false,100000);check(!c.request(),"Stopped controller accepts no request");
        check(!c.pedestrianGreen(100000)&&c.light(100000,0)==TrafficCycle.Light.RED,"Stopped controller must be red");
        c.setEnabled(true,100000);check(!c.busy(),"Restart must clear old requests");
        check(c.light(100000,0)==TrafficCycle.Light.RED_YELLOW,"Normal restart phase lost");
        c.restore(0,true,false,20,1); c.recover(5000);c.advance(5000);
        check(!c.pedestrianGreen(5000)&&c.light(5000,0)==TrafficCycle.Light.RED,"Load must recover through clearance");
        c.advance(5040);check(c.pedestrianGreen(5040),"Recovered crossing never enters walk");
        c.advance(5240);check(!c.busy()&&c.light(5240,1)==TrafficCycle.Light.RED_YELLOW,"Resume must serve the next vehicle group");
        var baseline=new TrafficCycle(600,60,40,20);c=new CrossingCycle();
        for(int t=0;t<10000;t++) { c.advance(t);for(int g=0;g<2;g++)check(c.light(t,g)==baseline.light(t,g),"Unrequested cycle changed"); }
        System.out.println("PASS: 100000 ticks, "+completed+" walk phases, clearance, repeat requests, restart/recovery and unchanged vehicle cycle.");
    }
}
