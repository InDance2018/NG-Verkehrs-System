package de.nevergate.verkehr;

/** Conservative movement matrix: unknown or shared/crossing paths never run together. */
public final class ConflictRules {
    private static final int[] MASK={1,2,4,3,5,6,7};
    private ConflictRules() {}
    public static boolean compatible(int route,int other) {
        if(route<0||route>=24||other<0||other>=24)return false;
        int a=route%4,b=other%4,ma=MASK[route/4+1],mb=MASK[other/4+1];
        if(a==b)return (ma&mb)==0;
        int delta=Math.floorMod(a-b,4);
        if(delta==2)return (ma&2)==0&&(mb&2)==0;
        return ma==4&&mb==4;
    }
}
