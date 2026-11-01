package de.nevergate.verkehr;
/** Exclusive straight-group phase. Entry only after the normal controller reaches all-red. */
public final class PriorityCycle {
    private long start=-1;private int group,green;
    public boolean active(){return start>=0;}
    public int group(){return group;}
    public void begin(long now,int group,boolean urgent){this.start=now;this.group=group==1?1:0;green=urgent?200:160;}
    public int light(long now,int target){
        if(!active()||target!=group)return 0;long t=now-start;
        return t<40?0:t<60?1:t<60+green?2:t<120+green?3:0;
    }
    public boolean advance(long now){if(active()&&now-start>=160+green){clear();return true;}return false;}
    public int remaining(long now){return active()?(int)Math.max(0,(160+green-(now-start)+19)/20):0;}
    public void clear(){start=-1;}
}
