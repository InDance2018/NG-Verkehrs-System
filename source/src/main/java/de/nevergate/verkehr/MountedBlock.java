package de.nevergate.verkehr;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.*;

/** A panel and its retained support share one occupied cell. */
public class MountedBlock extends OrientedBlock {
    public enum Attachment implements StringRepresentable {
        FREE, POST, MAST, MAST_TOP, SIGNAL_BRACKET, SIGN_BRACKET, HANGER, BOOM_END, SIDE_LEFT, SIDE_RIGHT, ALIGNED;
        public String getSerializedName(){return name().toLowerCase(Locale.ROOT);}
    }
    public enum Join implements StringRepresentable { NONE, LEFT, RIGHT, BOTH; public String getSerializedName(){return name().toLowerCase(Locale.ROOT);} }
    public static final EnumProperty<Join> JOIN=EnumProperty.create("join",Join.class);
    public static boolean recessed(BlockState s){return s.hasProperty(ATTACHMENT)&&(supportId(s.getValue(ATTACHMENT))!=null||s.getValue(ATTACHMENT)==Attachment.ALIGNED);}
    public static BlockState joined(BlockState s,BlockGetter w,BlockPos p){
        if(!s.hasProperty(JOIN)||!isSignal(s.getBlock()))return s;
        var f=s.getValue(FACING);boolean left=false,right=false;
        for(boolean r:new boolean[]{false,true}){
            var n=w.getBlockState(p.relative(r?f.getClockWise():f.getCounterClockWise()));
            boolean ok=isSignal(n.getBlock())&&n.getValue(FACING)==f&&recessed(n)==recessed(s);
            if(r)right=ok;else left=ok;
        }
        return s.setValue(JOIN,left?(right?Join.BOTH:Join.LEFT):(right?Join.RIGHT:Join.NONE));
    }
    public static final EnumProperty<Attachment> ATTACHMENT=EnumProperty.create("attachment",Attachment.class);
    public MountedBlock(Properties properties,boolean panel){super(properties,panel);registerDefaultState(defaultBlockState().setValue(ATTACHMENT,Attachment.FREE));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(ATTACHMENT);}
    public static String supportId(Attachment m){return switch(m){case POST->"post";case MAST->"signal_mast";case MAST_TOP->"signal_mast_top";case SIGNAL_BRACKET->"signal_bracket";case SIGN_BRACKET->"sign_bracket";case HANGER->"bridge_hanger";case BOOM_END->"signal_boom_end";default->null;};}
    public static Attachment support(BlockState s){for(var m:Attachment.values()){String id=supportId(m);if(id!=null&&s.is(NgTraffic.CONTENT.get(id).get()))return m;}return Attachment.FREE;}
    public boolean canMount(){return true;}
    public double mountDepth(){return 1.75;}
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){
        var s=super.getStateForPlacement(c);if(s==null)return null;
        if(this instanceof MobileSignalBlock)return s;
        var existing=c.getLevel().getBlockState(c.getClickedPos());var mount=support(existing);
        if(mount!=Attachment.FREE){
            if(existing.hasProperty(FACING)&&mount!=Attachment.MAST)s=s.setValue(FACING,existing.getValue(FACING));
            return joined(s.setValue(ATTACHMENT,mount),c.getLevel(),c.getClickedPos());
        }
        Direction face=c.getClickedFace();var adjacent=c.getLevel().getBlockState(c.getClickedPos().relative(face.getOpposite()));
        if(isSignal(this)&&isSignal(adjacent.getBlock())){
            s=s.setValue(FACING,adjacent.getValue(FACING));
            if(recessed(adjacent))s=s.setValue(ATTACHMENT,Attachment.ALIGNED);
        }
        return joined(s,c.getLevel(),c.getClickedPos());
    }
    public static boolean isSignal(Block b){return b instanceof SignalBlock&&!(b instanceof MobileSignalBlock)||b instanceof PedestrianBlock&&b!=NgTraffic.PEDESTRIAN_BUTTON.get();}
    public VoxelShape attachedShape(BlockState s,BlockGetter world,BlockPos pos,CollisionContext c,VoxelShape base){
        var m=s.getValue(ATTACHMENT);String id=supportId(m);var f=s.getValue(FACING);
        VoxelShape result=base;
        Join join=s.hasProperty(JOIN)&&isSignal(this)?s.getValue(JOIN):Join.NONE;
        double amount=this instanceof PedestrianBlock?4.5:5;double side=join==Join.LEFT?-amount:join==Join.RIGHT?amount:0;
        if(side!=0)result=result.move(f.getClockWise().getStepX()*side/16,0,f.getClockWise().getStepZ()*side/16);
        if(join==Join.BOTH){boolean warning=this instanceof SignalBlock signal&&signal.kind==SignalKind.WARNING;result=Shapes.or(result,rotateBox(f,0,warning?5:0,7,16,warning?11:16,10));}
        if(recessed(s))result=result.move(f.getStepX()*mountDepth()/16,0,f.getStepZ()*mountDepth()/16);
        if(id!=null){var support=NgTraffic.CONTENT.get(id).get().defaultBlockState();if(support.hasProperty(FACING))support=support.setValue(FACING,f);result=Shapes.or(result,support.getShape(world,pos,c));
            result=Shapes.or(result,isSignal(this)?rotateBox(f,side==0?7:2,6,4.5,side==0?9:14,8,8):rotateBox(f,7,10,6.5,9,11,8));
        }
        return result;
    }
    public static VoxelShape rotateBox(Direction f,double x,double y,double z,double xx,double yy,double zz){
        int n=switch(f){case EAST->1;case SOUTH->2;case WEST->3;default->0;};
        for(int i=0;i<n;i++){double old=x,oldMax=xx;x=16-zz;xx=16-z;z=old;zz=oldMax;}return box(x,y,z,xx,yy,zz);
    }
    @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return attachedShape(s,w,p,c,super.getShape(s,w,p,c));}
    @Override public List<ItemStack> getDrops(BlockState s,LootParams.Builder params){var drops=new ArrayList<>(super.getDrops(s,params));String id=supportId(s.getValue(ATTACHMENT));if(id!=null)drops.add(new ItemStack(NgTraffic.CONTENT.get(id).get()));return drops;}
    @Override public BlockState mirror(BlockState s,Mirror mirror){var result=super.mirror(s,mirror);if(mirror!=Mirror.NONE&&s.hasProperty(JOIN)){var join=s.getValue(JOIN);if(join==Join.LEFT)result=result.setValue(JOIN,Join.RIGHT);else if(join==Join.RIGHT)result=result.setValue(JOIN,Join.LEFT);}if(mirror!=Mirror.NONE){var m=s.getValue(ATTACHMENT);if(m==Attachment.SIDE_LEFT)result=result.setValue(ATTACHMENT,Attachment.SIDE_RIGHT);else if(m==Attachment.SIDE_RIGHT)result=result.setValue(ATTACHMENT,Attachment.SIDE_LEFT);}return result;}
    @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos op){
        var m=s.getValue(ATTACHMENT);
        if(m==Attachment.SIDE_LEFT||m==Attachment.SIDE_RIGHT)s=s.setValue(ATTACHMENT,Attachment.FREE);
        return joined(super.updateShape(s,d,other,w,p,op),w,p);
    }
}
