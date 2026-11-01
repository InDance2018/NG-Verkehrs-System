import de.nevergate.verkehr.*;
public final class PriorityCycleTest {
    private static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        for(boolean urgent:new boolean[]{false,true})for(int group=0;group<2;group++){
            var p=new PriorityCycle();p.begin(100,group,urgent);int green=0,yellow=0,redyellow=0;
            for(int t=0;t<600;t++){
                int a=p.light(100+t,0),b=p.light(100+t,1);
                require(!(a==2&&b==2),"Conflicting priority greens");
                require(p.light(100+t,1-group)==0,"Other group released");
                int own=p.light(100+t,group);
                if(own==2)green++;if(own==1)redyellow++;if(own==3)yellow++;
                if(t<40)require(own==0,"Missing initial clearance");
                boolean done=p.advance(100+t);
                if(done){require(t==(urgent?360:320),"Wrong phase duration");break;}
            }
            require(green==(urgent?200:160)&&yellow==60&&redyellow==20,"Unsafe color duration");
        }
        var normal=new CrossingCycle();normal.configure(0,5,5,6,false);normal.setEnabled(true,0);
        var priority=new PriorityCycle();int inserts=0;
        for(int now=0;now<100000;now++){
            if(now%701==0)normal.request((now/701)%2);
            if(priority.advance(now))normal.resumeAfterPriority(now);
            if(!priority.active()&&normal.priorityReady(now)&&now%3==0){priority.begin(now,(now/100)%2,now%2==0);inserts++;}
            if(!priority.active())normal.advance(now);
            int a=priority.active()?priority.light(now,0):normal.light(now,0).ordinal();
            int b=priority.active()?priority.light(now,1):normal.light(now,1).ordinal();
            require(!(a==2&&b==2),"Mixed controller conflict");
            if(priority.active())require(!normal.pedestrianGreen(now,0)&&!normal.pedestrianGreen(now,1),"Priority interrupted a live walk");
        }
        require(inserts>10,"No real insertion coverage");
        System.out.println("PASS: exclusive priority colors, 2s entry/exit clearance, 100000 ticks of mixed requests and protected pedestrian phases.");
    }
}
