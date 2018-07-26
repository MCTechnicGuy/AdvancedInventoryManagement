package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.client.render.NetworkInfoOverlayRenderer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class TileEntityNetworkElement extends TileEntity implements IProvidesNetworkInfo {
	
	@Nullable
    private TileEntityAIMCore coreTile;
	private BlockPos corePos;

	private boolean hasCore;
	private boolean isCoreActive;

	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.readServerCoreData(nbt);
	}
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
		return oldState.getBlock() != newState.getBlock();
	}
	
	@Nonnull
    @Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		this.writeServerCoreData(nbt);
		return nbt;
	}
	
	@Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writePacketCoreData(nbtTag);
		return new SPacketUpdateTileEntity(this.getPos(), 0, nbtTag);
	}

	@Override
    @SideOnly(Side.CLIENT)
	public void onDataPacket(NetworkManager net, @Nonnull SPacketUpdateTileEntity packet) {
		this.readPacketCoreData(packet.getNbtCompound());
	}

	private void writeServerCoreData(@Nonnull NBTTagCompound nbt) {
		if (this.hasServerCore()) {
			nbt.setInteger("coreX", this.getCore().getPos().getX());
			nbt.setInteger("coreY", this.getCore().getPos().getY());
			nbt.setInteger("coreZ", this.getCore().getPos().getZ());
		}
		nbt.setBoolean("hasCore", this.hasServerCore());
	}

	private void readServerCoreData(@Nonnull NBTTagCompound nbt) {
		if (nbt.getBoolean("hasCore")) {
			this.setCorePos(new BlockPos(nbt.getInteger("coreX"), nbt.getInteger("coreY"), nbt.getInteger("coreZ")));
		} else
			this.setCore(null);
	}

	void writePacketCoreData(@Nonnull NBTTagCompound nbt) {
	    if (this.hasServerCore()) {
	        nbt.setBoolean("hasCore", true);
	        nbt.setBoolean("isCoreActive", this.isCoreActive());
        } else {
	        nbt.setBoolean("hasCore", false);
        }
    }

    void readPacketCoreData(@Nonnull NBTTagCompound nbt) {
	    if (nbt.getBoolean("hasCore")) {
	        this.hasCore = true;
	        this.isCoreActive = nbt.getBoolean("isCoreActive");
        } else {
	        this.hasCore = false;
	        this.isCoreActive = false;
        }
    }

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound nbtTag = super.getUpdateTag();
		this.writePacketCoreData(nbtTag);
		return nbtTag;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		this.readPacketCoreData(tag);
	}

	public boolean isCoreActive() {
		if (!this.hasServerCore()) return false;
		if (this.hasWorld() && this.world.isRemote) return this.coreTile.isActive();
		else return this.coreTile.isActive() && this.coreTile.getConnectedPlayer() != null;
	}

	@Nullable
    public TileEntityAIMCore getCore() {
		if (coreTile != null && coreTile.isInvalid()) this.setCore(null);
		return coreTile;
	}

	public boolean hasServerCore() {
		if (corePos == null) return false;
		if (coreTile == null || coreTile.isInvalid()) {
			TileEntity te = world.getTileEntity(corePos);
			if (!(te instanceof TileEntityAIMCore)) {
				this.setCore(null);
				return false;
			}
			coreTile = (TileEntityAIMCore)te;
		}
		return true;
	}
	
	public boolean isPlayerAccessAllowed(@Nonnull EntityPlayer player) {
	    if (world.isRemote) return true;
		return !this.hasServerCore() || this.getCore().isPlayerAccessAllowed(player);
	}

	public boolean hasClientCore() {
	    return hasCore;
    }
	
	public void setCore(@Nullable TileEntityAIMCore newCore) {
		if (coreTile != newCore) {
			this.coreTile = newCore;
			if (newCore != null) setCorePos(newCore.getPos());
			else setCorePos(null);
		}
	}
	
	private void setCorePos(BlockPos newpos) {
		if (newpos != this.corePos) {
			this.corePos = newpos;
			this.updateBlock();
		}
	}
	
	public void updateBlock() {
		if (this.hasWorld()) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			this.markDirty();
		}
	}

	public boolean getCoreActive() {
		return isCoreActive;
	}

    @SideOnly(Side.CLIENT)
	public String getNameForOverlay() {
		return I18n.format(getUnlocalizedBlockName() + ".name");
	}

    @SideOnly(Side.CLIENT)
	public void renderStatusInformation(NetworkInfoOverlayRenderer renderer) {
        renderer.renderStatusString(this.getCoreActive());
    }

    @Nonnull
    @SideOnly(Side.CLIENT)
    private String getUnlocalizedBlockName() {
        return this.hasWorld() ? this.getBlockType().getUnlocalizedName() : "";
    }

    @Override
    public void invalidateServerInfo() {

    }

    @Nullable
    @Override
    public NBTTagCompound getTagForOverlayUpdate() {
        return null;
    }

    @Override
    public void handleTagForOverlayUpdate(NBTTagCompound nbt) {

    }
}
