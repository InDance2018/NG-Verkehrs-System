package de.nevergate.verkehr;

import java.util.LinkedHashSet;

/** Explicit registry identifiers; no class-name guessing or wildcard namespaces. */
public final class RoadVehicleRules {
    private RoadVehicleRules() {}
    public static String clean(String input){
        var result=new LinkedHashSet<String>();
        if(input==null)return "";
        for(String part:input.substring(0,Math.min(2048,input.length())).split("[,;\\s]+")){
            if(part.length()<=96&&part.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")){
                int length=result.stream().mapToInt(String::length).sum()+result.size()+part.length();
                if(!result.contains(part)&&length>1024)break;
                result.add(part);if(result.size()==16)break;
            }
        }
        return String.join(",",result);
    }
    public static boolean contains(String list,String id){
        for(String entry:list.split(","))if(entry.equals(id))return true;
        return false;
    }
}
