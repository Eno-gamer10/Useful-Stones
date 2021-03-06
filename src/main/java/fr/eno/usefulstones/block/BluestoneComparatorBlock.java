package fr.eno.usefulstones.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class BluestoneComparatorBlock extends ComparatorBlock implements IWaterLoggable
{
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BluestoneComparatorBlock()
	{
		super(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD));
		
		this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH)
				.with(POWERED, Boolean.valueOf(false)).with(MODE, ComparatorMode.COMPARE)
				.with(WATERLOGGED, Boolean.valueOf(false)));
	}
	
	@Override
	public boolean propagatesSkylightDown(BlockState state, IBlockReader reader, BlockPos pos)
	{
		return !state.get(WATERLOGGED);
	}

	@SuppressWarnings("deprecation")
	public IFluidState getFluidState(BlockState state)
	{
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
	}

	@Override
	public void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		
		builder.add(WATERLOGGED);
	}
}
