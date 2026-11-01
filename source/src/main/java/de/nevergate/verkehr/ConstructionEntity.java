package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ConstructionEntity extends BlockEntity {
    private UUID identity=UUID.randomUUID(),owner,parentIdentity;
    private BlockPos parent;
    private final List<BlockPos> lamps=new ArrayList<>();
    private final Set<UUID> players=new LinkedHashSet<>(),vehicles=new LinkedHashSet<>();
    private int mode=1,start=13000,end=23000,step=4,width=1;
    private boolean schedule=false,cleaning=false;
    private long holdUntil;
    public ConstructionEntity(BlockPos p,BlockState s){super(NgTraffic.CONSTRUCTION_ENTITY.get(),p,s);}
    public ConstructionBlock.Kind kind(){return ((ConstructionBlock)getBlockState().getBlock()).kind;}
    public UUID identity(){return identity;}
    public int mode(){return mode;}
    public int start(){return start;}
    public int end(){return end;}
    public int step(){return step;}
    public int width(){return width;}
    public boolean schedule(){return schedule;}
    public int linked(){return lamps.size()+1;}
    public int permitted(){return players.size()+vehicles.size();}
    public boolean active(){return getBlockState().getValue(ConstructionBlock.ACTIVE);}
    public void setOwner(UUID id){owner=id;changed();}
    public boolean canEdit(Player p){return p.getAbilities().mayBuild&&level!=null&&level.mayInteract(p,worldPosition)&&(kind()!=ConstructionBlock.Kind.ACCESS||owner==null||owner.equals(p.getUUID())||p.hasPermissions(2));}
    public ConstructionEntity control(){
        if(parent==null)return this;
        if(level==null||!level.hasChunkAt(parent))return null;
        if(level.getBlockEntity(parent) instanceof ConstructionEntity e&&e.identity.equals(parentIdentity)&&e.parent==null&&
           (kind()==ConstructionBlock.Kind.EXTENSION&&e.kind()==ConstructionBlock.Kind.GATE||kind()==ConstructionBlock.Kind.LAMP&&e.kind()==ConstructionBlock.Kind.LAMP&&e.lamps.contains(worldPosition)))return e;
        return null;
    }
    public boolean attach(ConstructionEntity target,Player player){
        if(kind()!=ConstructionBlock.Kind.LAMP||target.kind()!=ConstructionBlock.Kind.LAMP||target==this||parent!=null||target.parent!=null||!target.lamps.isEmpty()||lamps.size()>=31||level!=target.level||worldPosition.distSqr(target.worldPosition)>64*64||!canEdit(player)||!target.canEdit(player))return false;
        target.parent=worldPosition.immutable();target.parentIdentity=identity;lamps.add(target.worldPosition.immutable());target.changed();changed();return true;
    }
    public void disconnect(Player player){
        if(!canEdit(player))return;
        if(parent!=null){
            var root=control();
            if(root!=null){root.lamps.remove(worldPosition);root.changed();}
            parent=null;parentIdentity=null;changed();
        }else{for(var p:List.copyOf(lamps))if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof ConstructionEntity c&&identity.equals(c.parentIdentity)){c.parent=null;c.parentIdentity=null;c.changed();}lamps.clear();changed();}
    }
    public boolean permit(UUID id,boolean vehicle,boolean allow){
        Set<UUID> set=vehicle?vehicles:players;
        if(allow&&!set.contains(id)&&players.size()+vehicles.size()>=64)return false;
        boolean modified=allow?set.add(id):set.remove(id);if(modified)changed();return modified;
    }
    public boolean authorized(Entity entity){
        if(entity instanceof Player player)return owner!=null&&owner.equals(player.getUUID())||players.contains(player.getUUID());
        if(vehicles.contains(entity.getUUID()))return true;
        var driver=entity.getControllingPassenger();
        return driver instanceof Player&&authorized(driver);
    }
    public boolean resize(Player player,int wanted){
        if(kind()!=ConstructionBlock.Kind.GATE||wanted<1||wanted>4||!canEdit(player))return false;
        var axis=getBlockState().getValue(OrientedBlock.FACING).getClockWise();
        for(int i=1;i<Math.max(width,wanted);i++){
            var p=worldPosition.relative(axis,i);
            if(!level.hasChunkAt(p)||!level.mayInteract(player,p))return false;
            var state=level.getBlockState(p);
            if(i>=width&&i<wanted&&!level.getEntities((Entity)null,new AABB(p),e->e.isAlive()&&!(e instanceof ItemEntity)).isEmpty())return false;
            if(i<wanted&&!state.canBeReplaced()&&!(level.getBlockEntity(p) instanceof ConstructionEntity c&&c.kind()==ConstructionBlock.Kind.EXTENSION&&identity.equals(c.parentIdentity)))return false;
        }
        cleaning=true;
        for(int i=1;i<width;i++)if(i>=wanted){var p=worldPosition.relative(axis,i);if(level.getBlockEntity(p) instanceof ConstructionEntity c&&identity.equals(c.parentIdentity))level.removeBlock(p,false);}
        for(int i=1;i<wanted;i++){
            var p=worldPosition.relative(axis,i);
            if(!(level.getBlockEntity(p) instanceof ConstructionEntity c&&identity.equals(c.parentIdentity))){
                level.setBlock(p,NgTraffic.WORK_EXTENSION.get().defaultBlockState().setValue(OrientedBlock.FACING,getBlockState().getValue(OrientedBlock.FACING)),3);
                if(level.getBlockEntity(p) instanceof ConstructionEntity c){c.parent=worldPosition.immutable();c.parentIdentity=identity;c.changed();}
            }
        }
        width=wanted;cleaning=false;changed();return true;
    }
    public void removed(){
        if(level==null||cleaning)return;
        if(kind()==ConstructionBlock.Kind.GATE){
            cleaning=true;var d=getBlockState().getValue(OrientedBlock.FACING).getClockWise();
            for(int i=1;i<width;i++){var p=worldPosition.relative(d,i);if(level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof ConstructionEntity c&&identity.equals(c.parentIdentity))level.removeBlock(p,false);}
            cleaning=false;
        }else if(kind()==ConstructionBlock.Kind.EXTENSION){
            var root=control();
            if(root!=null&&!root.cleaning){root.cleaning=true;var d=root.getBlockState().getValue(OrientedBlock.FACING).getClockWise();
                for(int i=1;i<root.width;i++){var p=root.worldPosition.relative(d,i);if(!p.equals(worldPosition)&&level.hasChunkAt(p)&&level.getBlockEntity(p) instanceof ConstructionEntity c&&root.identity.equals(c.parentIdentity))level.removeBlock(p,false);}
                root.width=1;root.cleaning=false;root.changed();}
        }else if(kind()==ConstructionBlock.Kind.LAMP){
            if(parent!=null){var root=control();if(root!=null){root.lamps.remove(worldPosition);root.changed();}}
            // Unloaded children retain the old identity and fail closed when the leader is replaced.
        }
    }
    public void mode(int m){mode=Math.max(0,Math.min(2,m));changed();}
    public void timing(int from,int to,boolean enabled){start=Math.floorMod(from,24000);end=Math.floorMod(to,24000);schedule=enabled;changed();}
    public boolean action(Player player,int button){
        if(!canEdit(player))return false;
        if(button==0&&(kind()==ConstructionBlock.Kind.TRAILER||kind()==ConstructionBlock.Kind.LAMP||kind()==ConstructionBlock.Kind.TIMED||kind()==ConstructionBlock.Kind.ACCESS||kind()==ConstructionBlock.Kind.MAST)){mode((mode+1)%(kind()==ConstructionBlock.Kind.TRAILER||kind()==ConstructionBlock.Kind.LAMP?3:2));return true;}
        if(button>=1&&button<=3&&!(kind()==ConstructionBlock.Kind.TRAILER||kind()==ConstructionBlock.Kind.LAMP||kind()==ConstructionBlock.Kind.TIMED||kind()==ConstructionBlock.Kind.ACCESS||kind()==ConstructionBlock.Kind.MAST))return false;
        if(button==1){timing(start,end,!schedule);return true;}
        if(button==2){timing(start+1000,end,schedule);return true;}
        if(button==3){timing(start,end+1000,schedule);return true;}
        if(button==4&&kind()==ConstructionBlock.Kind.LAMP){step=step==2?4:step==4?8:step==8?12:2;changed();return true;}
        if(button==5)return resize(player,width%4+1);
        if(button==6&&kind()==ConstructionBlock.Kind.LAMP){disconnect(player);return true;}
        return false;
    }
    private void state(boolean lit,int model){
        var old=getBlockState();var next=old.setValue(ConstructionBlock.ACTIVE,lit).setValue(ConstructionBlock.MODE,model);
        if(!old.equals(next))level.setBlock(worldPosition,next,3);
    }
    public void tick(){
        if(level==null||level.isClientSide||level.getGameTime()%2!=0)return;
        if(kind()==ConstructionBlock.Kind.EXTENSION){
            // Do not mistake an unloaded leader for a deleted leader.
            if(parent!=null&&level.hasChunkAt(parent)&&control()==null)level.removeBlock(worldPosition,false);
            return;
        }
        var root=control();
        if(root==null){state(false,0);return;}
        if(kind()==ConstructionBlock.Kind.LAMP){
            boolean complete=true;
            for(var p:root.lamps)if(!level.hasChunkAt(p)||!(level.getBlockEntity(p) instanceof ConstructionEntity c)||!root.identity.equals(c.parentIdentity)){complete=false;break;}
            int index=this==root?0:root.lamps.indexOf(worldPosition)+1;
            boolean on=complete&&root.mode!=0&&(!root.schedule||ConstructionRules.scheduled(level.getDayTime(),root.start,root.end))&&(root.mode==2||ConstructionRules.running(level.getGameTime(),root.step,root.linked(),index));
            state(on,root.mode);return;
        }
        boolean enabled=mode!=0&&(!schedule||ConstructionRules.scheduled(level.getDayTime(),start,end));
        if(kind()==ConstructionBlock.Kind.TRAILER){state(enabled&&Math.floorMod(level.getGameTime(),20)<10,mode);return;}
        if(kind()==ConstructionBlock.Kind.MAST){state(enabled,mode);return;}
        if(ConstructionBlock.moving(kind())){
            var closed=getBlockState().setValue(ConstructionBlock.ACTIVE,false);
            var collision=closed.getCollisionShape(level,worldPosition).bounds().move(worldPosition);
            boolean obstacle=!level.getEntities((Entity)null,collision,e->e.isAlive()&&!(e instanceof ItemEntity)&&!(e instanceof net.minecraft.world.entity.ExperienceOrb)).isEmpty();
            boolean requested=kind()==ConstructionBlock.Kind.TIMED?!enabled:
                enabled&&!level.getEntities((Entity)null,new AABB(worldPosition).inflate(2,1,2),e->e.isAlive()&&e.getVehicle()==null&&authorized(e)).isEmpty();
            if(requested)holdUntil=level.getGameTime()+20;
            boolean open=requested||level.getGameTime()<holdUntil||active()&&obstacle;
            state(open,mode);
        }
    }
    private void changed(){setChanged();if(level!=null)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
    @Override protected void saveAdditional(CompoundTag tag){
        super.saveAdditional(tag);tag.putUUID("Identity",identity);if(owner!=null)tag.putUUID("Owner",owner);
        tag.putInt("Mode",mode);tag.putInt("Start",start);tag.putInt("End",end);tag.putInt("Step",step);tag.putInt("Width",width);tag.putBoolean("Schedule",schedule);
        if(parent!=null&&parentIdentity!=null){tag.putLong("Parent",parent.asLong());tag.putUUID("ParentIdentity",parentIdentity);}
        tag.putLongArray("Lamps",lamps.stream().mapToLong(BlockPos::asLong).toArray());
        var ps=new ListTag();players.forEach(id->ps.add(NbtUtils.createUUID(id)));tag.put("Players",ps);
        var vs=new ListTag();vehicles.forEach(id->vs.add(NbtUtils.createUUID(id)));tag.put("Vehicles",vs);
    }
    @Override public void load(CompoundTag t){
        super.load(t);identity=t.hasUUID("Identity")?t.getUUID("Identity"):UUID.randomUUID();owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;
        mode=Math.max(0,Math.min(2,t.getInt("Mode")));start=Math.floorMod(t.getInt("Start"),24000);end=Math.floorMod(t.getInt("End"),24000);
        step=Math.max(2,Math.min(12,t.getInt("Step")));width=Math.max(1,Math.min(4,t.getInt("Width")));schedule=t.getBoolean("Schedule");
        parent=t.contains("Parent",Tag.TAG_LONG)&&t.hasUUID("ParentIdentity")?BlockPos.of(t.getLong("Parent")):null;parentIdentity=parent==null?null:t.getUUID("ParentIdentity");
        lamps.clear();for(long p:t.getLongArray("Lamps"))if(lamps.size()<31&&!lamps.contains(BlockPos.of(p))&&!worldPosition.equals(BlockPos.of(p)))lamps.add(BlockPos.of(p));
        players.clear();vehicles.clear();
        for(var v:t.getList("Players",Tag.TAG_INT_ARRAY))if(players.size()<64&&v instanceof IntArrayTag a&&a.getAsIntArray().length==4)players.add(NbtUtils.loadUUID(v));
        for(var v:t.getList("Vehicles",Tag.TAG_INT_ARRAY))if(players.size()+vehicles.size()<64&&v instanceof IntArrayTag a&&a.getAsIntArray().length==4)vehicles.add(NbtUtils.loadUUID(v));
        holdUntil=0;
    }
    @Override public CompoundTag getUpdateTag(){return saveWithoutMetadata();}
    @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
}
