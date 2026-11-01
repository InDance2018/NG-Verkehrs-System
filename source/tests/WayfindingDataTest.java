import de.nevergate.verkehr.WayfindingData;

public final class WayfindingDataTest {
    public static void main(String[] args){
        var data=new WayfindingData();data.targets[0]="Moers\n§a\u202E West";data.targets[1]="äöüß😀".repeat(20);data.distances[0]="12345678901234567890";data.laneCount=99;data.lanes[0]=-7;data.directions[0]=999;
        var saved=data.copy();
        if(saved.targets[0].contains("\n")||saved.targets[0].contains("§")||saved.targets[0].contains("\u202E"))throw new AssertionError("Unsafe formatting retained");
        if(saved.targets[1].codePointCount(0,saved.targets[1].length())!=48)throw new AssertionError("Unicode limit incorrect");
        if(saved.distances[0].length()!=12||saved.laneCount!=4||saved.lanes[0]!=0||saved.directions[0]!=4)throw new AssertionError("Bounds incorrect");
        saved.targets[0]="Other";if(data.targets[0].equals("Other"))throw new AssertionError("Snapshot aliases source");
        data.targets[2]=null;if(!data.copy().targets[2].isEmpty())throw new AssertionError("Missing value not normalized");
        for(int i=0;i<5;i++)if(WayfindingData.arrow(i).isEmpty())throw new AssertionError("Missing arrow");
        System.out.println("PASS: sign Unicode sanitation, limits, lane bounds and independent snapshots");
    }
}
