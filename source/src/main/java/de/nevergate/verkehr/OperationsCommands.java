package de.nevergate.verkehr;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.EntityArgument;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
public final class OperationsCommands {
    public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("ngbetrieb").requires(s->s.hasPermission(2))
            .then(Commands.argument("pos",BlockPosArgument.blockPos())
                .then(Commands.literal("route").then(Commands.argument("linie",StringArgumentType.word()).then(Commands.argument("ziel",StringArgumentType.greedyString()).executes(c->{var n=node(c);if(n==null||n.kind()!=OperationsBlock.Kind.BUS_ROUTE)return 0;n.route(StringArgumentType.getString(c,"linie"),StringArgumentType.getString(c,"ziel"));c.getSource().sendSuccess(()->Component.literal("Buslinie gespeichert"),false);return 1;}))))
                .then(Commands.literal("kennzeichen").then(Commands.argument("wert",StringArgumentType.greedyString()).executes(c->{var n=node(c);boolean ok=n!=null&&n.kind()==OperationsBlock.Kind.PLATE_READER&&n.registerPlate(StringArgumentType.getString(c,"wert"));if(ok)c.getSource().sendSuccess(()->Component.literal("Kennzeichen für dieses Fahrzeug und seinen Besitzer freigegeben"),false);else c.getSource().sendFailure(Component.literal("Genau ein Fahrzeug mit passendem Kennzeichen und bekanntem Besitzer vor dem Leser erforderlich"));return ok?1:0;})))
                .then(Commands.literal("karte").then(Commands.argument("spieler",EntityArgument.player()).executes(c->{var n=node(c);return n!=null&&n.grantCard(c.getSource().getPlayerOrException(),EntityArgument.getPlayer(c,"spieler"))?1:0;})))
                .then(Commands.literal("status").executes(c->{var n=node(c);if(n==null)return 0;c.getSource().sendSuccess(()->Component.literal(n.kind()+": "+n.detail()+" · Zustand "+n.state()),false);return 1;}))));
    }
    private static OperationsEntity node(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var pos=BlockPosArgument.getLoadedBlockPos(c,"pos");var be=c.getSource().getLevel().getBlockEntity(pos);return be instanceof OperationsEntity n&&n.canEdit(c.getSource().getPlayerOrException())?n:null;
    }
}
