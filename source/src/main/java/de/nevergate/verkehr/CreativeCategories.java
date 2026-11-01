package de.nevergate.verkehr;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Explicit membership prevents new items silently falling into the wrong tab. */
public final class CreativeCategories {
    public record Entry(String id,String title,String icon,List<String> items) {}
    private static final List<Entry> ENTRIES=load();
    private static List<Entry> load(){
        try(var stream=CreativeCategories.class.getResourceAsStream("/assets/ng_verkehr/creative_categories.json")){
            if(stream==null)throw new IllegalStateException("Missing creative categories");
            var result=new ArrayList<Entry>();
            for(var value:JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonArray()){
                var json=value.getAsJsonObject();var items=new ArrayList<String>();
                for(var item:json.getAsJsonArray("items"))items.add(item.getAsString());
                result.add(new Entry(json.get("id").getAsString(),json.get("title").getAsString(),json.get("icon").getAsString(),List.copyOf(items)));
            }
            return List.copyOf(result);
        }catch(Exception error){throw new IllegalStateException("Invalid creative categories",error);}
    }
    public static List<Entry> entries(){return ENTRIES;}
    public static void validate(Set<String> expected){
        var seen=new HashSet<String>();var ids=new HashSet<String>();
        for(var category:ENTRIES){
            if(!ids.add(category.id())||category.items().isEmpty()||!category.items().contains(category.icon()))throw new IllegalStateException("Invalid tab "+category.id());
            for(var item:category.items())if(!expected.contains(item)||!seen.add(item))throw new IllegalStateException("Unknown or duplicate creative item: "+item);
        }
        if(!seen.equals(expected))throw new IllegalStateException("Creative categories must contain every project item exactly once");
    }
}
