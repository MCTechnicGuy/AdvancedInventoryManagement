package com.mctechnicguy.aim.tileentity;


import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class TileEntityEnderChestRelay extends TileEntityAIMDevice implements IItemHandler {

    public TileEntityEnderChestRelay() {}

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
    public int getSlots()
    {
        return isCoreActive() ? getInv().getSizeInventory() : 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        return isCoreActive() ? getInv().getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        if (!isCoreActive()) return stack;
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!getInv().isItemValidForSlot(slot, stack))
            return stack;

        ItemStack stackInSlot = getInv().getStackInSlot(slot);

        int m;
        if (!stackInSlot.isEmpty())
        {
            if (!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
                return stack;

            m = Math.min(stack.getMaxStackSize(), getInv().getInventoryStackLimit()) - stackInSlot.getCount();

            if (stack.getCount() <= m)
            {
                if (!simulate)
                {
                    ItemStack copy = stack.copy();
                    copy.grow(stackInSlot.getCount());
                    getInv().setInventorySlotContents(slot, copy);
                    getInv().markDirty();
                }

                return ItemStack.EMPTY;
            }
            else
            {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate)
                {
                    ItemStack copy = stack.splitStack(m);
                    copy.grow(stackInSlot.getCount());
                    getInv().setInventorySlotContents(slot, copy);
                    getInv().markDirty();
                    return stack;
                }
                else
                {
                    stack.shrink(m);
                    return stack;
                }
            }
        }
        else
        {
            m = Math.min(stack.getMaxStackSize(), getInv().getInventoryStackLimit());
            if (m < stack.getCount())
            {
                // copy the stack to not modify the original one
                stack = stack.copy();
                if (!simulate)
                {
                    getInv().setInventorySlotContents(slot, stack.splitStack(m));
                    getInv().markDirty();
                    return stack;
                }
                else
                {
                    stack.shrink(m);
                    return stack;
                }
            }
            else
            {
                if (!simulate)
                {
                    getInv().setInventorySlotContents(slot, stack);
                    getInv().markDirty();
                }
                return ItemStack.EMPTY;
            }
        }

    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (!isCoreActive()) return ItemStack.EMPTY;
        if (amount == 0)
            return ItemStack.EMPTY;

        ItemStack stackInSlot = getInv().getStackInSlot(slot);

        if (stackInSlot.isEmpty())
            return ItemStack.EMPTY;

        if (simulate)
        {
            if (stackInSlot.getCount() < amount)
            {
                return stackInSlot.copy();
            }
            else
            {
                ItemStack copy = stackInSlot.copy();
                copy.setCount(amount);
                return copy;
            }
        }
        else
        {
            int m = Math.min(stackInSlot.getCount(), amount);

            ItemStack decrStackSize = getInv().decrStackSize(slot, m);
            getInv().markDirty();
            return decrStackSize;
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    private IInventory getInv()
    {
        return getPlayer().getInventoryEnderChest();
    }
}
