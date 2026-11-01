package de.nevergate.verkehr;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.*;
public final class GuardrailBlock extends FenceBlock {
    public static final BooleanProperty SUPPORT=BooleanProperty.create("support");
    public GuardrailBlock(){super(NgTraffic.metal().noOcclusion());registerDefaultState(defaultBlockState().setValue(SUPPORT,false));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(SUPPORT);}
    @Override public boolean connectsTo(BlockState s,boolean sturdy,Direction d){return s.getBlock() instanceof RoadPartBlock part?part.connectsRail(d,s):s.getBlock() instanceof GuardrailBlock;}
    private static boolean rail(BlockState s){return s.getBlock() instanceof GuardrailBlock||s.getBlock() instanceof RoadPartBlock p&&p.id.startsWith("guardrail_");}
    private static boolean linked(BlockState a,BlockState b,Direction d){if(!rail(a)||!rail(b))return false;return (!(a.getBlock() instanceof RoadPartBlock pa)||pa.connectsRail(d,a))&&(!(b.getBlock() instanceof RoadPartBlock pb)||pb.connectsRail(d.getOpposite(),b));}
    private void schedule(Level level,BlockPos pos){if(!level.isClientSide)level.scheduleTick(pos,this,1);}
    @Override public void onPlace(BlockState s,Level w,BlockPos p,BlockState old,boolean moved){super.onPlace(s,w,p,old,moved);if(!old.is(this))schedule(w,p);}
    @Override public void neighborChanged(BlockState s,Level w,BlockPos p,Block b,BlockPos from,boolean moving){super.neighborChanged(s,w,p,b,from,moving);schedule(w,p);}
    @Override public BlockState updateShape(BlockState s,Direction d,BlockState other,LevelAccessor w,BlockPos p,BlockPos op){if(d.getAxis().isHorizontal())w.scheduleTick(p,this,1);return super.updateShape(s,d,other,w,p,op);}
    @Override public void tick(BlockState state,ServerLevel w,BlockPos pos,RandomSource random){
        // Visit only loaded cells; no chunk generation and a bounded component size.
        var nodes=new HashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();nodes.add(pos);queue.add(pos);
        while(!queue.isEmpty()&&nodes.size()<4096){var p=queue.remove();var s=w.getBlockState(p);for(var d:Direction.Plane.HORIZONTAL){var n=p.relative(d);if(w.hasChunkAt(n)&&!nodes.contains(n)&&linked(s,w.getBlockState(n),d)){nodes.add(n);queue.add(n);}}}
        // Start/end geometry anchors the spacing, independent of placement order.
        var order=Comparator.<BlockPos>comparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ);
        BlockPos anchor=nodes.stream().filter(p->w.getBlockState(p).getBlock() instanceof RoadPartBlock).min(order).orElseGet(()->nodes.stream().min(order).orElse(pos));
        var distance=new HashMap<BlockPos,Integer>();distance.put(anchor,0);queue.clear();queue.add(anchor);
        while(!queue.isEmpty()){var p=queue.remove();for(var d:Direction.Plane.HORIZONTAL){var n=p.relative(d);if(nodes.contains(n)&&!distance.containsKey(n)&&linked(w.getBlockState(p),w.getBlockState(n),d)){distance.put(n,distance.get(p)+1);queue.add(n);}}}
        for(var p:nodes){var s=w.getBlockState(p);if(!(s.getBlock() instanceof GuardrailBlock))continue;int n=distance.getOrDefault(p,0);boolean support=n%7==0;
            if(s.getValue(SUPPORT)!=support)w.setBlock(p,s.setValue(SUPPORT,support),2);
        }
    }
    @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){
        VoxelShape shape=Shapes.empty();if(s.getValue(SUPPORT))shape=box(7,0,7,9,12.6,9.3);
        for(var d:Direction.Plane.HORIZONTAL)if(s.getValue(PROPERTY_BY_DIRECTION.get(d))){shape=Shapes.or(shape,MountedBlock.rotateBox(d,6.7,6,0,9.3,12.6,8));}
        return shape.isEmpty()?box(6.7,6,6.7,9.3,12.6,9.3):shape;
    }
    @Override public VoxelShape getCollisionShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){return getShape(s,w,p,c);}
}
