import de.nevergate.verkehr.TrafficCycle;
import static de.nevergate.verkehr.TrafficCycle.Light.*;
public class TrafficCycleTest {
    static void check(boolean value,String why) { if(!value) throw new AssertionError(why); }
    public static void main(String[] args) {
        var c=new TrafficCycle(600,60,40,20);
        for(long t=-2L*c.period();t<=3L*c.period();t++) {
            var a=c.light(t,0); var b=c.light(t,1);
            check(a==RED||b==RED,"Conflicting phases at "+t);
            check(c.light(t+c.period(),0)==a,"Periodicity at "+t);
        }
        check(c.light(0,0)==RED_YELLOW,"Red-yellow first");
        check(c.light(20,0)==GREEN,"Green transition");
        check(c.light(620,0)==YELLOW,"Yellow transition");
        check(c.light(680,0)==RED,"Clearance start");
        check(c.light(719,0)==RED&&c.light(719,1)==RED,"Clearance end");
        check(c.light(720,1)==RED_YELLOW,"Opposing red-yellow");
        check(c.light(740,1)==GREEN,"Opposing green");
        for(int invalid:new int[]{-1,2}) { boolean thrown=false; try { c.light(0,invalid); } catch(IllegalArgumentException e) { thrown=true; } check(thrown,"Invalid group rejected"); }
        boolean thrown=false; try {new TrafficCycle(0,1,1,1);} catch(IllegalArgumentException e){thrown=true;} check(thrown,"Invalid duration rejected");
        System.out.println("PASS: 7201 ticks, conflict exclusion, clearance, transitions, periodicity and invalid inputs.");
    }
}
