package de.nevergate.verkehr;

/** Pure timing rules shared by real blocks and boundary tests. */
public final class ConstructionRules {
    private ConstructionRules(){}
    public static boolean scheduled(long time,int start,int end){
        int t=(int)Math.floorMod(time,24000);
        return start==end || (start<end?t>=start&&t<end:t>=start||t<end);
    }
    public static boolean running(long time,int step,int count,int index){
        return count>0&&index>=0&&index<count&&Math.floorMod(Math.floorDiv(time,Math.max(2,step)),count)==index;
    }
}
