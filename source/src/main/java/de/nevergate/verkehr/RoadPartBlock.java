package de.nevergate.verkehr;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;

/** Model and physical shape share the bundled road-parts definitions. */
public final class RoadPartBlock extends OrientedBlock {
    public final String id;
    private final VoxelShape[] shapes=new VoxelShape[4];
    public RoadPartBlock(String id) {
        super(NgTraffic.metal().noOcclusion(),false);this.id=id;
        try(var stream=RoadPartBlock.class.getResourceAsStream("/assets/ng_verkehr/road_parts.json")) {
            if(stream==null)throw new IllegalStateException("Missing road geometry");
            var boxes=JsonParser.parseReader(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray(id).get(1).getAsJsonArray();
            for(int rotation=0;rotation<4;rotation++){
                VoxelShape shape=Shapes.empty();
                for(var entry:boxes){var a=entry.getAsJsonArray();double x=a.get(0).getAsDouble(),y=a.get(1).getAsDouble(),z=a.get(2).getAsDouble(),xx=a.get(3).getAsDouble(),yy=a.get(4).getAsDouble(),zz=a.get(5).getAsDouble();
                    for(int k=0;k<rotation;k++){double oldX=x,oldXX=xx;x=16-zz;xx=16-z;z=oldX;zz=oldXX;}
                    shape=Shapes.or(shape,box(x,y,z,xx,yy,zz));
                }shapes[rotation]=shape.optimize();
            }
        }catch(Exception e){throw new IllegalStateException("Invalid road geometry: "+id,e);}
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return shapes[switch(state.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;}];
    }
    public boolean connectsRail(Direction direction,BlockState state) {
        return id.startsWith("guardrail_")&&direction.getAxis()!=state.getValue(FACING).getAxis();
    }
}
