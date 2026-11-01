package de.nevergate.verkehr;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class TrafficCommands {
    private static final SimpleCommandExceptionType WRONG=new SimpleCommandExceptionType(Component.literal("Passenden NG-Verkehr-Block angeben."));
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ngverkehr").requires(s->s.hasPermission(2))
            .then(Commands.literal("link").then(Commands.argument("signal",BlockPosArgument.blockPos())
                .then(Commands.argument("controller",BlockPosArgument.blockPos()).then(Commands.argument("group",IntegerArgumentType.integer(0,1)).executes(c->{
                    var signal=BlockPosArgument.getLoadedBlockPos(c,"signal"); var controller=BlockPosArgument.getLoadedBlockPos(c,"controller");
                    var world=c.getSource().getLevel();
                    if(!(world.getBlockEntity(signal) instanceof SignalEntity s)||!(world.getBlockEntity(controller) instanceof DeviceEntity d)||!d.isController()) throw WRONG.create();
                    if(signal.distSqr(controller)>256*256) throw new SimpleCommandExceptionType(Component.literal("Maximal 256 Bloecke Abstand.")).create();
                    s.link(controller,IntegerArgumentType.getInteger(c,"group")); c.getSource().sendSuccess(()->Component.literal("Ampel verbunden."),true); return 1;
                })))))
            .then(Commands.literal("controller").then(Commands.argument("pos",BlockPosArgument.blockPos()).then(Commands.argument("enabled",BoolArgumentType.bool()).executes(c->{
                var p=BlockPosArgument.getLoadedBlockPos(c,"pos");
                if(!(c.getSource().getLevel().getBlockEntity(p) instanceof DeviceEntity d)||!d.isController()) throw WRONG.create();
                d.setEnabled(BoolArgumentType.getBool(c,"enabled")); return 1;
            }))))
            .then(Commands.literal("text").then(Commands.argument("pos",BlockPosArgument.blockPos()).then(Commands.argument("text",StringArgumentType.greedyString()).executes(c->{
                var p=BlockPosArgument.getLoadedBlockPos(c,"pos");
                if(!(c.getSource().getLevel().getBlockEntity(p) instanceof DeviceEntity d)||!c.getSource().getLevel().getBlockState(p).is(NgTraffic.DISPLAY.get())) throw WRONG.create();
                d.setText(StringArgumentType.getString(c,"text")); return 1;
            }))))
            .then(Commands.literal("parking").then(Commands.argument("pos",BlockPosArgument.blockPos()).then(Commands.argument("capacity",IntegerArgumentType.integer(0,99999)).then(Commands.argument("occupied",IntegerArgumentType.integer(0,99999)).executes(c->{
                var p=BlockPosArgument.getLoadedBlockPos(c,"pos");
                if(!(c.getSource().getLevel().getBlockEntity(p) instanceof DeviceEntity d)||!c.getSource().getLevel().getBlockState(p).is(NgTraffic.PARKING.get())) throw WRONG.create();
                d.setParking(IntegerArgumentType.getInteger(c,"capacity"),IntegerArgumentType.getInteger(c,"occupied")); return 1;
            }))))));
    }
}
