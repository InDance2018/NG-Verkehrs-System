package de.nevergate.verkehr;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
/** Exact unions of the same boxes used by the equipment meshes. */
public final class EquipmentShapes {
 private static final Map<String,VoxelShape> SHAPES=load();
 private static Map<String,VoxelShape> load(){
  var out=new HashMap<String,VoxelShape>();
  try(var stream=EquipmentShapes.class.getResourceAsStream("/assets/ng_verkehr/equipment_shapes.json")){
   if(stream==null)throw new IllegalStateException("Missing equipment geometry");
   var root=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();
   for(var entry:root.entrySet()){
    VoxelShape shape=Shapes.empty();
    for(var row:entry.getValue().getAsJsonArray()){
     var b=row.getAsJsonArray();shape=Shapes.or(shape,Block.box(b.get(0).getAsDouble(),b.get(1).getAsDouble(),b.get(2).getAsDouble(),b.get(3).getAsDouble(),b.get(4).getAsDouble(),b.get(5).getAsDouble()));
    }
    out.put(entry.getKey(),shape.optimize());
   }return Map.copyOf(out);
  }catch(Exception ex){throw new IllegalStateException("Invalid equipment geometry",ex);}
 }
 public static VoxelShape get(String id,BlockState state){
  var fields=new ArrayList<String>();state.getValues().forEach((k,v)->fields.add(k.getName()+"="+v));Collections.sort(fields);
  var result=SHAPES.get(id+"|"+String.join("|",fields));
  if(result==null)throw new IllegalStateException("Missing equipment shape "+id+" "+state);
  return result;
 }
}
