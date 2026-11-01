import de.nevergate.verkehr.RoadSystemData;
import de.nevergate.verkehr.RoadVehicleRules;
public final class RoadSystemDataTest {
    public static void main(String[] args){
        var input=new RoadSystemData();input.name="Garage\n§a";input.capacity=-5;input.occupied=Integer.MAX_VALUE;input.speed=Integer.MAX_VALUE;input.mode=100;input.direction=-3;var safe=input.copy();
        if(safe.capacity!=1||safe.occupied!=1||safe.free()!=0||safe.mode!=4||safe.speed!=130||safe.direction!=0||safe.name.contains("\n")||safe.name.contains("§"))throw new AssertionError("Unsafe controller limits");
        safe.capacity=500;safe.occupied=-99;safe=safe.copy();if(safe.free()!=500||safe.occupied!=0)throw new AssertionError("Negative parking count");
        if(input.same(safe))throw new AssertionError("Snapshot equality failed");
        var rules=new RoadSystemData();rules.detection=9;rules.vehicleTypes="demo:car, demo:car;demo:truck BAD; minecraft:player";
        rules=rules.copy();if(rules.detection!=2||!rules.vehicleTypes.equals("demo:car,demo:truck,minecraft:player"))throw new AssertionError("Vehicle type sanitation failed");
        if(RoadVehicleRules.contains(rules.vehicleTypes,"demo:caravan")||!RoadVehicleRules.contains(rules.vehicleTypes,"demo:car"))throw new AssertionError("Vehicle IDs matched by prefix");
        rules.capacity=Integer.MAX_VALUE;rules.occupied=Integer.MAX_VALUE;rules=rules.copy();if(rules.capacity!=1600000||rules.occupied!=1600000)throw new AssertionError("Aggregate overflow");
        System.out.println("PASS: parking capacity/occupancy, mode, speed and text bounds");
    }
}
