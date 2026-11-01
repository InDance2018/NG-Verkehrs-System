import de.nevergate.verkehr.*;
public final class ProgramsTest {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        for(int a=0;a<24;a++)for(int b=0;b<24;b++){
            check(ConflictRules.compatible(a,b)==ConflictRules.compatible(b,a),"Asymmetric matrix");
            if(a==b)check(!ConflictRules.compatible(a,b),"Duplicate path accepted");
        }
        var c=new CrossingCycle();c.configure(0,5,5,6,true);c.setTurnMask(0xFFFFFF);c.setParallelPedMask(3);
        int batches=0,walk=0;
        for(int t=0;t<200000;t++){
            if(t%997==0)c.request(t%2);c.advance(t);
            int mask=0;for(int r=0;r<24;r++)if(c.turnLight(t,r%4,r/4+1)==TrafficCycle.Light.GREEN)mask|=1<<r;
            if(Integer.bitCount(mask)>1)batches++;
            for(int a=0;a<24;a++)for(int b=a+1;b<24;b++)if((mask&(1<<a))!=0&&(mask&(1<<b))!=0)check(ConflictRules.compatible(a,b),"Conflicting parallel routes");
            for(int g=0;g<2;g++)if(c.pedestrianGreen(t,g)){
                walk++;check(mask==0,"Turn crosses walk");check(c.light(t,1-g)==TrafficCycle.Light.RED,"Cross traffic on walk");
            }
        }
        check(batches>0&&walk>0,"Parallel/foot stages missing");
        c=new CrossingCycle();c.configure(0,5,5,6,true);c.setParallelPedMask(1);c.request(0);c.advance(180);
        check(c.light(220,0)==TrafficCycle.Light.GREEN&&c.pedestrianGreen(220,0),"Compatible parallel walk absent");
        c.setParallelPedMask(0);check(c.light(221,0)==TrafficCycle.Light.RED,"Unsafe path did not fall back to red");
        for(int program=1;program<=2;program++){
            c=new CrossingCycle();c.configure(program,5,5,6,true);c.setTurnMask(0xFFFFFF);
            for(int t=0;t<20000;t++){
                if(t%500==0)c.request(t%2);c.advance(t);
                check(c.turnMask()==0,"Special program retained protected intersection turns");
                if(program==1)check(c.light(t,1)==TrafficCycle.Light.RED,"Crossing program released group B");
                check(c.light(t,0)!=TrafficCycle.Light.GREEN||c.light(t,1)!=TrafficCycle.Light.GREEN,"Opposing worksite flows overlap");
            }
        }
        c=new CrossingCycle();c.configure(0,10,7,12,false);c.request(0);c.advance(280);
        check(c.pedestrianGreen(320,0)&&c.pedestrianGreen(559,0)&&!c.pedestrianGreen(560,0),"Custom walk duration wrong");
        System.out.println("PASS: 200000 ticks parallel matrix, clearance, pedestrian fallback, programs and custom durations");
    }
}
