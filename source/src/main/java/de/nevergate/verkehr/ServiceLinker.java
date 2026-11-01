package de.nevergate.verkehr;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
public final class ServiceLinker extends Item {
    public ServiceLinker(){super(new Item.Properties().stacksTo(1));}
    @Override public InteractionResult useOn(UseOnContext c){
        var p=c.getPlayer();var l=c.getLevel();var at=c.getClickedPos();if(p==null||!l.mayInteract(p,at))return InteractionResult.FAIL;if(l.isClientSide)return InteractionResult.SUCCESS;
        var t=c.getItemInHand().getOrCreateTag();var hit=l.getBlockEntity(at);
        if(hit instanceof ServiceEntity n&&(p.isShiftKeyDown()||!t.hasUUID("ServiceId"))){if(!n.canEdit(p))return InteractionResult.FAIL;t.putUUID("ServiceId",n.identity());t.putLong("ServicePos",at.asLong());t.putString("Dimension",l.dimension().location().toString());p.displayClientMessage(Component.literal("Gerät gewählt – jetzt Zielgerät anklicken"),true);return InteractionResult.CONSUME;}
        var pos=BlockPos.of(t.getLong("ServicePos"));if(!t.hasUUID("ServiceId")||!t.getString("Dimension").equals(l.dimension().location().toString())||at.distSqr(pos)>128*128||!l.hasChunkAt(pos))return InteractionResult.FAIL;
        if(l.getBlockEntity(pos) instanceof ServiceEntity n&&n.identity().equals(t.getUUID("ServiceId"))&&hit!=null&&n.connect(hit,p)){p.displayClientMessage(Component.literal("Geräte verbunden"),true);return InteractionResult.CONSUME;}
        p.displayClientMessage(Component.literal("Verbindung nicht möglich – Gerätetyp, Entfernung und Rechte prüfen"),true);return InteractionResult.FAIL;
    }
}
