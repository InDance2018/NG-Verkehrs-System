package de.nevergate.verkehr;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;

/** Server-thread payment bridge. Amounts are integer euro cents; no currency is created here.
 * NG-Wirtschaftssystem handles this event on MinecraftForge.EVENT_BUS and calls confirm
 * only after an atomic, durable debit. The transaction UUID must be idempotent in that provider.
 * Async/pending replies are intentionally unsupported: they cannot issue paid tickets.
 */
public final class EconomyPaymentEvent extends Event {
    public final ServerPlayer player;
    public final UUID transaction,zone;
    public final long cents;
    public final String purpose;
    private boolean confirmed;
    private String receipt="";
    public EconomyPaymentEvent(ServerPlayer p,UUID tx,UUID z,long amount,String purpose){
        if(amount<=0||amount>10000000)throw new IllegalArgumentException("Invalid payment amount");
        player=p;transaction=tx;zone=z;cents=amount;this.purpose=purpose;
    }
    public void confirm(String providerReceipt){
        if(providerReceipt==null||providerReceipt.isBlank()||providerReceipt.length()>160)throw new IllegalArgumentException("Missing receipt");
        if(confirmed)throw new IllegalStateException("Payment already confirmed");
        receipt=providerReceipt;confirmed=true;
    }
    public boolean confirmed(){return confirmed;}
    public String receipt(){return receipt;}
}
