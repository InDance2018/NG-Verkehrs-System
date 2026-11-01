package de.nevergate.verkehr;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** One full-height road cell; sixteen cells render one continuous arrow. */
public final class LaneArrowBlock extends OrientedBlock {
    public static final IntegerProperty TILE = IntegerProperty.create("tile", 0, 15);
    public LaneArrowBlock() {
        super(Properties.of().strength(2.5f).sound(SoundType.STONE), false);
        registerDefaultState(defaultBlockState().setValue(TILE, 0));
    }
    @Override public net.minecraft.world.item.ItemStack getCloneItemStack(net.minecraft.world.level.BlockGetter level, net.minecraft.core.BlockPos pos, BlockState state) {
        var id=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(this);
        return new net.minecraft.world.item.ItemStack(NgTraffic.LANE_MODULES.get(id.getPath()).get());
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TILE);
    }
}
