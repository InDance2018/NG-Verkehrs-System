package de.nevergate.verkehr;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Forge's normal ItemStack.useOn transaction supplies multi-place protection and rollback. */
public final class LaneModuleItem extends Item {
    private final Supplier<? extends Block> block;
    private final int length;
    public LaneModuleItem(Supplier<? extends Block> block, int length) {
        super(new Properties()); this.block=block; this.length=length;
    }
    public static List<BlockPos> footprint(BlockPos nearLeft, Direction forward, int length) {
        var result=new ArrayList<BlockPos>();
        for(int row=0;row<length;row++) for(int col=0;col<4;col++)
            result.add(nearLeft.relative(forward,row).relative(forward.getClockWise(),col));
        return List.copyOf(result);
    }
    public static int tile(int cell) { return (3-cell/4)*4+cell%4; }
    @Override public InteractionResult useOn(UseOnContext use) {
        var player=use.getPlayer(); var level=use.getLevel();
        if(player==null || !player.getAbilities().mayBuild || use.getItemInHand().isEmpty()) return InteractionResult.FAIL;
        if(!level.hasChunkAt(use.getClickedPos())) return InteractionResult.FAIL;
        var context=new BlockPlaceContext(use);
        var anchor=context.getClickedPos(); var facing=use.getHorizontalDirection();
        var positions=footprint(anchor,facing,length);
        var old=new ArrayList<BlockState>(); var states=new ArrayList<BlockState>();
        for(int i=0;i<positions.size();i++) {
            var pos=positions.get(i);
            // hasChunkAt before any block read: never generate or load a neighbour chunk.
            if(level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos) || !level.hasChunkAt(pos)
                || !level.mayInteract(player,pos) || !player.mayUseItemAt(pos,use.getClickedFace(),use.getItemInHand()))
                return failed(level,player);
            var current=level.getBlockState(pos);
            var cellContext=BlockPlaceContext.at(context,pos,use.getClickedFace());
            var state=block.get().defaultBlockState();
            if(length==4) state=state.setValue(OrientedBlock.FACING,facing).setValue(LaneArrowBlock.TILE,tile(i));
            if(!current.canBeReplaced(cellContext) || level.getBlockEntity(pos)!=null || !state.canSurvive(level,pos)
                || !level.isUnobstructed(state,pos,CollisionContext.of(player))) return failed(level,player);
            old.add(current); states.add(state);
        }
        if(level.isClientSide) return InteractionResult.SUCCESS;
        int capturedStart=level.capturedBlockSnapshots.size();
        for(int i=0;i<positions.size();i++) {
            if(!level.setBlock(positions.get(i),states.get(i),3)) {
                boolean restoring=level.restoringBlockSnapshots;
                level.restoringBlockSnapshots=true;
                for(int j=i-1;j>=0;j--) level.setBlock(positions.get(j),old.get(j),3);
                level.restoringBlockSnapshots=restoring;
                while(level.capturedBlockSnapshots.size()>capturedStart)
                    level.capturedBlockSnapshots.remove(level.capturedBlockSnapshots.size()-1);
                return InteractionResult.FAIL;
            }
        }
        if(!player.getAbilities().instabuild) use.getItemInHand().shrink(1);
        // CONSUME is handled by ForgeHooks: all cells are one EntityMultiPlaceEvent.
        return InteractionResult.CONSUME;
    }
    private static InteractionResult failed(Level level,net.minecraft.world.entity.player.Player player) {
        if(!level.isClientSide) player.displayClientMessage(Component.translatable("message.ng_verkehr.lane_space"),true);
        return InteractionResult.FAIL;
    }
    @Override public void appendHoverText(ItemStack stack,Level level,List<Component> lines,TooltipFlag flag) {
        lines.add(Component.translatable(length==1?"tooltip.ng_verkehr.asphalt_lane":"tooltip.ng_verkehr.arrow_lane"));
        lines.add(Component.translatable("tooltip.ng_verkehr.lane_anchor"));
    }
}
