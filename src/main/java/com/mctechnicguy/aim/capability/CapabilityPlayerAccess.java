package com.mctechnicguy.aim.capability;

import java.util.concurrent.Callable;

import com.mctechnicguy.aim.AdvancedInventoryManagement;
import com.mctechnicguy.aim.ModInfo;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CapabilityPlayerAccess implements ICapabilitySerializable {

	private EntityPlayer player;
	private boolean isPlayerAccessible;
	
	public static final ResourceLocation CAP_IDENTIFIER = new ResourceLocation(ModInfo.ID + ":CapPlayerAccessiblity");
	
	public CapabilityPlayerAccess(EntityPlayer p) {
		player = p;
		isPlayerAccessible = true;
	}
	
	public boolean isAccessible() {
		return this.isPlayerAccessible;
	}
	
	public void setAccessible(boolean b) {
		this.isPlayerAccessible = b;
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return AdvancedInventoryManagement.PLAYER_ACCESS_CAP != null
				&& capability == AdvancedInventoryManagement.PLAYER_ACCESS_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return AdvancedInventoryManagement.PLAYER_ACCESS_CAP != null
				&& capability == AdvancedInventoryManagement.PLAYER_ACCESS_CAP ? (T) this : null;
	}

	@Nonnull
    @Override
	public NBTBase serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("isAccessible", isPlayerAccessible);
		return nbt;
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		if (nbt instanceof NBTTagCompound)
			this.isPlayerAccessible = ((NBTTagCompound)nbt).getBoolean("isAccessible");
	}

	public static class Storage implements Capability.IStorage<CapabilityPlayerAccess> {
		
		@Nullable
        @Override
		public NBTBase writeNBT(Capability<CapabilityPlayerAccess> capability, CapabilityPlayerAccess instance,
				EnumFacing side) {
			return null;
		}

		@Override
		public void readNBT(Capability<CapabilityPlayerAccess> capability, CapabilityPlayerAccess instance,
				EnumFacing side, NBTBase nbt) {
		}

	}

	public static class Factory implements Callable<CapabilityPlayerAccess> {
		@Nullable
        @Override
		public CapabilityPlayerAccess call() throws Exception {
			return null;
		}
	}
}
