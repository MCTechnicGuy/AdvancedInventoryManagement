package com.mctechnicguy.aim.util;

import com.mctechnicguy.aim.tileentity.TileEntityNetworkCable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RayTracingHelper {

	private static float Pixel = 1F / 16F;
	private static double extension = 5 * Pixel;

	public static RayTraceResult rayTrace(@Nonnull World w, @Nonnull BlockPos pos, Vec3d v1, Vec3d v2, @Nonnull IBlockState b) {
		v1 = v1.addVector((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()));
		v2 = v2.addVector((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()));
		AxisAlignedBB bb = b.getBoundingBox(w, pos);

		Vec3d vec32 = v1.getIntermediateWithXValue(v2, bb.minX);
		Vec3d vec33 = v1.getIntermediateWithXValue(v2, bb.maxX);
		Vec3d vec34 = v1.getIntermediateWithYValue(v2, bb.minY);
		Vec3d vec35 = v1.getIntermediateWithYValue(v2, bb.maxY);
		Vec3d vec36 = v1.getIntermediateWithZValue(v2, bb.minZ);
		Vec3d vec37 = v1.getIntermediateWithZValue(v2, bb.maxZ);

		if (!isVecInsideYZBounds(vec32, w, pos, bb)) {
			vec32 = null;
		}

		if (!isVecInsideYZBounds(vec33, w, pos, bb)) {
			vec33 = null;
		}

		if (!isVecInsideXZBounds(vec34, w, pos, bb)) {
			vec34 = null;
		}

		if (!isVecInsideXZBounds(vec35, w, pos, bb)) {
			vec35 = null;
		}

		if (!isVecInsideXYBounds(vec36, w, pos, bb)) {
			vec36 = null;
		}

		if (!isVecInsideXYBounds(vec37, w, pos, bb)) {
			vec37 = null;
		}

		Vec3d vec38 = null;

		if (vec32 != null && (vec38 == null || v1.squareDistanceTo(vec32) < v1.squareDistanceTo(vec38))) {
			vec38 = vec32;
		}

		if (vec33 != null && (vec38 == null || v1.squareDistanceTo(vec33) < v1.squareDistanceTo(vec38))) {
			vec38 = vec33;
		}

		if (vec34 != null && (vec38 == null || v1.squareDistanceTo(vec34) < v1.squareDistanceTo(vec38))) {
			vec38 = vec34;
		}

		if (vec35 != null && (vec38 == null || v1.squareDistanceTo(vec35) < v1.squareDistanceTo(vec38))) {
			vec38 = vec35;
		}

		if (vec36 != null && (vec38 == null || v1.squareDistanceTo(vec36) < v1.squareDistanceTo(vec38))) {
			vec38 = vec36;
		}

		if (vec37 != null && (vec38 == null || v1.squareDistanceTo(vec37) < v1.squareDistanceTo(vec38))) {
			vec38 = vec37;
		}

		if (vec38 == null) {
			return null;
		} else {
			EnumFacing side = null;

			if (vec38 == vec32) {
				side = EnumFacing.WEST;
			}

			if (vec38 == vec33) {
				side = EnumFacing.EAST;
			}

			if (vec38 == vec34) {
				side = EnumFacing.DOWN;
			}

			if (vec38 == vec35) {
				side = EnumFacing.UP;
			}

			if (vec38 == vec36) {
				side = EnumFacing.NORTH;
			}

			if (vec38 == vec37) {
				side = EnumFacing.SOUTH;
			}

			return new RayTraceResult(vec38.addVector((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()),
					side, pos);
		}
	}

	private static boolean isVecInsideYZBounds(Vec3d v, @Nonnull World w, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB bb) {
		if (isInsideCoreYZBounds(v, bb))
			return true;
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (w.getTileEntity(pos) != null) {
				if (((TileEntityNetworkCable) w.getTileEntity(pos)).hasRealConnection(dir)) {
					if (isInsideConnectorYZBounds(v, dir, bb))
						return true;
				}
			}
		}
		return false;
	}

	private static boolean isVecInsideXZBounds(Vec3d v, @Nonnull World w, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB bb) {
		if (isInsideCoreXZBounds(v, bb))
			return true;
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (w.getTileEntity(pos) != null) {
				if (((TileEntityNetworkCable) w.getTileEntity(pos)).hasRealConnection(dir)) {
					if (isInsideConnectorXZBounds(v, dir, bb))
						return true;
				}
			}
		}
		return false;
	}

	private static boolean isVecInsideXYBounds(Vec3d v, @Nonnull World w, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB bb) {
		if (isInsideCoreXYBounds(v, bb))
			return true;
		for (EnumFacing dir : EnumFacing.VALUES) {
			if (w.getTileEntity(pos) != null) {
				if (((TileEntityNetworkCable) w.getTileEntity(pos)).hasRealConnection(dir)) {
					if (isInsideConnectorXYBounds(v, dir, bb))
						return true;
				}
			}
		}
		return false;
	}

	private static boolean isInsideCoreYZBounds(@Nullable Vec3d v, @Nonnull AxisAlignedBB bb) {
		return v != null && (v.y >= bb.minY && v.y <= bb.maxY && v.z >= bb.minZ && v.z <= bb.maxZ);
	}

	private static boolean isInsideCoreXZBounds(@Nullable Vec3d v, @Nonnull AxisAlignedBB bb) {
		return v != null && (v.x >= bb.minX && v.x <= bb.maxX && v.z >= bb.minZ && v.z <= bb.maxZ);
	}

	private static boolean isInsideCoreXYBounds(@Nullable Vec3d v, @Nonnull AxisAlignedBB bb) {
		return v != null && (v.x >= bb.minX && v.x <= bb.maxX
				&& v.y >= bb.minY && v.y <= bb.maxY);
	}

	private static boolean isInsideConnectorXZBounds(@Nullable Vec3d v, @Nonnull EnumFacing dir, @Nonnull AxisAlignedBB bb) {
		return v != null && (v.x >= bb.minX + (dir.getFrontOffsetX() >= 0 ? 0 : dir.getFrontOffsetX() * extension)
				&& v.x <= bb.maxX + (dir.getFrontOffsetX() <= 0 ? 0 : dir.getFrontOffsetX() * extension)
				&& v.z >= bb.minZ + (dir.getFrontOffsetZ() >= 0 ? 0 : dir.getFrontOffsetZ() * extension)
				&& v.z <= bb.maxZ + (dir.getFrontOffsetZ() <= 0 ? 0 : dir.getFrontOffsetZ() * extension));
	}

	private static boolean isInsideConnectorYZBounds(@Nullable Vec3d v, @Nonnull EnumFacing dir, @Nonnull AxisAlignedBB bb) {
		return v != null && (v.y >= bb.minY + (dir.getFrontOffsetY() >= 0 ? 0 : dir.getFrontOffsetY() * extension)
				&& v.y <= bb.maxY + (dir.getFrontOffsetY() <= 0 ? 0 : dir.getFrontOffsetY() * extension)
				&& v.z >= bb.minZ + (dir.getFrontOffsetZ() >= 0 ? 0 : dir.getFrontOffsetZ() * extension)
				&& v.z <= bb.maxZ + (dir.getFrontOffsetZ() <= 0 ? 0 : dir.getFrontOffsetZ() * extension));
	}

	private static boolean isInsideConnectorXYBounds(@Nullable Vec3d v, @Nonnull EnumFacing dir, @Nonnull AxisAlignedBB bb) {
		return v != null && (v.x >= bb.minX + (dir.getFrontOffsetX() >= 0 ? 0 : dir.getFrontOffsetX() * extension)
				&& v.x <= bb.maxX + (dir.getFrontOffsetX() <= 0 ? 0 : dir.getFrontOffsetX() * extension)
				&& v.y >= bb.minY + (dir.getFrontOffsetY() >= 0 ? 0 : dir.getFrontOffsetY() * extension)
				&& v.y <= bb.maxY + (dir.getFrontOffsetY() <= 0 ? 0 : dir.getFrontOffsetY() * extension));
	}
}
