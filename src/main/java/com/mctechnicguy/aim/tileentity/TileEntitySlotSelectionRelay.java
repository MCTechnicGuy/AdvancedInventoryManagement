package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.ModElementList;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySlotSelectionRelay extends TileEntityAIMDevice implements IItemHandler {

	public int slotID = 0;

	public TileEntitySlotSelectionRelay() {
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("slotID", slotID);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
	    if (nbt.hasKey("slotID")) {
	        this.slotID = nbt.getInteger("slotID");
        }
		super.readFromNBT(nbt);
	}

	@Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        try {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) this;
        } catch (ClassCastException x) {
            return super.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getStackInSlot(getCurrentSlotID()) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).insertItem(getCurrentSlotID(), stack, simulate) : stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).extractItem(getCurrentSlotID(), amount, simulate) : ItemStack.EMPTY;
    }

    private int getCurrentSlotID() {
		switch (this.getDeviceMode()) {
		case 0:
			return this.getPlayerInventory().currentItem;
		case 1:
			return this.getPlayerInventory().mainInventory.size() + this.getPlayerInventory().armorInventory.size();
		case 2:
			return this.slotID;
		default:
			return 0;
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).getSlotLimit(getCurrentSlotID()) : 64;
	}

	@Nonnull
    @Override
	public String getLocalizedName() {
		return "tile.slotselectionrelay.name.short";
	}

	@Nonnull
    @Override
	public ItemStack getDisplayStack() {
		return new ItemStack(ModElementList.blockSlotSelectionRelay);
	}

	@Nullable
    public Object[] setSlotID(int newID) {
		if (!this.isCoreActive())
			return null;

		String slotDesc;
		this.slotID = newID;
		
		if (slotID >= this.getPlayerInventory().getSizeInventory()) slotID = 0;
		if (slotID < 0) slotID = this.getPlayerInventory().getSizeInventory() - 1;
		
		if (InventoryPlayer.isHotbar(slotID))
			slotDesc = ".slotDesc.hotbar";
		else if (slotID < this.getPlayerInventory().mainInventory.size())
			slotDesc = ".slotDesc.maininv";
		else if (slotID < this.getPlayerInventory().mainInventory.size()
				+ this.getPlayerInventory().armorInventory.size())
			slotDesc = ".slotDesc.armor";
		else if (slotID == this.getPlayerInventory().mainInventory.size()
				+ this.getPlayerInventory().armorInventory.size())
			slotDesc = ".slotDesc.offhand";
		else
			slotDesc = ".slotDesc.none";
		
		return new Object[] { slotID, slotDesc };

	}



}
