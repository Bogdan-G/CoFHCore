package cofh.lib.world;

import static cofh.lib.world.WorldGenMinableCluster.*;

import cofh.lib.util.WeightedRandomBlock;

import java.util.List;
import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenGeode extends WorldGenerator {

	private final List<WeightedRandomBlock> cluster;
	private final List<WeightedRandomBlock> outline;
	private final WeightedRandomBlock[] genBlock;
	public List<WeightedRandomBlock> fillBlock = null;
	public boolean hollow = false;
	public int width = 16;
	public int height = 8;

	public WorldGenGeode(List<WeightedRandomBlock> resource, List<WeightedRandomBlock> material, List<WeightedRandomBlock> cover) {

		cluster = resource;
		genBlock = material.toArray(new WeightedRandomBlock[material.size()]);
		outline = cover;
	}

	@Override
	public boolean generate(World world, Random rand, int xStart, int yStart, int zStart) {

		int heightOff = height / 2;
		int widthOff = width / 2;
		xStart -= widthOff;
		zStart -= widthOff;

		if (yStart <= heightOff) {
			return false;
		}

		yStart -= heightOff;
		java.util.BitSet spawnBlock = new java.util.BitSet(width * width * height);
		java.util.BitSet hollowBlock = new java.util.BitSet(width * width * height);

		int W = width - 1, H = height - 1;

		for (int i = 0, e = rand.nextInt(4) + 4; i < e; ++i) {
			float xSize = rand.nextFloat() * 6.0F + 3.0F;
			float ySize = rand.nextFloat() * 4.0F + 2.0F;
			float zSize = rand.nextFloat() * 6.0F + 3.0F;
			float xCenter = rand.nextFloat() * (width - xSize - 2.0F) + 1.0F + xSize / 2.0F;
			float yCenter = rand.nextFloat() * (height - ySize - 4.0F) + 2.0F + ySize / 2.0F;
			float zCenter = rand.nextFloat() * (width - zSize - 2.0F) + 1.0F + zSize / 2.0F;
			float minDist = hollow ? (float)rand.nextGaussian() * 0.15f + 0.4f : 0f;

			for (int x = 1; x < W; ++x) {
				for (int z = 1; z < W; ++z) {
					for (int y = 1; y < H; ++y) {
						float xDist = (x - xCenter) / (xSize / 2.0F);
						float yDist = (y - yCenter) / (ySize / 2.0F);
						float zDist = (z - zCenter) / (zSize / 2.0F);
						float dist = xDist * xDist + yDist * yDist + zDist * zDist;

						if (dist < 1.0F) {
							if (hollow ? dist > minDist : true) spawnBlock.set((x * width + z) * height + y);
						}
						if (hollow) {
							if (dist <= minDist) hollowBlock.set((x * width + z) * height + y);
						}
					}
				}
			}
		}

		int x;
		int y;
		int z;

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					boolean flag = (fillBlock != null && hollowBlock.get((x * width + z) * height + y))
							|| spawnBlock.get((x * width + z) * height + y)
							|| ((x < W && spawnBlock.get(((x + 1) * width + z) * height + y)) || (x > 0 && spawnBlock.get(((x - 1) * width + z) * height + y))
									|| (z < W && spawnBlock.get((x * width + (z + 1)) * height + y)) || (z > 0 && spawnBlock.get((x * width + (z - 1)) * height + y))
									|| (y < H && spawnBlock.get((x * width + z) * height + (y + 1))) || (y > 0 && spawnBlock.get((x * width + z) * height + (y - 1))));

					if (flag && !canGenerateInBlock(world, xStart + x, yStart + y, zStart + z, genBlock)) {
						return false;
					}
				}
			}
		}

		boolean r = false;
		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (spawnBlock.get((x * width + z) * height + y)) {
						boolean t = generateBlock(world, xStart + x, yStart + y, zStart + z, cluster);
						r |= t;
						if (!t) {
							spawnBlock.clear((x * width + z) * height + y);
						}
					}
				}
			}
		}

		for (x = 0; x < width; ++x) {
			for (z = 0; z < width; ++z) {
				for (y = 0; y < height; ++y) {
					if (fillBlock != null && hollowBlock.get((x * width + z) * height + y)) {
						r |= generateBlock(world, xStart + x, yStart + y, zStart + z, fillBlock);
					} else {
						boolean flag = !spawnBlock.get((x * width + z) * height + y)
								&& ((x < W && spawnBlock.get(((x + 1) * width + z) * height + y)) || (x > 0 && spawnBlock.get(((x - 1) * width + z) * height + y))
										|| (z < W && spawnBlock.get((x * width + (z + 1)) * height + y))
										|| (z > 0 && spawnBlock.get((x * width + (z - 1)) * height + y))
										|| (y < H && spawnBlock.get((x * width + z) * height + (y + 1))) || (y > 0 && spawnBlock.get((x * width + z) * height + (y - 1))));

						if (flag) {
							r |= generateBlock(world, xStart + x, yStart + y, zStart + z, outline);
						}
					}
				}
			}
		}

		return r;
	}
}
