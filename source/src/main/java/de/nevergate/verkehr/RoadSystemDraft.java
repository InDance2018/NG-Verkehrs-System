package de.nevergate.verkehr;
/** Three-way merge keeps local edits while untouched fields follow the live server state. */
public final class RoadSystemDraft{
    private RoadSystemDraft(){}
    public static RoadSystemData merge(RoadSystemData base,RoadSystemData local,RoadSystemData remote){
        var d=local.copy();
        if(local.name.equals(base.name))d.name=remote.name;if(local.vehicleTypes.equals(base.vehicleTypes))d.vehicleTypes=remote.vehicleTypes;
        if(local.mode==base.mode)d.mode=remote.mode;if(local.speed==base.speed)d.speed=remote.speed;
        if(local.capacity==base.capacity)d.capacity=remote.capacity;if(local.occupied==base.occupied)d.occupied=remote.occupied;
        if(local.direction==base.direction)d.direction=remote.direction;if(local.detection==base.detection)d.detection=remote.detection;
        if(local.counting==base.counting)d.counting=remote.counting;if(local.heightCm==base.heightCm)d.heightCm=remote.heightCm;
        if(local.routeMode==base.routeMode&&local.routeTarget==base.routeTarget){d.routeMode=remote.routeMode;d.routeTarget=remote.routeTarget;}
        if(local.autoDisplay==base.autoDisplay)d.autoDisplay=remote.autoDisplay;return d.copy();
    }
}

