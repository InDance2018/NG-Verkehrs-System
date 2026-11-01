import de.nevergate.verkehr.CrossingCycle;
import de.nevergate.verkehr.TrafficCycle;

public final class ProtectedLeftTest {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        var c=new CrossingCycle();c.setTurnMask(1);c.advance(680);
        check(c.turnLight(680,0)==TrafficCycle.Light.RED,"Entry clearance missing");
        c.advance(720);check(c.turnLight(720,0)==TrafficCycle.Light.RED_YELLOW,"Red-yellow missing");
        c.advance(740);check(c.turnLight(740,0)==TrafficCycle.Light.GREEN,"Green missing");
        check(c.light(740,0)==TrafficCycle.Light.RED&&c.light(740,1)==TrafficCycle.Light.RED,"Straight traffic conflicts");
        check(c.request(0),"Pedestrian request during left phase lost");
        c.advance(940);check(c.turnLight(940,0)==TrafficCycle.Light.YELLOW,"Yellow missing");
        c.advance(1000);check(c.turnLight(1000,0)==TrafficCycle.Light.RED,"Exit clearance missing");
        c.advance(1040);check(c.light(1040,1)==TrafficCycle.Light.RED_YELLOW&&c.busy(0),"Next road or queued pedestrian lost");
        c=new CrossingCycle();c.restore(0,true,2,-1,1,0,1,0);c.restoreTurns(5,4,0,680);c.recover(5000);c.advance(5000);
        check(c.turnLight(5000,0)==TrafficCycle.Light.RED,"Loaded left phase must recover through red");
        c.advance(5060);check(c.turnLight(5060,0)==TrafficCycle.Light.GREEN&&!c.pedestrianGreen(5060,1),"Recovery released conflicting traffic");
        c.advance(5360);check(c.activeTurn()==2&&c.busy(1),"Saved next turn or pedestrian request lost");
        c.setEnabled(false,5400);check(c.turnLight(5400,2)==TrafficCycle.Light.RED&&!c.busy(),"Stop failed");
        c=new CrossingCycle();c.setTurnMask(15);int[] greens={0,0,0,0};int pedestrian=0;
        long lastStraight=-10000,lastPed=-10000,lastLeft=-10000;
        for(int t=0;t<100000;t++){
            if(t%173==5)c.request(0);if(t%239==20)c.request(1);c.advance(t);
            boolean straight=c.light(t,0)!=TrafficCycle.Light.RED||c.light(t,1)!=TrafficCycle.Light.RED;
            boolean walk=c.pedestrianGreen(t,0)||c.pedestrianGreen(t,1);int active=0;
            for(int a=0;a<4;a++)if(c.turnLight(t,a)!=TrafficCycle.Light.RED){active++;if(c.turnLight(t,a)==TrafficCycle.Light.GREEN)greens[a]++;}
            check(active<=1,"Multiple protected left approaches at "+t);
            check(active==0||(!straight&&!walk),"Conflicting protected left at "+t);
            if(active>0){check(t-lastStraight>=CrossingCycle.TURN_ENTRY,"Left entry clearance too short");check(t-lastPed>CrossingCycle.EXIT,"Left starts before pedestrian clearance");lastLeft=t;}
            if(straight){check(t-lastLeft>CrossingCycle.TURN_EXIT,"Vehicle restart clearance too short");lastStraight=t;}
            if(walk){check(t-lastLeft>CrossingCycle.TURN_EXIT,"Walk conflicts with previous turn");lastPed=t;pedestrian++;}
            check(!c.advance(t),"Updates must be idempotent");
        }
        for(int a=0;a<4;a++)check(greens[a]>1000,"Left approach starved: "+a);
        check(pedestrian>1000,"Pedestrians starved");
        System.out.println("PASS: 100000 ticks, protected N/E/S/W, pedestrian queues, clearances, load recovery and no conflicting releases.");
    }
}
