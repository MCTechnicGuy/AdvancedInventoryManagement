package com.mctechnicguy.aim.util;

import com.mctechnicguy.aim.tileentity.TileEntityNetworkCable;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WorldXYZNetworkCoordinate implements Comparable<WorldXYZNetworkCoordinate>{
	
	private int searchIndex;
	private BlockPos bpos;
	private TileEntity tileEntity;
	private boolean isCable;
	private EnumFacing attachedOnFace;
	
	public WorldXYZNetworkCoordinate (BlockPos bPos, int searchIndex, World world) {
		this.searchIndex = searchIndex;
		this.bpos = bPos;
		this.tileEntity = world.getTileEntity(bPos);
		this.isCable = tileEntity instanceof TileEntityNetworkCable;
	}

    public WorldXYZNetworkCoordinate (BlockPos pos, int searchIndex, World world, EnumFacing onFace) {
        this(pos, searchIndex, world);
        this.attachedOnFace = onFace;
    }
	
	public int getSearchIndex() {
		return this.searchIndex;
	}
	
	public BlockPos getPos() {
		return bpos;
	}


	@Override
	public int compareTo(@Nonnull WorldXYZNetworkCoordinate o) {
		return ((Integer)this.getSearchIndex()).compareTo(o.getSearchIndex());
	}
	
	@Nonnull
    @Override
	public String toString() {
		return "XYZNetworkCoord: [" + bpos.getX() + "; " + bpos.getY() + "; " + bpos.getZ() + "; " + this.getSearchIndex() + "]";
	}
	
	@Override
    public int hashCode() {
        return bpos.hashCode();
    }
	
	@Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldXYZNetworkCoordinate comparable = (WorldXYZNetworkCoordinate) o;
        return comparable.bpos.equals(bpos);
    }

	@Nonnull
    public Block getBlockInWorld(@Nonnull World worldObj) {
		return worldObj.getBlockState(bpos).getBlock();
	}

	@Nullable
    public TileEntity getTileInWorld() {
		return tileEntity;
	}

    public boolean isCable() {
        return isCable;
    }

    public EnumFacing getAttachedOnFace() {
        return attachedOnFace;
    }
}
