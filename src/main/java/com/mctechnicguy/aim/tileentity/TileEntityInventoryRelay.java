package com.mctechnicguy.aim.tileentity;

import com.mctechnicguy.aim.ModElementList;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityInventoryRelay extends TileEntityAIMDevice implements IItemHandler{

	public TileEntityInventoryRelay() { }

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

    private int getSlotOffset() {
        if (!isCoreActive()) return 0;
        switch (getDeviceMode()) {
            case 0: return 0;
            case 1: return 0;
            case 2: return InventoryPlayer.getHotbarSize();
            default: return 0;
        }
    }

    @Override
    public int getSlots() {
        if (!isCoreActive()) return 1;
        switch (getDeviceMode()) {
            case 0: return getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP).getSlots();
            case 1: return InventoryPlayer.getHotbarSize();
            case 2: return getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP).getSlots() - InventoryPlayer.getHotbarSize();
            default: return 1;
        }
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {
        return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP).getStackInSlot(slot + getSlotOffset()) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP).insertItem(slot + getSlotOffset(), stack, simulate) : stack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP).extractItem(slot + getSlotOffset(), amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return isCoreActive() ? getPlayer().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP).getSlotLimit(slot + getSlotOffset()) : 64;
    }

    @Nonnull
    @Override
	public String getLocalizedName() {
		return "tile.inventoryrelay.name";
	}

	@Nonnull
    @Override
	public ItemStack getDisplayStack() {
		return new ItemStack(ModElementList.blockInventoryRelay);
	}
}
