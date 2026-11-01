package de.nevergate.verkehr;
import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
public final class TrafficNodeCommands {
    public static void register(RegisterCommandsEvent e){
        e.getDispatcher().register(Commands.literal("ngverkehr").requires(s->s.hasPermission(2))
            .then(Commands.literal("traffictext").then(Commands.argument("pos",BlockPosArgument.blockPos()).then(Commands.argument("text",StringArgumentType.greedyString()).executes(c->{
                var p=BlockPosArgument.getLoadedBlockPos(c,"pos");if(!(c.getSource().getLevel().getBlockEntity(p) instanceof TrafficNodeEntity n))return 0;
                n.text(StringArgumentType.getString(c,"text"));c.getSource().sendSuccess(()->Component.literal("Verkehrstext gespeichert."),true);return 1;
            }))))
            .then(Commands.literal("traffictypes").then(Commands.argument("pos",BlockPosArgument.blockPos()).then(Commands.argument("types",StringArgumentType.greedyString()).executes(c->{
                var p=BlockPosArgument.getLoadedBlockPos(c,"pos");if(!(c.getSource().getLevel().getBlockEntity(p) instanceof TrafficNodeEntity n))return 0;
                n.types(StringArgumentType.getString(c,"types"));c.getSource().sendSuccess(()->Component.literal("Entity-Typen gespeichert: "+n.types()),true);return 1;
            }))))
            .then(Commands.literal("trafficmark").then(Commands.argument("entities",EntityArgument.entities()).then(Commands.argument("kind",StringArgumentType.word()).then(Commands.argument("active",BoolArgumentType.bool()).executes(c->{
                String kind=StringArgumentType.getString(c,"kind");if(!java.util.Set.of("bus","emergency","train","vehicle").contains(kind))return 0;
                String tag=kind.equals("emergency")?"ng_emergency_active":"ng_"+kind;int n=0;
                for(var entity:EntityArgument.getEntities(c,"entities")){if(BoolArgumentType.getBool(c,"active"))entity.addTag(tag);else entity.removeTag(tag);n++;}
                c.getSource().sendSuccess(()->Component.literal("Verkehrsmarkierung geändert."),true);return n;
            }))))));
    }
}
