package de.nevergate.verkehr;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
public final class ServiceCommands {
    public static void register(RegisterCommandsEvent event){
        var root=Commands.literal("ngservice").requires(s->s.hasPermission(2));
        root.then(Commands.literal("tariff").then(Commands.argument("pos",BlockPosArgument.blockPos()).then(Commands.argument("centsPerHour",IntegerArgumentType.integer(0,100000)).then(Commands.argument("freeMinutes",IntegerArgumentType.integer(0,1440)).then(Commands.argument("dailyCap",IntegerArgumentType.integer(0,1000000)).executes(c->{
            var pos=BlockPosArgument.getLoadedBlockPos(c,"pos");if(!(c.getSource().getLevel().getBlockEntity(pos) instanceof ServiceEntity n)||n.kind()!=ServiceBlock.Kind.TARIFF)return 0;
            n.configure(IntegerArgumentType.getInteger(c,"centsPerHour"),IntegerArgumentType.getInteger(c,"freeMinutes"),IntegerArgumentType.getInteger(c,"dailyCap"),n.duration(),n.group(),n.limit(),n.height(),n.interval());c.getSource().sendSuccess(()->Component.literal("Parktarif gespeichert"),false);return 1;
        }))))));
        root.then(Commands.literal("pass").then(Commands.argument("tariff",BlockPosArgument.blockPos()).then(Commands.argument("player",EntityArgument.player()).executes(c->{
            var p=EntityArgument.getPlayer(c,"player");var pos=BlockPosArgument.getLoadedBlockPos(c,"tariff");if(!(c.getSource().getLevel().getBlockEntity(pos) instanceof ServiceEntity n)||n.kind()!=ServiceBlock.Kind.TARIFF)return 0;
            var ledger=ParkingLedger.get(c.getSource().getLevel());var t=ledger.issue(p.getUUID(),n.identity(),ParkingLedger.now(c.getSource().getLevel()),true,ParkingLedger.DAY*30);if(t==null)return 0;
            var s=new net.minecraft.world.item.ItemStack(NgTraffic.PARK_PASS.get());s.getOrCreateTag().putUUID("Ticket",t.id);if(!p.getInventory().add(s))p.drop(s,false);return 1;
        }))));
        root.then(Commands.literal("revoke").then(Commands.argument("tariff",BlockPosArgument.blockPos()).then(Commands.argument("player",EntityArgument.player()).executes(c->{
            var p=EntityArgument.getPlayer(c,"player");var pos=BlockPosArgument.getLoadedBlockPos(c,"tariff");if(!(c.getSource().getLevel().getBlockEntity(pos) instanceof ServiceEntity n)||n.kind()!=ServiceBlock.Kind.TARIFF)return 0;return ParkingLedger.get(c.getSource().getLevel()).revokePasses(p.getUUID(),n.identity());
        }))));
        root.then(Commands.literal("reserve").then(Commands.argument("pos",BlockPosArgument.blockPos()).then(Commands.argument("player",EntityArgument.player()).executes(c->{var pos=BlockPosArgument.getLoadedBlockPos(c,"pos");if(!(c.getSource().getLevel().getBlockEntity(pos) instanceof ServiceEntity n)||n.kind()!=ServiceBlock.Kind.RESERVATION_BOLLARD)return 0;n.reserve(EntityArgument.getPlayer(c,"player").getUUID());return 1;}))));
        root.then(Commands.literal("unreserve").then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(c->{var pos=BlockPosArgument.getLoadedBlockPos(c,"pos");if(!(c.getSource().getLevel().getBlockEntity(pos) instanceof ServiceEntity n)||n.kind()!=ServiceBlock.Kind.RESERVATION_BOLLARD)return 0;n.reserve(null);return 1;})));
        event.getDispatcher().register(root);
    }
}
