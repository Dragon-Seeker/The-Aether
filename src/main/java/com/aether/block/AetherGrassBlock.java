package com.aether.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.FlowersFeature;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class AetherGrassBlock extends GrassBlock implements IAetherDoubleDropBlock {

	public AetherGrassBlock(Block.Properties properties) {
		super(properties);
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(DOUBLE_DROPS);
	}

	@Override
	public boolean canSustainPlant(BlockState state, IBlockReader world, BlockPos pos, Direction direction, IPlantable plantable) {
		PlantType plantType = plantable.getPlantType(world, pos.offset(direction));

		switch (plantType) {
		case Plains:
			return true;
		case Beach:
			for (Direction facing : Direction.Plane.HORIZONTAL) {
				if (world.getFluidState(pos.offset(facing)).isTagged(FluidTags.WATER)) {
					return true;
				}
			}
			return false;
		default:
			return super.canSustainPlant(state, world, pos, direction, plantable);
		}
	}

	@Override
	public void grow(final World worldIn, final Random rand, final BlockPos pos, final BlockState state) {
		final BlockPos posUp = pos.up();
		final BlockState tallGrassState = AetherBlocks.Decorations.getTallGrass().getDefaultState();

		for (int i = 0; i < 128; ++i) {
			BlockPos blockpos1 = posUp;
			int j = 0;

			while (true) {
				if (j >= i / 16) {
					BlockState blockstate2 = worldIn.getBlockState(blockpos1);
					if (blockstate2.getBlock() == tallGrassState.getBlock() && rand.nextInt(10) == 0) {
						((IGrowable) tallGrassState.getBlock()).grow(worldIn, rand, blockpos1, blockstate2);
					}

					if (!blockstate2.isAir(worldIn, blockpos1)) {
						break;
					}

					BlockState blockstate1;
					if (rand.nextInt(8) == 0) {
						List<ConfiguredFeature<?>> list = worldIn.getBiome(blockpos1).getFlowers();
						if (list.isEmpty()) {
							break;
						}

						blockstate1 = ((FlowersFeature) ((DecoratedFeatureConfig) list.get(0).config).feature.feature).getRandomFlower(rand, blockpos1);
					}
					else {
						blockstate1 = tallGrassState;
					}

					if (blockstate1.isValidPosition(worldIn, blockpos1)) {
						worldIn.setBlockState(blockpos1, blockstate1, 3);
					}

					break;
				}

				blockpos1 = blockpos1.add(rand.nextInt(3) - 1, (rand.nextInt(3) - 1) * rand.nextInt(3) / 2, rand.nextInt(3) - 1);

				if (worldIn.getBlockState(blockpos1.down()).getBlock() != this
					|| worldIn.getBlockState(blockpos1).isNormalCube(worldIn, blockpos1)) {
					break;
				}

				++j;
			} // end while loop
		}
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (!worldIn.isRemote) {
			if (!worldIn.isAreaLoaded(pos, 3)) {
				return; // Forge: prevent loading unloaded chunks when checking neighbor's light and spreading
			}

			if (!func_220257_b(state, worldIn, pos)) {
				worldIn.setBlockState(pos, AetherBlocks.AETHER_DIRT.getDefaultState().with(DOUBLE_DROPS, state.get(DOUBLE_DROPS)));
			}
			else {
				if (worldIn.getLight(pos.up()) >= 9) {
					BlockState blockstate = this.getDefaultState();

					for (int i = 0; i < 4; ++i) {
						BlockPos blockpos = pos.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
						BlockState blockstate2 = worldIn.getBlockState(blockpos);
						if (blockstate2.getBlock() == AetherBlocks.AETHER_DIRT
								&& func_220256_c(blockstate, worldIn, blockpos)) {
							worldIn.setBlockState(blockpos, blockstate.with(SNOWY, worldIn.getBlockState(blockpos.up()).getBlock() == Blocks.SNOW).with(DOUBLE_DROPS, blockstate2.get(DOUBLE_DROPS)));
						}
					}
				}
			}
		}
	}

	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean canGrow(IBlockReader world, BlockPos pos, BlockState state, boolean isClient) {
		return true;
	}

}
