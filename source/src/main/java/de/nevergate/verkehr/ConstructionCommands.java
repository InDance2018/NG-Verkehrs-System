package de.nevergate.verkehr;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;

/** Explicit operator configuration; no raw commands accepted through custom network packets. */
public final class ConstructionCommands {
    private static final SimpleCommandExceptionType WRONG=new SimpleCommandExceptionType(Component.literal("Baustellen-Zufahrtskontrolle angeben."));
    private static ConstructionEntity gate(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> c)throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        var p=BlockPosArgument.getLoadedBlockPos(c,"pos");
        if(c.getSource().getLevel().getBlockEntity(p) instanceof ConstructionEntity e&&e.kind()==ConstructionBlock.Kind.ACCESS)return e;
        throw WRONG.create();
    }
    public static void register(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("ngverkehr").requires(s->s.hasPermission(2))
            .then(Commands.literal("workaccess").then(Commands.argument("pos",BlockPosArgument.blockPos())
                .then(Commands.literal("player").then(Commands.argument("players",EntityArgument.players()).then(Commands.argument("allow",BoolArgumentType.bool()).executes(c->{
                    var e=gate(c);int n=0;for(var p:EntityArgument.getPlayers(c,"players"))if(e.permit(p.getUUID(),false,BoolArgumentType.getBool(c,"allow")))n++;
                    final int changed=n;c.getSource().sendSuccess(()->Component.literal("Berechtigungen geändert: "+changed),true);return n;
                }))))
                .then(Commands.literal("vehicle").then(Commands.argument("uuid",UuidArgument.uuid()).then(Commands.argument("allow",BoolArgumentType.bool()).executes(c->{
                    boolean changed=gate(c).permit(UuidArgument.getUuid(c,"uuid"),true,BoolArgumentType.getBool(c,"allow"));
                    c.getSource().sendSuccess(()->Component.literal(changed?"Fahrzeugberechtigung geändert.":"Keine Änderung (bereits gesetzt oder Liste voll)."),true);return changed?1:0;
                }))))
            )));
    }
}
