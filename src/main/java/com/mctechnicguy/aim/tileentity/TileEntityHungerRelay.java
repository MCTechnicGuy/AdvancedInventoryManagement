package com.mctechnicguy.aim.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityHungerRelay extends TileEntityAIMDevice implements IItemHandler {
	
	public TileEntityHungerRelay() {}

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
		return ItemStack.EMPTY;
	}

	@Nonnull
    @Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (!isCoreActive() || !isItemValid(stack)) return stack;
        if (!simulate) stack.getItem().onItemUseFinish(stack, this.getPlayer().world, this.getPlayer());
        return ItemStack.EMPTY;
	}

	@Nonnull
    @Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return ItemStack.EMPTY;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	private boolean isItemValid(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemFood && playerAcceptsFood(this.getPlayer(), (ItemFood)stack.getItem());
	}

	private boolean playerAcceptsFood(@Nonnull EntityPlayer player, @Nonnull ItemFood item) {
		switch(this.getDeviceMode()) {
		case 0: return player.canEat(false);
		case 1: return 20 - player.getFoodStats().getFoodLevel() >= item.getHealAmount(new ItemStack(item));
		case 2: return true;
		default: return false;
		}
	}
}
