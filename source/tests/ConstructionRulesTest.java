import de.nevergate.verkehr.ConstructionRules;
public final class ConstructionRulesTest {
    static void require(boolean b,String s){if(!b)throw new AssertionError(s);}
    public static void main(String[] args){
        for(int from=0;from<24000;from+=1000)for(int to=0;to<24000;to+=1000){
            int count=0;
            for(int t=0;t<24000;t++){
                boolean active=ConstructionRules.scheduled(t,from,to);
                require(active==ConstructionRules.scheduled(t+24000L,from,to),"Day periodicity");
                if(active)count++;
            }
            require(count==(from==to?24000:Math.floorMod(to-from,24000)),"Wrong active duration");
            if(from!=to){require(ConstructionRules.scheduled(from,from,to),"Start excluded");require(!ConstructionRules.scheduled(to,from,to),"End included");}
        }
        for(int n=1;n<=32;n++)for(int step:new int[]{2,4,8,12})for(int t=-100;t<1000;t++){
            int count=0;
            for(int i=0;i<n;i++){
                boolean lit=ConstructionRules.running(t,step,n,i);if(lit)count++;
                require(lit==ConstructionRules.running(t+(long)step*n,step,n,i),"Sequence period");
            }
            require(count==1,"Not exactly one active lamp");
        }
        require(!ConstructionRules.running(0,4,0,0)&&!ConstructionRules.running(0,4,3,-1),"Invalid group inputs");
        System.out.println("PASS: all 576 hourly schedule pairs over full days, overnight boundaries, and one active lamp for group sizes 1-32.");
    }
}
