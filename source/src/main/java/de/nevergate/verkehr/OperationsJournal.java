package de.nevergate.verkehr;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
/** Durable authority for card revocations and service state, independent of chunk autosave. */
public final class OperationsJournal extends SavedData {
    private final Map<UUID,CompoundTag> records=new HashMap<>();
    public static OperationsJournal get(ServerLevel l){return l.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(OperationsJournal::new,OperationsJournal::load,DataFixTypes.LEVEL),"ng_verkehr_operations");}
    public CompoundTag lookup(UUID node){var t=records.get(node);return t==null?null:t.copy();}
    public void checkpoint(UUID node,CompoundTag snapshot){if(!records.containsKey(node)&&records.size()>=50000)throw new IllegalStateException("Operations journal full");records.put(node,snapshot.copy());setDirty();}
    public static OperationsJournal load(CompoundTag t){var j=new OperationsJournal();for(var tag:t.getList("Records",10)){var row=(CompoundTag)tag;if(row.hasUUID("Node")&&j.records.size()<50000)j.records.put(row.getUUID("Node"),row.getCompound("Snapshot").copy());}return j;}
    @Override public CompoundTag save(CompoundTag t){var list=new ListTag();records.forEach((node,data)->{var row=new CompoundTag();row.putUUID("Node",node);row.put("Snapshot",data.copy());list.add(row);});t.put("Records",list);return t;}
}
