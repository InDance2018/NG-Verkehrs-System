import de.nevergate.verkehr.CrossingCycle;
import de.nevergate.verkehr.TrafficCycle;

public final class CrossingGroupsTest {
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    public static void main(String[] args) {
        var c=new CrossingCycle();
        check(c.request(1)&&!c.busy(0)&&c.busy(1),"A must not inherit B requests");
        check(!c.request(1)&&c.request(0),"Requests must deduplicate per group");
        c.advance(680);c.advance(720);
        check(c.pedestrianGreen(720,1)&&!c.pedestrianGreen(720,0),"First request B must be served first");
        c.advance(920);c.advance(960);
        check(c.pedestrianGreen(960,0)&&!c.pedestrianGreen(960,1),"Second request A must follow B");
        check(c.request(1),"B can queue again during A");
        c.advance(1160);
        check(c.light(1160,1)==TrafficCycle.Light.RED_YELLOW&&c.busy(1),"Vehicle traffic must resume after two served crossings");
        check(!c.pedestrianGreen(1160,0)&&!c.pedestrianGreen(1160,1),"Resumed vehicles conflict with pedestrians");
        var r=new CrossingCycle();
        r.restore(0,true,1,680,1,1,0,2);r.recover(5000);r.advance(5000);
        check(!r.pedestrianGreen(5000,1),"Recovery must start all-red");
        r.advance(5040);check(r.pedestrianGreen(5040,1)&&!r.pedestrianGreen(5040,0),"Active B lost after recovery");
        r.advance(5240);r.advance(5280);check(r.pedestrianGreen(5280,0),"Queued A lost after recovery");
        r.setEnabled(false,5290);check(!r.busy()&&!r.pedestrianGreen(5290,0)&&!r.request(1),"Stop must clear both groups");
        c=new CrossingCycle();long lastVehicle=-10000,lastWalk=-10000;int[] counts={0,0},lengths={0,0};
        for(int t=0;t<100000;t++) {
            if(t%231==10)c.request(0);if(t%317==30)c.request(1);c.advance(t);
            boolean a=c.pedestrianGreen(t,0),b=c.pedestrianGreen(t,1);
            check(!(a&&b),"Crossings released together at "+t);
            boolean vehicle=c.light(t,0)!=TrafficCycle.Light.RED||c.light(t,1)!=TrafficCycle.Light.RED;
            check(!(vehicle&&(a||b)),"Vehicle/pedestrian conflict at "+t);
            if(a||b){check(t-lastVehicle>=CrossingCycle.ENTRY,"Entry clearance lost");lastWalk=t;}
            if(vehicle){check(t-lastWalk>CrossingCycle.EXIT,"Exit clearance lost");lastVehicle=t;}
            for(int g=0;g<2;g++) {
                if(c.pedestrianGreen(t,g)){lengths[g]++;check(!c.request(g),"Active requests must not extend green");}
                else if(lengths[g]>0){check(lengths[g]==CrossingCycle.WALK,"Walk duration changed");counts[g]++;lengths[g]=0;}
            }
            check(!c.advance(t),"Same-tick updates must be idempotent");
        }
        check(counts[0]>30&&counts[1]>30,"One crossing starved");
        System.out.println("PASS: 100000 ticks, A="+counts[0]+", B="+counts[1]+", FIFO, isolation, deduplication, recovery and vehicle fairness.");
    }
}
