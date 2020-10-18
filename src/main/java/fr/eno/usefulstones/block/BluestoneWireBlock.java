package fr.eno.usefulstones.block;

import java.awt.Color;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fr.eno.usefulstones.init.InitBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Explosion.Mode;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BluestoneWireBlock extends Block implements IWaterLoggable, IBlockColor
{
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.REDSTONE_NORTH;
	public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.REDSTONE_EAST;
	public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.REDSTONE_SOUTH;
	public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.REDSTONE_WEST;
	public static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;
	public static final Map<Direction, EnumProperty<RedstoneSide>> FACING_PROPERTY_MAP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
	protected static final VoxelShape[] SHAPES = new VoxelShape[] { Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D), Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D), Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 13.0D, 1.0D, 16.0D), Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Block.makeCuboidShape(0.0D, 0.0D, 3.0D, 16.0D, 1.0D, 16.0D), Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 16.0D, 1.0D, 13.0D), Block.makeCuboidShape(3.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 13.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D) };
	private boolean canProvidePower = true;
	private final Set<BlockPos> blocksNeedingUpdate = Sets.newHashSet();

	public BluestoneWireBlock()
	{
		super(Block.Properties.create(Material.MISCELLANEOUS, DyeColor.BLUE).sound(SoundType.WOOD).doesNotBlockMovement().hardnessAndResistance(0.0F));

		this.setDefaultState(this.stateContainer.getBaseState().with(NORTH, RedstoneSide.NONE).with(EAST, RedstoneSide.NONE).with(SOUTH, RedstoneSide.NONE).with(WEST, RedstoneSide.NONE).with(POWER, Integer.valueOf(0)).with(WATERLOGGED, Boolean.valueOf(false)));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPES[getAABBIndex(state)];
	}

	private static int getAABBIndex(BlockState state)
	{
		int i = 0;
		boolean flag = state.get(NORTH) != RedstoneSide.NONE;
		boolean flag1 = state.get(EAST) != RedstoneSide.NONE;
		boolean flag2 = state.get(SOUTH) != RedstoneSide.NONE;
		boolean flag3 = state.get(WEST) != RedstoneSide.NONE;
		if (flag || flag2 && !flag && !flag1 && !flag3)
		{
			i |= 1 << Direction.NORTH.getHorizontalIndex();
		}

		if (flag1 || flag3 && !flag && !flag1 && !flag2)
		{
			i |= 1 << Direction.EAST.getHorizontalIndex();
		}

		if (flag2 || flag && !flag1 && !flag2 && !flag3)
		{
			i |= 1 << Direction.SOUTH.getHorizontalIndex();
		}

		if (flag3 || flag1 && !flag && !flag2 && !flag3)
		{
			i |= 1 << Direction.WEST.getHorizontalIndex();
		}

		return i;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		IBlockReader iblockreader = context.getWorld();
		BlockPos blockpos = context.getPos();
		return this.getDefaultState().with(WEST, this.getSide(iblockreader, blockpos, Direction.WEST)).with(EAST, this.getSide(iblockreader, blockpos, Direction.EAST)).with(NORTH, this.getSide(iblockreader, blockpos, Direction.NORTH)).with(SOUTH, this.getSide(iblockreader, blockpos, Direction.SOUTH));
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		if(!worldIn.isRemote())
			for(Direction face : Direction.values())
			{
				if(worldIn.getBlockState(currentPos.offset(face)).getBlock() == Blocks.REDSTONE_WIRE)
				{
					((World) worldIn).createExplosion((Entity) null, currentPos.getX(), currentPos.getY(), currentPos.getZ(), 2 + new Random().nextInt(4), Mode.DESTROY);
					break;
				}
			}
		
		if (facing == Direction.DOWN)
		{
			return stateIn;
		} else
		{
			return facing == Direction.UP ? stateIn.with(WEST, this.getSide(worldIn, currentPos, Direction.WEST)).with(EAST, this.getSide(worldIn, currentPos, Direction.EAST)).with(NORTH, this.getSide(worldIn, currentPos, Direction.NORTH)).with(SOUTH, this.getSide(worldIn, currentPos, Direction.SOUTH)) : stateIn.with(FACING_PROPERTY_MAP.get(facing), this.getSide(worldIn, currentPos, facing));
		}
	}

	@Override
	public void updateDiagonalNeighbors(BlockState state, IWorld worldIn, BlockPos pos, int flags)
	{
		try (BlockPos.PooledMutable blockpos$pooledmutable = BlockPos.PooledMutable.retain())
		{
			for (Direction direction : Direction.Plane.HORIZONTAL)
			{
				RedstoneSide redstoneside = state.get(FACING_PROPERTY_MAP.get(direction));
				if (redstoneside != RedstoneSide.NONE && worldIn.getBlockState(blockpos$pooledmutable.setPos(pos).move(direction)).getBlock() != this)
				{
					blockpos$pooledmutable.move(Direction.DOWN);
					BlockState blockstate = worldIn.getBlockState(blockpos$pooledmutable);
					if (blockstate.getBlock() != Blocks.OBSERVER)
					{
						BlockPos blockpos = blockpos$pooledmutable.offset(direction.getOpposite());
						BlockState blockstate1 = blockstate.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos), worldIn, blockpos$pooledmutable, blockpos);
						replaceBlock(blockstate, blockstate1, worldIn, blockpos$pooledmutable, flags);
					}

					blockpos$pooledmutable.setPos(pos).move(direction).move(Direction.UP);
					BlockState blockstate3 = worldIn.getBlockState(blockpos$pooledmutable);
					if (blockstate3.getBlock() != Blocks.OBSERVER)
					{
						BlockPos blockpos1 = blockpos$pooledmutable.offset(direction.getOpposite());
						BlockState blockstate2 = blockstate3.updatePostPlacement(direction.getOpposite(), worldIn.getBlockState(blockpos1), worldIn, blockpos$pooledmutable, blockpos1);
						replaceBlock(blockstate3, blockstate2, worldIn, blockpos$pooledmutable, flags);
					}
				}
			}
		}

	}

	private RedstoneSide getSide(IBlockReader worldIn, BlockPos pos, Direction face)
	{
		BlockPos blockpos = pos.offset(face);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		BlockPos blockpos1 = pos.up();
		BlockState blockstate1 = worldIn.getBlockState(blockpos1);
		if (!blockstate1.isNormalCube(worldIn, blockpos1))
		{
			boolean flag = blockstate.isSolidSide(worldIn, blockpos, Direction.UP) || blockstate.getBlock() == Blocks.HOPPER;
			if (flag && canConnectTo(worldIn.getBlockState(blockpos.up()), worldIn, blockpos.up(), null))
			{
				if (blockstate.isCollisionShapeOpaque(worldIn, blockpos))
				{
					return RedstoneSide.UP;
				}

				return RedstoneSide.SIDE;
			}
		}

		return !canConnectTo(blockstate, worldIn, blockpos, face) && (blockstate.isNormalCube(worldIn, blockpos) || !canConnectTo(worldIn.getBlockState(blockpos.down()), worldIn, blockpos.down(), null)) ? RedstoneSide.NONE : RedstoneSide.SIDE;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		BlockPos blockpos = pos.down();
		BlockState blockstate = worldIn.getBlockState(blockpos);
		return blockstate.isSolidSide(worldIn, blockpos, Direction.UP) || blockstate.getBlock() == Blocks.HOPPER;
	}

	private BlockState updateSurroundingRedstone(World worldIn, BlockPos pos, BlockState state)
	{
		state = this.updatePower(worldIn, pos, state);
		List<BlockPos> list = Lists.newArrayList(this.blocksNeedingUpdate);
		this.blocksNeedingUpdate.clear();

		for (BlockPos blockpos : list)
		{
			worldIn.notifyNeighborsOfStateChange(blockpos, this);
		}

		return state;
	}

	private BlockState updatePower(World worldIn, BlockPos pos, BlockState state)
	{
		BlockState blockstate = state;
		int i = state.get(POWER);
		this.canProvidePower = false;
		int j = worldIn.getRedstonePowerFromNeighbors(pos);
		this.canProvidePower = true;
		int k = 0;
		if (j < 15)
		{
			for (Direction direction : Direction.Plane.HORIZONTAL)
			{
				BlockPos blockpos = pos.offset(direction);
				BlockState blockstate1 = worldIn.getBlockState(blockpos);
				k = this.maxSignal(k, blockstate1);
				BlockPos blockpos1 = pos.up();
				if (blockstate1.isNormalCube(worldIn, blockpos) && !worldIn.getBlockState(blockpos1).isNormalCube(worldIn, blockpos1))
				{
					k = this.maxSignal(k, worldIn.getBlockState(blockpos.up()));
				} else if (!blockstate1.isNormalCube(worldIn, blockpos))
				{
					k = this.maxSignal(k, worldIn.getBlockState(blockpos.down()));
				}
			}
		}

		int l = k - 1;
		if (j > l)
		{
			l = j;
		}

		if (i != l)
		{
			state = state.with(POWER, Integer.valueOf(l));
			if (worldIn.getBlockState(pos) == blockstate)
			{
				worldIn.setBlockState(pos, state, 2);
			}

			this.blocksNeedingUpdate.add(pos);

			for (Direction direction1 : Direction.values())
			{
				this.blocksNeedingUpdate.add(pos.offset(direction1));
			}
		}

		return state;
	}

	private void notifyWireNeighborsOfStateChange(World worldIn, BlockPos pos)
	{
		if (worldIn.getBlockState(pos).getBlock() == this)
		{
			worldIn.notifyNeighborsOfStateChange(pos, this);

			for (Direction direction : Direction.values())
			{
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
				
				if(worldIn.getBlockState(pos.offset(direction)).getBlock() == Blocks.REDSTONE_WIRE)
					worldIn.createExplosion((Entity) null, pos.getX(), pos.getY(), pos.getZ(), 2 + new Random().nextInt(4), Mode.DESTROY);
			}

		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		if (oldState.getBlock() != state.getBlock() && !worldIn.isRemote)
		{
			this.updateSurroundingRedstone(worldIn, pos, state);

			for (Direction direction : Direction.Plane.VERTICAL)
			{
				worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
			}

			for (Direction direction1 : Direction.Plane.HORIZONTAL)
			{
				this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction1));
			}

			for (Direction direction2 : Direction.Plane.HORIZONTAL)
			{
				BlockPos blockpos = pos.offset(direction2);
				if (worldIn.getBlockState(blockpos).isNormalCube(worldIn, blockpos))
				{
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
				} else
				{
					this.notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
				}
			}

		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (!isMoving && state.getBlock() != newState.getBlock())
		{
			super.onReplaced(state, worldIn, pos, newState, isMoving);
			if (!worldIn.isRemote)
			{
				for (Direction direction : Direction.values())
				{
					worldIn.notifyNeighborsOfStateChange(pos.offset(direction), this);
				}

				this.updateSurroundingRedstone(worldIn, pos, state);

				for (Direction direction1 : Direction.Plane.HORIZONTAL)
				{
					this.notifyWireNeighborsOfStateChange(worldIn, pos.offset(direction1));
				}

				for (Direction direction2 : Direction.Plane.HORIZONTAL)
				{
					BlockPos blockpos = pos.offset(direction2);
					if (worldIn.getBlockState(blockpos).isNormalCube(worldIn, blockpos))
					{
						this.notifyWireNeighborsOfStateChange(worldIn, blockpos.up());
					} else
					{
						this.notifyWireNeighborsOfStateChange(worldIn, blockpos.down());
					}
				}

			}
		}
	}

	private int maxSignal(int existingSignal, BlockState neighbor)
	{
		if (neighbor.getBlock() != this)
		{
			return existingSignal;
		} else
		{
			int i = neighbor.get(POWER);
			return i > existingSignal ? i : existingSignal;
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		if (!worldIn.isRemote)
		{
			if (state.isValidPosition(worldIn, pos))
			{
				this.updateSurroundingRedstone(worldIn, pos, state);
			} else
			{
				spawnDrops(state, worldIn, pos);
				worldIn.removeBlock(pos, false);
			}

		}
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		return !this.canProvidePower ? 0 : blockState.getWeakPower(blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		if (!this.canProvidePower)
		{
			return 0;
		} else
		{
			int i = blockState.get(POWER);
			if (i == 0)
			{
				return 0;
			} else if (side == Direction.UP)
			{
				return i;
			} else
			{
				EnumSet<Direction> enumset = EnumSet.noneOf(Direction.class);

				for (Direction direction : Direction.Plane.HORIZONTAL)
				{
					if (this.isPowerSourceAt(blockAccess, pos, direction))
					{
						enumset.add(direction);
					}
				}

				if (side.getAxis().isHorizontal() && enumset.isEmpty())
				{
					return i;
				} else
				{
					return enumset.contains(side) && !enumset.contains(side.rotateYCCW()) && !enumset.contains(side.rotateY()) ? i : 0;
				}
			}
		}
	}

	private boolean isPowerSourceAt(IBlockReader worldIn, BlockPos pos, Direction side)
	{
		BlockPos blockpos = pos.offset(side);
		BlockState blockstate = worldIn.getBlockState(blockpos);
		boolean flag = blockstate.isNormalCube(worldIn, blockpos);
		BlockPos blockpos1 = pos.up();
		boolean flag1 = worldIn.getBlockState(blockpos1).isNormalCube(worldIn, blockpos1);
		if (!flag1 && flag && canConnectTo(worldIn.getBlockState(blockpos.up()), worldIn, blockpos.up(), null))
		{
			return true;
		} else if (canConnectTo(blockstate, worldIn, blockpos, side))
		{
			return true;
		} else if (blockstate.getBlock() == Blocks.REPEATER && blockstate.get(RedstoneDiodeBlock.POWERED) && blockstate.get(RedstoneDiodeBlock.HORIZONTAL_FACING) == side)
		{
			return true;
		} else
		{
			return !flag && canConnectTo(worldIn.getBlockState(blockpos.down()), worldIn, blockpos.down(), null);
		}
	}

	protected static boolean canConnectTo(BlockState blockState, IBlockReader world, BlockPos pos, @Nullable Direction side)
	{
		Block block = blockState.getBlock();
		if (block == Blocks.REDSTONE_WIRE || block == InitBlocks.BLUESTONE_WIRE.get())
		{
			return true;
		} else if (blockState.getBlock() == Blocks.REPEATER || blockState.getBlock() == InitBlocks.BLUESTONE_REPEATER.get())
		{
			Direction direction = blockState.get(RepeaterBlock.HORIZONTAL_FACING);
			return direction == side || direction.getOpposite() == side;
		} else if (Blocks.OBSERVER == blockState.getBlock())
		{
			return side == blockState.get(ObserverBlock.FACING);
		} else
		{
			return blockState.canConnectRedstone(world, pos, side) && side != null;
		}
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		return this.canProvidePower;
	}

	@OnlyIn(Dist.CLIENT)
	public static int colorMultiplier(int p_176337_0_)
	{
		float f = (float) p_176337_0_ / 15.0F;
		float f1 = f * 0.6F + 0.4F;
		if (p_176337_0_ == 0)
		{
			f1 = 0.3F;
		}

		float f2 = f * f * 0.7F - 0.5F;
		float f3 = f * f * 0.6F - 0.7F;
		if (f2 < 0.0F)
		{
			f2 = 0.0F;
		}

		if (f3 < 0.0F)
		{
			f3 = 0.0F;
		}

		int i = MathHelper.clamp((int) (f1 * 255.0F), 0, 255);
		int j = MathHelper.clamp((int) (f2 * 255.0F), 0, 255);
		int k = MathHelper.clamp((int) (f3 * 255.0F), 0, 255);
		return -16777216 | i << 16 | j << 8 | k;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		switch (rot)
		{
			case CLOCKWISE_180:
				return state.with(NORTH, state.get(SOUTH)).with(EAST, state.get(WEST)).with(SOUTH, state.get(NORTH)).with(WEST, state.get(EAST));
			case COUNTERCLOCKWISE_90:
				return state.with(NORTH, state.get(EAST)).with(EAST, state.get(SOUTH)).with(SOUTH, state.get(WEST)).with(WEST, state.get(NORTH));
			case CLOCKWISE_90:
				return state.with(NORTH, state.get(WEST)).with(EAST, state.get(NORTH)).with(SOUTH, state.get(EAST)).with(WEST, state.get(SOUTH));
			default:
				return state;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		switch (mirrorIn)
		{
			case LEFT_RIGHT:
				return state.with(NORTH, state.get(SOUTH)).with(SOUTH, state.get(NORTH));
			case FRONT_BACK:
				return state.with(EAST, state.get(WEST)).with(WEST, state.get(EAST));
			default:
				return super.mirror(state, mirrorIn);
		}
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side)
	{
		return true;
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
		builder.add(WATERLOGGED, NORTH, EAST, SOUTH, WEST, POWER);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		if (stateIn.get(POWER) > 0)
		{
			worldIn.addParticle(new RedstoneParticleData(0, 0, rand.nextFloat(), 1.0F), (float) rand.nextInt(10) / 10F, (float) rand.nextInt(10) / 10F, (float) rand.nextInt(10) / 10F, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public int getColor(BlockState state, ILightReader light, BlockPos pos, int p_getColor_4_)
	{
		int blue = 70;

		if (state.get(POWER) > 0)
		{
			blue = 70 + state.get(POWER) * 185 / 15;
		}
		return new Color(0, 0, blue).getRGB();
	}
}
