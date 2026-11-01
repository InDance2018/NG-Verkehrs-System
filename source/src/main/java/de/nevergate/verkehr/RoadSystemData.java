package de.nevergate.verkehr;
public final class RoadSystemData {
    public String name="",vehicleTypes="";
    public int mode=0,speed=50,capacity=100,occupied=0,direction=1,detection=0;
    public int counting=0,heightCm=220,routeMode=0,autoDisplay=0;
    public long routeTarget;
    public RoadSystemData copy(){
        var d=new RoadSystemData();d.name=WayfindingData.clean(name,48);d.mode=Math.max(0,Math.min(4,mode));d.speed=Math.max(20,Math.min(130,speed/10*10));
        d.capacity=Math.max(1,Math.min(1600000,capacity));d.occupied=Math.max(0,Math.min(d.capacity,occupied));d.direction=Math.max(0,Math.min(4,direction));
        d.vehicleTypes=RoadVehicleRules.clean(vehicleTypes);d.detection=Math.max(0,Math.min(2,detection));d.counting=Math.max(0,Math.min(1,counting));
        d.heightCm=Math.max(150,Math.min(650,heightCm/10*10));d.routeMode=Math.max(0,Math.min(2,routeMode));d.routeTarget=routeTarget;d.autoDisplay=Math.max(0,Math.min(1,autoDisplay));return d;
    }
    public int free(){return Math.max(0,capacity-occupied);}
    public boolean same(RoadSystemData d){return name.equals(d.name)&&mode==d.mode&&speed==d.speed&&capacity==d.capacity&&occupied==d.occupied&&direction==d.direction&&detection==d.detection&&vehicleTypes.equals(d.vehicleTypes)&&counting==d.counting&&heightCm==d.heightCm&&routeMode==d.routeMode&&routeTarget==d.routeTarget&&autoDisplay==d.autoDisplay;}
}