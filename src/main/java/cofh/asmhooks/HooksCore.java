package cofh.asmhooks;

import cofh.asmhooks.event.ModPopulateChunkEvent;
import cofh.core.CoFHProps;
import cofh.core.item.IEqualityOverrideItem;
import cofh.lib.util.helpers.MathHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class HooksCore {

	// { Forge hooks

	public static void preGenerateWorld(World world, int chunkX, int chunkZ) {

		MinecraftForge.EVENT_BUS.post(new ModPopulateChunkEvent.Pre(world, chunkX, chunkZ));
	}

	public static void postGenerateWorld(World world, int chunkX, int chunkZ) {

		MinecraftForge.EVENT_BUS.post(new ModPopulateChunkEvent.Post(world, chunkX, chunkZ));
	}

	// }

	// { Vanilla hooks
	public static boolean areItemsEqualHook(ItemStack held, ItemStack lastHeld) {

		if (held.getItem() != lastHeld.getItem()) {
			return false;
		}
		Item item = held.getItem();
		if (item instanceof IEqualityOverrideItem && ((IEqualityOverrideItem) item).isLastHeldItemEqual(held, lastHeld)) {
			return true;
		}
		if (held.isItemStackDamageable() && held.getItemDamage() != lastHeld.getItemDamage()) {
			return false;
		}

		return ItemStack.areItemStackTagsEqual(held, lastHeld);
	}

	public static void stackItems(EntityItem entity) {

		if (!CoFHProps.enableItemStacking) {
			return;
		}

		ItemStack stack = entity.getEntityItem();
		if (stack == null || stack.stackSize >= stack.getMaxStackSize()) {
			return;
		}

		@SuppressWarnings("rawtypes")
		Iterator iterator = entity.worldObj.getEntitiesWithinAABB(EntityItem.class, entity.boundingBox.expand(0.5D, 0.0D, 0.5D)).iterator();

		while (iterator.hasNext()) {
			entity.combineItems((EntityItem) iterator.next());
		}
	}

	@SuppressWarnings("rawtypes")
	public static List getEntityCollisionBoxes(World world, Entity entity, AxisAlignedBB bb) {

		if (!entity.canBePushed()) {
			List collidingBoundingBoxes = world.collidingBoundingBoxes;
			if (collidingBoundingBoxes == null) {
				collidingBoundingBoxes = world.collidingBoundingBoxes = new ArrayList();
			}
			collidingBoundingBoxes.clear();
			int i = MathHelper.floor(bb.minX);
			int j = MathHelper.floor(bb.maxX + 1.0D);
			int k = MathHelper.floor(bb.minY);
			int l = MathHelper.floor(bb.maxY + 1.0D);
			int i1 = MathHelper.floor(bb.minZ);
			int j1 = MathHelper.floor(bb.maxZ + 1.0D);
			int count = 0, count0 = 0;

			for (int x = i; x < j; ++x) {
				if (CoFHProps.entityCollision0 != -1 && count0>=CoFHProps.entityCollision0) {
				if (CoFHProps.enableentityCollision0logging) cpw.mods.fml.common.FMLLog.warning("CoFHCore HooksCore getEntityCollisionBoxes count0>=%s", CoFHProps.entityCollision0);
				count=0;break;}
				count0++;
				boolean xBound = x >= -30000000 & x < 30000000;
				for (int z = i1; z < j1; ++z) {
					if (CoFHProps.entityCollision1 != -1 && count>=CoFHProps.entityCollision1) {
					if (CoFHProps.enableentityCollision1logging) cpw.mods.fml.common.FMLLog.warning("CoFHCore HooksCore getEntityCollisionBoxes count>=%s %s, %s, %s, %s, %s, %s", CoFHProps.entityCollision1, i, j, k, l, i1, j1);
					count=0;break;}
					count++;
					boolean def = xBound & z >= -30000000 & z < 30000000;
					if (!world.blockExists(x, 64, z)) {
						continue;
					}
					if (def) {
						for (int y = k - 1; y < l; ++y) {
							world.getBlock(x, y, z).addCollisionBoxesToList(world, x, y, z, bb, collidingBoundingBoxes, entity);
						}
					} else {
						for (int y = k - 1; y < l; ++y) {
							Blocks.bedrock.addCollisionBoxesToList(world, x, y, z, bb, collidingBoundingBoxes, entity);
						}
					}
				}
			}

			return collidingBoundingBoxes;
		}
		return world.getCollidingBoundingBoxes(entity, bb);
	}

	@SideOnly(Side.CLIENT)
	public static void tickTextures(ITickable obj) {

		if (CoFHProps.enableAnimatedTextures) {
			obj.tick();
		}
	}

	public static boolean paneConnectsTo(IBlockAccess world, int x, int y, int z, ForgeDirection dir) {

		Block block = world.getBlock(x, y, z);
		return block.func_149730_j() || block.getMaterial() == Material.glass || block instanceof BlockPane
				|| world.isSideSolid(x, y, z, dir.getOpposite(), false);
	}
	// }

}
