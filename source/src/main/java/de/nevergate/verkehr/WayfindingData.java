package de.nevergate.verkehr;

import java.util.Arrays;

/** Bounded, plain-text sign content. No commands or JSON components are accepted. */
public final class WayfindingData {
    public static final int ROWS=3, MAX_TEXT=48, MAX_DISTANCE=12, MAX_LANES=4;
    public final String[] targets=new String[ROWS], distances=new String[ROWS];
    public final int[] directions=new int[ROWS], lanes=new int[MAX_LANES];
    public int laneCount=2;
    public WayfindingData() { Arrays.fill(targets,"");Arrays.fill(distances,"");Arrays.fill(directions,1);Arrays.fill(lanes,1); }
    public static String clean(String input,int max) {
        if(input==null)return "";
        var out=new StringBuilder();
        input.codePoints().filter(c->!Character.isISOControl(c)&&c!=0xA7&&c!=0x202A&&c!=0x202B&&c!=0x202D&&c!=0x202E&&c!=0x202C&&!(c>=0x2066&&c<=0x2069)).limit(max).forEach(out::appendCodePoint);
        return out.toString().strip();
    }
    public WayfindingData copy() {
        var result=new WayfindingData();result.laneCount=Math.max(1,Math.min(MAX_LANES,laneCount));
        for(int i=0;i<ROWS;i++){result.targets[i]=clean(targets[i],MAX_TEXT);result.distances[i]=clean(distances[i],MAX_DISTANCE);result.directions[i]=Math.max(0,Math.min(4,directions[i]));}
        for(int i=0;i<MAX_LANES;i++)result.lanes[i]=Math.max(0,Math.min(4,lanes[i]));
        return result;
    }
    public static String arrow(int direction) { return switch(direction){case 0->"←";case 2->"→";case 3->"↖";case 4->"↗";default->"↑";}; }
}
