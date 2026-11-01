package de.nevergate.verkehr;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod.EventBusSubscriber(modid=NgTraffic.ID)
public final class ParkingVehicleDiagnostics{
    @SubscribeEvent public static void register(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("ngverkehr").requires(s->s.hasPermission(2))
            .then(Commands.literal("vehiclecheck").then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(c->{
                var pos=BlockPosArgument.getLoadedBlockPos(c,"pos");var level=c.getSource().getLevel();
                if(!(level.getBlockEntity(pos) instanceof RoadSystemEntity node)){c.getSource().sendFailure(Component.literal("NG-Parkrechner oder verbundenes Gerät angeben."));return 0;}
                var rules=node.vehicleRules();var unknown=new java.util.ArrayList<String>();
                for(var id:rules.vehicleTypes.split(","))if(!id.isBlank()&&!BuiltInRegistries.ENTITY_TYPE.containsKey(new ResourceLocation(id)))unknown.add(id);
                c.getSource().sendSuccess(()->Component.literal("Fahrzeugtest: Modus "+rules.detection+"; unbekannte IDs: "+String.join(",",unknown)),false);
                int count=0;
                for(var entity:level.getEntities((net.minecraft.world.entity.Entity)null,new AABB(pos).inflate(6))){
                    if(count++>=16)break;
                    String id=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                    String text=id+" | erkannt="+RoadSystemEntity.vehicle(entity,rules)+" | Wurzel="+(entity.getRootVehicle()==entity)+" | Höhe="+String.format(java.util.Locale.ROOT,"%.2f m",entity.getBbHeight());
                    c.getSource().sendSuccess(()->Component.literal(text),false);
                }
                return Math.min(16,count);
            }))));
    }
}

