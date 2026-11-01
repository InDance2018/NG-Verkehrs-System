import de.nevergate.verkehr.CrossingCycle;
import de.nevergate.verkehr.TrafficCycle;

public final class AllSignalsTest {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        check(CrossingCycle.routeIndex(-1,1)==-1&&CrossingCycle.routeIndex(0,7)==-1,"Invalid routes accepted");
        var c=new CrossingCycle();c.setTurnMask(0xFFFFFF);int[] green=new int[24];
        int pedestrians=0,vehicles=0;long lastNormal=-10000,lastPed=-10000,lastRoute=-10000;
        for(int t=0;t<200000;t++){
            if(t%173==5)c.request(0);if(t%239==20)c.request(1);c.advance(t);
            boolean normal=c.light(t,0)!=TrafficCycle.Light.RED||c.light(t,1)!=TrafficCycle.Light.RED;
            boolean walk=c.pedestrianGreen(t,0)||c.pedestrianGreen(t,1);int active=0;
            for(int m=1;m<=6;m++)for(int a=0;a<4;a++){
                var light=c.turnLight(t,a,m);if(light!=TrafficCycle.Light.RED)active++;
                if(light==TrafficCycle.Light.GREEN)green[CrossingCycle.routeIndex(a,m)]++;
            }
            check(active<=1,"Protected routes overlap at "+t);
            check(active==0||(!normal&&!walk),"Protected route conflicts with vehicles/bicycles/pedestrians at "+t);
            if(active>0){check(t-lastNormal>=CrossingCycle.TURN_ENTRY,"Entry clearance missing");check(t-lastPed>CrossingCycle.EXIT,"Pedestrian clearance missing");lastRoute=t;}
            if(normal){check(t-lastRoute>CrossingCycle.TURN_EXIT,"Exit clearance missing");lastNormal=t;vehicles++;}
            if(walk){check(t-lastRoute>CrossingCycle.TURN_EXIT,"Walk release too soon");lastPed=t;pedestrians++;}
            check(!c.advance(t),"Repeated reads advance cycle");
        }
        for(int i=0;i<24;i++)check(green[i]>1000,"Route starved: "+i);
        check(vehicles>1000&&pedestrians>1000,"Regular traffic starved");
        // The highest route bit must survive save/load and recover through all-red.
        c=new CrossingCycle();c.restore(0,true,0,-1,0,0,0,0);c.restoreTurns(1<<23,0,23,700);c.recover(5000);
        check(c.turnLight(5000,3,6)==TrafficCycle.Light.RED,"Reload skipped clearance");
        check(c.turnLight(5060,3,6)==TrafficCycle.Light.GREEN,"High-bit combination route lost");
        c.setTurnMask(0);check(c.turnLight(5061,3,6)==TrafficCycle.Light.RED,"Removed route remains green");
        c.setEnabled(false,5062);check(c.light(5062,0)==TrafficCycle.Light.RED&&!c.busy(),"Stop failed");
        // Generic right assignment works even if no dedicated right block exists.
        c=new CrossingCycle();c.setTurnMask(1<<4);c.advance(680);
        check(c.turnLight(740,0,2)==TrafficCycle.Light.GREEN&&c.turnLight(740,0,1)==TrafficCycle.Light.RED,"Right-only phase missing");
        System.out.println("PASS: 200000 ticks, all 24 protected routes, FIFO pedestrian requests, clearance, fairness, high-bit load recovery, removal and stop.");
    }
}
